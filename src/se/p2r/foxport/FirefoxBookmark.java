/*
Copyright (c) 2014, Peer Törngren
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the p2r-foxport project nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package se.p2r.foxport;

import java.util.List;

public class FirefoxBookmark {
	private String title; // "Jul"
	private int id;  // 51
	private int parent; // 16
	private long dateAdded; // 1236710299000000
	private long lastModified; // 1236710299000000
	private List<FirefoxAnnotation> annos; // optional
	private String type; // "text/x-moz-place-container", "text/x-moz-place"  // TODO separate subclasses
	private String root; // "bookmarksMenuFolder" (only for x-moz-place-container?)
	private List<FirefoxBookmark> children; // only for x-moz-place-container?
	private String uri; // "http://www.p2r.se/links" (only for x-moz-place)

	public boolean isContainer() {
		return getType().toLowerCase().equals("text/x-moz-place-container") ;
	}
	public boolean isLink() {
		return getType().toLowerCase().equals("text/x-moz-place");
	}
	public boolean hasChildren() {
		return getChildren()!=null && !getChildren().isEmpty();
	}

	public String getDescription() {
		if (annos==null || annos.isEmpty()) {
			return null;
		}
		String description = null;
		for (FirefoxAnnotation prospect : annos) {
			if ("bookmarkProperties/description".equalsIgnoreCase(prospect.getName())) {
				if (description==null) {
					description = prospect.getValue();
				} else {
					throw new IllegalStateException("Multiple descriptions found. Have: " + description+", found: " + prospect.getValue());
				}
			}
		}
		return description;
	}
	
	public String getTitle() {
		return title;
	}
	public int getId() {
		return id;
	}
	public int getParent() {
		return parent;
	}
	public long getDateAdded() {
		return dateAdded;
	}
	public long getLastModified() {
		return lastModified;
	}
	public String getType() {
		return type;
	}
	public List<FirefoxAnnotation> getAnnotations() {
		return annos;
	}
	public String getRoot() {
		return root;
	}
	public List<FirefoxBookmark> getChildren() {
		return children;
	}
	public String getUri() {
		return uri;
	}

	@Override
	public String toString() {
		String childinfo = children==null ? ", (no children)" : ", children=" + children.size();
		return getClass().getSimpleName() + " [title=" + title + ", id=" + id + ", parent=" + parent + ", dateAdded=" + dateAdded + ", lastModified=" + lastModified + ", type=" + type + ", root=" + root + childinfo + childinfo + ", uri=" + uri + "]";
	}

}
