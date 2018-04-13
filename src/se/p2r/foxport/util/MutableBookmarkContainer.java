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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.p2r.foxport.Bookmark;

public class MutableBookmarkContainer implements Bookmark {

	private final String title;
	private final Map<String, MutableBookmarkContainer> childFoldersByTitle = new HashMap();
	private final List<Bookmark> childLinks = new ArrayList();
	private String description;

	public MutableBookmarkContainer(String title) {
		this.title = title;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public List<? extends Bookmark> getChildren() {
		List<Bookmark> result = new ArrayList(childFoldersByTitle.values());
		result.addAll(childLinks);
		return result;
	}

	@Override
	public boolean isLink() {
		return false;
	}

	@Override
	public boolean hasChildren() {
		return !empty();
	}

	private boolean empty() {
		return childLinks.isEmpty() && childFoldersByTitle.isEmpty();
	}

	@Override
	public String getUri() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isContainer() {
		return true;
	}

	/**
	 * Recursively merge children of supplied container with children of this
	 * container.
	 * 
	 * @param source
	 */
	public void merge(Bookmark source) {
		assert source.isContainer() : "Not a container: " + source;
		assert source.getTitle().equalsIgnoreCase(title) : "Wrong title: " + source;

		for (Bookmark sourceChild: source.getChildren()) {
			if (sourceChild.isLink()) {
				childLinks.add(sourceChild);
			} else {
				String childTitle = sourceChild.getTitle().toLowerCase();
				MutableBookmarkContainer targetChild = childFoldersByTitle.get(childTitle);
				
				if (targetChild==null) {
					targetChild = new MutableBookmarkContainer(childTitle);
					childFoldersByTitle.put(childTitle, targetChild);
				} 
				targetChild.merge(sourceChild);
			}
		}
	}

	@Override
	public String toString() {
		return "MutableBookmarkContainer [title=" + title + ", hasChildren()=" + hasChildren() + "]";
	}

}
