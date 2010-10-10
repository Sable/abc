// HttpFileResource.java
// $Id: HttpFileResource.java,v 1.4 2002/06/26 17:55:05 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.resources ;

import org.w3c.tools.resources.FileResource;
import org.w3c.tools.resources.FramedResource;

public class HttpFileResource extends FileResource {

    public void initialize(Object values[]) {
	super.initialize(values);
	try {
	    registerFrameIfNone("org.w3c.jigsaw.frames.HTTPFrame",
				"http-frame".intern());
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
