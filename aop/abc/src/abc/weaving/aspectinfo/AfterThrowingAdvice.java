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
import soot.Local;
import soot.RefType;
import soot.SootMethod;
import soot.Trap;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.tagkit.ThrowCreatedByCompilerTag;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.residues.Residue;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.InstructionShadowTag;
import abc.weaving.tagkit.InstructionSourceTag;
import abc.weaving.tagkit.Tagger;
import abc.weaving.weaver.AdviceInliner;
import abc.weaving.weaver.ShadowPoints;
import abc.weaving.weaver.WeavingContext;

/** Advice specification for after throwing advice without exception variable binding. 
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 */
public class AfterThrowingAdvice extends AbstractAfterAdvice {
    public AfterThrowingAdvice(Position pos) {
	super(pos);
    }

    public String toString() {
	return "after throwing";
    }

    public RefType getCatchType() {
	return RefType.v("java.lang.Throwable");
    }

    public void bindException(WeavingContext wc,AbstractAdviceDecl ad,Local Exception) {
    }

    private static void debug(String message) { 
	if(abc.main.Debug.v().afterThrowingWeaver) 
	    System.err.println("AFT*** " + message);
    }

    public void weave(SootMethod method,
		      LocalGeneratorEx localgen,
		      AdviceApplication adviceappl) {

	WeavingContext wc=adviceappl.advice.makeWeavingContext();
	doWeave(method,localgen,adviceappl,adviceappl.getResidue(),wc);
    }
    

    void doWeave(SootMethod method, LocalGeneratorEx lg,
		 AdviceApplication adviceappl,Residue residue,
		 WeavingContext wc) {
    AdviceInliner.v().addShadowMethod(method);
	ShadowPoints shadowpoints=adviceappl.shadowmatch.sp;
	AbstractAdviceDecl advicedecl=adviceappl.advice;

        Body b = method.getActiveBody();
        Chain units = b.getUnits().getNonPatchingChain();

	// end of shadow
	Stmt endshadow = shadowpoints.getEnd();
        
        NopStmt nop2 = Jimple.v().newNopStmt();
        GotoStmt goto1 = Jimple.v().newGotoStmt(nop2);
        if(advicedecl instanceof CflowSetup) {
            Tagger.tagStmt(goto1, InstructionKindTag.CFLOW_EXIT);
        } else if(advicedecl instanceof PerCflowSetup) {
            Tagger.tagStmt(goto1, InstructionKindTag.PERCFLOW_EXIT);
        } else if(advicedecl instanceof DeclareSoft) {
            Tagger.tagStmt(goto1, InstructionKindTag.EXCEPTION_SOFTENER);
        } else if(advicedecl instanceof DeclareMessage) {
            Tagger.tagStmt(goto1, InstructionKindTag.DECLARE_MESSAGE);
        } else {
            Tagger.tagStmt(goto1, InstructionKindTag.AFTER_THROWING_HANDLER);
        }
        Tagger.tagStmt(goto1, new InstructionSourceTag(advicedecl.sourceId));
        Tagger.tagStmt(goto1, new InstructionShadowTag(adviceappl.shadowmatch.shadowId));
        units.insertBefore(nop2, endshadow);
	units.insertBefore(goto1, nop2);

	//have ... 
	//    goto1:      goto nop2;
	//    nop2:       nop;
	//    endshadow:  nop;  

	RefType catchType=getCatchType();
	Local exception = lg.generateLocal(catchType,"exception");
	bindException(wc,advicedecl,exception);

        CaughtExceptionRef exceptRef = Jimple.v().newCaughtExceptionRef();
        IdentityStmt idStmt = Jimple.v().newIdentityStmt(exception, exceptRef);
        units.insertAfter(idStmt, goto1);

        ThrowStmt throwStmt = Jimple.v().newThrowStmt(exception);
	throwStmt.addTag(new ThrowCreatedByCompilerTag());
    
    if(advicedecl instanceof CflowSetup) {
        Tagger.tagStmt(throwStmt, InstructionKindTag.CFLOW_EXIT);
    } else if(advicedecl instanceof PerCflowSetup) {
        Tagger.tagStmt(throwStmt, InstructionKindTag.PERCFLOW_EXIT);
    } else if(advicedecl instanceof DeclareSoft) {
        Tagger.tagStmt(throwStmt, InstructionKindTag.EXCEPTION_SOFTENER);
    } else if(advicedecl instanceof DeclareMessage) {
        Tagger.tagStmt(throwStmt, InstructionKindTag.DECLARE_MESSAGE);
    } else {
        Tagger.tagStmt(throwStmt, InstructionKindTag.AFTER_THROWING_HANDLER);
    }
    Tagger.tagStmt(throwStmt, new InstructionSourceTag(advicedecl.sourceId));
    Tagger.tagStmt(throwStmt, new InstructionShadowTag(adviceappl.shadowmatch.shadowId));
         
    // store shadow/source tag for this residue in weaving context
    if(advicedecl instanceof CflowSetup) {
        wc.setKindTag(InstructionKindTag.CFLOW_TEST);
    }
    if(advicedecl instanceof PerCflowSetup) {
        wc.setKindTag(InstructionKindTag.CFLOW_TEST);
    }
    wc.setShadowTag(new InstructionShadowTag(adviceappl.shadowmatch.shadowId));
    wc.setSourceTag(new InstructionSourceTag(adviceappl.advice.sourceId));

	Stmt endresidue=residue.codeGen
	    (method,lg,units,idStmt,throwStmt,true,wc);

	//have ... 
	//    java.lang.Exception exception;
	//
	//    goto1:      goto nop2; 
	//    idStmt:     exception := @caughtexception
	//    nop2:       nop;  
	//    endshadow:  nop;
                
        units.insertAfter(throwStmt, endresidue);

        Chain invokestmts = advicedecl.makeAdviceExecutionStmts(adviceappl,lg,wc);

	for (Iterator stmtlist = invokestmts.iterator(); stmtlist.hasNext(); )
	  { Stmt nextstmt = (Stmt) stmtlist.next();
	    units.insertBefore(nextstmt,throwStmt);
	  }

	if (method.getName().equals("<clinit>")) 
	  // have to handle case of ExceptionInInitialzerError
	  {  //  if (exception instanceof java.lang.ExceptionInIntializerError)
	     //     throw (exception); 
	     debug("Adding extra check in clinit");

	     Local isInitError = 
	         lg.generateLocal(soot.BooleanType.v(),"isInitError");

	     Stmt assignbool = Jimple.v().
	        newAssignStmt(isInitError,   
		    Jimple.v().
		      newInstanceOfExpr(
		        exception, 
		        RefType.v("java.lang.ExceptionInInitializerError")));

             Stmt ifstmt = Jimple.v().
	          newIfStmt( Jimple.v().
		    newNeExpr(isInitError,IntConstant.v(0)), 
		    throwStmt );

	     //	     ThrowStmt throwInitError = Jimple.v().newThrowStmt(exception);

	     //	     units.insertAfter(throwInitError, idStmt);
	     units.insertAfter(ifstmt , idStmt);
	     units.insertAfter(assignbool,idStmt);
	  }

	Stmt beginshadow = shadowpoints.getBegin();
        Stmt begincode = (Stmt) units.getSuccOf(beginshadow);

	//have ... 
	//    java.lang.Exception exception;
	//    <AspectType> theAspect;
	//
	//    beginshadow:   nop
	//    begincode:     <some statement>
	//       ....        <stuff in between>
	//    goto1:         goto nop2;
	//    idStmt:        exception := @caughtexception;
	//    assignStmt:    theAspect = new AspectOf();
	//             .... invoke statements .... 
	//    throwStmt:     throw exception;
	//    nop2:          nop;  
	//    endshadow:     nop;

	Chain traps=b.getTraps();
	Trap t=traps.size()>0 ? (Trap) traps.getFirst() : null;

	// assume no exception ranges overlap with this one; make sure
	// we go after any that would be enclosed within this one.
	while(t!=null && (units.follows(t.getBeginUnit(),begincode) ||
			  (t.getBeginUnit()==begincode && 
			   units.follows(idStmt,t.getEndUnit()))))
	    t=(Trap) traps.getSuccOf(t);

	Trap newt=Jimple.v().
	    newTrap(catchType.getSootClass(),
		    begincode, idStmt, idStmt);

	if(t==null) traps.addLast(newt);
	else traps.insertBefore(newt,t);
    

	//  added 
	//     catch java.lang.Throwable 
	//         from begincode upto idStmt handlewith idStmt

      } // method doWeave 

}
