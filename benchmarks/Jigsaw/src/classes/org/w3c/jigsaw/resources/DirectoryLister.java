// DirectoryLister.java
// $Id: DirectoryLister.java,v 1.7 2000/08/16 21:37:44 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.resources ;

import org.w3c.tools.resources.FramedResource;

public class DirectoryLister extends FramedResource {

 
  public void initialize(Object values[]) {
    super.initialize(values);
    try {
      registerFrameIfNone("org.w3c.jigsaw.resources.DirectoryListerFrame",
			  "lister-frame");
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

}
