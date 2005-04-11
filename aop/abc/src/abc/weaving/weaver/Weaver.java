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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import polyglot.util.InternalCompilerError;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.toolkits.scalar.UnusedLocalEliminator;
import abc.main.AbcTimer;
import abc.main.Debug;
import abc.main.options.OptionsParser;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.DeclareMessage;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.residues.NeverMatch;
import abc.weaving.weaver.around.AroundWeaver;

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
    public static boolean doCflowOptimization = false;

    static public void reset() {
        unitBindings=new HashMap();
        doCflowOptimization=false;
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
    static public void optimizeResidues() {
        for( Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {
            final AbcClass cl = (AbcClass) clIt.next();
            for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
                final SootMethod method = (SootMethod) methodIt.next();

                MethodAdviceList adviceList=GlobalAspectInfo.v().getAdviceList(method);
                if (adviceList!=null) {
                    Iterator appIt=adviceList.allAdvice().iterator();
                    while (appIt.hasNext()) {
                        AdviceApplication appl=(AdviceApplication)appIt.next();
                        appl.setResidue(appl.getResidue().optimize());
                    }
                }
            }
        }
    }
    static public void resetForReweaving() {
        WeavingState.reset();
    	AroundWeaver.reset();
    	AfterBeforeInliner.reset();
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
    }

        static public void weave() {
            // add aspectOf(), hasAspect(), ...
            weaveGenerateAspectMethods();
            inlineConstructors();

            if( doCflowOptimization ) {
                Unweaver unweaver = new Unweaver();
                unweaver.save();
                unitBindings = unweaver.restore();

                // We could do several passes, but for now, just do one.
                weaveAdvice();
                runCflowAnalysis();
                optimizeResidues();
                reportMessages();
                if( !abc.main.Debug.v().dontWeaveAfterAnalysis ) {
                    unitBindings = unweaver.restore();
                    
                    resetForReweaving();
                    removeDeclareWarnings();
                    if(abc.main.Debug.v().countCflowStacks) {
                        new CflowStackCounter().count();
                    }
                    weaveAdvice();
                }
            } else {
                if(abc.main.Debug.v().debugUnweaver) {
                    Unweaver unweaver = new Unweaver();
                    unweaver.save();
                    debug("unweaver saved state");
                    unitBindings = unweaver.restore();
                    debug("unweaver restored state");
                    
                    resetForReweaving();
                    weaveAdvice();
                    debug("after weaveAdvice");
                    //if (true==true) return; ///
                    unitBindings = unweaver.restore();
                    debug("unweaver restored state (2)");
                    
                    resetForReweaving();
                    //throw new RuntimeException("just a test");
                }
                if( abc.main.Debug.v().optimizeResidues ) {
                    optimizeResidues();
                }
                reportMessages();
                removeDeclareWarnings();
                if(abc.main.Debug.v().countCflowStacks) {
                    new CflowStackCounter().count();
                }
                weaveAdvice();
                debug("after weaveAdvice (2)");
            }
        }
        
        public static void doInlining() {
        	Scene.v().releaseActiveHierarchy();
        	
        	if (OptionsParser.v().around_inlining())          
            	Weaver.runAroundInliner(); // needs to be called after exception checking

            if (OptionsParser.v().before_after_inlining())
            	Weaver.runAfterBeforeInliner();
        }
        public static void runAroundInliner() {
        	for( Iterator mIt = AroundWeaver.v().shadowMethods.iterator(); mIt.hasNext(); ) {
        	    final SootMethod m = (SootMethod) mIt.next();
        		AroundInliner.v().transform(m.getActiveBody());
        	}
        }
        public static void runBoxingRemover() {
        	for( Iterator mIt = AroundWeaver.v().shadowMethods.iterator(); mIt.hasNext(); ) {
        	    final SootMethod m = (SootMethod) mIt.next();
        		BoxingRemover.v().transform(m.getActiveBody());
        	}
        }
        
        public static void runAfterBeforeInliner() {
        	AfterBeforeInliner.v().doInlining();
        }

        static public void inlineConstructors() {
            ShadowPointsSetter sg = new ShadowPointsSetter(unitBindings);
            for( Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {
                final AbcClass cl = (AbcClass) clIt.next();
                SootClass scl = cl.getSootClass();
                sg.setShadowPointsPass1(scl);
                ConstructorInliner.inlineConstructors(scl);
                sg.setShadowPointsPass2(scl);
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
                abc.main.Main.phaseDebug("Add aspect code");

        }
        static public void reportMessages() {
            for( Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {
                final AbcClass cl = (AbcClass) clIt.next();
                for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
                    final SootMethod method = (SootMethod) methodIt.next();
                    if( !method.isConcrete() ) continue;
                    MethodAdviceList adviceList = GlobalAspectInfo.v().getAdviceList(method);
                    if(adviceList == null) continue;
                    for( Iterator aaIt = adviceList.allAdvice().iterator(); aaIt.hasNext(); ) {
                        final AdviceApplication aa = (AdviceApplication) aaIt.next();
                        aa.reportMessages();
                    }
                }
            }
        }
        static public void removeDeclareWarnings() {
            if(Debug.v().weaveDeclareWarning) return;
            for( Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {
                final AbcClass cl = (AbcClass) clIt.next();
                for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
                    final SootMethod method = (SootMethod) methodIt.next();
                    if( !method.isConcrete() ) continue;
                    MethodAdviceList adviceList = GlobalAspectInfo.v().getAdviceList(method);
                    if(adviceList == null) continue;
                    for( Iterator aaIt = adviceList.allAdvice().iterator(); aaIt.hasNext(); ) {
                        final AdviceApplication aa = (AdviceApplication) aaIt.next();
                        AbstractAdviceDecl decl = aa.advice;
                        if(decl instanceof DeclareMessage) {
                            aa.setResidue(NeverMatch.v());
                        }
                    }
                }
            }
        }
        static public void weaveAdvice() {
                PointcutCodeGen pg = new PointcutCodeGen();
                GenStaticJoinPoints gsjp = new GenStaticJoinPoints();

                for( Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {

                    final AbcClass cl = (AbcClass) clIt.next();
                        final SootClass scl = cl.getSootClass();

                        debug("--------- STARTING WEAVING OF CLASS >>>>> " + scl.getName());

                        //  PASS 1 --------- (no init or preinit)--------------------

                        // generate the Static Join Points
                        gsjp.genStaticJoinPoints(scl);
                        // print out advice info for debugging
                        if (abc.main.Debug.v().printAdviceInfo)
                                PrintAdviceInfo.printAdviceInfo(scl);
                        // pass one of weaver,
                        pg.weaveInAspectsPass(scl, 1);

                        // PASS 2  ----------- (handle init and preinit) -------------
                        // then do the weaving
                        pg.weaveInAspectsPass(scl, 2);

                        debug("--------- FINISHED WEAVING OF CLASS >>>>> " + scl.getName() + "\n");
                } // each class

                // around advice applying to around advice (adviceexecution) is woven in last
                pg.weaveInAroundAdviceExecutionsPass();

                //if (false)
                for( Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {

                    final AbcClass cl = (AbcClass) clIt.next();
                    for( Iterator mIt = cl.getSootClass().getMethods().iterator(); mIt.hasNext(); ) {
                        final SootMethod m = (SootMethod) mIt.next();
                        if( !m.hasActiveBody() ) continue;
                        Body b = m.getActiveBody();
                        CopyPropagator.v().transform(b);
                        ConstantPropagatorAndFolder.v().transform(b);
                        DeadAssignmentEliminator.v().transform(b);
                        UnusedLocalEliminator.v().transform(b);
                    }
                }
                AbcTimer.mark("Weaving advice");
                abc.main.Main.phaseDebug("Weaving advice");
        } // method weave
    private static void runCflowAnalysis() {
        CflowAnalysis cfab = null;
        try {
            cfab = (CflowAnalysis) Class.forName("abc.weaving.weaver.CflowAnalysisImpl").newInstance();
        } catch( Exception e ) {
            throw new InternalCompilerError("Couldn't load interprocedural analysis plugin. "+e);
        }
        cfab.run();
    }
} // class Weaver
