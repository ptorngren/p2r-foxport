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
import java.util.Arrays;
import java.util.Properties;

import se.p2r.foxport.firefox.FirefoxReader;
import se.p2r.foxport.util.Utils;

/**
 * Main entry for reading and exporting bookmarks.
 * 
 * @author peer
 *
 *@see FirefoxReader
 *
 */
public class BookmarkExporter {

	private static void run(File targetFolder, File cfgFile) throws ConfigurationException, IOException {
		Properties config = readProperties(cfgFile);
		
		Utils.log("<RUN> " + targetFolder + ", configured by file " + cfgFile.getAbsolutePath());
		new BookmarkProcessor(targetFolder).process(config); 
	}

	private static void run(File targetFolder, String... foldersWithNameAndDescription) throws ConfigurationException, IOException {
		Properties config = new Properties();
		for (String entry : foldersWithNameAndDescription) {
			String[] elements = entry.split("[=;]");
			config.put(elements[0], elements[1]+";"+elements[2]);
		}
		
		Utils.log("<RUN>" + targetFolder + ", configured by arguments: " + config.stringPropertyNames());
		new BookmarkProcessor(targetFolder).process(config); 
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

	
	private static void handleConfigurationError(ConfigurationException e) {
		System.err.println("Bad setup: " + e.getMessage());
		BookmarkExporter.printSyntax();
	}


	private static void printSyntax() {
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
	
	/**
	 * MAIN ENTRY. Specify desired actions and settings in arguments, as specified
	 * by {@link #printSyntax()}.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		int nofArgs = args.length;
		switch (nofArgs) {
		case 0:
		case 1:
			BookmarkExporter.printSyntax();
			break;

		case 2: {
			try {
				File targetFolder = new File(args[0]);
				File cfgFile = new File(args[1]);
				run(targetFolder, cfgFile);
			} catch (ConfigurationException e) {
				handleConfigurationError(e);
			}
			break;
		}
		default:
			try {
				File targetFolder = new File(args[0]);
				run(targetFolder, Arrays.copyOfRange(args, 1, args.length));
			break;
			} catch (ConfigurationException e) {
				handleConfigurationError(e);
			}
		}
	}

}
