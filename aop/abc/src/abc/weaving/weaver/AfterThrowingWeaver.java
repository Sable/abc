package abc.weaving.weaver;

import java.util.ArrayList;
import java.util.Iterator;

import soot.Body;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.jimple.AssignStmt;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.jimple.IntConstant;
import soot.tagkit.ThrowCreatedByCompilerTag;
import soot.util.Chain;
import abc.soot.util.*;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.ThrowingAdvice;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.residues.Residue;


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
			       ShadowPoints shadowpoints,Residue residue,
			       AbstractAdviceDecl advicedecl,WeavingContext wc)
      { 
        Body b = method.getActiveBody();
        Chain units = b.getUnits().getNonPatchingChain();

	ThrowingAdvice advicespec = (ThrowingAdvice) (advicedecl.getAdviceSpec());

	// end of shadow
	Stmt endshadow = shadowpoints.getEnd();
        
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
	throwStmt.addTag(new ThrowCreatedByCompilerTag());

	Stmt endresidue=residue.codeGen
	    (method,lg,units,idStmt,throwStmt,wc);

	//have ... 
	//    java.lang.Exception exception;
	//
	//    goto1:      goto nop2; 
	//    idStmt:     exception := @caughtexception
	//    nop2:       nop;  
	//    endshadow:  nop;
                
        units.insertAfter(throwStmt, endresidue);

        Chain invokestmts = advicedecl.makeAdviceExecutionStmts(lg,wc);

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
