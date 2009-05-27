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
package abc.da.weaving.weaver.dynainstr;

import soot.ArrayType;
import soot.BooleanType;
import soot.Local;
import soot.Scene;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.residues.Residue;
import abc.weaving.weaver.ConstructorInliningMap;
import abc.weaving.weaver.WeavingContext;

/**
 * This is a special residue used to dynamically enable or disable a shadow based on a boolean
 * value in an array. The residue passes if the boolean value at position {@link #shadowNumber}
 * is <code>true</code> and fails otherwise.
 *
 * @author Eric Bodden
 */
public class DynamicInstrumentationResidue extends Residue {

	protected int shadowNumber;

	/**
	 * Creates a new residue for the given shadow number.
	 * The residue passes if that shadow is enabled via its boolean flag.
	 * @param shadowNumber a valid shadow number; this must be smaller than
	 * the size of {@link ShadowRegistry#enabledShadows()} and greater or equal to 0.
	 */
	public DynamicInstrumentationResidue(int shadowNumber) {
		assert shadowNumber>=0 && shadowNumber<Shadow.reachableActiveShadows().size();
		
		this.shadowNumber = shadowNumber;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Stmt codeGen(SootMethod method, LocalGeneratorEx localgen,
			Chain units, Stmt begin, Stmt fail, boolean sense, WeavingContext wc) {
		if(!sense) return reverseSense(method, localgen, units, begin, fail, sense, wc);
		
		//fetch the boolean array to a local variable
		//boolean[] enabled_array = ShadowSwitch.enabled;
		Local array = localgen.generateLocal(ArrayType.v(BooleanType.v(),1),"enabled_array");
		SootFieldRef fieldRef = Scene.v().makeFieldRef(
			Scene.v().getSootClass(SpatialPartitioner.SHADOW_SWITCH_CLASS_NAME),
			"enabled",
			ArrayType.v(BooleanType.v(),1),
			true
		);		
		StaticFieldRef staticFieldRef = Jimple.v().newStaticFieldRef(fieldRef);
		AssignStmt assignStmt = Jimple.v().newAssignStmt(array, staticFieldRef);
		units.insertAfter(assignStmt, begin);
	
		//boolean enabled = enabled_array[shadowNumber];
		Local enabled = localgen.generateLocal(BooleanType.v(),"enabled");
		ArrayRef arrayRef = Jimple.v().newArrayRef(array, IntConstant.v(shadowNumber));
		AssignStmt countLoadStmt = Jimple.v().newAssignStmt(enabled, arrayRef);
		units.insertAfter(countLoadStmt, assignStmt);

		//if(enabled==false) goto fail;
		IfStmt testStmt = Jimple.v().newIfStmt(Jimple.v().newEqExpr(enabled, IntConstant.v(0)), fail);
		units.insertAfter(testStmt, countLoadStmt);
		
		return testStmt;
	}

	/**
	 * {@inheritDoc}
	 */
	public Residue inline(ConstructorInliningMap cim) {
		return new DynamicInstrumentationResidue(shadowNumber);
	}

	/**
	 * {@inheritDoc}
	 */
	public Residue optimize() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "dynamicswitch("+shadowNumber+")";
	}

}
