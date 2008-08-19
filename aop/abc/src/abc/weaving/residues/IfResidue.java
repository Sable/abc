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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import soot.BooleanType;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Type;
import soot.jimple.AssignStmt;
import soot.jimple.Expr;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.Tagger;
import abc.weaving.weaver.ConstructorInliningMap;
import abc.weaving.weaver.WeavingContext;

/** The dynamic residue of an if(...) pointcut
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 */ 

public class IfResidue extends Residue {
    private SootMethod impl;
    private List/*<WeavingVar>*/ args;

    public Residue optimize() { return this; }
    public Residue inline(ConstructorInliningMap cim) {
        return construct(impl, WeavingVar.inline(args, cim));
    }
    private IfResidue(SootMethod impl,List args) {
	this.impl=impl;
	this.args=args;
    }


    public Residue resetForReweaving() {
    	for( Iterator variableIt = args.iterator(); variableIt.hasNext(); ) {
    	    final WeavingVar variable = (WeavingVar) variableIt.next();
    		variable.resetForReweaving();
    	}
    	return this;
    }

    
    public static IfResidue construct(SootMethod impl,List args) {
	return new IfResidue(impl,args);
    }

    public String toString() {
	return "if(...)";
    }

    public Stmt codeGen
	(SootMethod method,LocalGeneratorEx localgen,
	 Chain units,Stmt begin,Stmt fail,boolean sense,
	 WeavingContext wc) {
    	
    if(wc.getKindTag() == null) {
        wc.setKindTag(InstructionKindTag.ADVICE_TEST);
    }
	List actuals=new Vector(args.size());
	Iterator it=args.iterator();
	Stmt currStmt = begin;
	while(it.hasNext()) {
		WeavingVar wv = (WeavingVar)it.next();
		Local loc = wv.get();
		// The type of the wv may not be the same as the formal type,
		// if this is a cflow variable of primitive type it will be boxed
		// In this case need to get the primitive value
		if (wv.mustBox()) {
			// The type of wv is necessarily a RefType, as wv is a boxed var
			RefType type = (RefType)wv.getType();
			
			SootClass boxClass=type.getSootClass();
			Type unboxedType = Restructure.JavaTypeInfo.getBoxingClassPrimType(boxClass);
			
			SootMethodRef unboxMethod=Scene.v().makeMethodRef
				(boxClass,
				 Restructure.JavaTypeInfo.getSimpleTypeBoxingClassMethodName(unboxedType),
	 			 new ArrayList(),
	 			 unboxedType,
	 			 false); 

			Local ifval=localgen.generateLocal(type,"ifparam");
		
			InvokeExpr unbox = Jimple.v().newVirtualInvokeExpr(loc, unboxMethod);
		
			Stmt assignstmt = Jimple.v().newAssignStmt
				(ifval, unbox);
            Tagger.tagStmt(assignstmt, wc);
				
			units.insertAfter(assignstmt, currStmt);
			currStmt = assignstmt;
			actuals.add(ifval);
		} 
		else
	    	actuals.add(loc);
	}
	
	Local ifresult=localgen.generateLocal(BooleanType.v(),"ifresult");
	InvokeExpr ifcall=Jimple.v().newStaticInvokeExpr(impl.makeRef(),actuals);
	AssignStmt assign=Jimple.v().newAssignStmt(ifresult,ifcall);
	Expr test;
	if(sense) test=Jimple.v().newEqExpr(ifresult,IntConstant.v(0));
	else test=Jimple.v().newNeExpr(ifresult,IntConstant.v(0));
	IfStmt abort=Jimple.v().newIfStmt(test,fail);
    Tagger.tagStmt(assign, wc);
    Tagger.tagStmt(abort, wc);
	units.insertAfter(assign,currStmt);
	units.insertAfter(abort,assign);
	return abort;
    }
}
