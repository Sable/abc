// SerializationException.java
// $Id: SerializationException.java,v 1.3 2000/08/16 21:37:54 ylafon Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.resources.serialization; 

/**
 * @version $Revision: 1.3 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class SerializationException extends RuntimeException {

    public SerializationException(String msg) {
	super(msg);
    }

}
