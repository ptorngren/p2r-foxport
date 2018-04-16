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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;

/**
 * @author peer
 *
 */
public final class Utils {
	public static enum BrowserType {FIREFOX, CHROME}

	private Utils() {} // static utility

	public static final String HTML = ".html";
	public static final String JSON = ".json";
	public static final String JSONLZ4 = ".jsonlz4";  // firefox "new" format
	public static final String ENCODING_JSON = System.getProperty("file.encoding"); // UTF-8? input, defined by your browser?
	public static final String ENCODING_HTML = "utf-8";  // output, defined by you to suite your browsers 


	public static void log(String string) {
		// TODO proper logging? Or is it overkill?
		System.out.println(string);
	}

	public static void debug(String string) {
		// TODO proper logging?  Or is it overkill?
		if (System.getProperties().containsKey("DEBUG")) {
			System.err.println(string);
		}
	}

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

	public static InputStreamReader getInputStreamReader(File file) throws FileNotFoundException {
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		InputStreamReader isr = new InputStreamReader(in);
		return isr;
	}

	/**
	 * Convert time in millis to ISO formatted string in default timezone.
	 * @param ms
	 * @param timeZone 
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
	 * @param timeZone 
	 * @return String formatted as ISO standard
	 */
	public static String formatTimeUTC(long ms) {
		DateFormat df = new SimpleDateFormat(" yyyy-MM-dd HH:mm ");
		TimeZone tz = TimeZone.getTimeZone("UTC");
		df.setTimeZone(tz);
		
		String today = df.format(new Date(ms));
		return today;
	}

}