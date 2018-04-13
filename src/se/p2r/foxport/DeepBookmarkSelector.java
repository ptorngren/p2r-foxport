/**
 * 
 */
package se.p2r.foxport;

import static se.p2r.foxport.Utils.debug;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

/**
 * Recursively select bookmarks from filter criteria (name matching).
 * 
 * @author peer
 *
 */
public class DeepBookmarkSelector {

	private final Collection<String> wantedFolderNames;

	public DeepBookmarkSelector(Collection<String> wanted) {
		this.wantedFolderNames = wanted;
	}

	public ListValuedMap<String, FirefoxBookmark> select(List<FirefoxBookmark> bookmarks) {
		ListValuedMap<String, FirefoxBookmark> result = new ArrayListValuedHashMap();
		for (FirefoxBookmark each : bookmarks) {
			if (accept(each)) {
				result.put(each.getTitle().toLowerCase(), each);
			}
			if (each.hasChildren()) {
				result.putAll(select(each.getChildren()));
			}
		}
		return result;
	}

	private boolean accept(FirefoxBookmark prospect) {
		boolean result = prospect.isContainer() && wantedFolderNames.contains(prospect.getTitle().toLowerCase());
		debug(prospect+": "+result);
		return result;
	}

}