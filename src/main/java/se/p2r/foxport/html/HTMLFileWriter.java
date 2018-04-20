/*
Copyright (c) 2014, Peer Törngren
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the p2r-foxport project nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

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
		debug("Writing html to "+outputFile+" ("+html.length()+" characters)" );
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
