// CvsException.java
// $Id: CvsException.java,v 1.3 2000/08/16 21:37:26 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.cvs ;

/**
 * This exception is used whenever an abnormal situation in CVS processing
 * is encountered.
 */

public class CvsException extends Exception {

    CvsException (String msg) {
	super (msg) ;
    }
}

   
	
