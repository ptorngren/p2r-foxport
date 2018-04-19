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
import java.net.URI;
import java.util.Arrays;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import se.p2r.foxport.util.NotYetImplementedException;
import se.p2r.foxport.util.Utils.BrowserType;

/**
 * Parses the commandline.
 * @author peer
 *
 */
public class CommandLineParser {

	public final class CommandLine {

		private final org.apache.commons.cli.CommandLine commandLine;
//		private final String name = BookmarkExporter.class.getSimpleName();
//		private String launch = "java " + name;
//		private String browserTypes = Arrays.asList(BrowserType.values()).toString();
//		private String[] syntax = {
//						name, 
//						"", 
//						String.format("Usage 1: %s -b=browsertype -t=targetFolder -c=configurationFile", launch),
//						String.format("Usage 2: %s -b=browsertype -t=targetFolder folderStatement...", launch), 
//						"",
//						"where:",
//						String.format("'browsertype' is one of %s,", browserTypes),
//						"'targetFolder' is a writable folder,",
//						"'configurationFile' is a file (absolute or relative) with one or more folderStatements (each on a separate line)",
//						"'folderStatement' is on format format 'bookmarkFolder=name;description'.",
//						"", 
//						"Command Line Example:",
//						String.format("  %s -b=FIREFOX -t=C:/temp \"Media=Media Links;Online news or entertainment\", \"Games=Games;Online games\"", launch),
//						"", 
//						"Configuration File Example:", 
//						String.format("  %s -b=CHROME -t=C:/temp C:/myStuff/%s.properties", launch, name),
//						"file contents:", 
//						"  # mappings (map a plain file name to the more complex folder name)",
//						"  map.media=Online Media Links",
//						"  # Folders to export: ", 
//						"  media=Shared Media Links;Online news or entertainment",
//						"  Games=Shared Games;My online games", 
//						"",
//						"Note 1: if no folders are named, all folders are exported using the default metadata (if any).",
//						"Note 2: launch with JVM arg '-DDEBUG' to get some basic debug info on stderr.",
//						};
				
		public CommandLine(String... args) throws ParseException {
			try {
				this.commandLine = new DefaultParser().parse(options, args);
			} catch (ParseException e) {
				printHelp();
				throw e;
			}
		}

		public BrowserType getBrowserType() throws MissingArgumentException {
			String type = getMandatoryArgument('b');
			return BrowserType.from(type);
		}

		public File getTargetFolder() throws MissingArgumentException {
			String target = commandLine.hasOption('t') ? commandLine.getOptionValue('t') : System.getProperty("java.io.tmpdir");
			return new File(target);
		}
		
		public File getConfigurationFile() {
			return new File(commandLine.getOptionValue('c'));
		}

		public boolean isConfigurationFileSpecified() {
			return commandLine.hasOption('c');
		}

		public boolean isPlainList() {
			return commandLine.hasOption('p');
		}

		public boolean isTree() {
			return !isPlainList();
		}

		private String getMandatoryArgument(char opt) throws MissingArgumentException {
			String optionValue = commandLine.getOptionValue(opt);
			if (optionValue==null) {
//				printSyntax();
				Option o = findOption(opt);
				throw new MissingArgumentException(String.format("-%s (--%s)", o.getOpt(), o.getLongOpt()));
			}
			return optionValue;
		}

		private Option findOption(char opt) {
			Option result = options.getOption(String.valueOf(opt));
			assert result!=null : "No such option: "+opt;
			return result; 
		}

//		private void printSyntax() {
//			for (String line: syntax) {
//				System.out.println(line);
//			}
//		}
//
		public boolean isUpload() {
			return commandLine.hasOption("u");
		}

		public URI getUploadURI() {
			 // FIXME implement
//			if (isUpload()) {
//				String[] optionValues = commandLine.getOptionValues("u");
//				return null;
//			}
			throw new NotYetImplementedException();
		}

		public boolean isHelp() {
			return commandLine.hasOption('h');
		}

		public int printHelp() {
			String name = "java " + BookmarkExporter.class.getSimpleName();
			new HelpFormatter().printHelp(name, options, true);
			return 0; // system exit code: ok
		}

	}
	private final Options options;
	private CommandLine activeCommandLine;

	public CommandLineParser() {
		
		// TODO group options
		this.options = new Options()
				.addOption("h", "help", false, "Show this help text")
				.addOption("b", "browser", true, "Browser type: "+Arrays.asList(BrowserType.values()).toString())
				.addOption("t", "target", true, "Target folder for writing exported files (default is user's temp directory)")
				.addOption("c", "config", true, "Configuration file (foobar.properties), mandatory if running Firefox")
				.addOption("p", "plain", false, "Plain list output (default is tree)")
				.addOption("u", "upload", true, "Upload destination (default is no upload)");
		this.options.getOption("b").setRequired(true);
	}

	public CommandLine parse(String... args) throws ParseException {
		activeCommandLine = new CommandLine(args);
		try {
			return validate(activeCommandLine);
		} catch (ParseException e) {
			activeCommandLine.printHelp();
			throw e;
		}
	}

	private CommandLine validate(CommandLine cl) throws ParseException {
		// FIXME implement
//		throw new ParseException("TEST");
		return cl;
	}

}
