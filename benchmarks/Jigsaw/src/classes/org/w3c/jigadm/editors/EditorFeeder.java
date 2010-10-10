// EditorFeeder.java
// $Id: EditorFeeder.java,v 1.3 2000/08/16 21:37:27 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import org.w3c.jigadm.RemoteResourceWrapper;

import java.util.Properties;

public interface EditorFeeder {

    /**
     * Compute the possible items for a StringArrayEditor.
     * @return The possible items for the selection.
     */

    public String[] getDefaultItems();

    public void initialize (RemoteResourceWrapper rrw, Properties p);
}
