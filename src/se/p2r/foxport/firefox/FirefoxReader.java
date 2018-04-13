/**
 * 
 */
package se.p2r.foxport.firefox;

import static se.p2r.foxport.util.Utils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import se.p2r.foxport.util.Utils;

/**
 * <p>
 * Reads firefox bookmarks.
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
public class FirefoxReader {

	private static final String[] ROOT_NAMES = {"Bookmarks Menu", "Bokmärkesmenyn"}; // TODO read from environment or config file (name depends on language)
	
	private static final FileFilter JSON_FILTER = new FileFilter() {
		public boolean accept(File pathname) {
			return endsWith(pathname, JSON); // || endsWith(pathname, JSONLZ4);  // Cannot read LZ4 files
		}

		@Override
		public String toString() {
			return "'*"+JSON+"'";
		}
		
	};
	
	private final File inputFile;

	public FirefoxReader() throws IOException {
		this.inputFile = findInputFile();
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
			br = new BufferedReader(Utils.getInputStreamReader(profile));
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

	private File findProfile() {
		String userHome = System.getProperty("user.home");
		return new File(userHome, "AppData/Roaming/Mozilla/Firefox/profiles.ini");
	}


	public FirefoxBookmark load() {
		FirefoxBookmarks allRoots = parseBookmarkFile();
		return findBookmarksRoot(allRoots.getChildren(), ROOT_NAMES); // find the "Bookmarks" folder
	}
	
	private FirefoxBookmark findBookmarksRoot(List<FirefoxBookmark> prospects, String[] wanted) {
		Collection<String> lowerWanted = Utils.toLowerCase(wanted);
		return prospects.stream()
				.filter(p->lowerWanted.contains(p.getTitle().toLowerCase()))
				.findFirst()
				.get();
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


}
