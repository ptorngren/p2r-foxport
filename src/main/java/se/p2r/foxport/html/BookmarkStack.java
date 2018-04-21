/*
Copyright (c) 2018, Peer Törngren
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the p2r-foxport project nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package se.p2r.foxport.html;

import java.util.Stack;

import se.p2r.foxport.Bookmark;

/**
 * Extends standard stack to produce human readable output of stack contents.
 * 
 * @author peer
 *
 */
public class BookmarkStack extends Stack<Bookmark> {

	private static final long serialVersionUID = 7376004981238845552L;

	public BookmarkStack(Bookmark root) {
		push(root);
	}

	@Override
	public synchronized String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < elementCount; i++) {
			if (i>0) {
				sb.append(" -- ");
			}
			Bookmark bm = elementAt(i);
			sb.append(bm.getName());
		}
		return sb.toString();
	}

}
