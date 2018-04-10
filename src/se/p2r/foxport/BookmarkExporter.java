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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
 */
public class BookmarkExporter {

	private static final String JSON = ".json";
	private static final String JSONLZ4 = ".jsonlz4";

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

	private static final String ENCODING_JSON = System.getProperty("file.encoding"); // UTF-8? input, defined by your Firefox?
	private static final String ENCODING_HTML = "utf-8";  // output, defined by you to suite browsers 
	private static final String[] ROOT_NAMES = {"Bookmarks Menu", "Bokmärkesmenyn"}; // TODO read from environment or config file (name depends on language)

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
		FirefoxBookmarks bookmarks = parseBookmarkFile();
		List<FirefoxBookmark> rootContainers = bookmarks.getChildren();
		List<FirefoxBookmark> theToolbar = select(rootContainers, ROOT_NAMES); // "Bookmarks Toolbar"
		assert theToolbar.size() == 1 : "Unexpected number of roots: " + theToolbar;
		
		List<FirefoxBookmark> rootFolders = theToolbar.get(0).getChildren();
		List<FirefoxBookmark> selected = select(rootFolders, config.stringPropertyNames());
		
		for (FirefoxBookmark root : selected) {
			if (root.getChildren()!=null) {
				processRoot(root, config.getProperty(root.getTitle(), ""));
			} else {
				log("Skipping empty root folder: "+root.getTitle());
			}
		}
	}

	private void processRoot(FirefoxBookmark root, String configurationInfo) {
		fileCounter++;
		log("Processing root folder #" + fileCounter + ":" + root.getTitle());
		String name = root.getTitle();
		String description = "";
		if (configurationInfo.length()>0) {
			String[] nameAndDescription = configurationInfo.split(";");
			name = nameAndDescription[0];
			description = nameAndDescription[1];
		}
		String html = new HTMLBookmarkGenerator(root, name, description, ENCODING_HTML).run();
		writeFile(html, outputFile(targetFolder, root));
	}

	private void writeFile(String html, File outputFile) {
		log("Writing html to "+outputFile+" ("+html.length()+" characters)" );
		PrintWriter writer = null;
		try {
			if (outputFile.isFile()) {
				debug("File found, replacing " + outputFile);
			} else {
				outputFile.getParentFile().mkdirs();
				outputFile.createNewFile();
			}
			writer = new PrintWriter(outputFile, ENCODING_HTML);
			writer.println(html);
			log("OK: Wrote "+outputFile);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to write file: "+outputFile, e);
		} finally {
			if (writer !=null) {
				writer.close();
			}
		}
	}

	private File outputFile(File targetFolder, FirefoxBookmark root) {
		return new File(targetFolder, root.getTitle()+".htm");
	}

	private List<FirefoxBookmark> select(List<FirefoxBookmark> prospects, String... wanted) {
		return select(prospects, Arrays.asList(wanted));
	}
	
	private List<FirefoxBookmark> select(List<FirefoxBookmark> prospects, Collection<String> wanted) {
		if (wanted.isEmpty()) {
			return prospects;
		}
		
		Collection<String> ignored = new ArrayList();
		List<FirefoxBookmark> result = new ArrayList();
		for (FirefoxBookmark prospect : prospects) {
			ignored.add(prospect.getTitle());
			for (String name : wanted) {
				if (name.equalsIgnoreCase(prospect.getTitle())) {
					result.add(prospect);
					ignored.remove(name);
				}
			}
		}
		
		log("Ignored folders: " + ignored);
		return result;
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

	public static void log(String string) {
		// TODO proper logging? Or is it overkill?
		System.out.println(string);
	}

	public static void debug(String string) {
		// TODO proper logging?  Or is it overkill?
		if (System.getProperties().containsKey("DEBUG")) {
			System.err.println(string);
		}
	}

	private static boolean endsWith(File file, String wantedEnding) {
		return file.getName().toLowerCase().endsWith(wantedEnding);
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
