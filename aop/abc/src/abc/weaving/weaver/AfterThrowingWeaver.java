package abc.weaving.weaver;

import java.util.ArrayList;
import java.util.Iterator;

import soot.Body;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.jimple.IntConstant;
import soot.util.Chain;
import abc.soot.util.*;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AfterThrowingAdvice;
import abc.weaving.matching.AdviceApplication;

/** Handle after throwing weaving.
 * @author Laurie Hendren
 * @author Jennifer Lhotak
 * @author Ondrej Lhotak
 * @date May 6, 2004
 */

public class AfterThrowingWeaver {


   private static void debug(String message)
     { if (abc.main.Debug.v().afterThrowingWeaver) 
          System.err.println("AFT*** " + message);
     }


    public static void doWeave(SootMethod method, LocalGeneratorEx lg,
	                      AdviceApplication adviceappl)
      { debug("Handling after throwing: " + adviceappl);
        Body b = method.getActiveBody();
        Chain units = b.getUnits().getNonPatchingChain();

	WeavingContext wc=PointcutCodeGen.makeWeavingContext(adviceappl);

	AbstractAdviceDecl advicedecl = adviceappl.advice;
	AfterThrowingAdvice advicespec = (AfterThrowingAdvice) (advicedecl.getAdviceSpec());

	// end of shadow
	Stmt endshadow = adviceappl.shadowpoints.getEnd();
        
        NopStmt nop2 = Jimple.v().newNopStmt();
        GotoStmt goto1 = Jimple.v().newGotoStmt(nop2);
        units.insertBefore(nop2, endshadow);
	units.insertBefore(goto1, nop2);

	//have ... 
	//    goto1:      goto nop2;
	//    nop2:       nop;
	//    endshadow:  nop;  

	RefType catchType=advicespec.getCatchType();
	Local exception = lg.generateLocal(catchType,"exception");
	advicespec.bindException(wc,advicedecl,exception);

        CaughtExceptionRef exceptRef = Jimple.v().newCaughtExceptionRef();
        IdentityStmt idStmt = Jimple.v().newIdentityStmt(exception, exceptRef);
        units.insertAfter(idStmt, goto1);

        ThrowStmt throwStmt = Jimple.v().newThrowStmt(exception);

	Stmt endresidue=adviceappl.residue.codeGen
	    (method,lg,units,idStmt,throwStmt,wc);

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
		    newEqExpr(isInitError,IntConstant.v(1)), 
		    throwStmt );

	     //	     ThrowStmt throwInitError = Jimple.v().newThrowStmt(exception);

	     //	     units.insertAfter(throwInitError, idStmt);
	     units.insertAfter(ifstmt , idStmt);
	     units.insertAfter(assignbool,idStmt);
	  }

	Stmt beginshadow = adviceappl.shadowpoints.getBegin();
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

        b.getTraps().
	  add(Jimple.v().
	      newTrap(catchType.getSootClass(),
		      begincode, idStmt, idStmt));

	//  added 
	//     catch java.lang.Throwable 
	//         from begincode upto idStmt handlewith idStmt

      } // method doWeave 
}
