package abc.main;

/** A class for storing debug flags. Default values go here;
    if you make a MyDebug class you can override them in the constructor
    there.

    @author Ganesh Sittampalam
*/
public class Debug {
    public static Debug v;
    static {
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


    public boolean aspectInfo=false;
    public boolean precedenceRelation=false;

    public boolean patternMatches=false;
    public boolean namePatternMatches=false;
    public boolean namePatternProcessing=false;
    public boolean declareParents=false;
    public boolean classKinds=false;
    public boolean classResolving=false;
    public boolean sootClassToClassType=false;

    public boolean showNormalizedPointcuts=false;
    public boolean showPointcutMatching=false;

    public boolean debugPointcutNormalization=false;

    public boolean matcherTest=false; // Print out the results of the matcher for regression testing etc
    public boolean matcherWarnUnimplemented=false; // Warn about unimplemented stuff in the matcher
                                                   // or things it sees (like patterns)
    public boolean traceMatcher=false;

    // Weaver
    public boolean testITDsOnly = false; // will skip advice weaving
    
    public boolean weaverDriver=false;  // main driver for weaver
    public boolean unweaver=false;  // saving and restoring jimple bodies
    public boolean cflowAnalysis=false;  // analysis of cflows
    public boolean aspectCodeGen=false; // inserting stuff into aspect class
    public boolean genStaticJoinPoints=false; // collect and gen SJP
    public boolean shadowPointsSetter=false; // collect shadow points
    public boolean pointcutCodeGen=false; // main pointcut generator
    public boolean printAdviceInfo=false; // dumps advice info
    public boolean residueCodeGen=false;
    public boolean beforeWeaver=false;
    public boolean afterReturningWeaver=false;
    public boolean afterThrowingWeaver=false;
    public boolean aroundWeaver=false;
    public boolean restructure=false; // restructuring utilities
    public boolean abcTimer=true;
    public boolean polyglotTimer=false;
    public boolean sootResolverTimer=false;
    public boolean timerTrace=false; // print time of phases as they run
    public boolean showArgsMatching=false;
    public boolean showBinds=false;
    public boolean showAdviceFormalSets=false;
    public boolean warnUntaggedSourceInfo=true;


    public boolean doValidate=false;  // validate jimple
    // dump CFG as dot plot, doValidate must also be true to enable it
    public boolean doValidateDumpCFG=false; 


    public boolean dontCheckExceptions=false;

    public boolean dontUseCflowCounter=false;

    public boolean dontShareCflowStacks=false;

    // Config stuff - to be moved, probably (FIXME)
    public boolean ajcCompliance=true; // strict ajc compliance
}
