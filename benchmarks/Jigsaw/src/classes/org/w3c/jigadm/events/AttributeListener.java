// AttributeListener.java
// $Id: AttributeListener.java,v 1.4 2000/08/16 21:37:28 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.events ;

import java.util.EventListener;

public interface AttributeListener extends EventListener {

    /**
     * Invoked when the value of the Attribute has changed
     */

    public void attributeChanged(AttributeChangeEvent e);
}
