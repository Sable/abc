/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2006 Eric Bodden
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

package abc.weaving.aspectinfo;

import polyglot.util.Position;
import soot.BooleanType;
import soot.Local;
import soot.SootMethod;
import soot.jimple.IntConstant;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.Residue;
import abc.weaving.residues.SeqResidue;
import abc.weaving.residues.SetResidue;
import abc.weaving.residues.TestResidue;
import abc.weaving.weaver.WeavingContext;

/** Advice specification for advice that applies both before and after
 *  a joinpoint.
 *  @author Ganesh Sittampalam
 *  @author Eric Bodden
 */
public class BeforeAfterAdvice extends AbstractAdviceSpec {
    private BeforeAdvice before; //FIXME is it correct that this variable is never read?
    private AfterAdvice after;

    public BeforeAfterAdvice(Position pos) {
        super(pos);
        before=new BeforeAdvice(pos);
        after=new AfterAdvice(pos);
    }

    public String toString() {
        return "beforeafter";
    }

    public Residue matchesAt(WeavingEnv we,ShadowMatch sm,AbstractAdviceDecl ad) {
        // BeforeAfterAdvice is just used for internal bookkeeping type stuff,
        // and always matches, even at joinpoints that don't support after (at such
        // joinpoints usually we get a push immediately followed by a pop, or similar
        // useless behaviour, but nested advice might make that relevant)
        return AlwaysMatch.v();
    }

    // For use with WeavingContext
    public static interface ChoosePhase {
        public void setBefore();
        public void setAfter();
    }

    public void weave(SootMethod method,LocalGeneratorEx localgen,AdviceApplication adviceappl) {
        WeavingContext wc=adviceappl.advice.makeWeavingContext();
        doWeave(method,localgen,adviceappl,adviceappl.getResidue(),wc);
    }

    void doWeave(SootMethod method,LocalGeneratorEx localgen,
               AdviceApplication adviceappl,Residue residue,
               WeavingContext wc) {

        ChoosePhase cp=(ChoosePhase) wc;

        Residue beforeResidue,afterResidue;
        if(residue instanceof AlwaysMatch) {
            // Laurie made me do it!
            beforeResidue=AlwaysMatch.v();
            afterResidue=AlwaysMatch.v();
        } else {
          Local adviceApplied=localgen.generateLocal(BooleanType.v(),"adviceApplied");
          beforeResidue
              =SeqResidue.construct
               (new SetResidue(adviceApplied,IntConstant.v(0)),
                SeqResidue.construct(residue,new SetResidue(adviceApplied,IntConstant.v(1))));
          afterResidue=new TestResidue(adviceApplied,IntConstant.v(1));
        }

        // Weave the after advice first to ensure that the exception range doesn't cover
        // the before advice. Otherwise the signalling variable adviceApplied is not
        // guaranteed to be initialised.
        cp.setAfter();
        after.doWeave(method,localgen,adviceappl,afterResidue,wc);

        cp.setBefore();
        BeforeAdvice.doWeave(method,localgen,adviceappl,beforeResidue,wc);

    } // method doWeave

}
