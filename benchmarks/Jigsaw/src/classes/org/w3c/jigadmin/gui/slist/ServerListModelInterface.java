// ServerListModelInterface.java
// $Id: ServerListModelInterface.java,v 1.7 2000/08/16 21:37:31 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.gui.slist;

import org.w3c.jigadmin.RemoteResourceWrapper;

import org.w3c.jigsaw.admin.RemoteAccessException;

/**
 * Interface for ServerList model.
 * @version $Revision: 1.7 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public interface ServerListModelInterface {

    /**
     * The nam eof the admin server.
     */
    public static final String ADMIN_SERVER_NAME = "Admin";

    /**
     * Returns a array of the server names.
     * @return an array of String
     */
    public String[] getServers();

    /**
     * Get the server with the given name.
     * @param name the server name
     * @return The RemoteResourceWrapper of the server.
     */
    public RemoteResourceWrapper getServer(String name);

}
