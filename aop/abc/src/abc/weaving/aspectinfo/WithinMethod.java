/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Damien Sereni
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

package abc.weaving.aspectinfo;

import java.util.Hashtable;

import soot.*;
import polyglot.util.Position;
import abc.weaving.residues.*;

/** Handler for <code>withincode</code> lexical pointcut with a method pattern.
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 *  @author Damien Sereni
 */
public class WithinMethod extends LexicalPointcut {
    private MethodPattern pattern;

    public WithinMethod(MethodPattern pattern,Position pos) {
        super(pos);
        this.pattern = pattern;
    }

    public MethodPattern getPattern() {
        return pattern;
    }

    protected Residue matchesAt(SootClass cls,SootMethod method) {
        if(method.getName().equals(SootMethod.constructorName) ||
           method.getName().equals(SootMethod.staticInitializerName) ||
           MethodCategory.adviceBody(method))
            return NeverMatch.v();

        if(!getPattern().matchesExecution(method))
            return NeverMatch.v();
        return AlwaysMatch.v();
    }

    public String toString() {
        return "withinmethod("+pattern+")";
    }

        /* (non-Javadoc)
         * @see abc.weaving.aspectinfo.Pointcut#unify(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable, java.util.Hashtable, abc.weaving.aspectinfo.Pointcut)
         */
        public boolean unify(Pointcut otherpc, Unification unification) {

                if (otherpc.getClass() == this.getClass()) {
                        if (pattern.equivalent(((WithinMethod)otherpc).getPattern())) {
                                unification.setPointcut(this);
                                return true;
                        } else return false;
                } else // Do the right thing if otherpc was a local vars pc
                        return LocalPointcutVars.unifyLocals(this,otherpc,unification);

        }
}
