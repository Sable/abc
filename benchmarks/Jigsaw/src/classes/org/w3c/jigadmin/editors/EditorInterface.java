// EditorInterface.java
// $Id: EditorInterface.java,v 1.4 2000/08/16 21:37:30 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors;

import java.util.Properties;

import java.awt.Component;

import org.w3c.jigadmin.RemoteResourceWrapper;

/**
 * @version $Revision: 1.4 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public interface EditorInterface {

    /**
     * Initialize this editor.
     * @param name the editor name
     * @param rrw the RemoteResourceWrapper wrapping the editor node.
     * @param p the editor properties
     */ 
    public void initialize(String name, 
			   RemoteResourceWrapper rrw, 
			   Properties p);

    /**
     * Get the Component.
     * @return a Component instance
     */
    public Component getComponent();

}
