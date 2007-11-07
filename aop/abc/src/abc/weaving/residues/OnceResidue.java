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

import java.util.Set;

import soot.BooleanType;
import soot.Local;
import soot.PatchingChain;
import soot.SootMethod;
import soot.jimple.EqExpr;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import abc.weaving.weaver.ConstructorInliningMap;
import abc.weaving.weaver.Weaver;
import abc.weaving.weaver.WeavingContext;

/**
 * This is a residue that can be used to guard the execution of another residue <i>r</i> (by conjunction with an {@link AndResidue}).
 * When combined with <i>r</i> in such a way, <i>r</i> will only be executed exactly once after execution of {@link #stmtAfterInit}.
 * This can make sense in cases where shadows are known to be invariant after their first execution. 
 * 
 * @author Eric Bodden
 */
public class OnceResidue extends Residue {

	protected final Set<Stmt> stmtsBeforeInit;
    
    /**
     * Constructs a new residue such that the shadow is executed once per execution of the method.
     */
    public OnceResidue() {
    	this(null);
    }
    
    /**
     * Constructs a new residue such that the shadow is executed once only after each execution of
     * stmtAfterInit.
     * @param stmtsBeforeInit the statements before which the flag for the shadow is do be (re-)initialized;
     * must not be <code>null</code>
     */
    public OnceResidue(Set<Stmt> stmtsBeforeInit) {
    	if(stmtsBeforeInit==null) {
    		throw new IllegalArgumentException();
    	}
    	this.stmtsBeforeInit = stmtsBeforeInit;
	}

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

    	Weaver weaver = abc.main.Main.v().getAbcExtension().getWeaver();

    	for (Stmt stmtBeforeInit : stmtsBeforeInit) {
        	//initialise if to false
    		Stmt afterInit = (stmtBeforeInit!=null) ? 
    				(Stmt) weaver.rebind(stmtBeforeInit) :
    				Restructure.findFirstRealStmt(method, units);
    		
    		assert units.contains(afterInit);
    		
    		Stmt initStmt = Jimple.v().newAssignStmt(flag, IntConstant.v(0));
            //units should not be a patching chain because otherwise jumps to afterInit
            //would be rerouted to initStmt and hence, the flag would be reset on every loop iteration
    		assert !(units instanceof PatchingChain); 
    		units.insertBefore(initStmt, afterInit);
		}
    	
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
		return this;
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
		return "onceAfter("+ (stmtsBeforeInit!=null?stmtsBeforeInit:"methodEntry")+ ")";
	}

}
