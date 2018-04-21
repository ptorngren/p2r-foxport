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
import java.util.Properties;

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
import se.p2r.foxport.util.Log;
import se.p2r.foxport.util.Utils;
import se.p2r.foxport.util.Utils.BrowserType;

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
		
		// read and write 
		if (options.isConfigurationFileSpecified()) {
			File cfgFile = options.getConfigurationFile();
			Properties config = readProperties(cfgFile);
			Log.log(String.format("<EXPORT> to: %s | configured by: %s", targetFolder , cfgFile.getAbsolutePath()));
			files = new ConfiguredBookmarkProcessor(browserType, targetFolder, isTree, isForceExport, linkTester).process(config);
		} else {
			Log.log(String.format("<EXPORT> to: %s", targetFolder));
			files = new BookmarkProcessor(browserType, targetFolder, isTree, isForceExport, linkTester).process();
		}
		Log.log("</EXPORT> Wrote " + files.size() + " files ("+linkTester.getNumberOfErrors()+" invalid links ignored)");
		
		// upload
		if (options.isUpload() && !files.isEmpty()) {
			URL url = options.getUploadURL();
			FileUploader publisher = new FileUploader(url);
			publisher.upload(files);
		}
		
		return 0;
	}

	private static Properties readProperties(File cfgFile) throws ConfigurationException {
		Properties cfg = new Properties();
		try {
			cfg.load(Utils.getInputStreamReader(cfgFile));
			return cfg;
		} catch (Exception e) {
			throw new ConfigurationException("Could not read configuration file: " + cfgFile, e);
		}
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
