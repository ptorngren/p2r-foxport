/**
 * 
 */
package se.p2r.foxport.html;

import static se.p2r.foxport.util.Utils.*;

import java.io.File;
import java.io.PrintWriter;

import se.p2r.foxport.Bookmark;

/**
 * @author peer
 *
 */
public class HTMLFileWriter {

	private final File outputFile;

	public HTMLFileWriter(File targetFolder, String id) {
		String fileName = id+HTML.toLowerCase();
		this.outputFile = new File(targetFolder, fileName);
	}

	public File writeFile(String html, Bookmark root) {
		writeFile(html, outputFile);
		return outputFile;
	}

	private void writeFile(String html, File outputFile) {
		log("Writing html to "+outputFile+" ("+html.length()+" characters)" );
		PrintWriter writer = null;
		try {
			if (outputFile.isFile()) {
				debug("File found, replacing " + outputFile);
			} else {
				outputFile.getParentFile().mkdirs();
				outputFile.createNewFile();
			}
			writer = new PrintWriter(outputFile, ENCODING_HTML);
			writer.println(html);
			log("OK: Wrote "+outputFile);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to write file: "+outputFile, e);
		} finally {
			if (writer !=null) {
				writer.close();
			}
		}
	}

}
