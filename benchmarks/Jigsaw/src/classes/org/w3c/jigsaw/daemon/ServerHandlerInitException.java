// ServerHandlerInitException.java
// $Id: ServerHandlerInitException.java,v 1.3 2000/08/16 21:37:35 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.daemon;

public class ServerHandlerInitException extends Exception {

    public ServerHandlerInitException(String msg) {
	super(msg);
    }
}
