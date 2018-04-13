package se.p2r.foxport;

import java.util.List;

public interface Bookmark {

	List<? extends Bookmark> getChildren();

	String getTitle();

	boolean isLink();

	boolean hasChildren();

	String getUri();

	String getDescription();

	boolean isContainer();

}
