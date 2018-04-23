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

/**
 * Parses a description string to separate export id from description (if
 * present). Tag is expected to be embedded in description using format
 * <code>"#tag#title;some descriptive text"</code>
 * 
 * @author peer
 *
 */
public class ExportTagParser {

	private final String exportId;
	private final String title;
	private final String description;

	/**
	 * 
	 * @param description syntax "#<exportId>#<title>;<description> where all elements are optional
	 * @param folderName as seen in browser, default for both {@link #exportId} and {@link #title} if not specified 
	 */
	public ExportTagParser(String description, String folderName) {
		String match = "#\\w*#.*"; // "#tag#whatever" or "##whatever"
		boolean isTag = description!=null && description.matches(match);
		
		if (isTag) {
			// export id
			String tag = "#\\w*#"; // #tag#
			String[] split = description.split("[#]");
			this.exportId = split.length>2 && !split[1].isEmpty() ? split[1] : folderName;
			String withoutTag = description.replaceFirst(tag, "");

			// title
			int delimiter = withoutTag.indexOf(';'); // title;description
			this.title = delimiter>0 ? withoutTag.substring(0, delimiter) : folderName;
			
			// description
			this.description = withoutTag.substring(1+delimiter);
			
			// assert parsing
			assert Utils.defined(this.exportId): "No export ID generated: "+description;
			assert Utils.defined(this.title) : "No export ID generated: "+description;
			
		} else {
			this.exportId = null;
			this.description = description;
			this.title = null;
		}
	}

	/**
	 * @return alphanumeric id or <code>null</code>
	 */
	public String getExportId() {
		return exportId;
	}

	/**
	 * @return human readable string or <code>null</code>
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return title parsed from description, or <code>null</code>
	 */
	public String getTitle() {
		return title;
	}

	public boolean isTaggedForExport() {
		return exportId!=null;
	}
}
