/**
 * 
 */
package se.p2r.foxport.util;

import static se.p2r.foxport.util.Utils.debug;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import se.p2r.foxport.Bookmark;

/**
 * Recursively select bookmarks from filter criteria (name matching).
 * 
 * @author peer
 *
 */
public class DeepBookmarkSelector {

	private final Set<String> wantedNames;

	public DeepBookmarkSelector(Set<String> wantedNames) {
		this.wantedNames = wantedNames;
	}

	public ListValuedMap<String, Bookmark> select(List<? extends Bookmark> bookmarks) {
		ListValuedMap<String, Bookmark> result = new ArrayListValuedHashMap();
		for (Bookmark each : bookmarks) {
			if (accept(each)) {
				result.put(each.getTitle().toLowerCase(), each);
			}
			if (each.hasChildren()) {
				result.putAll(select(each.getChildren()));
			}
		}
		return result;
	}

	private boolean accept(Bookmark prospect) {
		boolean result = prospect.isContainer() && wantedNames.contains(prospect.getTitle().toLowerCase());
		debug(prospect+": "+result);
		return result;
	}

}