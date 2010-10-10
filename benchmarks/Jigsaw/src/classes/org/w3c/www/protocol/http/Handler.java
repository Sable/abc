// Handler.java
// $Id: Handler.java,v 1.4 2000/08/16 21:38:02 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http ;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import java.io.IOException;

public class Handler extends URLStreamHandler {

    protected URLConnection openConnection (URL u)
	throws IOException 
    {
	return new HttpURLConnection(u);
    }

}
