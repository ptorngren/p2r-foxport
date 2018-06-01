/*
Copyright (c) 2014, Peer Törngren
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the p2r-foxport project nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package se.p2r.foxport.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListValuedMap;

import se.p2r.foxport.Bookmark;
import se.p2r.foxport.BookmarkReader;
import se.p2r.foxport.html.HTMLFileWriter;
import se.p2r.foxport.html.HTMLListGenerator;
import se.p2r.foxport.html.HTMLTreeGenerator;
import se.p2r.foxport.internal.exceptions.ConfigurationException;
import se.p2r.foxport.util.DeepBookmarkSelector;
import se.p2r.foxport.util.Log;
import se.p2r.foxport.util.Utils;
import se.p2r.foxport.util.Utils.BrowserType;

/**
 * <p>
 * Main entry for processing bookmarks. Bookmarks to export is defined by bookmark description. Depends on a reader to read bookmarks
 * and a writer to write HTML.
 * </p>
 * 
 * @author peer
 *
 * @see Bookmark#getDescription()
 */
public class BookmarkProcessor {
	
	protected final File targetFolder;
	protected final BrowserType browserType;
	protected final long timestamp;
	
	private final boolean generateTree;
	private final boolean isForceExport;
	private final LinkTester linkTester;

	public BookmarkProcessor(BrowserType browserType, File targetFolder, boolean isTree, boolean isForceExport, long timestamp, LinkTester linkTester) throws ConfigurationException {
		this.browserType = browserType;
		this.targetFolder = targetFolder;
		this.generateTree = isTree;
		this.isForceExport = isForceExport;
		this.timestamp = timestamp;
		this.linkTester = linkTester;
		if (!targetFolder.isDirectory()) {
			throw new ConfigurationException(new FileNotFoundException("Output folder does not exist: " + targetFolder));
		}
	}

	public List<File> process() throws IOException {
		BookmarkReader reader = BookmarkReader.Factory.makeReader(browserType);
		if (needsUpdate(reader)) {
			Bookmark bookmarksRoot = reader.load();
	
			// first select tagged root containers, then recursively collect tagged containers in these roots
			List<Bookmark> rootsToExport = selectChildrenToExport(bookmarksRoot);
			ListValuedMap<String, Bookmark> allContainersToExport = new DeepBookmarkSelector().select(rootsToExport);
			if (!allContainersToExport.isEmpty()) {
				return export(allContainersToExport);
			}
		}
		return Collections.EMPTY_LIST;
	}

	protected boolean needsUpdate(BookmarkReader reader) {
		if (isForceExport || reader.getTimestamp()>timestamp) {
			return true;
		}
		Log.log(String.format("Skipping export - bookmarks have not changed since last run. Bookmarks: %s - Last run: %s", Utils.formatTimeISO(reader.getTimestamp()), Utils.formatTimeISO(timestamp)));
		return false;
	}

	private List<File> export(ListValuedMap<String, Bookmark> containers) {
		List<File> files = new ArrayList();

		// process each selected folder by id
		for (String id : containers.keySet()) {
			List<Bookmark> containersWithSameID = containers.get(id);
			assert !containersWithSameID.isEmpty() : "No containers for id: " + id;
			
			// process each id (multiple containers may exist)
			Bookmark container = containersWithSameID.size() == 1 ? containersWithSameID.iterator().next() : merge(id, containersWithSameID);
			File exportedFile = processContainer(container);
			files.add(exportedFile);
		}
	
		return files;
	}

	private Bookmark merge(String id, List<Bookmark> containers) {
		MutableBookmarkContainer result = new MutableBookmarkContainer(id);
		for (Bookmark c : containers) {
			assert c.getExportId().equalsIgnoreCase(id) : "Not the same id: " + result + ", " + c;
			result.setTitle(c.getTitle());
			result.setDescription(c.getDescription());
			result.mergeChildren(c);
		}
		return result;
	}

	private File processContainer(Bookmark root) {
		String title = root.getTitle();
		String description = root.getDescription();
		String id = root.getExportId();
		
		return generate(root, title, description, id);
	}

	protected File generate(Bookmark root, String title, String description, String id) {
		Log.log("Processing root folder: " + id);
		String html = generateTree 
				? new HTMLTreeGenerator(root, title, description, linkTester).run()
				: new HTMLListGenerator(root, title, description, linkTester).run();
				
		return new HTMLFileWriter(targetFolder, id).writeFile(html, root);
	}

	private List<Bookmark> selectChildrenToExport(Bookmark bookmarksRoot) {
		List<? extends Bookmark> roots = bookmarksRoot.getChildren();
		List<Bookmark> result = roots.stream().filter(p -> p.isTaggedForExport()).collect(Collectors.toList());
		if (result.isEmpty()) {
			Log.warn("No folder is tagged for export: "+Utils.toNames(roots));
//			Expected format is #<exportId>#<title>;<description> (all elements are optional, only initial # is required)");
		}
		return result;
	}

}
