// AttributeChangedListener.java
// $Id: AttributeChangedListener.java,v 1.5 2000/08/16 21:37:53 ylafon Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.event;

import java.util.EventListener;

public interface AttributeChangedListener extends EventListener {

  /**
   * Gets called when a property changes.
   * @param evt The AttributeChangeEvent describing the change.
   */

  public void attributeChanged(AttributeChangedEvent evt);

}
