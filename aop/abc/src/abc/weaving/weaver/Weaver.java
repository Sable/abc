package abc.weaving.weaver;

import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;

/** The driver for the weaving process.  
 * @author Jennifer Lhotak
 * @author Ondrej Lhotak
 * @author Laurie Hendren
 * @date April 24, 2004
 */

public class Weaver {

    public void weave() {
        // Generate intertype methods and fields
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

        // Generate methods inside aspects needed for code gen and bodies of
	//   methods not filled in by front-end (i.e. aspectOf())
        AspectCodeGen ag = new AspectCodeGen();
        for( Iterator asIt = 
	         GlobalAspectInfo.v().getAspects().iterator(); 
		 asIt.hasNext(); ) {
            final Aspect as = (Aspect) asIt.next();
            ag.fillInAspect(as.getInstanceClass().getSootClass());
        }

	ShadowPointsSetter sg = new ShadowPointsSetter();
        PointcutCodeGen pg = new PointcutCodeGen();

        for( Iterator clIt = 
	         GlobalAspectInfo.v().getWeavableClasses().iterator(); 
		 clIt.hasNext(); ) {
            final AbcClass cl = (AbcClass) clIt.next();
	    final SootClass scl = cl.getSootClass();

	    // pass one, do not handle initialization and preinitialization
            sg.setShadowPointsPass1(scl);
            pg.weaveInAspectsPass1(scl);
	    // pass two, handle initializaiton and preinititalization
	    sg.setShadowPointsPass2(scl);
	    // TODO: pg.weaveInAspectsPass2(scl);
	} // each class

    } // method weave
} // class Weaver
