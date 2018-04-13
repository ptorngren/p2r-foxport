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
		int type = BooleanComparator.getTrueFirstComparator().compare(b1.isContainer(), b2.isContainer());
		return type==0 ? compareTitles(b1, b2) : type; 
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
