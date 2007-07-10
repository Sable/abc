/* abc - The AspectBench Compiler
 * Copyright (C) 2006
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
package abc.om.visit;

import polyglot.util.CodeWriter;
import polyglot.visit.PrettyPrinter;
import abc.aspectj.ast.ClassnamePatternExpr;

/**
 * @author Neil Ongkingco
 * Internal representation of an open class member. Is a list of 
 * (flag,list of CPEs) pairs)
 */
public class MSOpenClassMemberBase extends MSOpenClassMember {
    protected ClassnamePatternExpr cpe = null;
    protected OpenClassFlagSet flagSet = null;
    protected ClassnamePatternExpr toClauseCPE = null;
    
    public MSOpenClassMemberBase() {
    }
    
    public MSOpenClassMemberBase(
            OpenClassFlagSet openClassFlags, 
            ClassnamePatternExpr cpe,
            ClassnamePatternExpr toClauseCPE) {
        this.flagSet = openClassFlags;
        this.cpe = cpe;
        this.toClauseCPE = toClauseCPE;
    }
    
    public boolean isAllowed(OpenClassFlagSet.OCFType type, MSOpenClassContext context) {
        if (!checkedCPEMatch(context)) {
            return false;
        }
        if (!checkedToClauseMatch(context)) {
            return false;
        }
        return checkedFlagIsAllowed(type, context);
    }
    
    protected boolean checkedCPEMatch(MSOpenClassContext context) {
        if (cpe == null) {
            return false;
        } else {
            return cpe.matches(context.getClassNode());
        }
    }
    
    protected boolean checkedToClauseMatch(MSOpenClassContext context) {
        if (toClauseCPE == null) {
            return false;
        } else {
            return toClauseCPE.matches(context.getAspectNode());
        }
    }
    
    protected boolean checkedFlagIsAllowed(OpenClassFlagSet.OCFType type, MSOpenClassContext context) {
        if (this.flagSet == null) {
            return false;
        }
        return flagSet.isAllowed(type, context);
    }
    
    public String toString() {
        String result = "openclass ";
        result += flagSet.toString() + 
        	" to " + toClauseCPE.toString() + 
        	" : " + cpe.toString();
        return result;
    }
    
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("openclass ");
        if (flagSet != null) {
            flagSet.prettyPrint(w, pp);
        } else {
            w.write(" (false) ");
        }
        w.write(" to ");
        if (toClauseCPE != null) {
            toClauseCPE.prettyPrint(w, pp);
        } else {
            w.write(" (false) ");
        }
        w.write(" : ");
        if (cpe != null) {
            cpe.prettyPrint(w, pp);
        } else {
            w.write(" false ");
        }
    }
}

