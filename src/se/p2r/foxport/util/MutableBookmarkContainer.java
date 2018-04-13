package se.p2r.foxport.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.p2r.foxport.Bookmark;

public class MutableBookmarkContainer implements Bookmark {

	private final String title;
	private final Map<String, MutableBookmarkContainer> childFoldersByTitle = new HashMap();
	private final List<Bookmark> childLinks = new ArrayList();
	private String description;

	public MutableBookmarkContainer(String title) {
		this.title = title;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public List<? extends Bookmark> getChildren() {
		List<Bookmark> result = new ArrayList(childFoldersByTitle.values());
		result.addAll(childLinks);
		return result;
	}

	@Override
	public boolean isLink() {
		return false;
	}

	@Override
	public boolean hasChildren() {
		return !empty();
	}

	private boolean empty() {
		return childLinks.isEmpty() && childFoldersByTitle.isEmpty();
	}

	@Override
	public String getUri() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isContainer() {
		return true;
	}

	/**
	 * Recursively merge children of supplied container with children of this
	 * container.
	 * 
	 * @param source
	 */
	public void merge(Bookmark source) {
		assert source.isContainer() : "Not a container: " + source;
		assert source.getTitle().equalsIgnoreCase(title) : "Wrong title: " + source;

		for (Bookmark sourceChild: source.getChildren()) {
			if (sourceChild.isLink()) {
				childLinks.add(sourceChild);
			} else {
				String childTitle = sourceChild.getTitle().toLowerCase();
				MutableBookmarkContainer targetChild = childFoldersByTitle.get(childTitle);
				
				if (targetChild==null) {
					targetChild = new MutableBookmarkContainer(childTitle);
					childFoldersByTitle.put(childTitle, targetChild);
				} 
				targetChild.merge(sourceChild);
			}
		}
	}

	@Override
	public String toString() {
		return "MutableBookmarkContainer [title=" + title + ", hasChildren()=" + hasChildren() + "]";
	}

}
