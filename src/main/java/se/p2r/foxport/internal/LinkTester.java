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

import se.p2r.foxport.Bookmark;
import se.p2r.foxport.util.Log;

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

	public LinkTester(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean test(Bookmark bm) {
		try {
			return !enabled || doTest(bm);
		} catch (Exception e) {
			Log.warn("Unable to verify link: " + bm.getUri(), e);
		}
		return false;
	}

	private boolean doTest(Bookmark bm) throws URISyntaxException {
		URI uri = new URI(bm.getUri());
		String host = uri.getHost();
		Log.debug("Testing " + host + "...");
		try {
			InetAddress inet = InetAddress.getByName(host);
			Log.debug("host="+inet.getHostAddress());
			return true;
		} catch (UnknownHostException e) {
			Log.warn(String.format("Unknown host: %s [%s => %s]", host, bm.getTitle(), bm.getUri()));
			errorCtr++;
		}
		return false;
	}

	public int getNumberOfErrors() {
		return errorCtr;
	}

}
