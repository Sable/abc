/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Sascha Kuzins
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

/*
 * Created on Jul 6, 2004
 *
 */
package abc.weaving.residues;

import soot.Local;
import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.tagkit.Tagger;
import abc.weaving.weaver.WeavingContext;
import java.util.*;
import abc.weaving.weaver.*;

/**
 * @author Sascha Kuzins
 * @author Ondrej Lhotak
 * 
 * Needed for ambiguous bindings.
 * Generates code to set bits in a mask that
 * express this particular binding.
 */
public class BindMaskResidue extends Residue {

        private ResidueBox op = new ResidueBox();
        private Local bindMaskLocal;
        private int mask;
        BindMaskResidue(Bind bind, Local bindMaskLocal, int mask) {
                this.op.setResidue(bind);
                this.bindMaskLocal=bindMaskLocal;
                this.mask=mask;
        }

        public Residue optimize() {
            return new BindMaskResidue((Bind)getOp().optimize(), bindMaskLocal, mask);
        }
        public Residue inline(ConstructorInliningMap cim) {
            return new BindMaskResidue((Bind)getOp().inline(cim), cim.map(bindMaskLocal), mask);
        }
        /**
         * Generates code to set the bits in the mask, then
         * generates the Bind code. 
         * 
         */
        public Stmt codeGen(SootMethod method, LocalGeneratorEx localgen, Chain units, Stmt begin, Stmt fail, 
                            boolean sense, WeavingContext wc) {
                AssignStmt as=Jimple.v().newAssignStmt(
                        bindMaskLocal, Jimple.v().newOrExpr(bindMaskLocal, IntConstant.v(mask)));
                Tagger.tagStmt(as, wc);
                units.insertAfter(as, begin);
                return getOp().codeGen(method, localgen, units, as, fail, sense, wc);
        }

        /* 
         * 
         */
        public String toString() {
                return "bindmask(" + op + ", " + mask  + ")";
        }
        // remove this node
        public Residue resetForReweaving() {
        	return op.getResidue().resetForReweaving();
        	}
        
        public void getAdviceFormalBindings(Bindings bindings, AndResidue andRoot) {
                getOp().getAdviceFormalBindings(bindings, null);
        }
        public Residue restructureToCreateBindingsMask(soot.Local bindingsMaskLocal, Bindings bindings) {
                op.setResidue(getOp().restructureToCreateBindingsMask(bindingsMaskLocal, bindings));
                return this;
        }
        public Residue getOp() { return op.getResidue(); }
        public List/*ResidueBox*/ getResidueBoxes() {
            List/*ResidueBox*/ ret = new ArrayList();
            ret.add( op );
            ret.addAll( op.getResidue().getResidueBoxes() );
            return ret;
        }

}
