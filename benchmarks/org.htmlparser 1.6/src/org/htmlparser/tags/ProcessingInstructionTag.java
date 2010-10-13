// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Somik Raha
//
// Revision Control Information
//
// $Source: /cvsroot/htmlparser/htmlparser/src/org/htmlparser/tags/ProcessingInstructionTag.java,v $
// $Author: derrickoswald $
// $Date: 2006/03/19 15:01:25 $
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

package org.htmlparser.tags;

import org.htmlparser.nodes.TagNode;

/**
 * The XML processing instructions like &lt;?xml ... ?&gt; can be identified by this class.
 */
public class ProcessingInstructionTag
    extends
        TagNode
{
    /**
     * The set of names handled by this tag.
     */
    private static final String[] mIds = new String[] {"?"};

    /**
     * Create a new processing instruction tag.
     */
    public ProcessingInstructionTag ()
    {
    }

    /**
     * Return the set of names handled by this tag.
     * @return The names to be matched that create tags of this type.
     */
    public String[] getIds ()
    {
        return (mIds);
    }

    /**
     * Returns a string representation of this processing instruction suitable for debugging.
     * @return A string representing this tag.
     */
    public String toString()
    {
        String guts = toHtml();
        guts = guts.substring (1, guts.length () - 2);
        return "Processing Instruction : "+guts+"; begins at : "+getStartPosition ()+"; ends at : "+getEndPosition ();
    }
}
