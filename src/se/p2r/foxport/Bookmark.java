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

/**
 * An abstraction of a bookmark, providing the essential attributes of a
 * bookmark. The bookmark may be a folder ({@link #isContainer()}) which may or
 * may not have children (links or containers), or a link ({@link #isLink()})
 * that carries a URI. Both links and containers may carry a human readable
 * description. All bookmarks are expected to carry a name.
 * 
 * @author peer
 *
 */
public interface Bookmark {

	List<? extends Bookmark> getChildren();

	/**
	 * The name of the bookmark, as it appears in the browser (and in the generated
	 * file).
	 * 
	 * @return String (not <code>null</code>)
	 */
	String getTitle();

	/**
	 * Must not return the same result as {@link #isContainer()}.
	 * 
	 * @return boolean
	 */
	boolean isLink();

	/**
	 * Must not return the same result as {@link #isLink()}.
	 * 
	 * @return boolean
	 */
	boolean isContainer();

	/**
	 * Typically returns <code>true</code> for a container (unless it is empty) and
	 * always <code>false</code> for a link.
	 * 
	 * @return boolean
	 */
	boolean hasChildren();

	/**
	 * Return the URI (if any). Containers typically return <code>null</code> links
	 * typically returns a URI string. The string may be invalid.
	 * 
	 * @return String or <code>null</code>
	 */
	String getUri();

	/**
	 * Return a string describing the bookmark, if it exists.
	 * 
	 * @return String or <code>null</code>
	 */
	String getDescription();

	
//	/**
//	 * Return an ISO formatted String showing date and time when the bookmark was added (UTC timezone).
//	 * @return String 
//	 */
//	String getDateAdded();
//	
//	/**
//	 * Return an ISO formatted String showing date and time when the bookmark was most recently modified (UTC timezone).
//	 * @return String 
//	 */
//	String getLastModified();
}
