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

import soot.Local;
import soot.SootMethod;
import soot.jimple.*;
import soot.util.Chain;
import polyglot.util.InternalCompilerError;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.residues.Residue.Bindings;
import abc.weaving.weaver.WeavingContext;
import java.util.*;
import abc.weaving.weaver.*;

/** The sequencing of two dynamic residues, allowing
 *  side-effects in the left hand residue to run even
 *  if the right hand residue is NeverMatch
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 */
public class SeqResidue extends Residue {
    private ResidueBox left = new ResidueBox();
    private ResidueBox right = new ResidueBox();

    public Residue optimize() {
        return construct(getLeftOp().optimize(), getRightOp().optimize());
    }
    public Residue inline(ConstructorInliningMap cim) {
        return construct(getLeftOp().inline(cim), getRightOp().inline(cim));
    }

    /** Get the left operand */
    public Residue getLeftOp() {
        return left.getResidue();
    }

    /** Get the right operand */
    public Residue getRightOp() {
        return right.getResidue();
    }

    /** Get the left box */
    public ResidueBox getLeftOpBox() {
        return left;
    }

    /** Get the right box */
    public ResidueBox getRightOpBox() {
        return right;
    }

    public String toString() {
        return "("+getLeftOp()+") ; ("+getRightOp()+")";
    }

    public Residue resetForReweaving() {
        left.setResidue(left.getResidue().resetForReweaving());
        right.setResidue(right.getResidue().resetForReweaving());
        return this;
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
                        Chain units,Stmt begin,Stmt fail,boolean sense,
                        WeavingContext wc) {
        if(sense) {
            // want to fall through if both left and right succeed, otherwise jump to fail
            Stmt middle=getLeftOp().codeGen(method,localgen,units,begin,fail,true,wc);
            return getRightOp().codeGen(method,localgen,units,middle,fail,true,wc);
        } else {
            throw new InternalCompilerError("Negated SeqResidue is not supported");
            // Could just copy the code from AndResidue if this is wanted.
        }

    }

    /** Private constructor to force use of smart constructor */
    private SeqResidue(Residue left,Residue right) {
        this.left.setResidue(left);
        this.right.setResidue(right);
    }

    /** Smart constructor
     */
    public static Residue construct(Residue left,Residue right) {
        if(left==null || right==null)
            throw new InternalCompilerError("null residue created");
        // false ; x = false ,  x ; true = x
        if(NeverMatch.neverMatches(left) || right instanceof AlwaysMatch)
            return left;
        // true ; x = x  but x ; false != false because of side-effects in x
        if(left instanceof AlwaysMatch)
            return right;
        return new SeqResidue(left,right);
    }


    public void getAdviceFormalBindings(Bindings bindings, AndResidue andRoot) {
        getLeftOp().getAdviceFormalBindings(bindings, null);
        getRightOp().getAdviceFormalBindings(bindings, null);
    }

    public Residue restructureToCreateBindingsMask(soot.Local bindingsMaskLocal, Bindings bindings) {
        left.setResidue(getLeftOp().restructureToCreateBindingsMask(bindingsMaskLocal, bindings));
        right.setResidue(getRightOp().restructureToCreateBindingsMask(bindingsMaskLocal, bindings));
        return this;
    }
    public List/*ResidueBox*/ getResidueBoxes() {
        List/*ResidueBox*/ ret = new ArrayList();
        ret.add( left );
        ret.add( right );
        ret.addAll( left.getResidue().getResidueBoxes() );
        ret.addAll( right.getResidue().getResidueBoxes() );
        return ret;
    }
}
