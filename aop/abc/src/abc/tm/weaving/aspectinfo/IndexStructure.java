/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Julian Tibble
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.tm.weaving.aspectinfo;

import java.util.*;
import soot.*;

public class IndexStructure
{
    private ArrayList names;
    private int[] kinds;
    private int collectable_until;
    private int primitive_until;
    private int weak_until;
 
    public IndexStructure()
    {
        names = new ArrayList();
        kinds = new int[0];
    }

    public IndexStructure(Set coll, Set prim, Set weak, Set strong)
    {
        int height = coll.size() + prim.size() + weak.size() + strong.size();
        names = new ArrayList(height);
        kinds = new int[height];

        int start, end;
        start = 0;
        end = coll.size();
        names.addAll(coll);
        Arrays.fill(kinds, start, end, IndexingScheme.COLL_MAP);
        collectable_until = end;

        start = end;
        end += prim.size();
        names.addAll(prim);
        Arrays.fill(kinds, start, end, IndexingScheme.PRIM_MAP);
        primitive_until = end;

        start = end;
        end += weak.size();
        names.addAll(weak);
        Arrays.fill(kinds, start, end, IndexingScheme.WEAK_MAP);
        weak_until = end;

        start = end;
        end += strong.size();
        names.addAll(strong);
        Arrays.fill(kinds, start, end, IndexingScheme.STRONG_MAP);
    }

    public int height()
    {
        return names.size();
    }

    public String varName(int height)
    {
        return (String) names.get(height);
    }

    public List varNames()
    {
        return names;
    }

    public int kind(int height)
    {
        if (height == kinds.length)
            return IndexingScheme.SET;

        return kinds[height];
    }

    public int collectableUntil()
    {
        return collectable_until;
    }

    public int primitiveUntil()
    {
        return primitive_until;
    }

    public int weakUntil()
    {
        return weak_until;
    }

    public int hashCode()
    {
        return names.hashCode();
        // Java5 only:
        // ^ Arrays.hashCode(kinds);
        // (leaving it out shouldn't cause too many collisions)
    }

    public boolean equals(Object other)
    {
        if (! (other instanceof IndexStructure))
            return false;
        IndexStructure other_structure = (IndexStructure) other;

        return names.equals(other_structure.names)
            && Arrays.equals(kinds, other_structure.kinds);
    }
}
