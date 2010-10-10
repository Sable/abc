// DAVRequest.java
// $Id: DAVRequest.java,v 1.1 2000/10/11 13:20:08 bmahe Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.protocol.webdav;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVRequest extends org.w3c.www.protocol.http.Request {
    
    protected DAVRequest(DAVManager manager) {
	super(manager);
    }
}
