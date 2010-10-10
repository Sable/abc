// ResourceActionListener.java
// $Id: ResourceActionListener.java,v 1.5 2000/08/16 21:37:31 ylafon Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.jigadmin.events; 

/**
 * The ResourceAction listener class.
 * @version $Revision: 1.5 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public interface ResourceActionListener {

    /**
     * A resource action occured.
     * @param e the ResourceActionEvent
     */
    public void resourceActionPerformed(ResourceActionEvent e);

}
