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

import soot.SootMethod;
import soot.util.Chain;
import soot.jimple.*;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;
import java.util.*;
import abc.weaving.weaver.*;

/** Negation of a residue
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 *  @date 28-Apr-04
 */
public class NotResidue extends Residue {
    private ResidueBox op = new ResidueBox();

    /** Get the operand */
    public Residue getOp() {
        return op.getResidue();
    }

    public Residue optimize() {
        return construct(getOp().optimize());
    }
    public Residue inline(ConstructorInliningMap cim) {
        return construct(getOp().inline(cim));
    }

    public Residue resetForReweaving() {
        op.setResidue(op.getResidue().resetForReweaving());
        return this;
    }

        /** Private constructor to force use of smart constructor */
    private NotResidue(Residue op) {
        this.op.setResidue(op);
    }

    public String toString() {
        return "!("+op+")";
    }

    /** Smart constructor; some short-circuiting may need to be removed
     *  to mimic ajc behaviour
     */
    public static Residue construct(Residue op) {
        if(NeverMatch.neverMatches(op)) return AlwaysMatch.v();
        if(op instanceof AlwaysMatch) return NeverMatch.v();
        return new NotResidue(op);
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
                        Chain units,Stmt begin,Stmt fail,boolean sense,
                        WeavingContext wc) {
        if(abc.main.Debug.v().residueCodeGen)
            System.err.println("beginning not residue generation");

        return getOp().codeGen(method,localgen,units,begin,fail,!sense,wc);
    }


	public void getAdviceFormalBindings(Bindings bindings, AndResidue andRoot) {
		getOp().getAdviceFormalBindings(bindings, null);
	}
	public Residue restructureToCreateBindingsMask(soot.Local bindingsMaskLocal, Bindings bindings) {
		op.setResidue(getOp().restructureToCreateBindingsMask(bindingsMaskLocal, bindings));
		return this;
	}


        public List/*ResidueBox*/ getResidueBoxes() {
            List/*ResidueBox*/ ret = new ArrayList();
            ret.add( op );
            ret.addAll( op.getResidue().getResidueBoxes() );
            return ret;
        }
}
