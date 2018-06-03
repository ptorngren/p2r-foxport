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
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;

import se.p2r.foxport.Bookmark;
import se.p2r.foxport.util.Log;
import se.p2r.foxport.util.StringPrinter;

/**
 * Test links by looking up host name and checking response of URL get. 
 * 
 * @author peer
 *
 */
public class LinkTester {

	private final boolean enabled;
	private final LinkConnector connector = new LinkConnector();
	
	private int errorCtr = 0;
	private int movedCtr = 0;
	private Map<String, Collection<Bookmark>> badLinks = new TreeMap();
	private Map<String, Collection<Bookmark>> movedLinks = new TreeMap();

	public LinkTester(boolean enabled) {
		this.enabled = enabled;
	}

	public int getNumberOfErrors() {
		return errorCtr;
	}
	
	public int getNumberOfMoved() {
		return movedCtr;
	}
	
	public boolean test(Bookmark bm, Stack<Bookmark> trail) {
		try {
			return !enabled || doTest(bm, trail);
		} catch (Exception e) {
			Log.error(String.format("Unable to verify link: %s (%s) [%s]", bm.getUri(), trail, e.toString()));
			e.printStackTrace();
		}
		return false;
	}

	private boolean doTest(Bookmark bm, Stack<Bookmark> trail) {
		// parse URI, exit if bad
		URI uri = null;
		try {
			uri = new URI(bm.getUri());
		} catch (URISyntaxException e1) {
			return registerBadBookmark(bm, trail, String.format("Bad URI: %s => %s (%s)", bm.getName(), bm.getUri(), trail));
		}

		// probe host and URL 
		boolean hostOK = probeHost(uri, bm, trail);
		if (hostOK && uri.getScheme().equalsIgnoreCase("http")) {
			try {
				return probeURL(bm, uri.toURL(), trail);
			} catch (IOException e) {
				String msg = String.format("Cannot verify link: %s => %s (%s) [%s]", bm.getName(), bm.getUri(), trail, e.getMessage());
				Log.warn(msg, e);
				return true; // don't skip link
			}
		}
		return hostOK;
	}


	private boolean probeHost(URI uri, Bookmark bm, Stack<Bookmark> trail) {
		try {
			Log.debug(String.format("%s: testing host '%s' (%s) ...", bm.getName(), uri.getHost(), trail));
			InetAddress.getByName(uri.getHost());
			return true;
		} catch (UnknownHostException e) {
			return registerBadBookmark(bm, trail, String.format("Unknown host: %s [%s => %s] (%s)", bm.getUri(), bm.getName(), bm.getUri(), trail));
		}
	}

	/**
	 * @see "https://developer.mozilla.org/en-US/docs/Web/HTTP/Status"
	 * @see "https://stackoverflow.com/questions/1378199/how-to-check-if-a-url-exists-or-returns-404-with-java"
	 */
	private boolean probeURL(Bookmark bm, URL url, Stack<Bookmark> trail) throws IOException {
		Log.debug(String.format("%s: testing URL '%s' (%s) ...", bm.getName(), url, trail));
		int responseCode = connector.connect(url);
		
		switch (responseCode) {

		// OK 
		case HttpURLConnection.HTTP_OK: // 200
		case HttpURLConnection.HTTP_ACCEPTED: // 202
		case HttpURLConnection.HTTP_MOVED_TEMP: // 302
		case HttpURLConnection.HTTP_NOT_MODIFIED: // 304
		case 307:
			return true;

		// permanently redirected
		case HttpURLConnection.HTTP_MOVED_PERM: // 301
		case HttpURLConnection.HTTP_SEE_OTHER: // 303
		case 308:
			return registerMovedBookmark(bm, trail, responseCode);

		// no response
		case HttpURLConnection.HTTP_CLIENT_TIMEOUT: // 408
			return registerBadBookmark(bm, trail, String.format("Timeout on link (%d): %s => %s (%s)", Integer.valueOf(responseCode), bm.getName(), bm.getUri(), trail));
			
		// Everything else is a failure
		default:
			return registerBadBookmark(bm, trail, String.format("Unresolved link (%d): %s => %s (%s)", Integer.valueOf(responseCode), bm.getName(), bm.getUri(), trail));
		}
	}

	private boolean registerMovedBookmark(Bookmark bm, Stack<Bookmark> trail, int responseCode) {
		register(movedLinks, bm, trail);
		movedCtr++;
		String msg = String.format("URL moved (%d): %s => %s (%s)", Integer.valueOf(responseCode), bm.getName(), bm.getUri(), trail);
		Log.warn(msg);
		return true;
	}

	private boolean registerBadBookmark(Bookmark bm, Stack<Bookmark> trail, String msg) {
		register(badLinks, bm, trail);
		errorCtr++;
		Log.warn(msg);
		return false;
	}

	private void register(Map<String, Collection<Bookmark>> badLinks2, Bookmark bm, Stack<Bookmark> trail) {
		String key = trail.toString();
		Collection<Bookmark> values = badLinks2.get(key);
		if (values==null) {
			values = new HashSet();
			badLinks2.put(key, values);
		}
		values.add(bm);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String dump() {
		return dump("");
	}

	public String dump(String hdr) {
		StringPrinter p = new StringPrinter(hdr);
		dump(p);
		return p.close();
	}

	private void dump(StringPrinter out) {
		out.println(String.format("<%d BAD LINKS>", Integer.valueOf(errorCtr)));
		dump(out, badLinks);

		out.println();
		out.println(String.format("<%d MOVED LINKS>", Integer.valueOf(movedCtr)));
		dump(out, movedLinks);
	}

	private void dump(StringPrinter out, Map<String, Collection<Bookmark>> map) {
		for (Entry<String, Collection<Bookmark>> each: map.entrySet()) {
			String folderStructure = each.getKey();
			Collection<Bookmark> links = each.getValue();
			
			String hdr = String.format("%s (%d link(s))", folderStructure, Integer.valueOf(links.size()));
			out.println(hdr);
	
			for (Bookmark link: links) {
				String msg = String.format("\t%s\t[%s]", link.getName(), link.getUri());
				out.println(msg);
			}
		}
	}

}
