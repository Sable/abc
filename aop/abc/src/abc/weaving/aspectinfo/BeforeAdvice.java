/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Ganesh Sittampalam
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

import java.util.Iterator;

import polyglot.util.Position;
import soot.Body;
import soot.SootMethod;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.InstructionShadowTag;
import abc.weaving.tagkit.InstructionSourceTag;
import abc.weaving.weaver.AdviceInliner;
import abc.weaving.weaver.ShadowPoints;
import abc.weaving.weaver.WeavingContext;

/** Advice specification for before advice.
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 */
public class BeforeAdvice extends AbstractAdviceSpec {
    public BeforeAdvice(Position pos) {
        super(pos);
    }

    public String toString() {
        return "before";
    }

    public Residue matchesAt(WeavingEnv we,ShadowMatch sm,AbstractAdviceDecl ad) {
        if(sm.supportsBefore()) return AlwaysMatch.v();
        else return NeverMatch.v();
    }

    private static void debug(String message) {
        if(abc.main.Debug.v().beforeWeaver)
            System.err.println("BEF*** " + message);
    }

    public void weave(SootMethod method,LocalGeneratorEx localgen,AdviceApplication adviceappl) {
        WeavingContext wc=adviceappl.advice.makeWeavingContext();
        doWeave(method,localgen,adviceappl,adviceappl.getResidue(),wc);
    }

    static void doWeave(SootMethod method,
                        LocalGeneratorEx localgen,
                        AdviceApplication adviceappl,
                        Residue residue,
                        WeavingContext wc) {

        ShadowPoints shadowpoints=adviceappl.shadowmatch.sp;
        AbstractAdviceDecl advicedecl=adviceappl.advice;

        debug("Before weaver running at "+shadowpoints.getShadowMatch());

        AdviceInliner.v().addShadowMethod(method);
        
        Body b = method.getActiveBody();
        // this non patching chain is needed so that Soot doesn't "Fix"
        // the traps.
        Chain units = b.getUnits().getNonPatchingChain();

        // find location to weave in statements,
        // just after beginning of join point shadow
        Stmt beginshadow = shadowpoints.getBegin();
        Stmt followingstmt = (Stmt) units.getSuccOf(beginshadow);

        Stmt failpoint = Jimple.v().newNopStmt();
        units.insertBefore(failpoint,followingstmt);

        debug("Weaving in residue: "+residue);

        // weave in residue
        // store shadow/source tag for this residue in weaving context
        if(advicedecl instanceof CflowSetup) {
        wc.setKindTag(InstructionKindTag.CFLOW_TEST);
        }
        if(advicedecl instanceof PerCflowSetup) {
        wc.setKindTag(InstructionKindTag.CFLOW_TEST);
        }
        wc.setShadowTag(new InstructionShadowTag(adviceappl.shadowmatch.shadowId));
        wc.setSourceTag(new InstructionSourceTag(adviceappl.advice.sourceId));
        
        residue.codeGen
            (method,localgen,units,beginshadow,failpoint,true,wc);

        debug("Weaving in advice execution statements");

        Chain stmts = advicedecl.makeAdviceExecutionStmts(adviceappl,localgen,wc);

        debug("Generated stmts: " + stmts);

        for( Iterator nextstmtIt = stmts.iterator(); nextstmtIt.hasNext(); ) {

            final Stmt nextstmt = (Stmt) nextstmtIt.next();
            units.insertBefore(nextstmt,failpoint);
        }
    }
}
