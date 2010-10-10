// HttpPassDirectory.java
// $Id: HttpPassDirectory.java,v 1.3 2000/08/16 21:37:44 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.resources ;

public class HttpPassDirectory extends PassDirectory {

    public void initialize(Object values[]) {
	super.initialize(values);
	try {
	    registerFrameIfNone("org.w3c.jigsaw.frames.HTTPFrame",
				"http-frame");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
