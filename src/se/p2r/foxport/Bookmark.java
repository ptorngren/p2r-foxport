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

}
