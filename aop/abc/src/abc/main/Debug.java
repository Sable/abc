/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
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

package abc.main;

/** A class for storing debug flags. Default values go here;
 *  if you make a MyDebug class you can override them in the constructor
 *  there, or you can use -debug and -nodebug on the commandline
 *  @author Ganesh Sittampalam
*/
public class Debug {
    public static Debug v;
    static {
    	loadDebug();
    }
    
    private static void loadDebug() {
        try {
            v=(Debug)
                (ClassLoader.getSystemClassLoader()
                .loadClass("abc.main.MyDebug")
                 .newInstance());
        } catch(ClassNotFoundException e) {
            v=new Debug();
        } catch(Exception e) {
            System.err.println("Unknown failure trying to instantiate custom debug instance");
            v=new Debug();
        }
    }
    
    public static Debug v() {
        return v;
    }
    
    public static void reset() {
    	loadDebug();
    }


    public boolean aspectInfo=false;
    public boolean precedenceRelation=false;

    public boolean patternMatches=false;
    public boolean namePatternMatches=false;
    public boolean namePatternProcessing=false;
    public boolean declareParents=false;
    public boolean classKinds=false;
    public boolean classResolving=false;
    public boolean sootClassToClassType=false;

    public boolean showWeavableClasses=false;

    public boolean showNormalizedPointcuts=false;
    public boolean showPointcutMatching=false;

    public boolean debugPointcutNormalization=false;
    public boolean abstractPointcutLookup=false;

    public boolean printAdviceApplicationCount=false;
    public boolean matcherTest=false; // Print out the results of the matcher for regression testing etc
    public boolean matcherWarnUnimplemented=false; // Warn about unimplemented stuff in the matcher
                                                   // or things it sees (like patterns)
    public boolean traceMatcher=false;

    // Make thisJoinPoint be of type Object (but still construct a new one at each joinpoint),
    // making it useful only for comparing identity of joinpoints.
    public boolean thisJoinPointObject = false; 

    // Make thisJoinPoint be constructed using new DummyJP() (you must supply an implementation of
    // this class as part of the compile, or you will get an exception during compilation), again making it useful only for comparing identity, but with less type safety.
    public boolean thisJoinPointDummy = false; 

    // Weaver
    public boolean testITDsOnly = false; // will skip advice weaving

    public boolean weaverDriver=false;  // main driver for weaver
    public boolean unweaver=false;  // saving and restoring jimple bodies
    public boolean cflowAnalysis=false;  // interprocedural analysis of cflows
    public boolean cflowAnalysisStats=false;  // statistics from cflowinter

    public boolean aspectCodeGen=false; // inserting stuff into aspect class
    public boolean genStaticJoinPoints=false; // collect and gen SJP
    public boolean shadowPointsSetter=false; // collect shadow points
    public boolean pointcutCodeGen=false; // main pointcut generator
    public boolean printAdviceInfo=false; // dumps advice info
    public boolean residueCodeGen=false;
    public boolean tagWeavingCode=false; // add tags to some weaving statements
                                         // (e.g. nops) to aid debugging
    public boolean tagResidueCode=false; // add tags to some residue statements
                                         // to help with debugging
    public boolean instructionTagger = false; // print info about instruction
                                              // tagging
    public boolean printReweavingAnalysisTimeouts = true;   //print error message when
                                                            //a reweaving anslysis times out
    public boolean disableReweavingAnalysisTimeouts = false;//disable timeouts for reweaving analyses

    // Cleans up code (copy propagation, nop removal, dead code removal, etc) 
    // immediately after weaving advice
    public boolean cleanupAfterAdviceWeave=true; 
    public boolean beforeWeaver=false;
    public boolean afterReturningWeaver=false;
    public boolean afterThrowingWeaver=false;
    public boolean aroundWeaver=false;
    public boolean restructure=false; // restructuring utilities
    public boolean abcTimer=false;
    public boolean polyglotTimer=false;
    public boolean sootResolverTimer=false;
    public boolean timerTrace=false; // print time of phases as they run
    public boolean showArgsMatching=false;
    public boolean showBinds=false;
    public boolean showAdviceFormalSets=false;
    public boolean warnUntaggedSourceInfo=false;
    public boolean adviceInliner=false;
    public boolean aroundInliner = false;
    public boolean afterBeforeInliner=false;

    public boolean disableAspectOfOpt=false;
    public boolean disableDuplicatesRemover=false;
    
    public boolean unusedMethodsRemover=false;
    public boolean interprocConstantPropagator=false;
    public boolean boxingRemover=false;
    
    public boolean farJumpEliminator=false; 
    
    public boolean doValidate=false;  // validate jimple
    // dump CFG as dot plot, doValidate must also be true to enable it
    public boolean doValidateDumpCFG=false;


    public boolean dontCheckExceptions=false;

    public boolean debugCflowSharing=false;
    public boolean debugPointcutUnification=false;
    public boolean debugUnweaver=false; 
    public boolean dontWeaveAfterAnalysis=false;
    public boolean checkCflowOpt=false;
    public boolean dontRemovePushPop=false;
    public boolean nullCheckElim=true;
    public boolean allowDynamicTests = false;
    public boolean switchFolder = false;
    
    public boolean forceSingleThreadedCflow = false;
    public boolean forceStaticFieldCflow = false;
    
    public boolean traceAntTask=false;

    public boolean printWeavableClasses = false;
    public boolean debugPhases = false;
    public boolean debugMemUsage = false;

    public boolean weaveDeclareWarning = false;
    public boolean messageWeaver = false;

    public boolean warnPrecAmbiguity = false;
    
    // dump each advice application just before weaving it
    public boolean dumpAAWeave = false;

    public boolean optimizeResidues = false;
    public boolean constructorInliner = false;

    public boolean countCflowStacks = false;

    public boolean noContainsPointcut = false; //when enabled, disabled contains-pointcut 
    
    // Config stuff - to be moved, possibly (FIXME)
    public boolean ajcCompliance=true; // strict ajc compliance
    public boolean ajc120Compliance=true; // compliance with ajc 1.2.0 and before
    public boolean java13=false; // java 1.3 semantics

    // temporary tracematch stuff
    public boolean noNegativeBindings = false;
    public boolean printIndices = false;
    public boolean useNFA = false;
    public boolean useIndexing = true;              // made useIndexing the default
    public boolean onlyStrongRefs = false; 			//makes all references strong references 
    public boolean noCollectableWeakRefs = false;	//makes all collectable weak refs "normal" weak refs
    public boolean useCommonsCollections = false;   // Determine whether to use the builtin runtime maps or
    												// the maps from the commons collections for TM indexing
}
