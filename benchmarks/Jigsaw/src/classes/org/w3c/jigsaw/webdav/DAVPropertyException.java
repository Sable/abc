// DAVPropertyException.java
// $Id: DAVPropertyException.java,v 1.1 2000/10/05 16:18:53 bmahe Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.jigsaw.webdav;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVPropertyException extends Exception {

    private Object reason = null;

    public Object getReason() {
	return reason;
    }

    public DAVPropertyException(String msg, Object reason) {
	super(msg);
	this.reason = reason;
    }
    
}
