/*
Copyright (c) 2014, Peer Törngren
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the p2r-foxport project nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package se.p2r.foxport.net;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import se.p2r.foxport.util.NotYetImplementedException;
import se.p2r.foxport.util.Utils;

/**
 * Upload files to a specified FTP location. FIXME
 * http://www.codejava.net/java-se/networking/ftp/java-ftp-file-upload-tutorial-and-example
 * 
 * @author peer
 *
 */
public class BookmarkPublisher {

	private final URL url;

	public BookmarkPublisher(URL url) {
		this.url = url;
	}

	/**
	 * @param files
	 */
	public void publish(Collection<File> files) {
		try {
			URL target = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath());
			Utils.log(String.format("<UPLOAD>Uploading %d files to %s ...", files.size(), target));
			throw new NotYetImplementedException();
//			Utils.log(String.format("</UPLOAD>Uploaded %d files to %s", files.size(), uri));
		} catch (MalformedURLException e) {
			throw new IllegalStateException("Cannot create URL from URL. Should not happen.");
		}
	}

}
