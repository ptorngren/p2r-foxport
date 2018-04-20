/*
Copyright (c) 2014, Peer Törngren
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the p2r-foxport project nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package se.p2r.foxport.firefox;

import java.util.Collections;
import java.util.List;

import se.p2r.foxport.Bookmark;
import se.p2r.foxport.util.Utils;

public class FirefoxBookmark implements Bookmark {
	private String title; // "Jul"
	private int id; // 51
	private int parent; // 16
//	private long dateAdded; // 1236710299000000
//	private long lastModified; // 1236710299000000
	private List<FirefoxAnnotation> annos; // optional
	private String type; // "text/x-moz-place-container", "text/x-moz-place" // TODO separate subclasses
	private String root; // "bookmarksMenuFolder" (only for x-moz-place-container?)
	private List<FirefoxBookmark> children; // only for x-moz-place-container?
	private String uri; // "http://www.p2r.se/links" (only for x-moz-place)

	@Override
	public boolean isContainer() {
		return type.toLowerCase().equals("text/x-moz-place-container");
	}

	@Override
	public boolean isLink() {
		return type.toLowerCase().equals("text/x-moz-place");
	}

	@Override
	public boolean hasChildren() {
		return !getChildren().isEmpty();
	}

	@Override
	public String getDescription() {
		if (annos == null || annos.isEmpty()) {
			return null;
		}
		String description = null;
		for (FirefoxAnnotation prospect : annos) {
			if ("bookmarkProperties/description".equalsIgnoreCase(prospect.getName())) {
				if (description == null) {
					description = prospect.getValue();
				} else {
					throw new IllegalStateException("Multiple descriptions found. Have: " + description + ", found: " + prospect.getValue());
				}
			}
		}
		return description;
	}

	@Override
	public String getTitle() {
		return title;
	}

	public int getId() {
		return id;
	}

	public int getParent() {
		return parent;
	}

//	@Override
//	public String getDateAdded() {
//		return Utils.toISO(dateAdded);
//	}
//
//	@Override
//	public String getLastModified() {
//		return Utils.toISO(lastModified);
//	}
//
//	public String getType() {
//		return type;
//	}
//
	public List<FirefoxAnnotation> getAnnotations() {
		return annos;
	}

	public String getRoot() {
		return root;
	}

	@Override
	public List<FirefoxBookmark> getChildren() {
		return children == null ? Collections.emptyList() : children;
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public String toString() {
		return "FirefoxBookmark [title=" + title + ", hasChildren()=" + hasChildren() + "]";
	}

	@Override
	public boolean isTaggedForExport() {
		return getExportId()!=null ;
	}

	@Override
	public String getExportId() {
		return Utils.extractExportId(getDescription());
	}

}
