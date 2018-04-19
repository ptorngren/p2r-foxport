/*
Copyright (c) 2014, Peer Törngren
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the p2r-foxport project nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package se.p2r.foxport.chrome;

import static se.p2r.foxport.util.Utils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import se.p2r.foxport.BookmarkReader;

/**
 * Reads chrome bookmarks. Note: For editing comments in Chrome, you probably
 * need to add the <a
 * href=https://chrome.google.com/webstore/detail/bookmark-manager/gmlllbghnfkpflemihljekbapjopfjik>Google Chrome BookmarkManager</a>
 * 
 * @author peer
 *
 */
public class ChromeReader implements BookmarkReader {

	private final File inputFile;

	public ChromeReader() throws IOException {
		this.inputFile = findInputFile();
	}

	private static File findInputFile() throws IOException {
		String userHome = System.getProperty("user.home");
		File backupDirectory = new File(userHome, "AppData/Local/Google/Chrome/User Data/Default");
		if (!backupDirectory.isDirectory()) {
			throw new FileNotFoundException("Backup directory not found: " + backupDirectory);
		}
		File file = new File(backupDirectory, "Bookmarks");
		return file;
	}

	public ChromeBookmark load() {
		ChromeBookmarks contents = parseBookmarksFile();
		
		assert contents!=null : "No bookmarks produced from file: "+inputFile;
		assert contents.getRoots() !=null : "No roots produced from file: "+inputFile;
		assert contents.getOther() !=null : "No bookmarks produced from file: "+inputFile;
		debug("Parsed " + inputFile + ":" + contents);
		
		return contents.getOther();
	}
	
	private ChromeBookmarks parseBookmarksFile() {
		Reader reader = null;
		try {
			FileInputStream fis = new FileInputStream(inputFile);
			InputStreamReader isr = new InputStreamReader(fis, ENCODING_JSON);
			reader = new BufferedReader(isr);
			
			Gson gson = new GsonBuilder().create();
			ChromeBookmarks bookmarks = gson.fromJson(reader, ChromeBookmarks.class);
			return bookmarks;
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

	@Override
	public long getTimestamp() {
		return inputFile.lastModified();
	}

}
