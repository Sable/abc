/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
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

package abc.eaj.weaving.aspectinfo;

import soot.*;

import polyglot.util.Position;

import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;

import abc.eaj.weaving.matching.*;

/** Handler for <code>cast</code> shadow pointcut.
 *  @author Julian Tibble
 */
public class Cast extends ShadowPointcut
{
    private TypePattern pattern;

    public Cast(TypePattern pattern, Position pos)
    {
        super(pos);
        this.pattern = pattern;
    }

    public TypePattern getPattern()
    {
        return pattern;
    }

    protected Residue matchesAt(ShadowMatch sm)
    {
        if (!(sm instanceof CastShadowMatch)) return NeverMatch.v();
        Type cast_to = ((CastShadowMatch) sm).getCastType();

        if (!getPattern().matchesType(cast_to)) return NeverMatch.v();
        return AlwaysMatch.v();
    }

    public String toString()
    {
        return "cast(" + pattern + ")";
    }

        /* (non-Javadoc)
         * @see abc.weaving.aspectinfo.Pointcut#unify(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable, java.util.Hashtable, abc.weaving.aspectinfo.Pointcut)
         */
        public boolean unify(Pointcut otherpc, Unification unification) {

                if (otherpc.getClass() == this.getClass()) {
                        if (pattern.equivalent(((Cast)otherpc).getPattern())) {
                                unification.setPointcut(this);
                                return true;
                        } else return false;
                } else // Do the right thing if otherpc was a local vars pc
                        return LocalPointcutVars.unifyLocals(this,otherpc,unification);

        }
}
