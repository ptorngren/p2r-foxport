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
import java.text.SimpleDateFormat;
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
	protected final File timestampFile;
	
	private final boolean generateTree;
	private final long timestamp;
	private final boolean isForceExport;
	private final LinkTester linkTester;

	public BookmarkProcessor(BrowserType browserType, File targetFolder, boolean isTree, boolean isForceExport, LinkTester linkTester) throws ConfigurationException {
		this.browserType = browserType;
		this.targetFolder = targetFolder;
		this.generateTree = isTree;
		this.timestampFile = new File(targetFolder, getClass().getName()+".timestamp");
		this.timestamp = timestampFile==null ? 0 : timestampFile.lastModified();
		this.isForceExport = isForceExport;
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
			List<Bookmark> rootContainers = select(bookmarksRoot.getChildren());
			ListValuedMap<String, Bookmark> selectedContainers = new DeepBookmarkSelector().select(rootContainers);
	
			timestamp();
			return export(selectedContainers);
		}
		return Collections.EMPTY_LIST;
	}

	protected boolean needsUpdate(BookmarkReader reader) {
		if (isForceExport || reader.getTimestamp()>timestamp) {
			return true;
		}
		Log.log("Skipping export - bookmarks have not changed since last run: "+new SimpleDateFormat().format(Long.valueOf(timestamp)));
		return false;
	}

	protected void timestamp() throws IOException {
		timestampFile.createNewFile();  // does nothing if file already exists
		timestampFile.setLastModified(System.currentTimeMillis());
	}

	private List<File> export(ListValuedMap<String, Bookmark> selectedContainers) {
		List<File> files = new ArrayList();

		// process each selected folder by id
		for (String id : selectedContainers.keySet()) {
			List<Bookmark> containersWithSameID = selectedContainers.get(id);
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
		String name = root.getTitle();
		String description = root.getDescription();
		String id = root.getExportId();
		
		return generate(root, name, description, id);
	}

	protected File generate(Bookmark root, String name, String description, String id) {
		Log.debug("Processing root folder: " + id);
		String html = generateTree 
				? new HTMLTreeGenerator(root, name, description, linkTester).run()
				: new HTMLListGenerator(root, name, description, linkTester).run();
				
		return new HTMLFileWriter(targetFolder, id).writeFile(html, root);
	}

	private List<Bookmark> select(List<? extends Bookmark> prospects) {
		return prospects.stream().filter(p -> p.isTaggedForExport()).collect(Collectors.toList());
	}

}
