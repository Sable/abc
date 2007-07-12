/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
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
package abc.tm.weaving.weaver.tmanalysis;

import java.util.List;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.toolkits.scalar.UnusedLocalEliminator;
import abc.main.AbcTimer;
import abc.main.Debug;
import abc.main.Main;
import abc.soot.util.OptimizedNullCheckEliminator;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.stages.CallGraphAbstraction;
import abc.tm.weaving.weaver.tmanalysis.stages.FlowInsensitiveAnalysis;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger;
import abc.tm.weaving.weaver.tmanalysis.util.Statistics;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.weaver.AbstractReweavingAnalysis;

/**
 * A reweaving analysis that executes the flow-insensitive analysis
 * as described in our ECOOP 2007 paper.
 * @author Eric Bodden
 */
public class OptFlowInsensitiveAnalysis extends AbstractReweavingAnalysis {

	protected TMGlobalAspectInfo gai;
	
    public boolean analyze() {
    	gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();

		//nothing to do?
    	if(gai.getTraceMatches().size()==0 ||
    	   !ShadowRegistry.v().enabledShadowsLeft()) {
    		return false;
    	}

    	runIntraProcOptimizations();
    	
        AbcTimer.mark("Intrap. optimizations to ensure correctness of weaving");

        try {
    		doAnalyze();
    	} catch (Error e) {
    		Statistics.errorOccured = true;
    		throw e;
    	} catch (RuntimeException e) {
    		Statistics.errorOccured = true;
    		throw e;
    	}
    	
		//we do not need to reweave right away
        return false;
    }

    /**
     * Runs intraprocedural optimizations after weaving. Those are necessary for soundness.
     * We do <i>not</i> run {@link ConstantPropagatorAndFolder} nor {@link DeadAssignmentEliminator} here,
     * since that would potentially eliminate {@link Local}s that we need for variable bindings in the
     * tracematch analysis.
     */
    protected void runIntraProcOptimizations() {
        /*
         * NOTE TO SELF: Must NOT use UnconditionalBranchFolder nor DeadAssignmentEliminator here,
         * as this would undo the efforts by TMLoopExitRestructurer. 
         */        
        GlobalAspectInfo gai = Main.v().getAbcExtension().getGlobalAspectInfo();
        for (AbcClass abcClass : (Set<AbcClass>)gai.getWeavableClasses()) {
            SootClass sc = abcClass.getSootClass();
            for (SootMethod m : sc.getMethods()) {
                if(m.hasActiveBody()) {
                    Body b = m.getActiveBody();
                    CopyPropagator.v().transform(b);            	//probably not strictly necessary
                    ConstantPropagatorAndFolder.v().transform(b);
                    new OptimizedNullCheckEliminator().transform(b);//mostly for better readability of code during debugging
                    UnreachableCodeEliminator.v().transform(b);		//necessary for soundness
                    UnusedLocalEliminator.v().transform(b);     	//probably not strictly necessary
                    if(Debug.v().doValidate)
                        b.validate();
                }
            }
        }
        
    }

    /**
	 * Performs the actual analysis.
	 */
	protected void doAnalyze() {
		
		//tag shadows in Jimple 
		TMShadowTagger.v().apply();

		AbcTimer.mark("Tag shadows");
		
        //build the abstracted call graph
        CallGraphAbstraction.v().apply();

    	AbcTimer.mark("Build and abstract call graph");

    	if(!ShadowRegistry.v().enabledShadowsLeft()) {
    		return;
    	}

		FlowInsensitiveAnalysis.v().apply();

    	AbcTimer.mark("Flow-insensitive analysis");    	
    	
    	ShadowRegistry.v().disableAllUnneededSomeSyncAndBodyAdvice();

    	AbcTimer.mark("Disabling helper advice");    	
	}
	
	/** 
     * {@inheritDoc}
     */
    public void defaultSootArgs(List sootArgs) {
        //keep line numbers
        sootArgs.add("-keep-line-number");
    	//enable whole program mode
        sootArgs.add("-w");
        //disable all packs we do not need
        sootArgs.add("-p");
        sootArgs.add("wjtp");
        sootArgs.add("enabled:false");
        sootArgs.add("-p");
        sootArgs.add("wjop");
        sootArgs.add("enabled:false");
        sootArgs.add("-p");
        sootArgs.add("wjap");
        sootArgs.add("enabled:false");
        
    	//enable points-to analysis
        sootArgs.add("-p");
        sootArgs.add("cg");
        sootArgs.add("enabled:true");

        //enable Spark
        sootArgs.add("-p");
        sootArgs.add("cg.spark");
        sootArgs.add("enabled:true");

        //use on-demand points-to analysis within Spark
        sootArgs.add("-p");
        sootArgs.add("cg.spark");
        sootArgs.add("cs-demand:true");

        //in order to generate points-to sets for weaving variables, we have to
        //disable the straightlinecode optimizations which take place right
        //after weaving;
        //will be reset by Weaver after the analysis
        Debug.v().cleanupAfterAdviceWeave = false;
    }
    
}
