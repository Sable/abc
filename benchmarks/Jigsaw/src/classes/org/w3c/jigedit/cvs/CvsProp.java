// CvsProp.java
// $Id: CvsProp.java,v 1.2 2000/08/16 21:37:32 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigedit.cvs ;

import org.w3c.jigsaw.http.httpd;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.jigsaw.config.PropertySet;

import org.w3c.cvs.CvsDirectory;

import org.w3c.www.protocol.http.HttpManager;

class CvsProp extends PropertySet {
  private static String title = "Cvs properties";

  static {
    Class c = null;
    Attribute a = null;

    try {
      c = Class.forName("org.w3c.jigedit.cvs.CvsProp");
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
    // Register the path of CVS binary
    a = new StringAttribute(CvsDirectory.CVSPATH_P,
			    null,
			    Attribute.EDITABLE);
    AttributeRegistry.registerAttribute(c, a);
    // register the path of the CVS Repository
    a = new StringAttribute(CvsDirectory.CVSROOT_P,
			    null,
			    Attribute.EDITABLE);
    AttributeRegistry.registerAttribute(c, a);
    // register the path of the cvs wrapper (the shell script that
    // runs Cvs in the right directory
    a = new StringAttribute(CvsDirectory.CVSWRAP_P,
			    null,
			    Attribute.EDITABLE);
    AttributeRegistry.registerAttribute(c, a);
  }

  /**
   * Get this property set title.
   * @return A String encoded title.
   */

  public String getTitle() {
    return title;
  }

  CvsProp(String name, httpd server) {
    super(name, server);
  }

}
