// NotAStoreException.java
// $Id: NotAStoreException.java,v 1.1 1998/01/22 13:01:40 bmahe Exp $
// (c) COPYRIGHT MIT and INRIA, 1996-1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.store;

/**
 * This is exception gets thrown if an invalid resource store is detected.
 */

public class NotAStoreException extends Exception {

    public NotAStoreException(String msg) {
	super(msg);
    }

}
