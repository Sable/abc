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
import abc.weaving.weaver.WeavingContext;
import java.util.*;

/**
 * @author Sascha Kuzins
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
        /**
         * Generates code to set the bits in the mask, then
         * generates the Bind code. 
         * 
         */
        public Stmt codeGen(SootMethod method, LocalGeneratorEx localgen, Chain units, Stmt begin, Stmt fail, 
                            boolean sense, WeavingContext wc) {
                AssignStmt as=Jimple.v().newAssignStmt(
                        bindMaskLocal, Jimple.v().newOrExpr(bindMaskLocal, IntConstant.v(mask)));
                units.insertAfter(as, begin);
                return getOp().codeGen(method, localgen, units, as, fail, sense, wc);
        }

        /* 
         * 
         */
        public String toString() {
                return "bindmask(" + op  + ")";
        }
        
        public void getAdviceFormalBindings(Bindings bindings) {
                getOp().getAdviceFormalBindings(bindings);
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
