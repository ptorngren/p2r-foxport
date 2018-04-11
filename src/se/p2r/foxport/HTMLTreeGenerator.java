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

import static j2html.TagCreator.*;
import static j2html.attributes.Attr.*;
import static se.p2r.foxport.BookmarkExporter.log;

import java.util.ArrayList;
import java.util.List;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.Tag;
import j2html.tags.Text;

/**
 * Newer version of HTML generator. Uses the <a
 * href=https://github.com/tipsy/j2html>j2html</a> tag generator that supports
 * HTML 5, in particular the <code>&lt;detail&gt;</code> and
 * <code>&lt;summary&gt;</code> tags.
 * 
 * @author peer
 * @see HTMLListGenerator
 */
public class HTMLTreeGenerator {

	private static final String LINKBULLET = "▻ "; // HTML "&#9659;"
	private final FirefoxBookmark root;
	private final String characterEncoding;

	private int uriConter = 0;
	private int containerCounter = 0;
	private final String name;
	private int containerDepth = 0;
	private String description;


	public HTMLTreeGenerator(FirefoxBookmark root, String name, String description, String characterEncoding) {
		this.root = root;
		this.characterEncoding = characterEncoding;
		this.name = name;
		this.description = description;
	}

	public String run() {
		assert root.hasChildren() : root.getTitle() + ": no children; should have been checked by caller!";
		ContainerTag html = html(newHead(), newBody(root.getChildren()));
		log("Generated " + uriConter + " links in " + containerCounter + " containers");
		return html.render();
	}

	private ContainerTag newHead() {
		Tag meta = meta(); 
        meta.attr(CHARSET, characterEncoding);
        Tag title = title(name);
        Tag style = style(
        		newStyleElement("html", "font-family: Arial, Helvetica, sans-serif"),
        		newStyleElement("div", "margin-left: 1em"),
        		newStyleElement("details", "color: DarkSlateGrey","font-size: small", "margin-left: 1em"),
        		newStyleElement("summary", "color: black","font-size: medium"),
        		newStyleElement("a", "color: DodgerBlue","font-size: small")
        		);
        style.attr(TYPE, "text/css");
               
        return head(meta, title, style);
	}

	private DomContent newStyleElement(String key, String... values) {
		// TODO tag toolkit probably supports a better way to do this?
		String valueList = "";
		for (String value : values) {
			valueList+=value;
			valueList+=";";
		}
		String text = String.format("%s {%s}", key, valueList);
		return new Text(text );
	}

	private DomContent newBody(List<FirefoxBookmark> children) {
		return body()
				.with(h1(name))
				.with(rawHtml(description))
				.with(hr())
				.with(newContents(children));
	}

	private Iterable<DomContent> newContents(List<FirefoxBookmark> children) {
		List<DomContent> result = new ArrayList();
		for (FirefoxBookmark bm: children) {
			if (bm.isLink()) {
				result.add(newLink(bm));
			} else if (bm.hasChildren()) {
				result.add(newContainer(bm));
			} else {
				log("Skipping empty folder: " + bm.getTitle());
			}
		}
		return result;
	}

	private ContainerTag newLink(FirefoxBookmark bm) {
		ContainerTag link = a(bm.getTitle()).attr("href", bm.getUri());
		return div(LINKBULLET).with(link);
	}

	private DomContent newContainer(FirefoxBookmark bm) {
		containerDepth++;
		DomContent summary = containerDepth<2 ? summary(strong(bm.getTitle())) : summary(bm.getTitle());
		ContainerTag result = details().with(summary);
		if (!empty(bm.getDescription())) {
			ContainerTag description = div(bm.getDescription());
			result.with(description);
		}
		result.with(newContents(bm.getChildren()));
		containerDepth--;
		return result;
	}

	private static boolean empty(String s) {
		return s==null || s.trim().isEmpty();
	}

}