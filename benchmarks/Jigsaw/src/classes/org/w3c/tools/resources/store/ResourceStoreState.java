// ResourceStoreState.java
// $Id: ResourceStoreState.java,v 1.3 2000/08/16 21:37:55 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.store ;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.IntegerAttribute;

public class ResourceStoreState extends AttributeHolder {
    protected static int ATTR_RSKEY = -1;

    static {
	Attribute a = null;
	Class     c = null;
	try {
           c = Class.forName(
			  "org.w3c.tools.resources.store.ResourceStoreState");
	} catch (Error er) {
	    er.printStackTrace();
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// The rskey attribute:
	a = new IntegerAttribute("rskey",
				 new Integer(0),
				 Attribute.COMPUTED);
	ATTR_RSKEY = AttributeRegistry.registerAttribute(c, a);
    }

    public synchronized int getNextKey() {
	int rskey = getInt(ATTR_RSKEY, 0);
	setInt(ATTR_RSKEY, rskey+1);
	return rskey;
    }

    public synchronized int getCurrentKey() {
	return getInt(ATTR_RSKEY, 0);
    }
	
    public ResourceStoreState() {
	this(0);
    }

    public ResourceStoreState(int id) {
	super();
	setValue(ATTR_RSKEY, new Integer(id));
    }
}


