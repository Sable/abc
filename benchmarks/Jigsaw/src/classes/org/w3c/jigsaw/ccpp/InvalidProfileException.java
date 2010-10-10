// InvalidProfileException.java
// $Id: InvalidProfileException.java,v 1.2 2000/08/16 21:37:34 ylafon Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ccpp;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class InvalidProfileException extends Exception {

    public InvalidProfileException(String ref) {
	super(ref);
    }

}
