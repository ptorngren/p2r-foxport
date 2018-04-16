/*
Copyright (c) 2014, Peer Törngren
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the p2r-foxport project nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package se.p2r.foxport.chrome;

import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import se.p2r.foxport.Bookmark;

/**
 * A Chrome bookmark. 
 * 
 * TODO parse meta_info.stars.note
 * TODO convert date numbers to dates
 * @author peer
 *
 */
public class ChromeBookmark implements Bookmark {

	private static class MetaInfo {
		@SerializedName("last_visited_desktop")		
		private long lastVisitedDesktop; // "13168342699619004",
		
		 @SerializedName("stars.note")		
		 private String note; // "Detta är en länk till Handelsbanken",
		 @SerializedName("stars.version")		
		 private String version; // "crx.2.2016.128.11729"
	}

	private long id; // "1"
	
	@SerializedName("date_added")		
	private long dateAdded; // "13168342730424920"

	@SerializedName("date_modified")		
	private long dateModified; // "13168342699618897"
	private String name; // "Sparbanken Nord - Regionens egen bank"
	private String type; // "url", "folder"
	private String url; // "https://www.sparbankennord.se/"
	private List<ChromeBookmark> children;
	
	@SerializedName("meta_info")		
	private MetaInfo metaInfo;

	@Override
	public List<? extends Bookmark> getChildren() {
		return children == null ? Collections.emptyList() : children;
	}

	@Override
	public String getTitle() {
		return name;
	}

	@Override
	public boolean isLink() {
		return "url".equals(type);
	}

	@Override
	public boolean isContainer() {
		return "folder".equals(type);
	}

	@Override
	public boolean hasChildren() {
		return !(children == null || children.isEmpty());
	}

	@Override
	public String getUri() {
		return url;
	}

	public long getId() {
		return id;
	}

	public long getDate_added() {
		return dateAdded;
	}

	public long getDate_modified() {
		return dateModified;
	}

	@Override
	public String getDescription() {
		return metaInfo==null ? null : metaInfo.note;
	}

}
