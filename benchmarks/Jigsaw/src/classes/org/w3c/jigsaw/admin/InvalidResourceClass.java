// InvalidResourceClass.java 
// $Id: InvalidResourceClass.java,v 1.3 1998/01/22 13:48:13 bmahe Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.admin;

/**
 * The exception for invalid classes.
 */

public class InvalidResourceClass extends Exception {

    public InvalidResourceClass(String msg) {
	super(msg);
    }

}
