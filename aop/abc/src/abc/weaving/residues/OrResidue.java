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
import polyglot.util.InternalCompilerError;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;
import java.util.*;
import abc.weaving.weaver.*;

/** Disjunction of two residues
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 *  @date 28-Apr-04
 */
public class OrResidue extends Residue {
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
        return "("+getLeftOp()+") || ("+getRightOp()+")";
    }

    public Residue resetForReweaving() {
        left.setResidue(left.getResidue().resetForReweaving());
        right.setResidue(right.getResidue().resetForReweaving());
        return this;
    }
    /** Private constructor to force use of smart constructor */
    private OrResidue(Residue left,Residue right) {
        this.left.setResidue(left);
        this.right.setResidue(right);
    }

    /** Smart constructor; some short-circuiting may need to be removed
     *  to mimic ajc behaviour
     */
    public static Residue construct(Residue left,Residue right) {
        if(left==null || right==null)
            throw new InternalCompilerError("null residue created");
        // false || x = x ; x || true = true
        if(NeverMatch.neverMatches(left) || right instanceof AlwaysMatch)
            return right;
        // true || x = true ; x || right = x
        if(left instanceof AlwaysMatch || NeverMatch.neverMatches(right))
            return left;
        return new OrResidue(left,right);
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
                        Chain units,Stmt begin,Stmt fail,boolean sense,
                        WeavingContext wc) {
        if(sense) {
            // want to fall through if either left or right succeeds, otherwise jump to fail
            Stmt nopStmt=Jimple.v().newNopStmt();
            if(abc.main.Debug.v().tagResidueCode)
                nopStmt.addTag(new soot.tagkit.StringTag("^^ nop for or residue: "+this));
            // if left succeeds, goto nop stmt, otherwise fall through
            Stmt middle=getLeftOp().codeGen(method,localgen,units,begin,nopStmt,false,wc);
            // if right succeeds fall through, otherwise jump to fail
            Stmt end=getRightOp().codeGen(method,localgen,units,middle,fail,true,wc);
            // make fall through statement be the nop to catch the left residue succeeding
            units.insertAfter(nopStmt,end);
            return nopStmt;
        } else {
            // want to jump to fail if either left or right succeeds, otherwise fall through
            Stmt middle=getLeftOp().codeGen(method,localgen,units,begin,fail,false,wc);
            return getRightOp().codeGen(method,localgen,units,middle,fail,false,wc);
        }
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
