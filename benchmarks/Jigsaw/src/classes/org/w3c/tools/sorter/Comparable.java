// Comparable.java
// $Id: Comparable.java,v 1.3 2000/08/16 21:37:56 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.sorter ;

/**
 * used for string comparison.
 */
public interface Comparable {

    public boolean greaterThan(Comparable comp);

    public String getStringValue();

}
