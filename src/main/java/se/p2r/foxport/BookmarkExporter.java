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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.ParseException;

import se.p2r.foxport.chrome.ChromeReader;
import se.p2r.foxport.firefox.FirefoxReader;
import se.p2r.foxport.internal.ActiveOptions;
import se.p2r.foxport.internal.BookmarkProcessor;
import se.p2r.foxport.internal.CommandLineParser;
import se.p2r.foxport.internal.ConfiguredBookmarkProcessor;
import se.p2r.foxport.internal.LinkTester;
import se.p2r.foxport.internal.VersionInfo;
import se.p2r.foxport.internal.exceptions.ConfigurationException;
import se.p2r.foxport.net.FileUploader;
import se.p2r.foxport.util.BrowserType;
import se.p2r.foxport.util.Log;

/**
 * Main entry for reading and exporting bookmarks.
 * 
 * @author peer
 *
 * @see FirefoxReader
 * @see ChromeReader
 *
 */
public class BookmarkExporter {

	private static int run(ActiveOptions options) throws ConfigurationException, IOException {
		
		// init
		BrowserType browserType = options.getBrowserType();
		File targetFolder = options.getTargetFolder();
		boolean isTree = options.isTree();
		boolean isForceExport = options.isForceExport();
		LinkTester linkTester = new LinkTester(options.isTestLinks());
		Collection<File> files;
		File timestampFile = new File(targetFolder, BookmarkExporter.class.getName()+".timestamp");
		long timestamp = timestampFile.lastModified(); // returns 0 if not existing

		
		// read and write 
		if (options.isConfigurationFileSpecified()) {
			File cfgFile = options.getConfigurationFile();
			Log.log(String.format("<EXPORT> to: %s | configured by: %s", targetFolder , cfgFile.getAbsolutePath()));
			ConfiguredBookmarkProcessor configuredBookmarkProcessor = new ConfiguredBookmarkProcessor(browserType, targetFolder, isTree, isForceExport, timestamp, linkTester);
			files = configuredBookmarkProcessor.process(cfgFile);
		} else {
			Log.log(String.format("<EXPORT> to: %s", targetFolder));
			BookmarkProcessor bookmarkProcessor = new BookmarkProcessor(browserType, targetFolder, isTree, isForceExport, timestamp,linkTester);
			files = bookmarkProcessor.process();
		}
		Log.log("</EXPORT> Wrote " + files.size() + " files ("+linkTester.getNumberOfErrors()+" invalid links ignored)");
		
		// report errors
		if (linkTester.isEnabled()) {
			int errors = linkTester.getNumberOfErrors();
			int redirects = linkTester.getNumberOfMoved();
			if (errors+redirects>0) {
				String hdr = String.format("Found %d bad links and %d redirected links:", Integer.valueOf(errors), Integer.valueOf(redirects));
				Log.warn(linkTester.dump(hdr));
			} else {
				Log.log("(no bad links found)");
			}
		} else {
			Log.log("(links not tested)");
		}
		
		// upload
		if (options.isUpload() && !files.isEmpty()) {
			URL url = options.getUploadURL();
			FileUploader publisher = new FileUploader(url);
			publisher.upload(files);
		}
		
		// mark time if ending happy
		timestamp(timestampFile);
		return 0;
	}

	private static void timestamp(File timestampFile) throws IOException {
		timestampFile.createNewFile();  // does nothing if file already exists
		timestampFile.setLastModified(System.currentTimeMillis());
	}

	/**
	 * MAIN ENTRY. Specify desired actions and settings in arguments, as specified
	 * by {@link CommandLineParser}.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int result = 0;
		try {
			ActiveOptions commandLine = new CommandLineParser().parse(args);
			if (commandLine.isHelp()) {
				result = commandLine.printHelp() ;
			} else if (commandLine.isVersion()) {
				result = commandLine.printVersion();
			} else {
				result = run(commandLine);
				Log.log(String.format("Done! [%s]", new VersionInfo().getVersionString()));
			}
			
		} catch (MissingArgumentException e) {
			Log.error("Missing mandatory option: " + e.getMessage());
			result = 1;
		} catch (ParseException | IOException | ConfigurationException e) {
			Log.error(e.getMessage());
			result = 1;
		} catch (Exception e) {
			e.printStackTrace(System.err);
			result = 1;
		}
		System.exit(result);
	}
}
