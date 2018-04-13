/*
Copyright (c) 2014, Peer Törngren
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the p2r-foxport project nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package se.p2r.foxport.html;

import static se.p2r.foxport.util.Utils.*;

import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.Body;
import com.hp.gagawa.java.elements.Dl;
import com.hp.gagawa.java.elements.Dt;
import com.hp.gagawa.java.elements.H1;
import com.hp.gagawa.java.elements.H3;
import com.hp.gagawa.java.elements.Head;
import com.hp.gagawa.java.elements.Html;
import com.hp.gagawa.java.elements.Meta;
import com.hp.gagawa.java.elements.P;
import com.hp.gagawa.java.elements.Text;
import com.hp.gagawa.java.elements.Title;

import se.p2r.foxport.Bookmark;

/**
 * First version of generator, generates a plain list (no folding). 
 * Uses the <a href=https://github.com/dutoitns/gagawa>gagawa</a> HTML Generator.
 * 
 * @author peer
 * @see HTMLTreeGenerator
 *
 */
public class HTMLListGenerator {

	private final Bookmark root;
	private final String characterEncoding;

	private int uriConter = 0;
	private int containerCounter = 0;
	private final String name;
	private final String description;
	private int containerDepth = 0;


	public HTMLListGenerator(Bookmark root2, String name, String description, String characterEncoding) {
		this.root = root2;
		this.characterEncoding = characterEncoding;
		this.name = name;
		this.description = description;
	}

	public String run() {
		assert root.hasChildren() : root.getTitle() + ": no children; should have been checked by caller!";
		Html html = new Html();
		Dl dl = begin(html, root);
		for (Bookmark child : root.getChildren()) {
			append(dl, child);
		}
		log("Generated " + uriConter + " links in " + containerCounter + " containers");
		return html.write();
	}

	private Dl begin(Html html, Bookmark root2) {
//		<html><head>
//		<meta http-equiv="content-type" content="text/html; charset=windows-1252"><title>Peer's Off Duty Links</title>
//		</head><body><h1>Peer's Off Duty Links</h1>
        
		Head head = new Head();
        Meta meta = new Meta("text/html"); // mandatory to pass in constructor?
//      meta.setContent("text/html"); // 
        meta.setHttpEquiv("content-type");
        meta.setAttribute("charset", characterEncoding);
        head.appendChild(meta);
        html.appendChild(head);
       
        Title title = new Title();
		title.appendChild(new Text(name));
        head.appendChild(title);
       
        Body body = new Body();
        html.appendChild(body);
        H1 h1 = new H1();
	    h1.appendText(name);
	    body.appendChild(h1);
    
        P p = new P();
        p.appendText(description);
        body.appendChild(p);
        
        Dl dl = new Dl();
        appendDescription(dl, root2);  // Doesn't work? No description saved in JSON?
        body.appendChild(dl);
        
        return dl;
	}

	private void append(Dl dl, Bookmark bm) {
		if (bm.isLink()) {
			appendLink(dl, bm);
		} else if (bm.isContainer()) {
			if (bm.hasChildren()) {
				containerDepth++;
				containerCounter++;
				appendContainer(dl, bm);
				for (Bookmark child : bm.getChildren()) {
					append(dl, child);
				}
				containerDepth--;
			} else {
				log("Skipping empty folder: " + bm.getTitle());
			}
		} else {
			throw new IllegalArgumentException("Unexpected type: " + bm);
		}
	}

	private void appendDescription(Dl dl, Bookmark bm) {
		String description = bm.getDescription();
		if (description!=null && description.trim().length()>0) {
			P p = new P();
			p.appendText(description);
			dl.appendChild(p);
			debug(p.write());
		}
	}

	private void appendLink(Dl dl, Bookmark bm) {
//		<dt><a href="http://www.p2r.se/links">(p2r links)</a></dt>
		uriConter++;
		Dt dt = new Dt();
		A a = new A();
		
		a.appendText(bm.getTitle());
		a.setHref(bm.getUri());
		
		dt.appendChild(a);
		dl.appendChild(dt);
		appendDescription(dl, bm);
		debug(dt.write());
	}

	private Dl appendContainer(Dl dlIn, Bookmark bm) {
//		<dt><h3 folded="">Household (economy, home improvement, etc)</h3></dt>
		Text title = new Text(bm.getTitle());

		Dt dt = new Dt();
		H3 h3 = new H3();
		debug(bm.getTitle()+", depth="+containerDepth);
//		h3.setAttribute("folded", "");
        
		h3.appendChild(title);
		dt.appendChild(h3);
        dlIn.appendChild(dt);
        
		appendDescription(dlIn, bm);

		debug(dt.write());
		
		Dl dlOut = new Dl();
		dlIn.appendChild(dlOut);
		return dlOut;
	}

	public Bookmark getRoot() {
		return root;
	}

	public int getUriConter() {
		return uriConter;
	}

	public int getContainerCounter() {
		return containerCounter;
	}

}
