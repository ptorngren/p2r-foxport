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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;

import se.p2r.foxport.util.Utils.BrowserType;

/**
 * Parses the commandline.
 * @author peer
 *
 */
public class CommandLineParser {

	public final class ActiveOptions {

		private final CommandLine commandLine;
				
		public ActiveOptions(String... args) throws ParseException {
			try {
				this.commandLine = new DefaultParser().parse(validOptions, args);
				List<String> excessive = this.commandLine.getArgList();
				if (!excessive.isEmpty()) {
					throw new UnrecognizedOptionException("Unrecognized option(s): "+excessive.toString());
				}
			} catch (ParseException e) {
				printHelp();
				throw e;
			}
		}

		public boolean hasOption(Option o) {
			return commandLine.hasOption(o.getOpt());
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
				Option o = findOption(opt);
				throw newMissingArgumentException(o);
			}
			return optionValue;
		}

		private Option findOption(char opt) {
			Option result = validOptions.getOption(String.valueOf(opt));
			assert result!=null : "No such option: "+opt;
			return result; 
		}

		public boolean isUpload() {
			return commandLine.hasOption("u");
		}

		/**
		 * Create URL for FTP upload. Syntax is expected to follow
		 * <a href="ftp://ftp.funet.fi/pub/doc/rfc/rfc1738.txt">RFC 1738</a>. Example:
		 * <code>ftp://user:password@p2r.se:21/links</code>
		 * 
		 * @return
		 * @see http://jkorpela.fi/ftpurl.html
		 */
		public URL getUploadURL() {
			if (isUpload()) {
				String url = commandLine.getOptionValue('u');
				try {
					return new URL(url);
				} catch (MalformedURLException e) {
					throw new IllegalArgumentException("Invalid upload address: "+url, e);
				}
			}
			return null;
		}

		public boolean isHelp() {
			return commandLine.hasOption('h');
		}

		public int printHelp() {
			String name = "java " + BookmarkExporter.class.getSimpleName();
			new HelpFormatter().printHelp(name, validOptions, true);
			return 0; // system exit code: ok
		}

	}
	
	private final Options validOptions;
	private ActiveOptions activeOptions;

	public CommandLineParser() {
		
		// TODO group options
		this.validOptions = new Options()
				.addOption("h", "help", false, "Show this help text")
				.addOption("b", "browser", true, "Browser type: "+Arrays.asList(BrowserType.values()).toString())
				.addOption("t", "target", true, "Target folder for writing exported files (default is user's temp directory)")
				.addOption("c", "config", true, "Configuration file (foobar.properties), mandatory if running Firefox")
				.addOption("p", "plain", false, "Plain list output (default is tree)")
				.addOption("u", "upload", true, "Upload to FTP destination (default is no upload). Format follows RFC 1738: 'ftp://<user>:<password>@<host>:<port>/<path>'");
		this.validOptions.getOption("b").setRequired(true);
	}

	public ActiveOptions parse(String... args) throws ParseException {
		activeOptions = new ActiveOptions(args);
		try {
			return validate(activeOptions);
		} catch (ParseException e) {
			activeOptions.printHelp();
			throw e;
		}
	}

	private ActiveOptions validate(ActiveOptions cl) throws ParseException {
		for (Option o: validOptions.getOptions()) {
			if (o.isRequired() && !cl.hasOption(o)) {
				throw newMissingArgumentException(o);
			}
		}
		return cl;
			
	}

	private static MissingArgumentException newMissingArgumentException(Option o) throws MissingArgumentException {
		return new MissingArgumentException(String.format("-%s (--%s)", o.getOpt(), o.getLongOpt()));
	}

}
