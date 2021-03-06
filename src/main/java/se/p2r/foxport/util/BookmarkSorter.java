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

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections4.comparators.BooleanComparator;

import se.p2r.foxport.Bookmark;

public class BookmarkSorter implements Comparator<Bookmark> {

	private static final BookmarkSorter instance = new BookmarkSorter();
	
	@Override
	public int compare(Bookmark b1, Bookmark b2) {
		int type = BooleanComparator.getTrueFirstComparator().compare(isContainer(b1), isContainer(b2));
		return type==0 ? compareTitles(b1, b2) : type; 
	}

	private static Boolean isContainer(Bookmark b1) {
		return Boolean.valueOf(b1.isContainer());
	}

	private int compareTitles(Bookmark b1, Bookmark b2) {
		return title(b1).compareTo(title(b2));
	}

	private String title(Bookmark b) {
		String title = b.getTitle();
		return title==null ? "" : title.toLowerCase().trim();
	}

	public static Collection<Bookmark> sort(Collection<? extends Bookmark> children) {
		Set<Bookmark> result = new TreeSet(instance);
		result.addAll(children);
		return result;
	}

}
