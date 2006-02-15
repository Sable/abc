/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Ondrej Lhotak
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

package abc.weaving.residues;

import java.util.*;
import soot.*;
import soot.util.Chain;
import soot.jimple.*;
import polyglot.util.InternalCompilerError;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.Tagger;
import abc.weaving.weaver.*;

/** Used for per-aspects; a residue that evaluates to true if an
 *  aspect is present, and false otherwise
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 */

public class HasAspect extends Residue {

    public Residue optimize() { return this; }
    public Residue inline(ConstructorInliningMap cim) {
        if(pervalue == null) return new HasAspect(aspct, null);
        return new HasAspect(aspct, pervalue.inline(cim));
    }
    private SootClass aspct;

    // null to indicate singleton aspect; i.e. no params to hasAspect
    // (probably not actually used, but kept for symmetry with AspectOf)
    private ContextValue pervalue;

    public HasAspect(SootClass aspct,ContextValue pervalue) {
        this.aspct=aspct;
        this.pervalue=pervalue;
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
                        Chain units,Stmt begin,Stmt fail,boolean sense,
                        WeavingContext wc) {

        if(!sense)
            throw new InternalCompilerError("hasAspect residue should never be used negated");

        List paramTypes;
        List params;
        if(pervalue==null) {
            params=new ArrayList(); paramTypes=new ArrayList();
        } else {
            params=new ArrayList(1); paramTypes=new ArrayList(1);
            paramTypes.add(Scene.v().getSootClass("java.lang.Object").getType());
            params.add(pervalue.getSootValue());
        }

        Local hasaspect = localgen.generateLocal(BooleanType.v(),"hasAspect");
        AssignStmt stmtHasAspect = Jimple.v().newAssignStmt
            (hasaspect, Jimple.v().newStaticInvokeExpr
             (Scene.v().makeMethodRef(aspct,"hasAspect",paramTypes,BooleanType.v(),true),params));
        if(wc.getKindTag() == null) {
            wc.setKindTag(InstructionKindTag.ADVICE_TEST);
        }
        Tagger.tagStmt(stmtHasAspect, wc);
        units.insertAfter(stmtHasAspect,begin);

        Stmt abort=Jimple.v().newIfStmt
            (Jimple.v().newEqExpr(hasaspect,IntConstant.v(0)),fail);
        Tagger.tagStmt(abort, wc);
        units.insertAfter(abort,stmtHasAspect);
        return abort;
    }

    public String toString() {
        return "hasAspect("+aspct+","+pervalue+")";
    }

}
