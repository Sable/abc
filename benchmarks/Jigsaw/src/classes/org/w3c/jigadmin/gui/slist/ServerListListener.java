// ServerListModelListener.java
// $Id: ServerListListener.java,v 1.3 2000/08/16 21:37:31 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.gui.slist;

import java.util.EventListener;

import org.w3c.jigadmin.RemoteResourceWrapper;

/**
 * The interface for ServerList listeners
 * @version $Revision: 1.3 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public interface ServerListListener extends EventListener {

    /**
     * A server has been selected.
     * @param name the server name.
     * @param rrw the server RemoteResourceWrapper
     */
    public void serverSelected(String name, RemoteResourceWrapper rrw);

}
