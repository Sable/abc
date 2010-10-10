// InvalidParentException.java
// $Id: InvalidParentException.java,v 1.2 2000/08/16 21:37:52 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class InvalidParentException extends RuntimeException {

    public InvalidParentException(String msg) {
	super(msg);
    }

}
