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
import abc.weaving.residues.Residue;
import abc.weaving.tagkit.InstructionShadowTag;
import abc.weaving.tagkit.InstructionSourceTag;
import abc.weaving.weaver.AdviceInliner;
import abc.weaving.weaver.ShadowPoints;
import abc.weaving.weaver.WeavingContext;

/** Advice specification for after returning advice without return variable binding. 
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 */
public class AfterReturningAdvice extends AbstractAfterAdvice {
    public AfterReturningAdvice(Position pos) {
	super(pos);
    }

    public String toString() {
	return "after returning";
    }

    private static void debug(String message) { 
	if(abc.main.Debug.v().afterReturningWeaver) 
	    System.err.println("AFR*** " + message);
    }

    public void weave(SootMethod method,
			     LocalGeneratorEx localgen,
			     AdviceApplication adviceappl) {
	WeavingContext wc=adviceappl.advice.makeWeavingContext();
	doWeave(method,localgen,adviceappl,adviceappl.getResidue(),wc);
    }
    

    void doWeave(SootMethod method, LocalGeneratorEx localgen,
		 AdviceApplication adviceappl,Residue residue,
		 WeavingContext wc) {
    AdviceInliner.v().addShadowMethod(method);
	ShadowPoints shadowpoints=adviceappl.shadowmatch.sp;
	AbstractAdviceDecl advicedecl=adviceappl.advice;

	debug("In after returning weaver");
        Body b = method.getActiveBody();
	// Use the non-patching chain to stop soot "fixing" up the jumps
        Chain units = b.getUnits().getNonPatchingChain();

	Stmt endshadow = shadowpoints.getEnd();
	Stmt prevstmt = (Stmt) units.getPredOf(endshadow);

	Stmt failpoint = Jimple.v().newNopStmt();
	units.insertBefore(failpoint,endshadow);

	debug("generating residue code");
    debug("src/shadow: " + adviceappl.shadowmatch.shadowId + " " + adviceappl.advice.sourceId);
    // store shadow/source tag for this residue in weaving context
    wc.setShadowTag(new InstructionShadowTag(adviceappl.shadowmatch.shadowId));
    wc.setSourceTag(new InstructionSourceTag(adviceappl.advice.sourceId));
	residue.codeGen
	    (method,localgen,units,prevstmt,failpoint,true,wc);

	debug("making advice execution statements");
        Chain stmts = advicedecl.makeAdviceExecutionStmts(adviceappl,localgen,wc);
        debug("Generated stmts: " + stmts);

	// weave in statements just before end of join point shadow

	for (Iterator stmtlist = stmts.iterator(); stmtlist.hasNext(); )
	  { Stmt nextstmt = (Stmt) stmtlist.next();
	    units.insertBefore(nextstmt,failpoint);
	  }
	debug("after returning weaver finished");
      } // method doWeave 

}
