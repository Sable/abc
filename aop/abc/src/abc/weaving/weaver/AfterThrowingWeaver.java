package abc.weaving.weaver;

import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import soot.javaToJimple.LocalGenerator;

/** Handle after throwing weaving.
 * @author Laurie Hendren
 * @author Jennifer Lhotak
 * @author Ondrej Lhotak
 * @date May 6, 2004
 */

public class AfterThrowingWeaver {

   /** set to false to disable debugging messages for After Throwing Weaver */
   public static boolean debug = true;

   private static void aftdebug(String message)
     { if (debug) System.err.println("AFT*** " + message);
     }


    public static void doWeave(SootMethod method, LocalGenerator lg,
	                      AdviceApplication adviceappl)
      { aftdebug("Handling after returning: " + adviceappl);
        Body b = method.getActiveBody();
        Chain units = b.getUnits();
	AdviceDecl advicedecl = adviceappl.advice;
	AdviceSpec advicespec = advicedecl.getAdviceSpec();
	SootClass aspect = advicedecl.getAspect().
	                          getInstanceClass().getSootClass();
	SootMethod advicemethod = advicedecl.getImpl().getSootMethod();

	// end of shadow
	Stmt endshadow = adviceappl.shadowpoints.getEnd();

        
        NopStmt nop2 = Jimple.v().newNopStmt();
        units.insertBefore(nop2, endshadow);

	//have ... 
	//    nop2:       nop;
	//    endshadow:  nop;  
       
        GotoStmt goto1 = Jimple.v().newGotoStmt(endshadow);
        units.insertAfter(goto1, nop2);

	//have ... 
	//    nop2:       nop;
	//    goto1:      goto endshadow;
	//    endshadow:  nop;  
	
        Local catchLocal = lg.generateLocal(
	                      RefType.v("java.lang.Throwable"));
        CaughtExceptionRef exceptRef = Jimple.v().newCaughtExceptionRef();
        IdentityStmt idStmt = Jimple.v().newIdentityStmt(catchLocal, exceptRef);
        units.insertAfter(idStmt, goto1);

	//have ... 
	//    java.lang.Exception catchLocal;
	//
	//    nop2:       nop;  
	//    goto1:      goto endshadow;
	//    idStmt:     catchLocal := @caughtexception
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
        InvokeStmt vInvokeStmt =  
	  Jimple.v().
	    newInvokeStmt(Jimple.v().newVirtualInvokeExpr(l, advicemethod));
        units.insertAfter( vInvokeStmt, assignStmt);

        ThrowStmt throwStmt = Jimple.v().newThrowStmt(catchLocal);
        units.insertAfter(throwStmt, vInvokeStmt);

	Stmt beginshadow = adviceappl.shadowpoints.getBegin();
        Stmt begincode = (Stmt) units.getSuccOf(beginshadow);

	//have ... 
	//    java.lang.Exception catchLocal;
	//    <AspectType> l;
	//
	//    beginshadow:   nop
	//    begincode:     <some statement>
	//      ....  <stuff in between>
	//    nop2:          nop;  
	//    goto1:         goto nop2;
	//    idStmt:        catchLocal := @caughtexception;
	//    assignStmt:    l = new AspectOf();
	//    vInvokeStmt:   l.<advicemethod>();
	//    throwStmt:     throw catchLocal;
	//    endshadow:     nop;

        b.getTraps().
	  add(Jimple.v().
	      newTrap(Scene.v().getSootClass("java.lang.Throwable"), 
              begincode, endshadow, idStmt));

	//  added 
	//     catch java.lang.Throwable 
	//         from begincode upto endshadow handlewith idStmt

      } // method doWeave 
}
