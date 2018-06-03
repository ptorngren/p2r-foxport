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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Connect to an URL and get response code without hanging on bad URLs. Can get input stream to force 
 * error instead of hanging when getting response code (some pages will not return response code), but we will have a hard time to find proper codes.
 * Better to use a timer that cancels if no response is returned in time. 
 * @author peer
 *
 * @see "https://community.oracle.com/thread/1147201"
 */
public class LinkConnector {

	private static class MyConnectorTask implements Callable<Integer>  {

		private final HttpURLConnection huc;

		public MyConnectorTask(HttpURLConnection huc) {
			this.huc = huc;
		}

		@Override
		public Integer call() throws Exception {
			System.out.println(MyConnectorTask.class.getName()+".call() ["+huc.getRequestMethod()+"]:"+huc.getURL());
			huc.connect();
			return Integer.valueOf(huc.getResponseCode());
		}

	}

	private final int timeout;

	public LinkConnector() {
		this(3000);
	}

	public LinkConnector(int timeoutMilliseconds) {
		this.timeout = timeoutMilliseconds;
	}

	/**
	 * @param url
	 * @return HTTP response code (example: 200, 404) 
	 * @throws IOException
	 * @throws ProtocolException
	 */
	public int connect(URL url) throws IOException, ProtocolException {
		int responseCode = doConnect(url, "HEAD");
		if (mightWorkWithGET(responseCode)) {
			responseCode = doConnect(url, "GET");
		}
		return responseCode;
	}

	private boolean mightWorkWithGET(int code) {
		switch (code) {
		case HttpURLConnection.HTTP_NOT_FOUND:
		case HttpURLConnection.HTTP_BAD_METHOD:
		case HttpURLConnection.HTTP_FORBIDDEN:
			return true;

		default:
			return false;
		}
	}

	/* Should ideally use "HEAD", but some sites seem to report 404 for "HEAD" whereas "GET" works fine (example: http://www.laget.se/sdg)
	 * Then again, some sites respond 200 for "GET" but "408" for "HEAD (example: http://hotell.kelkoo.se/Error/PageNotFound?aspxerrorpath=/TrafficInspection/23ff9ee0-6711-11e8-abb1-d56650547a15)
	 * And some sites respond 405 for "HEAD" but "302" for "GET" (example: http://bose.com)
	 */
	private int doConnect(URL url, String requestMethod) throws IOException, ProtocolException {
		HttpURLConnection huc = null;
		try {
			// create connection
			huc = (HttpURLConnection) url.openConnection();
			huc.setRequestMethod(requestMethod); 
			huc.setConnectTimeout(timeout);
			huc.setReadTimeout(timeout);
			huc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
			
			// probe connection
			return openConnection(url, huc);
		} finally {
			if (huc!=null) {
				huc.disconnect();
			}
		}
	}

	private int openConnection(URL url, HttpURLConnection huc) throws IOException, ProtocolException {
		int responseCode = callConnector(huc);
		URL returnedURL = huc.getURL(); // may be different after redirect
		
		if (responseCode==HttpURLConnection.HTTP_MOVED_PERM && isSecureProtocolChange(url, huc)) {
			// ignore changes from http to https
			responseCode = HttpURLConnection.HTTP_ACCEPTED;
		} else if (redirected(responseCode) && !equal(url, returnedURL)) {
				// if redirected, test the redirection URL
				responseCode = connect(returnedURL);
		}
		return responseCode;
	}

	private boolean redirected(int responseCode) {
		return responseCode>=300 && responseCode<400;
	}

	private boolean equal(URL u1, URL u2) {
		// URL.equals() seem to be case significant which causes infinite loops
		return u1.toString().equalsIgnoreCase(u2.toString());
	}

	private int callConnector(HttpURLConnection huc) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Integer> future = executor.submit(new MyConnectorTask(huc));
		Integer responseCode = Integer.valueOf(HttpURLConnection.HTTP_CLIENT_TIMEOUT);
		
		try {
			responseCode = future.get(timeout, TimeUnit.MILLISECONDS);
		} catch (TimeoutException | InterruptedException | ExecutionException e) {
			future.cancel(true);
		}

		executor.shutdownNow();
		return responseCode.intValue();
	}

	private boolean isSecureProtocolChange(URL url, HttpURLConnection huc) {
		String redirectedURL = huc.getHeaderField("Location");
		return redirectedURL!=null && stripProtocol(url.toString()).equals(stripProtocol(redirectedURL));
	}

	private String stripProtocol(String url) {
		return url.replaceFirst("http.*://", "<protocol>");
	}


}
