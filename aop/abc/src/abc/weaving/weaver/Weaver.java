/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Jennifer Lhotak
 * Copyright (C) 2004 Ondrej Lhotak
 * Copyright (C) 2004 Laurie Hendren
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
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
    static private Map unitBindings = new HashMap();
    private static boolean doCflowOptimization = true;

    static public void reset() {
        unitBindings=new HashMap();
        doCflowOptimization=true;
    }

    static public Map getUnitBindings() {
        return unitBindings;
    }
    static public Unit rebind(Unit ut) {
        Unit result=(Unit)unitBindings.get(ut);
        if (result!=null)
                return result;
        else
                return ut;
    }
    static public void resetForReweaving() {
        // reset all residues
        for( Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {
            final AbcClass cl = (AbcClass) clIt.next();
            for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
                final SootMethod method = (SootMethod) methodIt.next();

                MethodAdviceList adviceList=GlobalAspectInfo.v().getAdviceList(method);
                if (adviceList!=null) {
                    Iterator appIt=adviceList.allAdvice().iterator();
                    while (appIt.hasNext()) {
                        AdviceApplication appl=(AdviceApplication)appIt.next();
                        appl.setResidue(
                                        appl.getResidue().resetForReweaving());
                    }
                }
            }
        }
        // reset all advice
        for( Iterator adIt = GlobalAspectInfo.v().getAdviceDecls().iterator(); adIt.hasNext(); ) {
            final AbstractAdviceDecl ad = (AbstractAdviceDecl) adIt.next();
            ad.resetForReweaving();
        }
        // reset all shadow points
        // In practice most will be thrown away later,
        // but InterfaceInitialization ones won't and to keep things robust
        // just reset the lot.

        for( Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {

            final AbcClass cl = (AbcClass) clIt.next();
            for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
                final SootMethod method = (SootMethod) methodIt.next();

                List/*<ShadowMatch>*/ shadowList=GlobalAspectInfo.v().getShadowMatchList(method);
                Iterator smIt=shadowList.iterator();
                while (smIt.hasNext()) {
                    ShadowMatch sm=(ShadowMatch) smIt.next();
                    if(sm.sp!=null) sm.sp.resetForReweaving();
                }
            }
        }

    }

    public static boolean finalWeave;

        static public void weave() {
            finalWeave=false;
            if( !soot.options.Options.v().whole_program() ) doCflowOptimization = false;
            if( doCflowOptimization ) {
                weaveGenerateAspectMethods();
                Unweaver unweaver = new Unweaver();
                unweaver.save();
                unitBindings = unweaver.restore();

                // We could do several passes, but for now, just do one.
                if(abc.main.Debug.v().dontWeaveAfterAnalysis) finalWeave=true;
                weaveAdvice();
                CflowAnalysisBridge cfab = new CflowAnalysisBridge();
                cfab.run();
                if( !abc.main.Debug.v().dontWeaveAfterAnalysis ) {
                    unitBindings = unweaver.restore();
                    AroundWeaver.reset();
                    resetForReweaving();
                    finalWeave=true;
                    weaveAdvice();
                }
            } else {
                // add aspectOf(), hasAspect(), ...
                weaveGenerateAspectMethods();
                if(abc.main.Debug.v().debugUnweaver) {
                    Unweaver unweaver = new Unweaver();
                    unweaver.save();
                    debug("unweaver saved state");
                    unitBindings = unweaver.restore();
                    debug("unweaver restored state");
                    AroundWeaver.reset();
                    resetForReweaving();
                    weaveAdvice();
                    debug("after weaveAdvice");
                    //if (true==true) return; ///
                    unitBindings = unweaver.restore();
                    debug("unweaver restored state (2)");
                    AroundWeaver.reset();
                    resetForReweaving();
                    //throw new RuntimeException("just a test");
                }
                finalWeave=true;
                weaveAdvice();
                debug("after weaveAdvice (2)");
            }
        }

        static public void weaveGenerateAspectMethods() {
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
        static public void weaveAdvice() {
                ShadowPointsSetter sg = new ShadowPointsSetter(unitBindings);
                PointcutCodeGen pg = new PointcutCodeGen();
                GenStaticJoinPoints gsjp =
                    new GenStaticJoinPoints(abc.main.Main.v().getAbcExtension().runtimeSJPFactoryClass());

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
