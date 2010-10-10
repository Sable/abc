// UnknownResourceException.java
// $Id: UnknownResourceException.java,v 1.2 2000/08/16 21:37:55 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.store ;

import org.w3c.tools.resources.Resource;

public class UnknownResourceException extends RuntimeException {

    public UnknownResourceException (Resource resource) {
	super("ResourceStore mismatch for resource "+resource.getIdentifier());
    }

}
