/*
Copyright (c) 2014, Peer Törngren
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the p2r-foxport project nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package se.p2r.foxport;

import static se.p2r.foxport.util.Utils.ENCODING_HTML;
import static se.p2r.foxport.util.Utils.ENCODING_JSON;
import static se.p2r.foxport.util.Utils.JSON;
import static se.p2r.foxport.util.Utils.JSONLZ4;
import static se.p2r.foxport.util.Utils.debug;
import static se.p2r.foxport.util.Utils.endsWith;
import static se.p2r.foxport.util.Utils.log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListValuedMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import se.p2r.foxport.firefox.FirefoxBookmark;
import se.p2r.foxport.firefox.FirefoxBookmarks;
import se.p2r.foxport.html.HTMLFileWriter;
import se.p2r.foxport.html.HTMLListGenerator;
import se.p2r.foxport.html.HTMLTreeGenerator;
import se.p2r.foxport.util.DeepBookmarkSelector;
import se.p2r.foxport.util.MutableBookmarkContainer;
import se.p2r.foxport.util.Utils;

/**
 * <p>
 * Main entry for reading Firefox bookmarks.
 * </p>
 * <p>
 * <b>NOTE:</b>Cannot read the 'new' compressed format (LZ4).<br>
 * User must manually export bookmarks to JSON format and place it in the
 * Firefox backup folder, typically something like
 * <code>%USERPROFILE%/AppData/Roaming/Mozilla/Firefox/Profiles/l0sic08k.default/bookmarkbackups</code>
 * </p>
 * 
 * @author peer
 *
 * <p>TODO redesign the way to identify subfolders, use comments from browser instead. BUT: Firefox does not export descriptions.
 * Current design is very brittle to do name matching + we cannot handle names with spaces + we want a simple file name (not matching the folder name)
 * Also, we want to read Chrome bookmarks anyway.
 * </p>  
 * @see #mapNames(Properties)
 * @see #getDescription(Properties, String)
 */
public class BookmarkExporter {

	public class ConfigurationException extends Exception {
		private static final long serialVersionUID = 8929701975216314212L;

		public ConfigurationException(String message, Throwable cause) {
			super(message, cause);
		}

		public ConfigurationException(Throwable cause) {
			super(cause);
		}

	}

	private static final FileFilter JSON_FILTER = new FileFilter() {
		public boolean accept(File pathname) {
			return endsWith(pathname, JSON); // || endsWith(pathname, JSONLZ4);  // Cannot read LZ4 files
		}

		@Override
		public String toString() {
			return "'*"+JSON+"'";
		}
		
	};

	private static final String[] ROOT_NAMES = {"Bookmarks Menu", "Bokmärkesmenyn"}; // TODO read from environment or config file (name depends on language)
	private static final boolean GENERATE_TREE = true;

	private final File inputFile;
	private final File targetFolder;

	private int fileCounter;

	
	public BookmarkExporter(File targetFolder) throws IOException, ConfigurationException {
		this.targetFolder = targetFolder;
		this.inputFile = findInputFile();
		if (!targetFolder.isDirectory()) {
			throw new ConfigurationException(new FileNotFoundException("Output folder does not exist: " + targetFolder));
		}
	}

	private void run(File cfgFile) throws ConfigurationException {
		log("<RUN>" + inputFile + " => " + targetFolder + " configured by file " + cfgFile.getAbsolutePath());
		fileCounter = 0;
		Properties config = readProperties(cfgFile);
		processBookmarks(config); 
		log("</RUN> Wrote " + fileCounter + " files");
	}

	public static void printSyntax() {
		String name = BookmarkExporter.class.getSimpleName();
		String launch = "java " + name;
		String[] lines = {
				name,
				"",
				String.format("Usage 1: %s targetFolder configurationFile", launch),
				String.format("Usage 2: %s targetFolder folderStatement...", launch),
				"",
				"Where targetFolder is an existing, writable folder,",
				"folderStatement is on format format 'bookmarkFolder=name;description', ",
				"and configurationFile is a file (absolute or relatiove) with one or more folderStatements (each on a separate line).",
				"",
				"Command Line Example:",
				String.format("  %s C:/temp \"Media=Media Links;Online news or entertainment\", \"Games=Games;Online games\"", launch),
				"",
				"Configuration File Example:",
				String.format("  %s C:/temp C:/myStuff/%s.properties", launch, name),
				"File contents:",
				"  # Folders to export: ",
				"  Media=Media Links;Online news or entertainment",
				"  Games=Games;Online games",
				"",
				"Note 1: if no folders are named, all folders are exported using the default metadata (if any).",
				"Note 2: launch with JVM arg '-DDEBUG' to get some basic debug info on stderr.",
		};
		for (String line : lines) {
			System.out.println(line);
		}
	}
	
	private static void handleConfigurationError(ConfigurationException e) {
		System.err.println("Bad setup: " + e.getMessage());
		BookmarkExporter.printSyntax();
	}


	public void run(String... foldersWithNameAndDescription) {
		log("<RUN>" + inputFile + " => " + targetFolder + " configured by " + foldersWithNameAndDescription.length + " command line arguments");
		fileCounter = 0;
		Properties config = new Properties();
		for (String entry : foldersWithNameAndDescription) {
			String[] elements = entry.split("[=;]");
			config.put(elements[0], elements[1]+";"+elements[2]);
		}
		processBookmarks(config);
		log("</RUN> Wrote " + fileCounter + " files");
	}

	private Properties readProperties(File cfgFile) throws ConfigurationException {
		Properties cfg = new Properties();
		try {
			cfg.load(getInputStreamReader(cfgFile));
			return cfg;
		} catch (Exception e) {
			throw new ConfigurationException("Could not read configuration file: " + cfgFile, e);
		}
	}

	private File findInputFile() throws IOException {
		File backupDirectory = findBookmarkDirectory();
		File mostRecent = null;
		for (File file : backupDirectory.listFiles(JSON_FILTER)) {
			if (mostRecent==null || mostRecent.lastModified() < file.lastModified()) {
				mostRecent = file;
			}
		};
		if (mostRecent==null) {
			String msg = "No files found in "+backupDirectory+" using filter " + JSON_FILTER;
			throw new FileNotFoundException(msg);
		}
		return mostRecent;
	}

	private File findBookmarkDirectory() throws IOException {
		File profile = findProfile();
		File profileDirectory = findProfileDirectory(profile, null);
		File backupDirectory = new File(profileDirectory, "bookmarkBackups");
		if (!backupDirectory.isDirectory()) {
			throw new FileNotFoundException("Backup directory not found: " + backupDirectory);
		}
		return backupDirectory;
	}

	private File findProfileDirectory(File profile, String profileName) throws IOException {
		List<String> lines = readFile(profile);
		boolean inProfile = false;
		String header = "Name=" + (profileName == null ? "default" : profileName);

		for (String string : lines) {
			String[] pair = string.split("=");
			if (inProfile) {
				if (pair[0].equals("Path")) {
					File file = new File(pair[1]);
					return file.isAbsolute() ? file : new File(profile.getParentFile(), file.getPath());
				}
			} else {
				inProfile = string.equalsIgnoreCase(header);
			}
		}
		throw new IllegalArgumentException("No such profile: " + profileName + " (file: " + profile + ")");
	}

	private List<String> readFile(File profile) throws FileNotFoundException, IOException {
		List<String> lines = new ArrayList();
		BufferedReader br = null;
		try {
			br = new BufferedReader(getInputStreamReader(profile));
			String strLine = null;
			while ((strLine = br.readLine()) != null) {
				lines.add(strLine);
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
		return lines;
	}

	private InputStreamReader getInputStreamReader(File file) throws FileNotFoundException {
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		InputStreamReader isr = new InputStreamReader(in);
		return isr;
	}

	private File findProfile() {
		String userHome = System.getProperty("user.home");
		return new File(userHome, "AppData/Roaming/Mozilla/Firefox/profiles.ini");
	}

	private void processBookmarks(Properties config) {
		FirefoxBookmarks fileRoots = parseBookmarkFile();
		FirefoxBookmark bookmarksRoot = find(fileRoots.getChildren(), ROOT_NAMES); // find the "Bookmarks" folder
		Map<String, String> mappings = mapNames(config);
		
		// first select root containers mentioned in config (avoid trash, tmp, private, etc)
		// then recursively collect folders in these roots
		List<FirefoxBookmark> rootContainers = select(bookmarksRoot.getChildren(), mappings);
		ListValuedMap<String, Bookmark> selectedContainers = new DeepBookmarkSelector(mappings).select(rootContainers);
		
		// process each selected folder
		for (String folderName: selectedContainers.keySet()) {
			String id = mappings.get(folderName);
			String[] description = config.getProperty(id, "").split(";");
			List<Bookmark> containers = selectedContainers.get(folderName);
			assert !containers.isEmpty() : "No containers for title: "+folderName; 
			Bookmark root = containers.size()==1 ? containers.iterator().next() : merge(folderName, containers);
			processContainer(id, root, description);
		}
	}

	// TODO kludge - rethink a better way to handle names
	// map names to folders bidirectional. If not mapped, entry has same key and value.
	private Map<String, String> mapNames(Properties config) {
		Map<String, String> result = new HashMap();
		for (Entry<Object, Object> each: config.entrySet()) {
			String key=(String) each.getKey();
			boolean map = key.startsWith("map.");
			if (map) {
				String id = key.substring(4).toLowerCase();
				String folder = ((String) each.getValue()).toLowerCase();
				result.put(id, folder);
				result.put(folder, id);
			} else {
				result.put(key, key);
			}
		}
		return result;
	}

	private Bookmark merge(String folderName, List<Bookmark> containers) {
		MutableBookmarkContainer result = new MutableBookmarkContainer(folderName);
		for (Bookmark c : containers) {
			assert c.getTitle().equalsIgnoreCase(folderName) : "Not the same title: "+result+", "+c; 
			result.merge(c);
		}
		return result;
	}

	private void processContainer(String id, Bookmark root, String... nameAndDescription) {
		fileCounter++;
		log("Processing root folder #" + fileCounter + ":" + root.getTitle());
		
		String name = root.getTitle();
		String description = "";
		if (nameAndDescription.length>0) {
			name = nameAndDescription[0];
			description = nameAndDescription.length>1 ? nameAndDescription[1] : "";
		}
		String html = GENERATE_TREE 
				? new HTMLTreeGenerator(root, name, description, ENCODING_HTML).run() 
				: new HTMLListGenerator(root, name, description, ENCODING_HTML).run();
		new HTMLFileWriter(targetFolder, id).writeFile(html, root);
	}

	private FirefoxBookmark find(List<FirefoxBookmark> prospects, String[] wanted) {
		Collection<String> lowerWanted = Utils.toLowerCase(wanted);
		return prospects.stream()
				.filter(p->lowerWanted.contains(p.getTitle().toLowerCase()))
				.findFirst()
				.get();
	}
	
	private List<FirefoxBookmark> select(List<FirefoxBookmark> prospects, Map<String, String> mappings) {
		Collection<String> folderNames = mappings.values();
		return prospects.stream()
				.filter(p->folderNames.contains(p.getTitle().toLowerCase()))
				.collect(Collectors.toList());
	}
	private FirefoxBookmarks parseBookmarkFile() {
		if (endsWith(inputFile, JSON)) {
			return parseJSON();
		}
		if (endsWith(inputFile, JSONLZ4)) {
			throw new UnsupportedOperationException("Cannot parse compressed JSON: "+inputFile);
		}
		throw new IllegalArgumentException("Unexpected file type: " + inputFile);
	}

	private FirefoxBookmarks parseJSON() {
		Reader reader = null;
		try {
			FileInputStream fis = new FileInputStream(inputFile);
			InputStreamReader isr = new InputStreamReader(fis, ENCODING_JSON);
			reader = new BufferedReader(isr);
			
			Gson gson = new GsonBuilder().create();
			FirefoxBookmarks ffb = gson.fromJson(reader, FirefoxBookmarks.class);
			debug("Parsed " + inputFile + ":" + ffb);
			return ffb;
		} catch (Exception e) {
			throw new RuntimeException("Unable to parse input file: " + inputFile, e);
		} finally {
			if (reader!=null) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new RuntimeException("Could not close reader on file: " + inputFile, e);
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		int nofArgs = args.length;
		switch (nofArgs) {
		case 0:
		case 1:
			BookmarkExporter.printSyntax();
			break;

		case 2: {
			try {
				new BookmarkExporter(new File(args[0])).run(new File(args[1]));
			} catch (ConfigurationException e) {
				handleConfigurationError(e);
			}
			break;
		}
		default:
			try {
				new BookmarkExporter(new File(args[0])).run(Arrays.copyOfRange(args, 1, args.length));
			} catch (ConfigurationException e) {
				handleConfigurationError(e);
			}
			break;
		}
	}

}
