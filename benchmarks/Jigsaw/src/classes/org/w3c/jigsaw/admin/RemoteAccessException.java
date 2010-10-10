// RemoteAccessException.java
// $Id: RemoteAccessException.java,v 1.3 1998/01/22 13:48:53 bmahe Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.admin;

/**
 * The exception for network failure.
 */

public class RemoteAccessException extends Exception {

    public RemoteAccessException(String msg) {
	super(msg);
    }

}
