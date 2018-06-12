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

import java.util.List;

public class FirefoxBookmarks {
	
	private String title;
	private int id;
	private long dateAdded;
	private long lastModified;
	private String type;
	private String root;
	private List<FirefoxBookmark> children;
	
	public String getTitle() {
		return title;
	}

	public int getId() {
		return id;
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

	public String getRoot() {
		return root;
	}

	public List<FirefoxBookmark> getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [title=" + title + ", id=" + id + ", dateAdded=" + dateAdded + ", lastModified=" + lastModified + ", type=" + type + ", root=" + root + ", children=" + children.size() + "]";
	}

}
