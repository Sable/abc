// ServerShutdownHook.java
// $Id: ServerShutdownHook.java,v 1.4 2000/08/16 21:37:35 ylafon Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.daemon;

/**
 * The shutdown hook used to handle ctrl-c and such
 * NOTE, should work with jdk1.3
 */
class ServerShutdownHook extends Thread {
    private static final boolean debug = false;

    private ServerHandlerManager shm = null;

    /**
     * shut down everything then exit
     */
    public void run() {
	if (debug)
	    System.out.println("*** ShutdownHook, synching");
	shm.shutdown();
    }

    ServerShutdownHook(ServerHandlerManager shm) {
	super();
	this.shm = shm;
    }
}
