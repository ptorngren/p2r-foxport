/**
 * 
 */
package se.p2r.foxport.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author peer
 *
 */
public final class Utils {
	private Utils() {} // static utility

	public static final String HTML = ".html";
	public static final String JSON = ".json";
	public static final String JSONLZ4 = ".jsonlz4";
	public static final String ENCODING_JSON = System.getProperty("file.encoding"); // UTF-8? input, defined by your Firefox?
	public static final String ENCODING_HTML = "utf-8";  // output, defined by you to suite browsers 


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


}
