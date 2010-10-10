// ServerHelperInterface.java
// $Id: ServerHelperInterface.java,v 1.5 2000/08/16 21:37:31 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors;

/**
 * The interface for server helpers.
 * @version $Revision: 1.5 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public interface ServerHelperInterface extends EditorInterface {

    /**
     * The tooltip property name
     */
    public final static String TOOLTIP_P = "tooltip";

   
    /**
     * Get the helper name.
     * @return a String instance
     */
    public String getName();

    /**
     * Get the helper tooltip
     * @return a String
     */    
    public String getToolTip();

}
