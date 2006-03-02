/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Neil Ongkingco
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

/*
 * Created on Sep 1, 2005
 *
 */
package abc.om.weaving.aspectinfo;

import polyglot.util.Position;
import abc.weaving.aspectinfo.ShadowPointcut;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;

/**
 * Boolean pointcut. Returns AlwaysMatch if true, NeverMatch if false.
 * Used as initial values in signatures.
 * 
 * @author Neil Ongkingco
 */
public class BoolPointcut extends ShadowPointcut {

    boolean b = false;
    
    public static BoolPointcut construct(boolean b, Position pos) {
        return new BoolPointcut(b, pos);
    }
    
    private BoolPointcut(boolean b, Position pos) {
        super(pos);
        this.b = b;
    }
    
    /* (non-Javadoc)
     * @see abc.weaving.aspectinfo.ShadowPointcut#matchesAt(abc.weaving.matching.ShadowMatch)
     */
    protected Residue matchesAt(ShadowMatch sm) {
        Residue ret = null;
        if (b) {
            ret = AlwaysMatch.v();
        }
        else {
            ret = NeverMatch.v();
        }
        return ret;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new Boolean(b).toString();
    }

}
