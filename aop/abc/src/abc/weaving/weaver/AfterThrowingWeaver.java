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
import soot.util.Chain;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.AdviceSpec;
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
	AdviceDecl advicedecl = adviceappl.advice;
	AdviceSpec advicespec = advicedecl.getAdviceSpec();
	SootClass aspect = advicedecl.getAspect().
	                          getInstanceClass().getSootClass();
	SootMethod advicemethod = advicedecl.getImpl().getSootMethod();

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
	
        Local catchLocal = lg.generateLocal(
	                      RefType.v("java.lang.Throwable"));
        CaughtExceptionRef exceptRef = Jimple.v().newCaughtExceptionRef();
        IdentityStmt idStmt = Jimple.v().newIdentityStmt(catchLocal, exceptRef);
        units.insertAfter(idStmt, goto1);

	//have ... 
	//    java.lang.Exception catchLocal;
	//
	//    goto1:      goto nop2; 
	//    idStmt:     catchLocal := @caughtexception
	//    nop2:       nop;  
	//    endshadow:  nop;
                
        // no params
        Local l = lg.generateLocal(aspect.getType());
        AssignStmt assignStmt =  
	  Jimple.v().
	    newAssignStmt( l, 
		           Jimple.v().
			     newStaticInvokeExpr(aspect.getMethod("aspectOf",
				                             new ArrayList())));
        units.insertAfter( assignStmt, idStmt);

        ThrowStmt throwStmt = Jimple.v().newThrowStmt(catchLocal);
        units.insertAfter(throwStmt, assignStmt);

        Chain invokestmts =  
                PointcutCodeGen.makeAdviceInvokeStmt 
		                      (l,adviceappl,units,lg);
	for (Iterator stmtlist = invokestmts.iterator(); stmtlist.hasNext(); )
	  { Stmt nextstmt = (Stmt) stmtlist.next();
	    units.insertBefore(nextstmt,throwStmt);
	  }

	Stmt beginshadow = adviceappl.shadowpoints.getBegin();
        Stmt begincode = (Stmt) units.getSuccOf(beginshadow);

	//have ... 
	//    java.lang.Exception catchLocal;
	//    <AspectType> l;
	//
	//    beginshadow:   nop
	//    begincode:     <some statement>
	//       ....        <stuff in between>
	//    goto1:         goto nop2;
	//    idStmt:        catchLocal := @caughtexception;
	//    assignStmt:    l = new AspectOf();
	//             .... invoke statements .... 
	//    throwStmt:     throw catchLocal;
	//    nop2:          nop;  
	//    endshadow:     nop;

        b.getTraps().
	  add(Jimple.v().
	      newTrap(Scene.v().getSootClass("java.lang.Throwable"), 
              begincode, idStmt, idStmt));

	//  added 
	//     catch java.lang.Throwable 
	//         from begincode upto idStmt handlewith idStmt

      } // method doWeave 
}
