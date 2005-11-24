/* abc - The AspectBench Compiler
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
import soot.jimple.*;
import soot.util.*;
import abc.weaving.tagkit.Tagger;
import abc.weaving.weaver.WeavingContext;
import abc.soot.util.LocalGeneratorEx;
import java.util.*;
import abc.weaving.weaver.*;

/** A dynamic residue that throws an exception when tested. The intended use
 * is as an assertion failure. To check that a residue r is always true,
 * generate r or assert. To check that r is always false, generate r and assert.
 *  @author Ondrej Lhotak
 */
public class AssertResidue extends Residue {
    public Residue optimize() { return this; }
    public Residue inline(ConstructorInliningMap cim) {
        return new AssertResidue(message);
    }
    private String message;
    public AssertResidue() {
    }

    public AssertResidue(String message) {
        this.message = message;
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
                        Chain units,Stmt begin,Stmt fail,boolean sense,
                        WeavingContext wc) {

        Local excLocal = localgen.generateLocal(RefType.v("java.lang.RuntimeException"), "checkLocal");
        Stmt newStmt = Jimple.v().newAssignStmt(excLocal,
                Jimple.v().newNewExpr(RefType.v("java.lang.RuntimeException")));
        Tagger.tagStmt(newStmt, wc);
        units.insertAfter(newStmt, begin);
        ArrayList args = new ArrayList();
        ArrayList types = new ArrayList();
        if(message != null) {
            args.add(StringConstant.v(message));
            types.add(RefType.v("java.lang.String"));
        }
        Stmt initStmt = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(
                    excLocal, Scene.v().makeMethodRef(
                        RefType.v("java.lang.RuntimeException").getSootClass(),
                        "<init>",
                        types,
                        VoidType.v(),
                        false ),
                    args));
        Tagger.tagStmt(initStmt, wc);
        units.insertAfter(initStmt, newStmt);
        Stmt throwStmt = Jimple.v().newThrowStmt(excLocal);
        Tagger.tagStmt(throwStmt, wc);
        units.insertAfter(throwStmt, initStmt);
        return throwStmt;
    }

    public String toString() {
        return "assert";
    }
}
