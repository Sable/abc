/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
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

package abc.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Trap;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.tagkit.Host;
import soot.util.Chain;
import abc.aspectj.ExtensionInfo;
import abc.aspectj.parse.AbcLexer;
import abc.aspectj.parse.LexerAction_c;
import abc.aspectj.parse.PerClauseLexerAction_c;
import abc.aspectj.parse.sym;
import abc.main.options.OptionsParser;
import abc.soot.util.CastRemover;
import abc.soot.util.FarJumpEliminator;
import abc.soot.util.InstanceOfEliminator;
import abc.soot.util.OptimizedNullCheckEliminator;
import abc.soot.util.SwitchFolder;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.CflowSetup;
import abc.weaving.aspectinfo.DeclareMessage;
import abc.weaving.aspectinfo.DeclareSoft;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.aspectinfo.PerCflowSetup;
import abc.weaving.aspectinfo.PerTargetSetup;
import abc.weaving.aspectinfo.PerThisSetup;
import abc.weaving.matching.AbcSJPInfo;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.ClassInitializationShadowMatch;
import abc.weaving.matching.ConstructorCallShadowType;
import abc.weaving.matching.ExecutionShadowType;
import abc.weaving.matching.GetFieldShadowType;
import abc.weaving.matching.HandlerShadowType;
import abc.weaving.matching.InterfaceInitializationShadowMatch;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.matching.MethodCallShadowType;
import abc.weaving.matching.NewStmtMethodPosition;
import abc.weaving.matching.PreinitializationShadowType;
import abc.weaving.matching.SJPInfo;
import abc.weaving.matching.SetFieldShadowType;
import abc.weaving.matching.ShadowType;
import abc.weaving.matching.StmtMethodPosition;
import abc.weaving.matching.TrapMethodPosition;
import abc.weaving.matching.WholeMethodPosition;
import abc.weaving.matching.AdviceApplication.ResidueConjunct;
import abc.weaving.weaver.AbstractReweavingAnalysis;
import abc.weaving.weaver.AdviceInliner;
import abc.weaving.weaver.CflowCodeGenUtils;
import abc.weaving.weaver.ReweavingAnalysis;
import abc.weaving.weaver.ReweavingPass;
import abc.weaving.weaver.Weaver;
import abc.weaving.weaver.ReweavingPass.ID;

/**
 * This class should be sub-classed to extend the behaviour of abc
 * <p>
 * A sub-class, with overriden methods to effect some new behaviour,
 * can be loaded at runtime by using the "-ext" switch to abc.
 *
 * @author Julian Tibble
 * @author Eric Bodden
 */
public class AbcExtension
{
    private static final ID PASS_CFLOW_ANALYSIS = new ID("CFlow analysis");
    private static final ID PASS_DEBUG_UNWEAVER = new ID("Debug Unweaver");
    
    private GlobalAspectInfo globalAspectInfo = null;
    private Weaver weaver = null;
    private CompileSequence compileSequence = null;
    private List<ReweavingPass> reweavingPasses;
    
	/** If true, error reporting is suspended. */
	private boolean suspendErrorReporting;

    /**
     * Constructs a version string for all loaded extensions
     */
    final public String versions()
    {
        StringBuffer versions = new StringBuffer();
        collectVersions(versions);
        return versions.toString();
    }

    /**
     * Override this method to add the version information
     * for this extension, calling the same method in the
     * super-class to ensure that all extensions are
     * reported.
     */
    protected void collectVersions(StringBuffer versions)
    {
        versions.append("abc version " +
                        new abc.aspectj.Version().toString() +
                        "\n");
    }

    public final CompileSequence getCompileSequence() {
    	if(compileSequence == null)
    		compileSequence = createCompileSequence();
    	return compileSequence;
    }

	/**
	 * Creates the unique compile sequence for this extension.
	 * This method is called exactly once.
	 */
	protected CompileSequence createCompileSequence() {
		return new CompileSequence(this);
	}

    /**
     * Reports an error, if error reporting is not suspended.
     * @see #suspendErrorReporting()
     * @see #resumeErrorReporting()
     */
    public void reportError(ErrorInfo ei) {
    	if(!suspendErrorReporting)
    		getErrorQueue().enqueue(ei);
    }
    
    /**
     * Reports an error, if error reporting is not suspended.
     * @see #suspendErrorReporting()
     * @see #resumeErrorReporting()
     */
    public void reportError(int level, String s, Position pos) {    	
    	if(!suspendErrorReporting)
    		forceReportError(level, s, pos);
    }
    
    /**
     * Reports an error, even if error reporting is suspended.
     * @see #suspendErrorReporting()
     * @see #resumeErrorReporting()
     */
	public void forceReportError(int level, String message, Position position) {
		getErrorQueue().enqueue(level, message, position);
	}

	
	/**
     * Creates an instance of the <code>ExtensionInfo</code> structure
     * used for extending the Polyglot-based frontend.
     */
    public ExtensionInfo makeExtensionInfo(Collection<String> jar_classes,
                                            Collection<String> aspect_sources)
    {
        return new abc.aspectj.ExtensionInfo(jar_classes, aspect_sources);
    }

    /**
     * Returns the GlobalAspectInfo structure, which stores all the
     * AspectJ-specific information from the frontend for use in the
     * backend.
     */
    final public GlobalAspectInfo getGlobalAspectInfo()
    {
        if (globalAspectInfo == null)
            globalAspectInfo = createGlobalAspectInfo();

        return globalAspectInfo;
    }

    /**
     * Override this to create a custom global aspect info.
     */
    protected GlobalAspectInfo createGlobalAspectInfo() {
        return new GlobalAspectInfo();
    }

    /**
     * Returns the Weaver object, which co-ordinates everything that
     * happens in the backend.
     */
    final public Weaver getWeaver()
    {
        if (weaver == null)
            weaver = createWeaver();

        return weaver;
    }

    /**
     * Override this to create a custom weaver.
     */
    protected Weaver createWeaver() {
        return new Weaver();
    }

    /**
     *  Make a new AdviceInliner, which is responsible for inlining
     *  advice bodies, as well as pointcuts which are implemented by 
     *  methods, such as if.
     *  The instance is cached in abc.weaving.weaver.AdviceInliner,
     *  so we don't do so here like we do with the Weaver.
     */
    public AdviceInliner makeAdviceInliner()
    {
	return new AdviceInliner();
    }
    

    /**
     * Get all the shadow joinpoints that are matched
     * in this extension of AspectJ
     */
    final public Iterator<ShadowType> shadowTypes()
    {
        return listShadowTypes().iterator();
    }

    /**
     * Override this method to add new joinpoints to the abc.
     * Call the same method in the super-class to ensure
     * the standard joinpoints needed are loaded too.
     */
    protected List<ShadowType> listShadowTypes()
    {
        List<ShadowType> shadowTypes = new LinkedList<ShadowType>();

        shadowTypes.add(new ConstructorCallShadowType());
        shadowTypes.add(new ExecutionShadowType());
        shadowTypes.add(new GetFieldShadowType());
        shadowTypes.add(new HandlerShadowType());

        // the next two lines show the preferred method of doing this
        // i.e. without creating the extra *ShadowType class
        // FIXME: make all of the join point matching classes like this
        shadowTypes.add(ClassInitializationShadowMatch.shadowType());
        shadowTypes.add(InterfaceInitializationShadowMatch.shadowType());

        shadowTypes.add(new MethodCallShadowType());
        shadowTypes.add(new PreinitializationShadowType());
        shadowTypes.add(new SetFieldShadowType());

        return shadowTypes;
    }

    /** This method adds optimisation passes specificially
     *  required for abc.
     */
    public void addJimplePacks() {
    	 
    	 
		PackManager.v().getPack("jtp").add(new Transform("jtp.uce", UnreachableCodeEliminator.v()));		
		
		//PackManager.v().getPack("jtp").add(new Transform("jtp.sf", SwitchFolder.v()));
		
		
		if (Debug.v().nullCheckElim) {
			// want this to run before Dead assignment eliminiation
			PackManager.v().getPack("jop").insertBefore(new Transform("jop.nullcheckelim", new OptimizedNullCheckEliminator()), "jop.dae");
		}

		if (Debug.v().switchFolder) {
			// must be inserted somewhere before the unreachable code eliminator
			PackManager.v().getPack("jop").insertBefore(new Transform("jop.sf", SwitchFolder.v()), "jop.uce1");
		}
		
		if (Debug.v().instanceOfEliminator) {
			// add before constant propagator and folder, which crucially is before nullcheckelim
			PackManager.v().getPack("jop").insertBefore(new Transform("jop.ioe", InstanceOfEliminator.v()), "jop.cpf");
		}
		
		if (OptionsParser.v().around_inlining() || OptionsParser.v().before_after_inlining()) {
			PackManager.v().getPack("jop").insertAfter(new Transform("jop.cr", CastRemover.v()), "jop.dae");
		
			// make this the very last pass after all optimizations
			PackManager.v().getPack("jop").insertAfter(new Transform("jop.fje", FarJumpEliminator.v()), "jop.ule");
		}
		
		//if (Debug.v().aroundInliner) {
		//	PackManager.v().getPack("jop").add(new Transform("jop.aroundinliner", AroundInliner.v()));
		//}
	}
    
    /**
	 * Call Scene.v().addBasicClass for each runtime class that the backend
	 * might generate code for. Derived implementations should normally make
	 * sure to call the superclass implementation.
	 */
    public void addBasicClassesToSoot()
    {
    	CflowCodeGenUtils.addBasicClassesToSoot();
        Scene.v().addBasicClass("org.aspectbench.runtime.reflect.Factory",
                                SootClass.SIGNATURES);
        Scene.v().addBasicClass("org.aspectj.lang.JoinPoint",
                                SootClass.HIERARCHY);
        Scene.v().addBasicClass("org.aspectj.lang.JoinPoint$StaticPart",
                                SootClass.HIERARCHY);
        Scene.v().addBasicClass("org.aspectj.lang.SoftException",
                                SootClass.SIGNATURES);
        Scene.v().addBasicClass("org.aspectj.lang.NoAspectBoundException",
                                SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System",
                                SootClass.SIGNATURES);
    }

    /**
	 * Specify the class that will be used at runtime to generate
	 * StaticJoinPoint objects.
	 */
    public String runtimeSJPFactoryClass() {
        return "org.aspectbench.runtime.reflect.Factory";
    }

    /**
	 * Create a (compile-time) static join point information object that
         * generates code to initialize static join point fields with
         * reflective information about a join point.
	 */
    public SJPInfo createSJPInfo(String kind, String signatureTypeClass,
            String signatureType, String signature, Host host) {
        return new AbcSJPInfo(kind, signatureTypeClass, signatureType,
                signature, host);
    }

    /**
     * Initialise the HashMaps that define how keywords are handled in the different lexer states.
     *
     * Keywords are added by calling the methods addJavaKeyword(), addAspectJKeyword(),
     * lexer.addPointcutKeyword() and addPointcutIfExprKeyword(), which are defined in the Lexer_c
     * class. There are the utility methods lexer.addGlobalKeyword() (which adds its parameters to all
     * four states) and lexer.addAspectJContextKeyword() (which adds its parameters to the AspectJ and
     * PointcutIfExpr states).
     *
     * Each of these methods takes two arguments - a String (the keyword to be added) and a
     * class implementing abc.aspectj.parse.LexerAction defining what to do when this keyword is
     * encountered.
     *
     * @author pavel
     */
    public void initLexerKeywords(AbcLexer lexer) {
        lexer.addGlobalKeyword("abstract",      new LexerAction_c(new Integer(sym.ABSTRACT)));
        if(!abc.main.Debug.v().java13)
        	lexer.addGlobalKeyword("assert",        new LexerAction_c(new Integer(sym.ASSERT)));
        lexer.addGlobalKeyword("boolean",       new LexerAction_c(new Integer(sym.BOOLEAN)));
        lexer.addGlobalKeyword("break",         new LexerAction_c(new Integer(sym.BREAK)));
        lexer.addGlobalKeyword("byte",          new LexerAction_c(new Integer(sym.BYTE)));
        lexer.addGlobalKeyword("case",          new LexerAction_c(new Integer(sym.CASE)));
        lexer.addGlobalKeyword("catch",         new LexerAction_c(new Integer(sym.CATCH)));
        lexer.addGlobalKeyword("char",          new LexerAction_c(new Integer(sym.CHAR)));
        lexer.addGlobalKeyword("class",         new LexerAction_c(new Integer(sym.CLASS)) {
                            public int getToken(AbcLexer lexer) {
                                if(!lexer.getLastTokenWasDot()) {
                                    lexer.enterLexerState(lexer.currentState() == lexer.aspectj_state() ?
                                            lexer.aspectj_state() : lexer.java_state());
                                }
                                return token.intValue();
                            }
                        });
        lexer.addGlobalKeyword("const",         new LexerAction_c(new Integer(sym.CONST)));
        lexer.addGlobalKeyword("continue",      new LexerAction_c(new Integer(sym.CONTINUE)));
        lexer.addGlobalKeyword("default",       new LexerAction_c(new Integer(sym.DEFAULT)));
        lexer.addGlobalKeyword("do",            new LexerAction_c(new Integer(sym.DO)));
        lexer.addGlobalKeyword("double",        new LexerAction_c(new Integer(sym.DOUBLE)));
        lexer.addGlobalKeyword("else",          new LexerAction_c(new Integer(sym.ELSE)));
        lexer.addGlobalKeyword("extends",       new LexerAction_c(new Integer(sym.EXTENDS)));
        lexer.addGlobalKeyword("final",         new LexerAction_c(new Integer(sym.FINAL)));
        lexer.addGlobalKeyword("finally",       new LexerAction_c(new Integer(sym.FINALLY)));
        lexer.addGlobalKeyword("float",         new LexerAction_c(new Integer(sym.FLOAT)));
        lexer.addGlobalKeyword("for",           new LexerAction_c(new Integer(sym.FOR)));
        lexer.addGlobalKeyword("goto",          new LexerAction_c(new Integer(sym.GOTO)));
        // if is handled specifically, as it differs in pointcuts and non-pointcuts.
        //lexer.addGlobalKeyword("if",            new LexerAction_c(new Integer(sym.IF)));
        lexer.addGlobalKeyword("implements",    new LexerAction_c(new Integer(sym.IMPLEMENTS)));
        lexer.addGlobalKeyword("import",        new LexerAction_c(new Integer(sym.IMPORT)));
        lexer.addGlobalKeyword("instanceof",    new LexerAction_c(new Integer(sym.INSTANCEOF)));
        lexer.addGlobalKeyword("int",           new LexerAction_c(new Integer(sym.INT)));
        lexer.addGlobalKeyword("interface",     new LexerAction_c(new Integer(sym.INTERFACE),
                                            new Integer(lexer.java_state())));
        lexer.addGlobalKeyword("long",          new LexerAction_c(new Integer(sym.LONG)));
        lexer.addGlobalKeyword("native",        new LexerAction_c(new Integer(sym.NATIVE)));
        lexer.addGlobalKeyword("new",           new LexerAction_c(new Integer(sym.NEW)));
        lexer.addGlobalKeyword("package",       new LexerAction_c(new Integer(sym.PACKAGE)));
        lexer.addGlobalKeyword("private",       new LexerAction_c(new Integer(sym.PRIVATE)));
        /* ------------  keyword added to the Java part ------------------ */
        lexer.addGlobalKeyword("privileged",    new LexerAction_c(new Integer(sym.PRIVILEGED)));
        /* ------------  keyword added to the Java part ------------------ */
        lexer.addGlobalKeyword("protected",     new LexerAction_c(new Integer(sym.PROTECTED)));
        lexer.addGlobalKeyword("public",        new LexerAction_c(new Integer(sym.PUBLIC)));
        lexer.addGlobalKeyword("return",        new LexerAction_c(new Integer(sym.RETURN)));
        lexer.addGlobalKeyword("short",         new LexerAction_c(new Integer(sym.SHORT)));
        lexer.addGlobalKeyword("static",        new LexerAction_c(new Integer(sym.STATIC)));
        lexer.addGlobalKeyword("strictfp",      new LexerAction_c(new Integer(sym.STRICTFP)));
        lexer.addGlobalKeyword("super",         new LexerAction_c(new Integer(sym.SUPER)));
        lexer.addGlobalKeyword("switch",        new LexerAction_c(new Integer(sym.SWITCH)));
        lexer.addGlobalKeyword("synchronized",  new LexerAction_c(new Integer(sym.SYNCHRONIZED)));
        // this is handled explicitly, as it differs in pointcuts and non-pointcuts.
        //lexer.addGlobalKeyword("this",          new LexerAction_c(new Integer(sym.THIS)));
        lexer.addGlobalKeyword("throw",         new LexerAction_c(new Integer(sym.THROW)));
        lexer.addGlobalKeyword("throws",        new LexerAction_c(new Integer(sym.THROWS)));
        lexer.addGlobalKeyword("transient",     new LexerAction_c(new Integer(sym.TRANSIENT)));
        lexer.addGlobalKeyword("try",           new LexerAction_c(new Integer(sym.TRY)));
        lexer.addGlobalKeyword("void",          new LexerAction_c(new Integer(sym.VOID)));
        lexer.addGlobalKeyword("volatile",      new LexerAction_c(new Integer(sym.VOLATILE)));
        lexer.addGlobalKeyword("while",         new LexerAction_c(new Integer(sym.WHILE)));

        lexer.addPointcutKeyword("adviceexecution", new LexerAction_c(new Integer(sym.PC_ADVICEEXECUTION)));
        lexer.addPointcutKeyword("args", new LexerAction_c(new Integer(sym.PC_ARGS)));
        lexer.addPointcutKeyword("call", new LexerAction_c(new Integer(sym.PC_CALL)));
        lexer.addPointcutKeyword("cflow", new LexerAction_c(new Integer(sym.PC_CFLOW)));
        lexer.addPointcutKeyword("cflowbelow", new LexerAction_c(new Integer(sym.PC_CFLOWBELOW)));
        lexer.addPointcutKeyword("error", new LexerAction_c(new Integer(sym.PC_ERROR)));
        lexer.addPointcutKeyword("execution", new LexerAction_c(new Integer(sym.PC_EXECUTION)));
        lexer.addPointcutKeyword("get", new LexerAction_c(new Integer(sym.PC_GET)));
        lexer.addPointcutKeyword("handler", new LexerAction_c(new Integer(sym.PC_HANDLER)));
        lexer.addPointcutKeyword("if", new LexerAction_c(new Integer(sym.PC_IF),
                                    new Integer(lexer.pointcutifexpr_state())));
        lexer.addPointcutKeyword("initialization", new LexerAction_c(new Integer(sym.PC_INITIALIZATION)));
        lexer.addPointcutKeyword("parents", new LexerAction_c(new Integer(sym.PC_PARENTS)));
        lexer.addPointcutKeyword("precedence", new LexerAction_c(new Integer(sym.PC_PRECEDENCE)));
        lexer.addPointcutKeyword("preinitialization", new LexerAction_c(new Integer(sym.PC_PREINITIALIZATION)));
        lexer.addPointcutKeyword("returning", new LexerAction_c(new Integer(sym.PC_RETURNING)));
        lexer.addPointcutKeyword("set", new LexerAction_c(new Integer(sym.PC_SET)));
        lexer.addPointcutKeyword("soft", new LexerAction_c(new Integer(sym.PC_SOFT)));
        lexer.addPointcutKeyword("staticinitialization", new LexerAction_c(new Integer(sym.PC_STATICINITIALIZATION)));
        lexer.addPointcutKeyword("target", new LexerAction_c(new Integer(sym.PC_TARGET)));
        lexer.addPointcutKeyword("this", new LexerAction_c(new Integer(sym.PC_THIS)));
        lexer.addPointcutKeyword("throwing", new LexerAction_c(new Integer(sym.PC_THROWING)));
        lexer.addPointcutKeyword("warning", new LexerAction_c(new Integer(sym.PC_WARNING)));
        lexer.addPointcutKeyword("within", new LexerAction_c(new Integer(sym.PC_WITHIN)));
        lexer.addPointcutKeyword("withincode", new LexerAction_c(new Integer(sym.PC_WITHINCODE)));

        /* Special redefinition of aspect keyword so that we don't go out of ASPECTJ state
            and remain in POINTCUT state */
        lexer.addPointcutKeyword("aspect", new LexerAction_c(new Integer(sym.ASPECT)));

        /* ASPECTJ reserved words - these cannot be used as the names of any identifiers within
           aspect code. */
        lexer.addAspectJContextKeyword("after", new LexerAction_c(new Integer(sym.AFTER),
                                    new Integer(lexer.pointcut_state())));
        lexer.addAspectJContextKeyword("around", new LexerAction_c(new Integer(sym.AROUND),
                                    new Integer(lexer.pointcut_state())));
        lexer.addAspectJContextKeyword("before", new LexerAction_c(new Integer(sym.BEFORE),
                                    new Integer(lexer.pointcut_state())));
        lexer.addAspectJContextKeyword("declare", new LexerAction_c(new Integer(sym.DECLARE),
                                    new Integer(lexer.pointcut_state())));
        lexer.addAspectJContextKeyword("issingleton", new LexerAction_c(new Integer(sym.ISSINGLETON)));
        lexer.addAspectJContextKeyword("percflow", new PerClauseLexerAction_c(new Integer(sym.PERCFLOW),
                                    new Integer(lexer.pointcut_state())));
        lexer.addAspectJContextKeyword("percflowbelow", new PerClauseLexerAction_c(
                                    new Integer(sym.PERCFLOWBELOW), new Integer(lexer.pointcut_state())));
        lexer.addAspectJContextKeyword("pertarget", new PerClauseLexerAction_c(new Integer(sym.PERTARGET),
                                    new Integer(lexer.pointcut_state())));
        lexer.addAspectJContextKeyword("perthis", new PerClauseLexerAction_c(new Integer(sym.PERTHIS),
                                    new Integer(lexer.pointcut_state())));
        lexer.addAspectJContextKeyword("proceed", new LexerAction_c(new Integer(sym.PROCEED)));

        // Overloaded keywords - they mean different things in pointcuts, hence have to be
        // declared separately.
        lexer.addJavaKeyword("if", new LexerAction_c(new Integer(sym.IF)));
        lexer.addAspectJKeyword("if", new LexerAction_c(new Integer(sym.IF)));
        lexer.addPointcutIfExprKeyword("if", new LexerAction_c(new Integer(sym.IF)));
        lexer.addJavaKeyword("this", new LexerAction_c(new Integer(sym.THIS)));
        lexer.addAspectJKeyword("this", new LexerAction_c(new Integer(sym.THIS)));
        lexer.addPointcutIfExprKeyword("this", new LexerAction_c(new Integer(sym.THIS)));
        // keywords added to the Java part:
        lexer.addAspectJKeyword("aspect", new LexerAction_c(new Integer(sym.ASPECT),
								new Integer(lexer.aspectj_state())));
        lexer.addPointcutIfExprKeyword("aspect", new LexerAction_c(new Integer(sym.ASPECT),
                                new Integer(lexer.aspectj_state())));
        lexer.addAspectJKeyword("pointcut", new LexerAction_c(new Integer(sym.POINTCUT),
                                new Integer(lexer.pointcut_state())));
        lexer.addPointcutIfExprKeyword("pointcut", new LexerAction_c(new Integer(sym.POINTCUT),
                				new Integer(lexer.pointcut_state())));
        
        if(!abc.main.Debug.v().pureJava) {
            lexer.addJavaKeyword("aspect", new LexerAction_c(new Integer(sym.ASPECT),
                    			new Integer(lexer.aspectj_state())));
	        lexer.addJavaKeyword("pointcut", new LexerAction_c(new Integer(sym.POINTCUT),
	                            new Integer(lexer.pointcut_state())));
        }

    }

    /** This method is responsible for taking a method and calling
     *  AdviceApplication.doShadows for each "position" in the method that might have a join
     *  point associated with it. The base list of positions consists of WholeMethodPosition,
     *  StmtMethodPosition, NewStmtMethodPosition and TrapMethodPosition; if a new
     *  join point requires something else then it will be necessary to override this method
     *  and add a new kind of method position.
     */
    public void findMethodShadows(GlobalAspectInfo info,
                                  MethodAdviceList mal,
                                  SootClass cls,
                                  SootMethod method)
        throws polyglot.types.SemanticException {

        // Do whole body shadows
        if(MethodCategory.weaveExecution(method))
            AdviceApplication.doShadows(info,mal,cls,method,new WholeMethodPosition(method));

        // Do statement shadows
        if(abc.main.Debug.v().traceMatcher)
            System.err.println("Doing statement shadows");

        if(MethodCategory.weaveInside(method)) {
            Chain<Unit> stmtsChain=method.getActiveBody().getUnits();
            Stmt current,next;

            if(!stmtsChain.isEmpty()) { // I guess this is actually never going to be false
                for(current=(Stmt) stmtsChain.getFirst();
                    current!=null;
                    current=next) {
                    if(abc.main.Debug.v().traceMatcher)
                        System.err.println("Stmt = "+current);
                    next=(Stmt) stmtsChain.getSuccOf(current);
                    AdviceApplication.doShadows(info,mal,cls,method,
                                                new StmtMethodPosition(method,current));
                    AdviceApplication.doShadows(info,mal,cls,method,
                                                new NewStmtMethodPosition(method,current,next));
                }
            }
        }

        // Do exception handler shadows

        if(abc.main.Debug.v().traceMatcher)
            System.err.println("Doing exception shadows");

        Chain<Trap> trapsChain=method.getActiveBody().getTraps();
        Trap currentTrap;

        if(!trapsChain.isEmpty()) {
            for(currentTrap=(Trap) trapsChain.getFirst();
                currentTrap!=null;
                currentTrap=(Trap) trapsChain.getSuccOf(currentTrap))

                AdviceApplication.doShadows(info,mal,cls,method,new TrapMethodPosition(method,currentTrap));
        }


    }
    
    /** Get the precedence relationship between two aspects.
     *  @param a the first advice decl.
     *  @param b the second advice decl.
     *  @return
     *    {@link GlobalAspectInfo.PRECEDENCE_NONE} if none of the advice decls have precedence,
     *    {@link GlobalAspectInfo.PRECEDENCE_FIRST} if the first advice decl has precedence,
     *    {@link GlobalAspectInfo.PRECEDENCE_SECOND} if the second advice decl has precedence, or
     *    {@link GlobalAspectInfo.PRECEDENCE_CONFLICT} if there is a precedence
     *     conflict between the two advice decls.
     */
    public int getPrecedence(AbstractAdviceDecl a,AbstractAdviceDecl b) {
        // a quick first pass to assist in separating out the major classes of advice
        // consider delegating this
        int aprec=getPrecNum(a),bprec=getPrecNum(b);
        if(aprec>bprec) return GlobalAspectInfo.PRECEDENCE_FIRST;
        if(aprec<bprec) return GlobalAspectInfo.PRECEDENCE_SECOND;

        // CflowSetup needs to be compared by depth first
        if(a instanceof CflowSetup && b instanceof CflowSetup)
            return CflowSetup.getPrecedence((CflowSetup) a,(CflowSetup) b);

        if(!a.getDefiningAspect().getName().equals(b.getDefiningAspect().getName()))
            return abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getPrecedence(a.getDefiningAspect(),b.getDefiningAspect());
        
        //if both advice are inherited from the same aspect but are 
        //being applied by different concrete aspects
        if(!a.getAspect().getName().equals(b.getAspect().getName()))
            return abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getPrecedence(a.getAspect(),b.getAspect());

        if(a instanceof AdviceDecl && b instanceof AdviceDecl)
            return AdviceDecl.getPrecedence((AdviceDecl) a,(AdviceDecl) b);

        if(a instanceof DeclareSoft && b instanceof DeclareSoft)
            return DeclareSoft.getPrecedence((DeclareSoft) a,(DeclareSoft) b);

        // We don't care about precedence since these won't ever get woven
        if(a instanceof DeclareMessage && b instanceof DeclareMessage)
            return GlobalAspectInfo.PRECEDENCE_NONE;

        throw new InternalCompilerError
            ("case not handled when comparing "+a+" and "+b);
    }
    protected int getPrecNum(AbstractAdviceDecl d) {
        if(d instanceof PerCflowSetup) return ((PerCflowSetup) d).isBelow()? 0 : 4;
        else if(d instanceof CflowSetup) return ((CflowSetup) d).isBelow() ? 1 : 3;
        else if(d instanceof PerThisSetup) return 4;
        else if(d instanceof PerTargetSetup) return 4;
        else if(d instanceof AdviceDecl) return 2;
        else if(d instanceof DeclareSoft) return 5; //FIXME: no idea where this should go
        else if(d instanceof DeclareMessage) return 6;
        else throw new InternalCompilerError("Advice type not handled: "+d.getClass(),
                                             d.getPosition());
    }
    
    
	/** return the list of residue conjuncts. This should return a list all of whose elements
	 *   are of type abc.weaving.matching.AdviceApplication.ResidueConjunct.
	 */
	public List<ResidueConjunct> residueConjuncts(final AbstractAdviceDecl ad,
												 final abc.weaving.aspectinfo.Pointcut pc,
												 final abc.weaving.matching.ShadowMatch sm,
												 final SootMethod method,
												 final SootClass cls,
												 final abc.weaving.matching.WeavingEnv we) {
            return abc.weaving.matching.AdviceApplication.residueConjuncts(ad,pc,sm,method,cls,we);	
		}

    /**
     * Adds a new reweaving passes to the pass list.
     * @param passes the current list of reweaving passes; add your analysis passes
     * here as needed; do not forget to call <code>super</code>
     */
    protected void createReweavingPasses(List<ReweavingPass> passes) {
        if( OptionsParser.v().O() >= 3 ) {
            try {
                ReweavingAnalysis ana = (ReweavingAnalysis) Class.forName("abc.weaving.weaver.CflowAnalysisImpl").newInstance();                
                passes.add( new ReweavingPass( PASS_CFLOW_ANALYSIS, ana ) );
            } catch( Exception e ) {
                throw new InternalCompilerError("Couldn't load interprocedural analysis plugin 'CflowAnalysisImpl'.",e);
            }
        }
        
        if(Debug.v().debugUnweaver) {
            //to debug the unweaver, add an empty reweaving analysis
            passes.add( new ReweavingPass(PASS_DEBUG_UNWEAVER,new AbstractReweavingAnalysis() {
				public boolean analyze() {
					//actually do reweave
					return true;
				}
			}));
        }
    }
    
    /**
     * Returns the reweaving passes for this extension.
     * @return the reweaving passes
     */
    public final List<ReweavingPass> getReweavingPasses() {
        if(reweavingPasses == null) {
            reweavingPasses = new ArrayList<ReweavingPass>();
            createReweavingPasses(reweavingPasses);
        }
        return reweavingPasses;
    }

    public ErrorQueue getErrorQueue() {
    	return compileSequence.error_queue;
    }
    
    public void setErrorQueue(ErrorQueue eq) {
    	compileSequence.error_queue = eq;
    }
    
    /** Suspends error reporting until {@link #resumeErrorReporting()} is called.  */
    public void suspendErrorReporting() {
    	suspendErrorReporting = true;
    }

    /** Resumes error reporting after {@link #suspendErrorReporting()} was called.  */
    public void resumeErrorReporting() {
    	suspendErrorReporting = false;
    }

    /**
	 * This method is called right after "declare parents" has been processed and
	 * all Jimple bodies have been loaded. Subclasses can implement this method to
	 * do restrucuring of bodies prior to weaving. This default implementation does nothing.
	 */
	public void doMethodRestructuring() {
	}
}
