// DuplicateNameException.java
// $Id: DuplicateNameException.java,v 1.2 2000/08/16 21:37:52 ylafon Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.resources;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DuplicateNameException extends RuntimeException {

    public DuplicateNameException(String name) {
	super(name);
    }

    public String getName() {
	return getMessage();
    }

}
