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

/**
 * An abstraction of a bookmark folder, irrespective of browser type.
 * Capable of merging with other containers with the same name.
 * 
 * @author peer
 *
 */
public class MutableBookmarkContainer implements Bookmark {

	private final Map<String, MutableBookmarkContainer> childFoldersByTitle = new HashMap();
	private final List<Bookmark> childLinks = new ArrayList();
	private final boolean isTaggedForExport = true;
	private final String exportId;

	private String description;
	private String title;

	public MutableBookmarkContainer(String exportId) {
		this.exportId = exportId;
	}
	
	public MutableBookmarkContainer(Bookmark source) {
		this.title = source.getTitle();
		this.description = source.getDescription();
		this.exportId = source.getExportId();
		assert validTitle();
	}

	private boolean validTitle() {
		boolean empty = title == null || title.trim().isEmpty();
		assert !empty : "No title";
		return true;
	}

	@Override
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		// concatenate if title already set
		if (this.title==null) {
			this.title = title;
		} else if (!this.title.equalsIgnoreCase(title)) {
			this.title = String.format("%s & %s", this.title, title);
		}
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

	public void setDescription(String description) {
		if (description != null) {
			if (this.description == null) {
				this.description = description;
			} else if (!this.description.contains(description)) {
				// concatenate if description already set
				// TODO proper line separator
				this.description += "\n\n" + description;
			}
		}
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
	public void mergeChildren(Bookmark source) {
		assert source.isContainer() : "Not a container: " + source;
		assert source.getTitle().equalsIgnoreCase(title) : "Wrong title. This: " + title + ", source to merge: " + source;

		for (Bookmark sourceChild: source.getChildren()) {
			if (sourceChild.isLink()) {
				childLinks.add(sourceChild);
			} else {
				String childTitle = sourceChild.getTitle().toLowerCase();
				MutableBookmarkContainer targetChild = childFoldersByTitle.get(childTitle);
				
				if (targetChild==null) {
					targetChild = new MutableBookmarkContainer(sourceChild);
					childFoldersByTitle.put(childTitle, targetChild);
				} 
				targetChild.mergeChildren(sourceChild);
			}
		}
	}

	@Override
	public String toString() {
		return "MutableBookmarkContainer [title=" + title + ", hasChildren()=" + hasChildren() + "]";
	}

	@Override
	public boolean isTaggedForExport() {
		return isTaggedForExport;
	}

	@Override
	public String getExportId() {
		return exportId;
	}

}
