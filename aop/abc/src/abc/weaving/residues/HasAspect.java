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

import java.util.*;
import soot.*;
import soot.util.Chain;
import soot.jimple.*;
import polyglot.util.InternalCompilerError;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** A residue that puts the relevant aspect instance into a 
 * local variable in the weaving context
 *  @author Ganesh Sittampalam
 */ 

public class HasAspect extends Residue {

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

	// We don't expect the frontend/matcher to produce a residue that does this. 
	// There's no reason we couldn't just do the standard "automatic fail" thing 
	// if there was ever a need, though.
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

	units.insertAfter(stmtHasAspect,begin);

	Stmt abort=Jimple.v().newIfStmt
	    (Jimple.v().newEqExpr(hasaspect,IntConstant.v(0)),fail);
	units.insertAfter(abort,stmtHasAspect);
	return abort;
    }

    public String toString() {
	return "aspectof("+aspct+","+pervalue+")";
    }

}
