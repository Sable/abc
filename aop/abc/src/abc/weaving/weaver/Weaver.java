package abc.weaving.weaver;

import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import abc.weaving.weaver.*;
import abc.main.AbcTimer;

/** The driver for the weaving process.  
 * @author Jennifer Lhotak
 * @author Ondrej Lhotak
 * @author Laurie Hendren
 * @date April 24, 2004
 */

public class Weaver {


    private static void debug(String message)
      { if (abc.main.Debug.v().weaverDriver) 
	  System.err.println("WEAVER DRIVER ***** " + message);
      }	

    public void weave() {
        // Generate intertype methods and fields
        debug("Generating intertype methods and fields ....");
        IntertypeGenerator ig = new IntertypeGenerator();
	//  --- Intertype methods
        for( Iterator imdIt = 
	        GlobalAspectInfo.v().getIntertypeMethodDecls().iterator(); 
		imdIt.hasNext(); ) {
            final IntertypeMethodDecl imd = (IntertypeMethodDecl) imdIt.next();
            ig.addMethod( imd );
        }
        // --- Intertype fields
        for( Iterator ifdIt =
	        GlobalAspectInfo.v().getIntertypeFieldDecls().iterator(); 
		ifdIt.hasNext(); ) {
            final IntertypeFieldDecl ifd = (IntertypeFieldDecl) ifdIt.next();
            ig.addField( ifd );
        }

	AbcTimer.mark("Intertype weave");

        // Generate methods inside aspects needed for code gen and bodies of
	//   methods not filled in by front-end (i.e. aspectOf())
	debug("Generating extra code in aspects");
        AspectCodeGen ag = new AspectCodeGen();
        for( Iterator asIt = 
	         GlobalAspectInfo.v().getAspects().iterator(); 
		 asIt.hasNext(); ) {
            final Aspect as = (Aspect) asIt.next();
            ag.fillInAspect(as);
        }

	AbcTimer.mark("Add aspect code");

	ShadowPointsSetter sg = new ShadowPointsSetter();
        PointcutCodeGen pg = new PointcutCodeGen();
	GenStaticJoinPoints gsjp = new GenStaticJoinPoints();

        for( Iterator clIt = 
	         GlobalAspectInfo.v().getWeavableClasses().iterator(); 
		 clIt.hasNext(); ) {
            final AbcClass cl = (AbcClass) clIt.next();
	    final SootClass scl = cl.getSootClass();

	    debug("--------- STARTING WEAVING OF CLASS >>>>> " + scl.getName());

	    //  PASS 1 --------- (no init or preinit)--------------------
	       
	    // need to put in shadows for staticinit so SJP stuff can be
	    //   inserted BEFORE the beginning point of the shadow.  If this
	    //   is not done,  then the staticinitialization joinpoint will 
	    //   try to use an uninitialized SJP.
            sg.setShadowPointsPass1(scl);
	    // generate the Static Join Points
	    gsjp.genStaticJoinPoints(scl);
	    // print out advice info for debugging
	    if (abc.main.Debug.v().printAdviceInfo) 
	       PrintAdviceInfo.printAdviceInfo(scl);
	    // pass one of weaver, 
            pg.weaveInAspectsPass(scl,1);

	    // PASS 2  ----------- (handle init and preinit) -------------
	    // first set the shadows,this may trigger inlining
	    sg.setShadowPointsPass2(scl);
	    // then do the weaving
	    pg.weaveInAspectsPass(scl,2);

	    debug("--------- FINISHED WEAVING OF CLASS >>>>> " + 
		  scl.getName() + "\n");
	} // each class

      AbcTimer.mark("Weaving advice");
    } // method weave
} // class Weaver
