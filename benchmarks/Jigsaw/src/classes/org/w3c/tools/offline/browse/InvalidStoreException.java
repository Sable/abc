// InvalidStore.java
// $Id: InvalidStoreException.java,v 1.2 2002/02/04 17:28:12 cbournez Exp $
// (c) COPYRIGHT MIT and INRIA, 2002.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.offline.browse;

/**
 * The store is not valid (missing repository file...) .
 */
public class InvalidStoreException extends Exception {

    public InvalidStoreException( String msg) {
		super("reading store failed: "+msg);
    }

}


