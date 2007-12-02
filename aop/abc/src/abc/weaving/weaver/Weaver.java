/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Jennifer Lhotak
 * Copyright (C) 2004 Ondrej Lhotak
 * Copyright (C) 2004 Laurie Hendren
 * Copyright (C) 2006 Eric Bodden
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import abc.main.Main;
import abc.main.options.OptionsParser;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.DeclareMessage;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.ResidueBox;
import abc.weaving.weaver.around.AroundWeaver;

/** The driver for the weaving process.
 * @author Jennifer Lhotak
 * @author Ondrej Lhotak
 * @author Laurie Hendren
 * @author Eric Bodden
 * @date April 24, 2004
 */

public class Weaver {

    protected final AspectCodeGen ag;
    
    protected Map<Object,Object> unitBindings = new HashMap();
    protected Map<Object,Object> reverseUnitBindings;

	protected Unweaver unweaver; 

    /**
     * Creates a new Weaver with a default aspect code generator.
     */
    public Weaver() {
        this(new AspectCodeGen());
    }
    
    /**
     * Creates a new Weaver with a custom aspect code generator.
     */
    public Weaver(AspectCodeGen aspectCodeGen) {
        ag = aspectCodeGen;
    }

    private void debug(String message)
      { if (Debug.v().weaverDriver)
          System.err.println("WEAVER DRIVER ***** " + message);
      }

    public Map getUnitBindings() {
        return unitBindings;
    }

    /**
     * Due to reweaving, there can be multiple versions (copies) of one and the
     * same unit. This method returns for the original version of a unit the current unit,
     * i.e. the unit after the original bodies were restored last.
     * @param unitTrapOrLocal a possibly original unit, trap or local, i.e. from before weaving
     * @return the current version of unitTrapOrLocal or unitTrapOrLocal itself, if no current version exists
     * @see #reverseRebind(Unit)
     */
    public Object rebind(Object unitTrapOrLocal) {
        Object result= unitBindings.get(unitTrapOrLocal);
        if (result!=null)
                return result;
        else
                return unitTrapOrLocal;
    }
    
    /**
     * Due to reweaving, there can be multiple versions (copies) of one and the
     * same unit. This method returns for the current version of a unit the original version
     * that existed right before the first weaving. That way, by using this method
     * one can get the same view on a unit across multiple reweaving steps. 
     * @param unitTrapOrLocal a current unit, trap or local
     * @return the copy of this unit, trap or local that existed right before the first weaving
     * or unitTrapOrLocal itself, if there exists no such copy
     */
    public Object reverseRebind(Object unitTrapOrLocal) {
    	Object result= reverseUnitBindings.get(unitTrapOrLocal);
        if (result!=null)
                return result;
        else
                return unitTrapOrLocal;
    }
    
    /**
     * Optimizes residues, i.e. we exchange for example AlwaysMatch &amp;&amp; R by just R. 
     */
    public void optimizeResidues() {
        for( Iterator clIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
            final AbcClass cl = (AbcClass) clIt.next();
            for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
                final SootMethod method = (SootMethod) methodIt.next();

                MethodAdviceList adviceList=abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getAdviceList(method);
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
    
    /**
     * Resets the (hopefully) complete state of the weaver and inliner.
     */
    public void resetForReweaving() {
        WeavingState.reset();
    	AroundWeaver.reset();
    	AdviceInliner.reset();
        // reset all residues
        for( Iterator clIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
            final AbcClass cl = (AbcClass) clIt.next();
            for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
                final SootMethod method = (SootMethod) methodIt.next();

                MethodAdviceList adviceList=abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getAdviceList(method);
                if (adviceList!=null) {
                    Iterator appIt=adviceList.allAdvice().iterator();
                    while (appIt.hasNext()) {
                        AdviceApplication appl=(AdviceApplication)appIt.next();
                        appl.setResidue(appl.getResidue().resetForReweaving());
                    }
                }
            }
        }
        // reset all advice
        for( Iterator adIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getAdviceDecls().iterator(); adIt.hasNext(); ) {
            final AbstractAdviceDecl ad = (AbstractAdviceDecl) adIt.next();
            ad.resetForReweaving();
        }
    }

        /**
         * High level routine which performs the whole weaving.
         */
        public void weave() {
            // add aspectOf(), hasAspect(), ...
            weaveGenerateAspectMethods();
            //inline preinitialization etc.
            inlineConstructors();

            final List reweavingPasses = abc.main.Main.v().getAbcExtension().getReweavingPasses();
                        
            if( abc.main.Debug.v().optimizeResidues ) {
                //optimize residues, i.e. AlwaysMatch && R -> R, etc.
                optimizeResidues();
            }
            
            ReweavingPass lastPass = null; 
            
            //if we do reweaving
            if(reweavingPasses.size() > 0) {
                //no warning output during reweaving
            	Main.v().getAbcExtension().suspendErrorReporting();
            	
                //store the unwoven state first
                unweaver = new Unweaver();
                unweaver.save();
                storeBindings(unweaver);
                //then do the initial weaving
                weaveAdvice();
                
                //for all reweaving passes
                for (Iterator iter = reweavingPasses.iterator(); iter.hasNext();) {                    
                    ReweavingPass pass = (ReweavingPass) iter.next();                    
                    //perform the reweaving analysis
                    boolean reweaveNow = pass.analyze();

                    if(ResidueBox.wasAnyResidueChanged()) {
                        //optimize the residues if any was changed
                        optimizeResidues();
                    }
                    
                    boolean isLastPassInList = !iter.hasNext();
                    
                    //we need to reweave now
                    if(reweaveNow) { 
                        
                        //restore the weaving state again (analysis could have tampered with it?)
                        storeBindings(unweaver);
                        //reset weaver for reweaving
                        resetForReweaving();
                        //do stuff immediately prior to reweaving
                        pass.setupWeaving();
                        //if this is the lass pass in the list, we reweave below anyway, so
                        //do not reweave here but just store a handle so that it can tear down properly in the end
                        if(!isLastPassInList) {
                            //recompute shadow points
                            inlineConstructors();
	                        //reweave
	                        weaveAdvice();
	                        //do stuff immediately prior after reweaving
	                        pass.tearDownWeaving();
                        } else {
                        	lastPass = pass;
                        }
                    }
                }

                if(!abc.main.Debug.v().dontWeaveAfterAnalysis) {
                    //restore the weaving state again (analysis could have tampered with it?)
                    storeBindings(unweaver);
                    
                    //reset for the final weaving step
                    resetForReweaving();

                    //reenable the error queue
                    Main.v().getAbcExtension().resumeErrorReporting();
                    
                    inlineConstructors();

                    Debug.v().cleanupAfterAdviceWeave = true;
                }
            }
            
            if( abc.main.Debug.v().optimizeResidues ) {
                optimizeResidues();
            }

            if(!abc.main.Debug.v().dontWeaveAfterAnalysis) {
                //don't forget to process declare warning/errors;
                //we don't do this earlier, cause the analyses can possibly
                //support declare warning/error through static analysis
                reportMessages();
                
                //remove declare warnings/errors;
                //this must be done before the last weaving to get
                //the right bytecode
                removeDeclareWarnings();
                
                //do the final weaving
                weaveAdvice();        
            }
            
            //let the last pass tear down
            if(lastPass!=null) {
            	lastPass.tearDownWeaving();
            }
            
            //allow all reweaving passes to clean up memory etc.
            for (Iterator iter = reweavingPasses.iterator(); iter.hasNext();) {                    
                ReweavingPass pass = (ReweavingPass) iter.next();
                pass.cleanup();
            }
        }
        
		public void doInlining() {
        	Scene.v().releaseActiveHierarchy();
        	
        	if (OptionsParser.v().around_inlining() || OptionsParser.v().before_after_inlining())          
            	runInliner(); // needs to be called after exception checking

            
        }
        
        public void runInliner() {
        	AdviceInliner.v().run();
        	/*InterprocConstantPropagator.inlineConstantArguments();
        	List l=new LinkedList(AroundInliner.v().adviceMethodsNotInlined);
        	for( Iterator mIt = l.iterator(); mIt.hasNext(); ) {
        	    final SootMethod m = (SootMethod) mIt.next();
        		AroundInliner.v().transform(m.getActiveBody(), 1);
        	}*/
        	
        }        

        public void runBoxingRemover() {
        	AdviceInliner.v().runBoxingRemover();
        }        
        
        /**
         * Inlines constructors of all weavable classes with initialization and preinitialization advice. 
         */
        public void inlineConstructors() {
            ShadowPointsSetter sg = new ShadowPointsSetter(unitBindings);
            for( Iterator clIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
                final AbcClass cl = (AbcClass) clIt.next();
                SootClass scl = cl.getSootClass();
                sg.setShadowPointsPass1(scl);
                ConstructorInliner.inlineConstructors(scl);
                sg.setShadowPointsPass2(scl);
            }
        }
        
        /**
         * Fills in stubs for aspect methods generated by the frontend. (e.g. aspectOf)  
         */
        public void weaveGenerateAspectMethods() {
                // Generate methods inside aspects needed for code gen and bodies of
                //   methods not filled in by front-end (i.e. aspectOf())
                debug("Generating extra code in aspects");
                for( Iterator asIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getAspects().iterator(); asIt.hasNext(); ) {
                    final Aspect as = (Aspect) asIt.next();
                        ag.fillInAspect(as);
                }

                AbcTimer.mark("Add aspect code");
                abc.main.Debug.phaseDebug("Add aspect code");

        }
        
        /**
         * Adds the error and warning messages generated by "declare error" and "declare warning"
         * to the error queue.
         */
        public void reportMessages() {
            for( Iterator clIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
                final AbcClass cl = (AbcClass) clIt.next();
                for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
                    final SootMethod method = (SootMethod) methodIt.next();
                    if( !method.isConcrete() ) continue;
                    MethodAdviceList adviceList = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getAdviceList(method);
                    if(adviceList == null) continue;
                    for( Iterator aaIt = adviceList.allAdvice().iterator(); aaIt.hasNext(); ) {
                        final AdviceApplication aa = (AdviceApplication) aaIt.next();
                        aa.reportMessages();
                    }
                }
            }
        }
        
        /**
         * Changes the residues of all "declare warnings" and "declare errors" to NeverMatch.
         * TODO why do we actually need this? 
         */
        public void removeDeclareWarnings() {
            if(Debug.v().weaveDeclareWarning) return;
            for( Iterator clIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
                final AbcClass cl = (AbcClass) clIt.next();
                for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
                    final SootMethod method = (SootMethod) methodIt.next();
                    if( !method.isConcrete() ) continue;
                    MethodAdviceList adviceList = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getAdviceList(method);
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
        
        /**
         * Performs the actual advice weaving.
         */
        public void weaveAdvice() {
                PointcutCodeGen pg = new PointcutCodeGen();
                GenStaticJoinPoints gsjp = new GenStaticJoinPoints();

                for( Iterator clIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {

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

                if (abc.main.Debug.v().cleanupAfterAdviceWeave)
                for( Iterator clIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
                    final AbcClass cl = (AbcClass) clIt.next();
                    for( Iterator mIt = cl.getSootClass().getMethods().iterator(); mIt.hasNext(); ) {
                        final SootMethod m = (SootMethod) mIt.next();
                        if( !m.hasActiveBody() ) continue;
                        Body b = m.getActiveBody();
                        CopyPropagator.v().transform(b);
                        ConstantPropagatorAndFolder.v().transform(b);
			// This has been observed to remove nops as well as dead assignments
                        DeadAssignmentEliminator.v().transform(b);
                        UnusedLocalEliminator.v().transform(b);
                    }
                }
                AbcTimer.mark("Weaving advice");
                abc.main.Debug.phaseDebug("Weaving advice");
        } // method weave

		protected void storeBindings(Unweaver unweaver) {
			unitBindings = unweaver.restore();			
			reverseUnitBindings = new HashMap();
			for (Iterator entryIter = unitBindings.entrySet().iterator(); entryIter.hasNext();) {
				Entry entry = (Entry) entryIter.next();
				reverseUnitBindings.put(entry.getValue(), entry.getKey());
			}
		}

		/**
		 * @return the unweaver
		 */
		public Unweaver getUnweaver() {
			return unweaver;
		}
		
} // class Weaver
