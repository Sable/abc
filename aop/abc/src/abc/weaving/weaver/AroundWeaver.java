package abc.weaving.weaver;

import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import soot.javaToJimple.LocalGenerator;

/** Handle after throwing weaving.
 * @author Sacha Kuzins 
 * @date May 6, 2004
 */

public class AroundWeaver {

   /** set to false to disable debugging messages for Around Weaver */
   public static boolean debug = true;

   private static void debug(String message)
     { if (debug) System.err.println("ARD *** " + message);
     }


    public static void doWeave(SootClass cl, SootMethod method, 
	                       LocalGenerator localgen, 
			       AdviceApplication adviceappl)
      { debug("Handling after returning: " + adviceappl);
        Body b = method.getActiveBody();
        Chain units = b.getUnits();
	AdviceDecl advicedecl = adviceappl.advice;
	AdviceSpec advicespec = advicedecl.getAdviceSpec();
	SootClass aspect = advicedecl.getAspect().
	                          getInstanceClass().getSootClass();
	SootMethod advicemethod = advicedecl.getImpl().getSootMethod();

	debug("Around weaving not supported yet");

      } // method doWeave 
}
