// UnloadedGenerationException.java
// $Id: UnloadedGenerationException.java,v 1.2 2000/08/16 21:38:04 ylafon Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.protocol.http.cache;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class UnloadedGenerationException extends RuntimeException {

    public UnloadedGenerationException(String msg) {
	super(msg);
    }

}
