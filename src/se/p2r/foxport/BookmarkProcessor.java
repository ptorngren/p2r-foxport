/*
Copyright (c) 2014, Peer Törngren
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the p2r-foxport project nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package se.p2r.foxport;

import static se.p2r.foxport.util.Utils.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListValuedMap;

import se.p2r.foxport.firefox.FirefoxReader;
import se.p2r.foxport.html.HTMLFileWriter;
import se.p2r.foxport.html.HTMLListGenerator;
import se.p2r.foxport.html.HTMLTreeGenerator;
import se.p2r.foxport.internal.ConfigurationException;
import se.p2r.foxport.util.DeepBookmarkSelector;
import se.p2r.foxport.util.MutableBookmarkContainer;

/**
 * <p>
 * Main entry for processing bookmarks. Depends on a reader to read bookmarks
 * and a writer to write HTML.
 * </p>
 * 
 * @author peer
 *
 * TODO redesign the way to identify subfolders, read comments from browser instead. 
 * BUT: Firefox does not export descriptions. Current design is very brittle to do name 
 * matching + we cannot handle names with spaces + we want a simple file name (not matching the folder name) 
 * Also, we want to read Chrome bookmarks anyway.
 *         
 * @see #mapNames(Properties)
 * @see #getDescription(Properties, String)
 */
public class BookmarkProcessor {

	// TODO should read from config
	private static final boolean GENERATE_TREE = true;

	private final File targetFolder;

	private int fileCounter;

	public BookmarkProcessor(File targetFolder) throws IOException, ConfigurationException {
		this.targetFolder = targetFolder;
		if (!targetFolder.isDirectory()) {
			throw new ConfigurationException(
					new FileNotFoundException("Output folder does not exist: " + targetFolder));
		}
	}

	public void process(Properties config) throws IOException {
		fileCounter = 0;

		// TODO load reader based on config (Firefox or Chrome)
		Bookmark bookmarksRoot = new FirefoxReader().load();
		Map<String, String> mappings = mapNames(config);

		// first select root containers mentioned in config (avoid trash, tmp, private,
		// etc)
		// then recursively collect folders in these roots
		List<Bookmark> rootContainers = select(bookmarksRoot.getChildren(), mappings);
		ListValuedMap<String, Bookmark> selectedContainers = new DeepBookmarkSelector(mappings.keySet()).select(rootContainers);

		List<File> htmlFiles = export(config, selectedContainers, mappings);
		publish(htmlFiles);
	}

	// FIXME http://www.codejava.net/java-se/networking/ftp/java-ftp-file-upload-tutorial-and-example
	private void publish(List<File> htmlFiles) {
//		log("</RUN> Published " + fileCounter + " files");
	}

	private List<File> export(Properties config, ListValuedMap<String, Bookmark> selectedContainers, Map<String, String> mappings) {
		List<File> files = new ArrayList();
		// process each selected folder
		for (String folderName : selectedContainers.keySet()) {
			String id = mappings.get(folderName);
			String[] description = config.getProperty(id, "").split(";");
			List<Bookmark> containers = selectedContainers.get(folderName);
			assert !containers.isEmpty() : "No containers for title: " + folderName;
			Bookmark root = containers.size() == 1 ? containers.iterator().next() : merge(folderName, containers);
			files.add(processContainer(id, root, description));
		}

		log("</RUN> Wrote " + fileCounter + " files");
		return files;
	}

	// TODO kludge - rethink a better way to handle names
	// map names to folders bidirectional. If not mapped, entry has same key and
	// value.
	private Map<String, String> mapNames(Properties config) {
		Map<String, String> result = new HashMap();
		for (Entry<Object, Object> each : config.entrySet()) {
			String key = (String) each.getKey();
			boolean map = key.startsWith("map.");
			if (map) {
				String id = key.substring(4).toLowerCase();
				String folder = ((String) each.getValue()).toLowerCase();
				result.put(id, folder);
				result.put(folder, id);
			} else {
				result.put(key, key);
			}
		}
		return result;
	}

	private Bookmark merge(String folderName, List<Bookmark> containers) {
		MutableBookmarkContainer result = new MutableBookmarkContainer(folderName);
		for (Bookmark c : containers) {
			assert c.getTitle().equalsIgnoreCase(folderName) : "Not the same title: " + result + ", " + c;
			result.merge(c);
		}
		return result;
	}

	private File processContainer(String id, Bookmark root, String... nameAndDescription) {
		fileCounter++;
		log("Processing root folder #" + fileCounter + ":" + root.getTitle());

		String name = root.getTitle();
		String description = "";
		if (nameAndDescription.length > 0) {
			name = nameAndDescription[0];
			description = nameAndDescription.length > 1 ? nameAndDescription[1] : "";
		}
		String html = GENERATE_TREE ? new HTMLTreeGenerator(root, name, description).run()
				: new HTMLListGenerator(root, name, description).run();
		return new HTMLFileWriter(targetFolder, id).writeFile(html, root);
	}

	private List<Bookmark> select(List<? extends Bookmark> prospects, Map<String, String> mappings) {
		Collection<String> folderNames = mappings.values();
		return prospects.stream().filter(p -> folderNames.contains(p.getTitle().toLowerCase()))
				.collect(Collectors.toList());
	}

}
