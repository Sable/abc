// HttpDirectoryResource.java
// $Id: HttpDirectoryResource.java,v 1.2 2000/08/16 21:37:44 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.resources ;

public class HttpDirectoryResource extends DirectoryResource {

  public void initialize(Object values[]) {
    super.initialize(values);
    try {
      registerFrameIfNone("org.w3c.jigsaw.frames.HTTPFrame",
			  "http-frame");
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

}
