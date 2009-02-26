/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
 * Copyright (C) 2007 Patrick Lam
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
import soot.IntType;
import soot.Local;
import soot.Scene;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.residues.AndResidue;
import abc.weaving.residues.Residue;
import abc.weaving.weaver.ConstructorInliningMap;
import abc.weaving.weaver.WeavingContext;

/**
 * This is a special residue used to count how often a shadow is executed.
 * <b>This must be combined with other residues using {@link AndResidue},
 * setting this residue to the <i>right-hand side</i> of the And!</b>
 *
 * @author Eric Bodden
 * @author Patrick Lam
 */
public class ShadowCountResidue extends Residue {

	protected int shadowNumber;

	/**
	 * Creates a new residue for the given shadow number.
	 * The residue passes if that shadow is enabled via its boolean flag.
	 * @param shadowNumber a valid shadow number; this must be smaller than
	 * the size of {@link ShadowRegistry#enabledShadows()} and greater or equal to 0.
	 */
	public ShadowCountResidue(int shadowNumber) {
		assert shadowNumber>=0;
		
		this.shadowNumber = shadowNumber;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Stmt codeGen(SootMethod method, LocalGeneratorEx localgen,
			Chain units, Stmt begin, Stmt fail, boolean sense, WeavingContext wc) {		
		//fetch the boolean array to a local variable
		//boolean[] counts = ShadowSwitch.counts;
		Local array = localgen.generateLocal(ArrayType.v(IntType.v(),1),"counts");
		SootFieldRef fieldRef = Scene.v().makeFieldRef(
			Scene.v().getSootClass(DynamicInstrumenter.SHADOW_SWITCH_CLASS_NAME),
			"counts",
			ArrayType.v(IntType.v(),1),
			true
		);		
		StaticFieldRef staticFieldRef = Jimple.v().newStaticFieldRef(fieldRef);
		AssignStmt assignStmt = Jimple.v().newAssignStmt(array, staticFieldRef);
		units.insertAfter(assignStmt, begin);

		//int count = enabled_array[shadowNumber]
		Local count=localgen.generateLocal(IntType.v(),"count");
		ArrayRef arrayRefLoad = Jimple.v().newArrayRef(array, IntConstant.v(shadowNumber));
		AssignStmt countLoadStmt = Jimple.v().newAssignStmt(count, arrayRefLoad);
		units.insertAfter(countLoadStmt, assignStmt);

		//count++
		AssignStmt countIncStmt = Jimple.v().newAssignStmt(count, Jimple.v().newAddExpr(count, IntConstant.v(1)));
		units.insertAfter(countIncStmt, countLoadStmt);

        //enabled_array[shadowNumber] = count
        ArrayRef arrayRefStore = Jimple.v().newArrayRef(array, IntConstant.v(shadowNumber));
        AssignStmt countStoreStmt = Jimple.v().newAssignStmt(arrayRefStore, count);
        units.insertAfter(countStoreStmt, countIncStmt);

		return countStoreStmt;
	}

	/**
	 * {@inheritDoc}
	 */
	public Residue inline(ConstructorInliningMap cim) {
		return new ShadowCountResidue(shadowNumber);
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
		return "counts["+shadowNumber+"]++";
	}

}
