/*
Copyright (c) 2018, Peer Törngren
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the p2r-foxport project nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package se.p2r.foxport.internal;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;

import se.p2r.foxport.BookmarkExporter;
import se.p2r.foxport.util.Utils;
import se.p2r.foxport.util.Utils.BrowserType;

public final class ActiveOptions {

	private final CommandLine commandLine;
	private final Options validOptions;
			
	public ActiveOptions(Options validOptions, String... args) throws ParseException {
		this.validOptions = validOptions;
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

	public BrowserType getBrowserType() {
		return commandLine.hasOption('b') ? BrowserType.from(commandLine.getOptionValue('b')) : BrowserType.CHROME;
	}

	public File getTargetFolder() {
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

	public boolean isForceExport() {
		return commandLine.hasOption('f');
	}

	public boolean isTree() {
		return !isPlainList();
	}

	public boolean isUpload() {
		return commandLine.hasOption("u");
	}

	/**
	 * Create URL for FTP upload. Syntax is expected to follow
	 * <a href="ftp://ftp.funet.fi/pub/doc/rfc/rfc1738.txt">RFC 1738</a>. Example:
	 * <code>ftp://user:password@p2r.se:21/links</code>
	 * 
	 * @see "http://jkorpela.fi/ftpurl.html"
	 * @return url
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
		new HelpFormatter().printHelp(160, name, null, validOptions, null, true);
		return 0; // system exit code: ok
	}

	/* internal */
	boolean hasOption(Option o) {
		return commandLine.hasOption(o.getOpt());
	}

	public boolean isVersion() {
		return commandLine.hasOption('v');
	}

	public int printVersion() {
		Properties prop = Utils.loadPropertyFile("version.properties");
		
		String name = prop.getProperty("name");
		String version = prop.getProperty("version");
		String timestamp = prop.getProperty("timestamp");
		String msg = String.format("%s | version: %s | timestamp: %s", name, version, timestamp);
		System.out.println(msg);
		return 0;
	}

}