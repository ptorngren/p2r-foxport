/*
Copyright (c) 2014, Peer Törngren
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the p2r-foxport project nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package se.p2r.foxport.internal;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import se.p2r.foxport.util.Utils.BrowserType;

/**
 * Parses the commandline.
 * @author peer
 *
 */
public class CommandLineParser {
	
	static final String BROWSERTYPE = "b";
	static final String CONFIGURATIONFILE = "c";
	static final String FORCE = "f";
	static final String HELP = "h";
	static final String LISTOUTPUT = "l";
	static final String PROBE = "p";
	static final String TARGETFOLDER = "t";
	static final String UPLOAD = "u";
	static final String VERSION = "v";

	private final Options validOptions;
	private ActiveOptions activeOptions;

	public CommandLineParser() {
		
		// TODO group options
		this.validOptions = new Options()
				.addOption(BROWSERTYPE, "browser", true, String.format("Browser type: %s (default is %s)", BrowserType.names(), BrowserType.CHROME))
				.addOption(CONFIGURATIONFILE, "config", true, "Configuration file (foobar.properties), mandatory if running Firefox")
				.addOption(FORCE, "force", false, "Force export (ignore timestamps)")
				.addOption(HELP, "help", false, "Show this help text")
				.addOption(LISTOUTPUT, "list", false, "List output (default is tree)")
				.addOption(PROBE, "probe", false, "Probe links, ignore if host name is unknown (or not responding).")
				.addOption(TARGETFOLDER, "target", true, "Target folder for writing exported files (default is user's temp directory)")
				.addOption(UPLOAD, "upload", true, "Upload to FTP destination (default is no upload). Format follows RFC 1738: 'ftp://<user>:<password>@<host>:<port>/<path>'")
				.addOption(VERSION, "version", false, "show version info");
	}

	public ActiveOptions parse(String... args) throws ParseException {
		activeOptions = new ActiveOptions(validOptions, args);
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

	private static MissingArgumentException newMissingArgumentException(Option o) {
		return new MissingArgumentException(String.format("-%s (--%s)", o.getOpt(), o.getLongOpt()));
	}

}
