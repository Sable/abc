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

import java.text.NumberFormat;

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

    public boolean parserTrace=false;
    
    public boolean aspectInfo=false;
    public boolean precedenceRelation=false;

    // Follow ajc in not generating adviceDecls with statically false pointcuts;
    // may suppress some errors in the presence of && if(false);
    public boolean lazyAdviceGeneration = false;
    
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
    //warning: if disabled, this might lead to Jimple code that is not necessarily valid!
    //warning: if reweaving is enabled, this currently is always set to TRUE for the last reweaving pass!
    public boolean cleanupAfterAdviceWeave=true; 
    public boolean beforeWeaver=false;
    public boolean afterReturningWeaver=false;
    public boolean afterThrowingWeaver=false;
    public boolean aroundWeaver=false;

    // Removes duplicate methods caused by around weaving/inlining
    public boolean removeDupAroundMethods = true;

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
    public boolean instanceOfEliminator = true;
    public boolean traceInstanceOfEliminator = false;
    public boolean assumeNoDynamicLoading = false; // tells the InstanceOfEliminator to ignore
    					// the possibility of dynamic class loading introducing new types.
    
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

    
    /* This flag does not really have any effect any more, since residues are now already
     * optimized during construction. The only time where they might need to be re-optimized
     * is if a static analysis modifies a ResidueBox, which is then done automatically
     * and regardless of this flag.
     */
    public boolean optimizeResidues = false;
    public boolean constructorInliner = false;

    public boolean countCflowStacks = false;

    public boolean noContainsPointcut = false; //when enabled, disabled contains-pointcut 
    
    // Config stuff - to be moved, possibly (FIXME)
    public boolean ajcCompliance=true; // strict ajc compliance
    public boolean ajc120Compliance=true; // compliance with ajc 1.2.0 and before
    public boolean java13=false; // java 1.3 semantics
    public boolean java15=false; // java 1.5 semantics

    // temporary tracematch stuff
    public boolean noNegativeBindings = false;
    public boolean printIndices = false;
    public boolean useNFA = false;
    public boolean useIndexing = true;              // made useIndexing the default
    public boolean originalIndexChoosing = true;
    public boolean onlyStrongRefs = false; 			//makes all references strong references 
    public boolean noCollectableWeakRefs = false;	//makes all collectable weak refs "normal" weak refs
    public boolean useCommonsCollections = false;   // Determine whether to use the builtin runtime maps or
    												// the maps from the commons collections for TM indexing
    public boolean generateLeakWarnings = true;

    // make every weakref used for tracematches have the same hashcode
    public boolean clashHashCodes = false;

    // whether or not to turn on the ITD optimisation for tracematches
    public boolean useITDs = false;

    //temporary openmod stuff
    public boolean omPrecedenceDebug = false;
    public boolean omCollectModuleAspectsDebug = false;
    public boolean omMatchingDebug = false;
    public boolean omASTPrintDebug = false;
    public boolean omOpenClassParentDebug = false;
    public boolean omOpenClassITDDebug = false;
    public boolean omNormalizeDebug = false;

    //restructuring of synchronized methods for lock/unlock pointcuts;
    //currently generates synchronized blocks which dava cannot deal with
    public boolean enableLockPointcuts = false;
    
    //enable pointcut maybeShared() (backed by ThreadLocalObjectsAnalysis)
    public boolean enableMaybeSharedPointcut = true;
   
    //enable optimization of pointcut maybeShared()
	public boolean optimizeMaybeSharedPointcut = false;

	// Avoid clobbering the variable namespace with 'global'
    public boolean noGlobalPointcut = false;
    
    // To compile java1.4 code in a java5 environment, you sometimes need to allow
    // covariant return types (since the API uses them).
    public boolean allowCovariantReturn = false;
    
    // Suppress AspectJ keywords
    public boolean pureJava = false;
    
    //verbose mode for static tracematch analysis
    public boolean debugTmAnalysis = false;
    //dump shadow statistics for static tracematch optimization 
	public boolean tmShadowStatistics = false;
	//use CSV format for statistics
	public boolean csv = false;
	//dump shadows as they are disabled
	public boolean tmShadowDump = false;
	//enable dynamic instrumentation
	public boolean dynaInstr = false;
	//enable shadow counting
	public boolean shadowCount = false;
	
    //output potential points of failure in PFG files    
    public boolean outputPFGs = false;
    //output clickable HTML graphs
    public boolean outputHTML = false;
    

    public static void phaseDebug(String s) {
        if( Debug.v().debugPhases ) { 
        	String m="Done phase: "+s;        	
        	if (Debug.v().debugMemUsage) {
        		System.gc();        		
        		long bytes=(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        		
        		
        		NumberFormat numberFormatter=NumberFormat.getNumberInstance();       		
        	
        		String mem= numberFormatter.format(bytes) + " used. " + 
					numberFormatter.format(Runtime.getRuntime().totalMemory()) + " heap.";
        		int padding=79-m.length()-mem.length();
        		String p="";
        		while(padding-->0)
        			p+=" ";
        		m=m+ p + mem;
        	}
        	System.err.println(m);
        }
    }

}
