/* Abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
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

import soot.*;
import soot.jimple.*;
import soot.util.Chain;
import soot.jimple.Stmt;
import soot.jimple.IntConstant;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** is a context value null?
 *  @author Ganesh Sittampalam
 */ 

public class IsNull extends Residue {
    private ContextValue value;

    public IsNull(ContextValue value) {
	this.value=value;
    }

    public String toString() {
	return "isnull("+value+")";
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
			Chain units,Stmt begin,Stmt fail,boolean sense,
			WeavingContext wc) {

	Value v=value.getSootValue();
	Expr test;
	if(sense) test=Jimple.v().newNeExpr(v,NullConstant.v());
	else test=Jimple.v().newEqExpr(v,NullConstant.v());
	Stmt abort=Jimple.v().newIfStmt(test,fail);
	units.insertAfter(abort,begin);
	return abort;
    }

}
