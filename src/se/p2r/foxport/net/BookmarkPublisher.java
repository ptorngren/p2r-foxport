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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import se.p2r.foxport.util.Utils;

/**
 * Upload files to a specified FTP location.
 * 
 * 
 * @author peer
 * @see http://www.codejava.net/java-se/networking/ftp/java-ftp-file-upload-tutorial-and-example
 */
public class BookmarkPublisher {

	private final URL url;
	private final String strippedURL;

	public BookmarkPublisher(URL url) {
		this.url = url;
		String externalForm = url.toExternalForm();
		String userInfo = url.getUserInfo();
		this.strippedURL = externalForm.replaceAll(userInfo, "(user:password)");
	}

	public void publish(Collection<File> localFiles) {
		try {
			Utils.log(String.format("<UPLOAD>Uploading %d files to %s ...", localFiles.size(), strippedURL));
			
			boolean ok = doPublish(localFiles);
			if (ok) {
				Utils.log(String.format("</UPLOAD>Uploaded %d files to %s", localFiles.size(), strippedURL));
			} else {
				Utils.log("Failed to upload all files to "+strippedURL);
			}
			
		} catch (IOException e) {
			Utils.log("Failed to upload all files to "+strippedURL);
			e.printStackTrace();
		}
	}

	private boolean doPublish(Collection<File> files) throws SocketException, IOException {
		FTPClient ftpClient = createClient();
		for (File file : files) {
			Utils.log(String.format("Uploading %s ...", file));
			boolean ok = upload(ftpClient, file);
			if (ok) {
				Utils.log(String.format("Uploaded %s", file));
			}
		}
//		return ftpClient.completePendingCommand();
		return true;
	}

	private boolean upload(FTPClient ftpClient, File localFile) throws FileNotFoundException, IOException {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(localFile);
			return ftpClient.storeFile(localFile.getName(), inputStream);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}

//	private void upload2(FTPClient ftpClient, File localFile) throws IOException {
//		String secondRemoteFile = localFile.getName();
//
//		FileInputStream inputStream = null;
//		OutputStream outputStream = null;
//		try {
//			inputStream = new FileInputStream(localFile);
//			outputStream = ftpClient.storeFileStream(secondRemoteFile);
//			byte[] bytesIn = new byte[4096];
//			int read = 0;
//
//			while ((read = inputStream.read(bytesIn)) != -1) {
//				outputStream.write(bytesIn, 0, read);
//			}
//		} finally {
//			if (inputStream != null) {
//				inputStream.close();
//			}
//			if (outputStream != null) {
//				outputStream.close();
//			}
//		}
//	}
//
	private FTPClient createClient() throws SocketException, IOException {
		String host = url.getHost();
		Utils.log("Connecting to " + host + " ...");
		
		String[] userInfo = url.getUserInfo().split(":");
		assert userInfo.length==2;
		String user = userInfo[0];
		String pw = userInfo[1];
		int port = url.getPort();

		FTPClient ftpClient = new FTPClient();
		ftpClient.connect(host, port);
		ftpClient.login(user, pw);
		ftpClient.enterLocalPassiveMode();

		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		Utils.log("Connected to " + host);
		
		return ftpClient;
	}


}
