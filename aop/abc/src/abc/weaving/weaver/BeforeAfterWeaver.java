package abc.weaving.weaver;

import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import abc.soot.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.residues.*;

/** Handle beforeafter weavering.
 * @author Ganesh Sittampalam
 */

public class BeforeAfterWeaver {

   private static void debug(String message)
     { if (abc.main.Debug.v().beforeWeaver) 
         System.err.println("BFA*** " + message);
     }

    public static void doWeave(SootMethod method, LocalGeneratorEx localgen,
			       ShadowPoints shadowpoints,Residue residue,
			       AbstractAdviceDecl advicedecl,WeavingContext wc)
      { 
        Body b = method.getActiveBody();
        // this non patching chain is needed so that Soot doesn't "Fix" 
        // the traps. 
        Chain units = b.getUnits().getNonPatchingChain();

	BeforeAfterAdvice.ChoosePhase cp=(BeforeAfterAdvice.ChoosePhase) wc;

	Local adviceApplied=localgen.generateLocal(BooleanType.v(),"adviceApplied");
	Residue beforeResidue
	    =AndResidue.construct
	    (new SetResidue(adviceApplied,IntConstant.v(0)),
	     AndResidue.construct(residue,new SetResidue(adviceApplied,IntConstant.v(1))));
	Residue afterResidue=new TestResidue(adviceApplied,IntConstant.v(1));

	// Weave the after advice first to ensure that the exception range doesn't cover
	// the before advice. Otherwise the signalling variable adviceApplied is not
	// guaranteed to be initialised.
	cp.setAfter();
	AfterReturningWeaver.doWeave(method,localgen,shadowpoints,afterResidue,advicedecl,wc);
	AfterThrowingWeaver.doWeave(method,localgen,shadowpoints,afterResidue,advicedecl,wc);
	cp.setBefore();
	BeforeWeaver.doWeave(method,localgen,shadowpoints,beforeResidue,advicedecl,wc);

      } // method doWeave 

}
