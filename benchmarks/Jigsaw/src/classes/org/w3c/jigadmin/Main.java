// Main.java
// $Id: Main.java,v 1.1 1999/01/26 15:23:09 bmahe Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin;

/**
 * a place holder for running the administration tool
 */

public class Main {

    public static void main(String[] args) {
	if(args.length == 0) {
	    String[] arg = {"http://localhost:8009/"};
	    org.w3c.jigadmin.gui.ServerBrowser.main(arg);
	} else {
	    org.w3c.jigadmin.gui.ServerBrowser.main(args);
	}
    }
}
