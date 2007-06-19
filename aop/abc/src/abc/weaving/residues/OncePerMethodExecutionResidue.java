/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
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

import soot.BooleanType;
import soot.Local;
import soot.SootMethod;
import soot.jimple.EqExpr;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import abc.weaving.weaver.ConstructorInliningMap;
import abc.weaving.weaver.WeavingContext;

/**
 * This is a residue that can be used to guard the execution of another residue <i>r</i> (by conjunction with an {@link AndResidue}).
 * When combined with <i>r</i> in such a way, <i>r</i> will only be executed exactly once per execution of the surrounding method.
 * This can make sense in cases where shadows are known to be invariant after their first execution inside a method. 
 * 
 * @author Eric Bodden
 */
public class OncePerMethodExecutionResidue extends Residue {

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Stmt codeGen(SootMethod method, LocalGeneratorEx localgen,
			Chain units, Stmt begin, Stmt fail, boolean sense, WeavingContext wc) {
		if(!sense) {
			throw new RuntimeException("Not (yet) designed to be used under negation!");
		}
		
		//create a fresh boolean flag
		Local flag = localgen.generateLocal(BooleanType.v());
		//initialize if to false right after all identity statements
		Stmt firstRealStmt = Restructure.findFirstRealStmt(method, units);
		Stmt initStmt = Jimple.v().newAssignStmt(flag, IntConstant.v(0));
		units.insertBefore(initStmt, firstRealStmt);
		
		//now at the begin statement, check the status of the flag:
		//if it is still false, set it to true and execute the shadow;
		//else, jump to fail
		
		//if(visited) goto fail
		EqExpr eqExpr = Jimple.v().newEqExpr(flag, IntConstant.v(1));
		Stmt branch = Jimple.v().newIfStmt(eqExpr, fail); 
		units.insertAfter(branch,begin);
		
		//visited = true;
		Stmt assignTrueStmt = Jimple.v().newAssignStmt(flag, IntConstant.v(1));
		units.insertAfter(assignTrueStmt,branch);
		
		return assignTrueStmt;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Residue inline(ConstructorInliningMap cim) {
		return new OncePerMethodExecutionResidue();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Residue optimize() {
		return this;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "oncePerMethod()";
	}

}
