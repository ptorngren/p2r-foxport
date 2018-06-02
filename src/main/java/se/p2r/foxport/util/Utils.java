/*
Copyright (c) 2014, Peer Törngren
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the p2r-foxport project nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package se.p2r.foxport.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.stream.Collectors;

import se.p2r.foxport.Bookmark;
import se.p2r.foxport.internal.exceptions.ConfigurationException;
import se.p2r.foxport.internal.exceptions.FatalException;
import se.p2r.foxport.internal.exceptions.UnhandledException;

/**
 * @author peer
 *
 */
public final class Utils {
	public static enum BrowserType {FIREFOX, CHROME;
		
		public static BrowserType from(String type) {
			return type.length() == 1 ? fromShort(type.charAt(0)) : valueOf(type.toUpperCase());
		}

		private static BrowserType fromShort(char type) {
			switch (type) {
			case 'c':
				return CHROME;

			case 'f':
				return FIREFOX;

			default:
				throw new IllegalArgumentException("Unexpexted id: " + type);
			}
		}

		/**
		 * @return human readable string of choices
		 */
		public static String names() {
			return Arrays.asList(values()).toString();
		}
	}

	private Utils() {} // static utility
	
	public static final String SYSTEM_ENCODING = System.getProperty("file.encoding");
	public static final String UTF8 = StandardCharsets.UTF_8.name();
	public static final String ISO8859 = StandardCharsets.ISO_8859_1.name();

	public static final String HTML = ".html";
	public static final String JSON = ".json";
	public static final String JSONLZ4 = ".jsonlz4";  // firefox "new" format
	
	// TODO figure out how to use charsets properly
	public static final String ENCODING_JSON = SYSTEM_ENCODING; // UTF-8? input, defined by your browser?
	public static final String ENCODING_HTML = SYSTEM_ENCODING;  // output, defined by you to suite your browsers 


	public static final boolean endsWith(File file, String wantedEnding) {
		return file.getName().toLowerCase().endsWith(wantedEnding);
	}

	public static Collection<String> toLowerCase(String... strings) {
		List<String> result = new ArrayList();
		for (String each : strings) {
			result.add(each.toLowerCase());
		}
		return result;
	}

	public static InputStreamReader getInputStreamReader(File file, String characterSet) throws FileNotFoundException {
		InputStream in = new FileInputStream(file);
		InputStreamReader isr;
		try {
			isr = new InputStreamReader(in, characterSet);
		} catch (UnsupportedEncodingException e) {
			throw new FatalException("Cannot handle character set: "+characterSet, e);
		}
		return isr;
	}

	/**
	 * Convert time in millis to ISO formatted string in default timezone.
	 * @param ms
	 * @return String formatted as ISO standard
	 */
	public static String formatTimeISO(long ms) {
		DateFormat df = new SimpleDateFormat(" yyyy-MM-dd HH:mm ");
		TimeZone tz = TimeZone.getDefault();
		df.setTimeZone(tz);
		
		String today = df.format(new Date(ms));
		return today;
	}

	/**
	 * Convert time in millis to ISO formatted string in UTC timezone.
	 * @param ms
	 * @return String formatted as ISO standard
	 */
	public static String formatTimeUTC(long ms) {
		DateFormat df = new SimpleDateFormat(" yyyy-MM-dd HH:mm ");
		TimeZone tz = TimeZone.getTimeZone("UTC");
		df.setTimeZone(tz);
		
		String today = df.format(new Date(ms));
		return today;
	}

	/**
	 * Extract the id for external export (this will be the name of the exported file), if defined.
	 * 
	 * @return String with valid filename (without path or suffix), or <code>null</code>
	 */
	public static String extractExportId(String stringOrNull) {
		return stringOrNull==null || stringOrNull.trim().isEmpty() ? null : stringOrNull.split(":")[0];
	}

	/**
	 * Rerturn <code>null</code> if string is missing or empty.
	 * @param s
	 * @return trimmed String or <code>null</code>
	 */
	public static String nullIfEmpty(String s) {
		return s==null || s.trim().isEmpty() ? null : s.trim();
	}

	public static boolean defined(String s) {
		boolean undefined = s==null || s.trim().isEmpty();
		return !undefined;
	}

	public static Properties loadPropertyFileResource(String filename) {
		Properties result = new Properties();
		InputStream input = null;
		try {
			input = Utils.class.getClassLoader().getResourceAsStream(filename);;
			result.load(input);
		} catch (IOException e) {
			Log.fatal(e);
			System.out.println("<unknown>");
		} finally {
			if (input!=null) {
				try {
					input.close();
				} catch (IOException e) {
					throw new UnhandledException("Unable to close stream", e);
				}
			}
		}
		return result;
	}

	public static Properties loadPropertyFile(File cfgFile, String characterSet) throws ConfigurationException {
		Properties cfg = new Properties();
		try {
			cfg.load(getInputStreamReader(cfgFile, characterSet));
			return cfg;
		} catch (Exception e) {
			throw new ConfigurationException("Could not read configuration file: " + cfgFile, e);
		}
	}

	/**
	 * Extract names from supplied bookmarks.
	 * @param bookmarks
	 * @return Collection of names
	 */
	public static Collection<String> toNames(Collection<? extends Bookmark> bookmarks) {
		return bookmarks.stream().map(b->b==null ? String.valueOf(null) : b.getName()).collect(Collectors.toList());
	}
}