/* Abc - The AspectBench Compiler
 * Copyright (C) 2004 Jennifer Lhotak
 * Copyright (C) 2004 Ondrej Lhotak
 * Copyright (C) 2004 Laurie Hendren
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL; 
 * if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

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
    private Map unitBindings = new HashMap();
    private static boolean doCflowOptimization = true;
	public void weave() {
            if( !soot.options.Options.v().whole_program() ) doCflowOptimization = false;
            if( doCflowOptimization ) {
                weaveGenerateAspectMethods();
                Unweaver unweaver = new Unweaver();
                unweaver.save();
                unitBindings = unweaver.restore();

                // We could do several passes, but for now, just do one.
                weaveAdvice();
                CflowAnalysisBridge cfab = new CflowAnalysisBridge();
                cfab.run();
                unitBindings = unweaver.restore();
                weaveAdvice();

            } else {
                weaveGenerateAspectMethods();
                weaveAdvice();
            }
        }
	
	public void weaveGenerateAspectMethods() {
		// Generate methods inside aspects needed for code gen and bodies of
		//   methods not filled in by front-end (i.e. aspectOf())
		debug("Generating extra code in aspects");
		AspectCodeGen ag = new AspectCodeGen();
		for( Iterator asIt = GlobalAspectInfo.v().getAspects().iterator(); asIt.hasNext(); ) {
		    final Aspect as = (Aspect) asIt.next();
			ag.fillInAspect(as);
		}
	
		AbcTimer.mark("Add aspect code");

        }
	public void weaveAdvice() {
		ShadowPointsSetter sg = new ShadowPointsSetter(unitBindings);
		PointcutCodeGen pg = new PointcutCodeGen();
		GenStaticJoinPoints gsjp = new GenStaticJoinPoints();
	
		for( Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {
	
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
			pg.weaveInAspectsPass(scl, 1);
	
			// PASS 2  ----------- (handle init and preinit) -------------
			// first set the shadows,this may trigger inlining
			sg.setShadowPointsPass2(scl);
			// then do the weaving
			pg.weaveInAspectsPass(scl, 2);

			debug("--------- FINISHED WEAVING OF CLASS >>>>> " + scl.getName() + "\n");
		} // each class
		
		// around advice applying to around advice (adviceexecution) is woven in last
		pg.weaveInAroundAdviceExecutionsPass();
		
		AbcTimer.mark("Weaving advice");
	} // method weave
} // class Weaver
