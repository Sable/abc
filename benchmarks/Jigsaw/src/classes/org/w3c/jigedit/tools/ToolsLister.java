// ToolsLister.java
// $Id: ToolsLister.java,v 1.2 2000/08/16 21:37:33 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigedit.tools;

import org.w3c.tools.resources.FramedResource;

public class ToolsLister extends FramedResource {

    public void initialize(Object values[]) {
	super.initialize(values);
	try {
	    registerFrameIfNone("org.w3c.jigedit.tools.ToolsListerFrame",
				"toolslister-frame");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
