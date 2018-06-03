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

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

/**
 * Not a proper unit test since it depends on brittle, external resources.
 * Use primarily to test drive logic on specific URLs found using ordinary processing. 
 * @author peer
 *
 */
public class LinkConnectorTest {

	private LinkConnector testee;

	@Before
	public void setUp() {
		testee = new LinkConnector();
	}

	@Test
	public void ok200() throws Exception {
		expect(200, "http://p2r.se");
		expect(200, "http://p2r.se/starters");
		expect(200, "http://www.rollingstones.com"); // will give 403 unless user agent is set
	}

	@Test
	public void ok200WithGET() throws Exception {
		expect(200, "http://www.laget.se/sdg/");
		expect(302, "http://bose.com/");
	}
	
	@Test
	public void redirected300() throws Exception {
		expect(301, "http://code.google.com/p/socialmusicdiscovery");
	}

	@Test
	public void fail400() throws Exception {
		expect(404, "http://p2r.se/dummy");
		expect(408, "http://hotell.kelkoo.se/Error/PageNotFound?aspxerrorpath=/TrafficInspection/23ff9ee0-6711-11e8-abb1-d56650547a15");
	}
		
	@Test
	public void failWithTimeout() throws Exception {
		testee = new LinkConnector(1);
		expect(408, "http://p2r.se");
	}
	
	private void expect(int expectedCode, String url) {
		try {
			URL u = new URL(url);
			assertEquals(url, expectedCode, testee.connect(u));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

}
