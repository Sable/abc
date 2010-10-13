// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2003 Derrick Oswald
//
// Revision Control Information
//
// $Source: /cvsroot/htmlparser/htmlparser/src/org/htmlparser/filters/XorFilter.java,v $
// $Author: ian_macfarlane $
// $Date: 2006/05/16 09:11:41 $
// $Revision: 1.1 $
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//

package org.htmlparser.filters;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;

/**
 * Accepts nodes matching an odd number of its predicates filters (XOR operation).
 * For example, where it has two filters, it accepts only if and only if one of the two filters accepts the Node, but does not accept if both filters accept the Node. 
 */
public class XorFilter implements NodeFilter
{
    /**
     * The predicates that are to be xor'ed together;
     */
    protected NodeFilter[] mPredicates;

    /**
     * Creates a new instance of an XorFilter.
     * With no predicates, this would always answer <code>false</code>
     * to {@link #accept}.
     * @see #setPredicates
     */
    public XorFilter ()
    {
        setPredicates (null);
    }

    /**
     * Creates an XorFilter that accepts nodes acceptable to either filter, but not both.
     * @param left One filter.
     * @param right The other filter.
     */
    public XorFilter (NodeFilter left, NodeFilter right)
    {
        NodeFilter[] predicates;

        predicates = new NodeFilter[2];
        predicates[0] = left;
        predicates[1] = right;
        setPredicates (predicates);
    }
    
    /**
     * Creates an XorFilter that accepts nodes acceptable an odd number of the given filters.
     * @param predicates The list of filters. 
     */
    public XorFilter (NodeFilter[] predicates)
    {
        setPredicates (predicates);
    }

    /**
     * Get the predicates used by this XorFilter.
     * @return The predicates currently in use.
     */
    public NodeFilter[] getPredicates ()
    {
        return (mPredicates);
    }

    /**
     * Set the predicates for this XorFilter.
     * @param predicates The list of predidcates to use in {@link #accept}.
     */
    public void setPredicates (NodeFilter[] predicates)
    {
        if (null == predicates)
            predicates = new NodeFilter[0];
        mPredicates = predicates;
    }

    //
    // NodeFilter interface
    //

    /**
     * Accept nodes that are acceptable to an odd number of its predicate filters.
     * @param node The node to check.
     * @return <code>true</code> if an odd number of the predicate filters find the node
     * is acceptable, <code>false</code> otherwise.
     */
    public boolean accept (Node node)
    {
        int countTrue;

        countTrue = 0;

        for (int i = 0; i < mPredicates.length; i++)
            if (mPredicates[i].accept (node))
                ++countTrue;

        return ((countTrue % 2) == 1);
    }
}
