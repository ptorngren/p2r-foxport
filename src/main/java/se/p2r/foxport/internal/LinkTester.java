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

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
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
 * Test links by looking up host name. Would like to ping hosts, but this takes
 * time + seems brittle? Not all hosts seem to respond properly.
 * 
 * @author peer
 *
 */
public class LinkTester {

	private final boolean enabled;
	private int errorCtr = 0;
	private Map<String, Collection<Bookmark>> badLinks = new TreeMap();

	public LinkTester(boolean enabled) {
		this.enabled = enabled;
	}

	public int getNumberOfErrors() {
		return errorCtr;
	}
	
	public boolean test(Bookmark bm, Stack<Bookmark> trail) {
		try {
			return !enabled || doTest(bm, trail);
		} catch (Exception e) {
			Log.warn(String.format("Unable to verify link: %s (%s)", bm.getUri(), trail), e);
		}
		return false;
	}

	private boolean doTest(Bookmark bm, Stack<Bookmark> trail) throws URISyntaxException {
		String host = new URI(bm.getUri()).getHost();
		try {
			return probe(host);
		} catch (UnknownHostException e) {
			handleBadLink(bm, trail, host);
		}
		return false;
	}

	private void handleBadLink(Bookmark bm, Stack<Bookmark> trail, String host) {
		Log.warn(String.format("Unknown host: %s [%s => %s] (%s)", host, bm.getName(), bm.getUri(), trail));
		errorCtr++;
		register(bm, trail);
	}

	private void register(Bookmark bm, Stack<Bookmark> trail) {
		String key = trail.toString();
		Collection<Bookmark> values = badLinks.get(key);
		if (values==null) {
			values = new HashSet();
			badLinks.put(key, values);
		}
		values.add(bm);
	}

	private boolean probe(String host) throws UnknownHostException {
		Log.debug("Testing " + host + "...");
		InetAddress inet = InetAddress.getByName(host);
		Log.debug("OK: host="+inet.getHostAddress());
		return true;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String dump() {
		StringPrinter p = new StringPrinter();
		dump(p);
		p.close();
		
		System.out.println(p.toString());
		return p.flush();
	}

	private void dump(StringPrinter out) {
		for (Entry<String, Collection<Bookmark>> each: badLinks.entrySet()) {
			String folderStructure = each.getKey();
			Collection<Bookmark> links = each.getValue();
			
			String hdr = String.format("%s (%d bad link(s))", folderStructure, Integer.valueOf(links.size()));
			out.println(hdr);
	
			for (Bookmark link: links) {
				String msg = String.format("\t%s\t[%s]", link.getName(), link.getUri());
				out.println(msg);
			}
		}
	}

}
