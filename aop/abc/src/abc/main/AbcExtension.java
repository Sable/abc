/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
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

import abc.soot.util.SwitchFolder;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import abc.weaving.weaver.*;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.annotation.nullcheck.*;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.util.*;

import java.util.*;

import abc.aspectj.parse.*;

/**
 * This class should be sub-classed to extend the behaviour of abc
 * <p>
 * A sub-class, with overriden methods to effect some new behaviour,
 * can be loaded at runtime by using the "-ext" switch to abc.
 *
 * @author Julian Tibble
 */
public class AbcExtension
{
    /**
     * Constructs a version string for all loaded extensions
     */
    final public String versions()
    {
        StringBuffer versions = new StringBuffer();
        collectVersions(versions);
        return versions.toString();
    }

    /*
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

    /*
     * Creates an instance of the <code>ExtensionInfo</code> structure
     * used for extending the Polyglot-based frontend.
     */
    public abc.aspectj.ExtensionInfo
            makeExtensionInfo(Collection jar_classes,
                              Collection aspect_sources)
    {
        return new abc.aspectj.ExtensionInfo(jar_classes, aspect_sources);
    }

    /**
     * Get all the shadow joinpoints that are matched
     * in this extension of AspectJ
     */
    final public Iterator /*<ShadowType>*/ shadowTypes()
    {
        return listShadowTypes().iterator();
    }

    /**
     * Override this method to add new joinpoints to the abc.
     * Call the same method in the super-class to ensure
     * the standard joinpoints needed are loaded too.
     */
    protected List /*<ShadowType>*/ listShadowTypes()
    {
        List /*<ShadowType*/ shadowTypes = new LinkedList();

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
			// Add a null check eliminator that knows about abc specific stuff
			NullCheckEliminator.AnalysisFactory f = new NullCheckEliminator.AnalysisFactory() {
				public BranchedRefVarsAnalysis newAnalysis(soot.toolkits.graph.UnitGraph g) {
					return new BranchedRefVarsAnalysis(g) {
						public boolean isAlwaysNonNull(Value v) {
							if (super.isAlwaysNonNull(v))
								return true;
							if (v instanceof InvokeExpr) {
								InvokeExpr ie = (InvokeExpr) v;
								SootMethodRef m = ie.getMethodRef();
								if (m.name().equals("makeJP") && m.declaringClass().getName().equals("org.aspectbench.runtime.reflect.Factory"))
									return true;
								if (m.name().equals("getStack") || m.name().equals("getCounter"))
									if (m.declaringClass().getName().equals("org.aspectbench.runtime.internal.CFlowStack")
											|| m.declaringClass().getName().equals("org.aspectbench.runtime.internal.CFlowCounter"))
										return true;
							}
							return false;
						}
					};
				}
			};
			// want this to run before Dead assignment eliminiation
			PackManager.v().getPack("jop").insertBefore(new Transform("jop.nullcheckelim", new NullCheckEliminator(f)), "jop.dae");
		}

		if (Debug.v().cflowIntraAnalysis) {
			// Cflow Intraprocedural Analysis
			// Two phases:
			//              - Collapse all the local vars assigned to the same
			//                CflowStack/CflowCounter field
			//                to the same var, only needs to be assigned once
			//              - Get the stack/counter for the current thread for
			//                each of these lazily
			//                to avoid repeated currentThread()s
			PackManager.v().getPack("jop").insertBefore(new Transform("jop.cflowintra", CflowIntraproceduralAnalysis.v()), "jop.dae");
			// Before running the cflow intraprocedural, need to aggregate cflow
			// vars
			PackManager.v().getPack("jop").insertBefore(new Transform("jop.cflowaggregate", CflowIntraAggregate.v()), "jop.cflowintra");
		}

		if (Debug.v().switchFolder) {
			// must be inserted somewhere before the unreachable code eliminator
			//PackManager.v().getPack("jop").add(new Transform("jtp.sf", SwitchFolder.v()));
		//	PackManager.v().getPack("jop").insertBefore(new Transform("jtp.sf", SwitchFolder.v()), "jop.cse");
			PackManager.v().getPack("jop").insertBefore(new Transform("jop.sf", SwitchFolder.v()), "jop.uce1");
		}
	}

    /**
	 * Call Scene.v().addBasicClass for each runtime class that the backend
	 * might generate code for. Derived implementations should normally make
	 * sure to call the superclass implementation.
	 */
    public void addBasicClassesToSoot()
    {
        Scene.v().addBasicClass("org.aspectbench.runtime.internal.CFlowStack",
                                SootClass.SIGNATURES);
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
        Scene.v().addBasicClass("org.aspectbench.runtime.internal.CFlowCounter",
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
        lexer.addJavaKeyword("aspect", new LexerAction_c(new Integer(sym.ASPECT),
                                new Integer(lexer.aspectj_state())));
        lexer.addAspectJKeyword("aspect", new LexerAction_c(new Integer(sym.ASPECT),
                                new Integer(lexer.aspectj_state())));
        lexer.addPointcutIfExprKeyword("aspect", new LexerAction_c(new Integer(sym.ASPECT),
                                new Integer(lexer.aspectj_state())));
        lexer.addJavaKeyword("pointcut", new LexerAction_c(new Integer(sym.POINTCUT),
                                new Integer(lexer.pointcut_state())));
        lexer.addAspectJKeyword("pointcut", new LexerAction_c(new Integer(sym.POINTCUT),
                                new Integer(lexer.pointcut_state())));
        lexer.addPointcutIfExprKeyword("pointcut", new LexerAction_c(new Integer(sym.POINTCUT),
                                new Integer(lexer.pointcut_state())));

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
            Chain stmtsChain=method.getActiveBody().getUnits();
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

        Chain trapsChain=method.getActiveBody().getTraps();
        Trap currentTrap;

        if(!trapsChain.isEmpty()) {
            for(currentTrap=(Trap) trapsChain.getFirst();
                currentTrap!=null;
                currentTrap=(Trap) trapsChain.getSuccOf(currentTrap))

                AdviceApplication.doShadows(info,mal,cls,method,new TrapMethodPosition(method,currentTrap));
        }


    }

}
