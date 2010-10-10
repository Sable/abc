// SpaceEntry.java
// $Id: SpaceEntry.java,v 1.1 1998/01/22 12:58:40 bmahe Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

public interface SpaceEntry {

  /**
   * Get the Key. This key must be unique and unchanged
   * during the all life.
   * @return an int.
   */
  public Integer getEntryKey();

}
