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
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.ParseException;

import se.p2r.foxport.chrome.ChromeReader;
import se.p2r.foxport.firefox.FirefoxReader;
import se.p2r.foxport.internal.ConfigurationException;
import se.p2r.foxport.net.BookmarkPublisher;
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

	private static List<File> run(BrowserType type, File targetFolder, boolean isTree) throws ConfigurationException, IOException {
		Utils.log("<RUN> " + targetFolder);
		return new BookmarkProcessor(type, targetFolder, isTree).process();
	}

	private static List<File> run(BrowserType type, File targetFolder, boolean isTree, File cfgFile) throws ConfigurationException, IOException {
		Properties config = readProperties(cfgFile);
		Utils.log("<RUN> " + targetFolder + ", configured by file " + cfgFile.getAbsolutePath());
		return new ConfiguredBookmarkProcessor(type, targetFolder, isTree).process(config);
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

	private static int run(CommandLineParser.Arguments commandLine) throws ConfigurationException, IOException, MissingArgumentException {
		Collection<File> files;
		if (commandLine.isConfigurationFileSpecified()) {
			files = run(commandLine.getBrowserType(), commandLine.getTargetFolder(), commandLine.isTree(),
					commandLine.getConfigurationFile());
		} else {
			files = run(commandLine.getBrowserType(), commandLine.getTargetFolder(), commandLine.isTree());
		}

		if (commandLine.isUpload() && !files.isEmpty()) {
			new BookmarkPublisher(commandLine.getUploadURL()).publish(files);
		}
		
		return 0;
	}

	/**
	 * MAIN ENTRY. Specify desired actions and settings in arguments, as specified
	 * by {@link CommandLineParser}.
	 * 
	 * @param args
	 * @throws IOException
	 * @throws ParseException 
	 * @throws ConfigurationException 
	 */
	public static void main(String[] args) {
			int result;
			try {
				CommandLineParser.Arguments commandLine = new CommandLineParser().parse(args);
				result = commandLine.isHelp() ? commandLine.printHelp() : run(commandLine);
			} catch (Exception e) {
				e.printStackTrace(System.err);;
				result = 1;
			}
			System.exit(result);
		}

}
