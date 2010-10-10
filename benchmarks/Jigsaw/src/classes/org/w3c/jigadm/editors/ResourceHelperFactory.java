// ResourceHelperFactory.java
// $Id: ResourceHelperFactory.java,v 1.8 2000/08/16 21:37:27 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import org.w3c.jigadm.PropertyManager;
import org.w3c.jigadm.RemoteResourceWrapper;

public class ResourceHelperFactory {

    public static synchronized
    ResourceHelper[] getHelpers(RemoteResourceWrapper rrw) {
	PropertyManager pm        = PropertyManager.getPropertyManager();
	String          classes[] = pm.getHelperClasses(rrw);
	ResourceHelper  helpers[] = null;

	if ( classes != null ) {
	    // Create the helpers (skip impedance mismatch):
	    helpers = new ResourceHelper[classes.length];
	    if ( helpers.length == 0 )
		return null;
	    for (int i = 0 ; i < classes.length ; i++) {
		try {
		    Class c    = Class.forName(classes[i]);
		    helpers[i] = (ResourceHelper) c.newInstance();
		} catch (Exception ex) {
		    // FIXME: should have an object to report that to...
		    System.out.println("cannot create helper: \""+
				       classes[i]+
				       " for \""+
				       rrw);
		}		
	    }
	}
	return helpers;
    }

}
