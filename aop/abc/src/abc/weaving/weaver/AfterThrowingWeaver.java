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


    public static void doWeave(SootMethod method, LocalGenerator localgen,
	                      AdviceApplication adviceappl)
      { aftdebug("Handling after returning: " + adviceappl);
        Body b = method.getActiveBody();
        Chain units = b.getUnits();
	AdviceDecl advicedecl = adviceappl.advice;
	AdviceSpec advicespec = advicedecl.getAdviceSpec();
	SootClass aspect = advicedecl.getAspect().
	                          getInstanceClass().getSootClass();
	SootMethod advicemethod = advicedecl.getImpl().getSootMethod();

	aftdebug("After throwing weaving not supported yet");

      } // method doWeave 
}
