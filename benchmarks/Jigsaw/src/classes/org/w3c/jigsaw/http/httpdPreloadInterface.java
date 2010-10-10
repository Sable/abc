// httpdPreloadInterface.java
// $Id: httpdPreloadInterface.java,v 1.3 2000/08/16 21:37:42 ylafon Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http;

/**
 * @version $Revision: 1.3 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public interface httpdPreloadInterface {

    /**
     * Perform some actions on the server just before startup
     * @param server the http server
     */
    public void preload(httpd server);

}
