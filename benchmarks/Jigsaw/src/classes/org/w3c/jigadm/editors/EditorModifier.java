// EditorModifier.java
// $Id: EditorModifier.java,v 1.2 2000/08/16 21:37:27 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

public interface EditorModifier {

    /**
     * Modify the selected item
     * @return The modifier item
     */

    public String modify(String item);

}
