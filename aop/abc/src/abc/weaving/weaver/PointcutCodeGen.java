package abc.weaving.weaver;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import soot.javaToJimple.LocalGenerator;

public class PointcutCodeGen {

   /** set to false to disable debugging messages for PointcutCodeGen */
   public static boolean debug = true;

   private static void debug(String message)
     { if (debug) System.err.println("PCG*** " + message);
     }

   public void weaveInAspectsPass1( SootClass cl) {
     for( Iterator methodIt = cl.getMethods().iterator(); 
	     methodIt.hasNext(); ) 
       { // get the next method
         final SootMethod method = (SootMethod) methodIt.next();
         debug("   --- BEGIN weaveInAspectsPass1 for method " + 
	                method.getName());

	 // nothing to do for abstract or native methods 
         if( method.isAbstract() ) continue;
         if( method.isNative() ) continue;

	 // get all the advice list for this method
         MethodAdviceList adviceList = 
	     GlobalAspectInfo.v().getAdviceList(method);

	 // if no advice list for this method, nothing to do
	 if ( (adviceList == null) || 
	      (!adviceList.hasBodyAdvice() && !adviceList.hasStmtAdvice())
	    )
           { debug("No body or stmt advice for method " + method.getName());
	     continue;
	   }

	 // have something to do ...
         Body b = method.getActiveBody();
         LocalGenerator localgen = new LocalGenerator(b);

	 // do the body advice 
         for (Iterator alistIt = adviceList.bodyAdvice.iterator(); 
	      alistIt.hasNext();)
           { final AdviceApplication execappl = 
	          (AdviceApplication) alistIt.next(); 
	     weave_one(cl,method,localgen,execappl);
	   } // each body advice 

	 // do the stmt advice
         for (Iterator alistIt = adviceList.stmtAdvice.iterator(); 
	     alistIt.hasNext();)
           { final AdviceApplication execappl = 
	          (AdviceApplication) alistIt.next(); 
	     //TODO: weave_one(cl,method,localgen,execappl);
	   }  // each stmt advice
	 
         debug("   --- END weaveInAspectsPass1 for method " + 
	                method.getName());
	} // each method 
   } // method weaveInAspectsPass1
	 
	    
    private void weave_one( SootClass cl, SootMethod method,
                            LocalGenerator localgen, 
			    AdviceApplication adviceappl)
      { AdviceDecl advicedecl = adviceappl.advice;
        AdviceSpec advicespec = advicedecl.getAdviceSpec();	
	if ( advicespec instanceof BeforeAdvice ) 
           BeforeWeaver.doWeave(method, localgen, adviceappl);
        else if ( advicespec instanceof AfterReturningAdvice ) 
           AfterReturningWeaver.doWeave(method, localgen, adviceappl);
	else if ( advicespec instanceof AfterThrowingAdvice )
	   AfterThrowingWeaver.doWeave(method, localgen, adviceappl);
	else if (advicespec instanceof AfterAdvice)
	   {  AfterThrowingWeaver.doWeave(method,localgen,adviceappl);
	      AfterReturningWeaver.doWeave(method,localgen,adviceappl);
	   }
	else if (advicespec instanceof AroundAdvice ) 
	   AroundWeaver.doWeave(cl, method, localgen, adviceappl);
	else 
	   throw new CodeGenException ("Unsupported kind of advice: " +
	                                 advicespec); 
      }  // method weave_one
    
}
