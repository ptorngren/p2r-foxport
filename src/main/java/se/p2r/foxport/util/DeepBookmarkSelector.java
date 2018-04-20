/*
Copyright (c) 2014, Peer Törngren
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the p2r-foxport project nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package se.p2r.foxport.util;


import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import se.p2r.foxport.Bookmark;

/**
 * Recursively select bookmarks from filter criteria (name matching).
 * 
 * @author peer
 *
 */
public class DeepBookmarkSelector {

	private final Set<String> wantedNames;

	/**
	 * Constructor for selecting folders based on {@link Bookmark#isTaggedForExport()}.
	 */
	public DeepBookmarkSelector() {
		this.wantedNames = Collections.EMPTY_SET;
	}

	/**
	 * Constructor for selecting folders based on  {@link Bookmark#getTitle()}.
	 */
	public DeepBookmarkSelector(Set<String> wantedNames) {
		this.wantedNames = wantedNames;
	}

	public ListValuedMap<String, Bookmark> select(List<? extends Bookmark> bookmarks) {
		ListValuedMap<String, Bookmark> result = new ArrayListValuedHashMap();
		for (Bookmark each : bookmarks) {
			if (accept(each)) {
				result.put(each.getTitle().toLowerCase(), each);
			}
			if (each.hasChildren()) {
				result.putAll(select(each.getChildren()));
			}
		}
		return result;
	}

	private boolean accept(Bookmark prospect) {
		boolean result = prospect.isContainer() && (prospect.isTaggedForExport() || wantedNames.contains(prospect.getTitle().toLowerCase()));
		Log.debug(prospect+": "+result);
		return result;
	}

}