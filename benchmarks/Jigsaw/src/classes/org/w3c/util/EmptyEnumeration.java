// EmptyEnumeration.java
// $Id: EmptyEnumeration.java,v 1.3 2000/08/16 21:37:58 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * An empty enumeration.
 */

public class EmptyEnumeration implements Enumeration {

    public final boolean hasMoreElements() {
	return false;
    }

    public final Object nextElement() {
	throw new NoSuchElementException("empty enumeration");
    }

}
