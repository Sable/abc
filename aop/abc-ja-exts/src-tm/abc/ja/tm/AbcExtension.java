/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Oege de Moor
 * Copyright (C) 2008 Pavel Avgustinov
 * Copyright (C) 2008 Torbjorn Ekman
 * Copyright (C) 2008 Julian Tibble
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

package abc.ja.tm;

import java.util.Collection;
import java.util.List;

import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import soot.Scene;
import soot.SootClass;
import abc.aspectj.parse.AbcLexer;
import abc.aspectj.parse.LexerAction_c;
import abc.aspectj.parse.PerClauseLexerAction_c;
import abc.ja.tm.parse.JavaParser.Terminals;
import abc.main.Debug;
import abc.main.options.OptionsParser;
import abc.tm.weaving.aspectinfo.TMAdviceDecl;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.weaver.TMWeaver;
import abc.tm.weaving.weaver.itds.ITDAnalysis;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.CflowSetup;
import abc.weaving.aspectinfo.DeclareMessage;
import abc.weaving.aspectinfo.DeclareSoft;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.weaver.ReweavingPass;
import abc.weaving.weaver.Weaver;
import abc.weaving.weaver.ReweavingPass.ID;

/**
 * @author Julian Tibble
 * @author Oege de Moor
 * @author Eric Bodden
 */
public class AbcExtension extends abc.ja.eaj.AbcExtension
{

    protected static final ID PASS_ITD_ANALYSIS = new ID("itd-analysis");

    protected void collectVersions(StringBuffer versions)
    {
        super.collectVersions(versions);
        versions.append(" with TraceMatching " +
                        new abc.ja.tm.Version().toString() +
                        "\n");
    }

    public abc.aspectj.ExtensionInfo
	makeExtensionInfo(Collection/*<String>*/ jar_classes,
			  Collection/*<String>*/ aspect_sources)
    {
        return new abc.tm.ExtensionInfo(jar_classes, aspect_sources);
    }
 
    protected GlobalAspectInfo createGlobalAspectInfo()
    {
        return new TMGlobalAspectInfo();
    }

    public Weaver createWeaver()
    {
        return new TMWeaver();
    }

    public void initLexerKeywords(AbcLexer lexer)
    {
        // Cannot call super to add base keywords unfortunately.

        lexer.addGlobalKeyword("abstract",      new LexerAction_c(new Integer(Terminals.ABSTRACT)));
        if(!abc.main.Debug.v().java13)
        	lexer.addGlobalKeyword("assert",        new LexerAction_c(new Integer(Terminals.ASSERT)));
        lexer.addGlobalKeyword("boolean",       new LexerAction_c(new Integer(Terminals.BOOLEAN)));
        lexer.addGlobalKeyword("break",         new LexerAction_c(new Integer(Terminals.BREAK)));
        lexer.addGlobalKeyword("byte",          new LexerAction_c(new Integer(Terminals.BYTE)));
        lexer.addGlobalKeyword("case",          new LexerAction_c(new Integer(Terminals.CASE)));
        lexer.addGlobalKeyword("catch",         new LexerAction_c(new Integer(Terminals.CATCH)));
        lexer.addGlobalKeyword("char",          new LexerAction_c(new Integer(Terminals.CHAR)));
        lexer.addGlobalKeyword("class",         new LexerAction_c(new Integer(Terminals.CLASS)) {
                            public int getToken(AbcLexer lexer) {
                                if(!lexer.getLastTokenWasDot()) {
                                    lexer.enterLexerState(lexer.currentState() == lexer.aspectj_state() ?
                                            lexer.aspectj_state() : lexer.java_state());
                                }
                                return token.intValue();
                            }
                        });
        lexer.addGlobalKeyword("const",         new LexerAction_c(new Integer(Terminals.EOF))); // Disallow 'const' keyword
        lexer.addGlobalKeyword("continue",      new LexerAction_c(new Integer(Terminals.CONTINUE)));
        lexer.addGlobalKeyword("default",       new LexerAction_c(new Integer(Terminals.DEFAULT)));
        lexer.addGlobalKeyword("do",            new LexerAction_c(new Integer(Terminals.DO)));
        lexer.addGlobalKeyword("double",        new LexerAction_c(new Integer(Terminals.DOUBLE)));
        lexer.addGlobalKeyword("else",          new LexerAction_c(new Integer(Terminals.ELSE)));
        lexer.addGlobalKeyword("extends",       new LexerAction_c(new Integer(Terminals.EXTENDS)));
        lexer.addGlobalKeyword("final",         new LexerAction_c(new Integer(Terminals.FINAL)));
        lexer.addGlobalKeyword("finally",       new LexerAction_c(new Integer(Terminals.FINALLY)));
        lexer.addGlobalKeyword("float",         new LexerAction_c(new Integer(Terminals.FLOAT)));
        lexer.addGlobalKeyword("for",           new LexerAction_c(new Integer(Terminals.FOR)));
        lexer.addGlobalKeyword("goto",          new LexerAction_c(new Integer(Terminals.EOF))); // disallow 'goto' keyword
        // if is handled specifically, as it differs in pointcuts and non-pointcuts.
        //lexer.addGlobalKeyword("if",            new LexerAction_c(new Integer(Terminals.IF)));
        lexer.addGlobalKeyword("implements",    new LexerAction_c(new Integer(Terminals.IMPLEMENTS)));
        lexer.addGlobalKeyword("import",        new LexerAction_c(new Integer(Terminals.IMPORT)));
        lexer.addGlobalKeyword("instanceof",    new LexerAction_c(new Integer(Terminals.INSTANCEOF)));
        lexer.addGlobalKeyword("int",           new LexerAction_c(new Integer(Terminals.INT)));
        lexer.addGlobalKeyword("interface",     new LexerAction_c(new Integer(Terminals.INTERFACE),
                                            new Integer(lexer.java_state())));
        lexer.addGlobalKeyword("long",          new LexerAction_c(new Integer(Terminals.LONG)));
        lexer.addGlobalKeyword("native",        new LexerAction_c(new Integer(Terminals.NATIVE)));
        lexer.addGlobalKeyword("new",           new LexerAction_c(new Integer(Terminals.NEW)));
        lexer.addGlobalKeyword("package",       new LexerAction_c(new Integer(Terminals.PACKAGE)));
        lexer.addGlobalKeyword("private",       new LexerAction_c(new Integer(Terminals.PRIVATE)));
        /* ------------  keyword added to the Java part ------------------ */
        lexer.addGlobalKeyword("privileged",    new LexerAction_c(new Integer(Terminals.PRIVILEGED)));
        /* ------------  keyword added to the Java part ------------------ */
        lexer.addGlobalKeyword("protected",     new LexerAction_c(new Integer(Terminals.PROTECTED)));
        lexer.addGlobalKeyword("public",        new LexerAction_c(new Integer(Terminals.PUBLIC)));
        lexer.addGlobalKeyword("return",        new LexerAction_c(new Integer(Terminals.RETURN)));
        lexer.addGlobalKeyword("short",         new LexerAction_c(new Integer(Terminals.SHORT)));
        lexer.addGlobalKeyword("static",        new LexerAction_c(new Integer(Terminals.STATIC)));
        lexer.addGlobalKeyword("strictfp",      new LexerAction_c(new Integer(Terminals.STRICTFP)));
        lexer.addGlobalKeyword("super",         new LexerAction_c(new Integer(Terminals.SUPER)));
        lexer.addGlobalKeyword("switch",        new LexerAction_c(new Integer(Terminals.SWITCH)));
        lexer.addGlobalKeyword("synchronized",  new LexerAction_c(new Integer(Terminals.SYNCHRONIZED)));
        // this is handled explicitly, as it differs in pointcuts and non-pointcuts.
        //lexer.addGlobalKeyword("this",          new LexerAction_c(new Integer(Terminals.THIS)));
        lexer.addGlobalKeyword("throw",         new LexerAction_c(new Integer(Terminals.THROW)));
        lexer.addGlobalKeyword("throws",        new LexerAction_c(new Integer(Terminals.THROWS)));
        lexer.addGlobalKeyword("transient",     new LexerAction_c(new Integer(Terminals.TRANSIENT)));
        lexer.addGlobalKeyword("try",           new LexerAction_c(new Integer(Terminals.TRY)));
        lexer.addGlobalKeyword("void",          new LexerAction_c(new Integer(Terminals.VOID)));
        lexer.addGlobalKeyword("volatile",      new LexerAction_c(new Integer(Terminals.VOLATILE)));
        lexer.addGlobalKeyword("while",         new LexerAction_c(new Integer(Terminals.WHILE)));

        if(abc.main.Debug.v().java15) {
          lexer.addJavaKeyword("enum", new LexerAction_c(new Integer(Terminals.ENUM)));
          lexer.addAspectJKeyword("enum", new LexerAction_c(new Integer(Terminals.ENUM)));
        }

        lexer.addPointcutKeyword("adviceexecution", new LexerAction_c(new Integer(Terminals.PC_ADVICEEXECUTION)));
        lexer.addPointcutKeyword("args", new LexerAction_c(new Integer(Terminals.PC_ARGS)));
        lexer.addPointcutKeyword("call", new LexerAction_c(new Integer(Terminals.PC_CALL)));
        lexer.addPointcutKeyword("cflow", new LexerAction_c(new Integer(Terminals.PC_CFLOW)));
        lexer.addPointcutKeyword("cflowbelow", new LexerAction_c(new Integer(Terminals.PC_CFLOWBELOW)));
        lexer.addPointcutKeyword("error", new LexerAction_c(new Integer(Terminals.PC_ERROR)));
        lexer.addPointcutKeyword("execution", new LexerAction_c(new Integer(Terminals.PC_EXECUTION)));
        lexer.addPointcutKeyword("get", new LexerAction_c(new Integer(Terminals.PC_GET)));
        lexer.addPointcutKeyword("handler", new LexerAction_c(new Integer(Terminals.PC_HANDLER)));
        lexer.addPointcutKeyword("if", new LexerAction_c(new Integer(Terminals.PC_IF),
                                    new Integer(lexer.pointcutifexpr_state())));
        lexer.addPointcutKeyword("initialization", new LexerAction_c(new Integer(Terminals.PC_INITIALIZATION)));
        lexer.addPointcutKeyword("parents", new LexerAction_c(new Integer(Terminals.PC_PARENTS)));
        lexer.addPointcutKeyword("precedence", new LexerAction_c(new Integer(Terminals.PC_PRECEDENCE)));
        lexer.addPointcutKeyword("preinitialization", new LexerAction_c(new Integer(Terminals.PC_PREINITIALIZATION)));
        lexer.addPointcutKeyword("returning", new LexerAction_c(new Integer(Terminals.PC_RETURNING)));
        lexer.addPointcutKeyword("set", new LexerAction_c(new Integer(Terminals.PC_SET)));
        lexer.addPointcutKeyword("soft", new LexerAction_c(new Integer(Terminals.PC_SOFT)));
        lexer.addPointcutKeyword("staticinitialization", new LexerAction_c(new Integer(Terminals.PC_STATICINITIALIZATION)));
        lexer.addPointcutKeyword("target", new LexerAction_c(new Integer(Terminals.PC_TARGET)));
        lexer.addPointcutKeyword("this", new LexerAction_c(new Integer(Terminals.PC_THIS)));
        lexer.addPointcutKeyword("throwing", new LexerAction_c(new Integer(Terminals.PC_THROWING)));
        lexer.addPointcutKeyword("warning", new LexerAction_c(new Integer(Terminals.PC_WARNING)));
        lexer.addPointcutKeyword("within", new LexerAction_c(new Integer(Terminals.PC_WITHIN)));
        lexer.addPointcutKeyword("withincode", new LexerAction_c(new Integer(Terminals.PC_WITHINCODE)));

        /* Special redefinition of aspect keyword so that we don't go out of ASPECTJ state
            and remain in POINTCUT state */
        lexer.addPointcutKeyword("aspect", new LexerAction_c(new Integer(Terminals.ASPECT)));

        /* ASPECTJ reserved words - these cannot be used as the names of any identifiers within
           aspect code. */
        lexer.addAspectJContextKeyword("after", new LexerAction_c(new Integer(Terminals.AFTER),
                                    new Integer(lexer.pointcut_state())));
        lexer.addAspectJContextKeyword("around", new LexerAction_c(new Integer(Terminals.AROUND),
                                    new Integer(lexer.pointcut_state())));
        lexer.addAspectJContextKeyword("before", new LexerAction_c(new Integer(Terminals.BEFORE),
                                    new Integer(lexer.pointcut_state())));
        lexer.addAspectJContextKeyword("declare", new LexerAction_c(new Integer(Terminals.DECLARE),
                                    new Integer(lexer.pointcut_state())));
        lexer.addAspectJContextKeyword("issingleton", new LexerAction_c(new Integer(Terminals.ISSINGLETON)));
        lexer.addAspectJContextKeyword("percflow", new PerClauseLexerAction_c(new Integer(Terminals.PERCFLOW),
                                    new Integer(lexer.pointcut_state())));
        lexer.addAspectJContextKeyword("percflowbelow", new PerClauseLexerAction_c(
                                    new Integer(Terminals.PERCFLOWBELOW), new Integer(lexer.pointcut_state())));
        lexer.addAspectJContextKeyword("pertarget", new PerClauseLexerAction_c(new Integer(Terminals.PERTARGET),
                                    new Integer(lexer.pointcut_state())));
        lexer.addAspectJContextKeyword("perthis", new PerClauseLexerAction_c(new Integer(Terminals.PERTHIS),
                                    new Integer(lexer.pointcut_state())));
        lexer.addAspectJContextKeyword("proceed", new LexerAction_c(new Integer(Terminals.PROCEED)));

        // Overloaded keywords - they mean different things in pointcuts, hence have to be
        // declared separately.
        lexer.addJavaKeyword("if", new LexerAction_c(new Integer(Terminals.IF)));
        lexer.addAspectJKeyword("if", new LexerAction_c(new Integer(Terminals.IF)));
        lexer.addPointcutIfExprKeyword("if", new LexerAction_c(new Integer(Terminals.IF)));
        lexer.addJavaKeyword("this", new LexerAction_c(new Integer(Terminals.THIS)));
        lexer.addAspectJKeyword("this", new LexerAction_c(new Integer(Terminals.THIS)));
        lexer.addPointcutIfExprKeyword("this", new LexerAction_c(new Integer(Terminals.THIS)));
        // keywords added to the Java part:
        lexer.addAspectJKeyword("aspect", new LexerAction_c(new Integer(Terminals.ASPECT),
                                new Integer(lexer.aspectj_state())));
        lexer.addPointcutIfExprKeyword("aspect", new LexerAction_c(new Integer(Terminals.ASPECT),
                                new Integer(lexer.aspectj_state())));
        lexer.addAspectJKeyword("pointcut", new LexerAction_c(new Integer(Terminals.POINTCUT),
                                new Integer(lexer.pointcut_state())));
        lexer.addPointcutIfExprKeyword("pointcut", new LexerAction_c(new Integer(Terminals.POINTCUT),
                                new Integer(lexer.pointcut_state())));
        
        if(!abc.main.Debug.v().pureJava) {
            lexer.addJavaKeyword("aspect", new LexerAction_c(new Integer(Terminals.ASPECT),
            					new Integer(lexer.aspectj_state())));
            lexer.addJavaKeyword("pointcut", new LexerAction_c(new Integer(Terminals.POINTCUT),
                                new Integer(lexer.pointcut_state())));
        }

        // keyword for the "cast" pointcut extension
        lexer.addPointcutKeyword("cast", new LexerAction_c(new Integer(Terminals.PC_CAST)));

        // keyword for the "throw" pointcut extension
        lexer.addPointcutKeyword("throw", new LexerAction_c(new Integer(Terminals.PC_THROW)));

        // keyword for the "global pointcut" extension
        if(!Debug.v().noGlobalPointcut)
        	lexer.addGlobalKeyword("global", new LexerAction_c(new Integer(Terminals.GLOBAL),
                            new Integer(lexer.pointcut_state())));

        // keyword for the "cflowdepth" pointcut extension
        lexer.addPointcutKeyword("cflowdepth", new LexerAction_c(new Integer(Terminals.PC_CFLOWDEPTH)));
        
        // keyword for the "cflowbelowdepth" pointcut extension
        lexer.addPointcutKeyword("cflowbelowdepth", new LexerAction_c(new Integer(Terminals.PC_CFLOWBELOWDEPTH)));

        // keyword for the "let" pointcut extension
        lexer.addPointcutKeyword("let", new LexerAction_c(new Integer(Terminals.PC_LET),
                new Integer(lexer.pointcutifexpr_state())));
 
        // keywords for the "monitorenter/monitorexit" pointcut extension
        if(Debug.v().enableLockPointcuts) {
	        lexer.addPointcutKeyword("lock", new LexerAction_c(new Integer(Terminals.PC_LOCK)));
	        lexer.addPointcutKeyword("unlock", new LexerAction_c(new Integer(Terminals.PC_UNLOCK)));
        }


        if(!Debug.v().noContainsPointcut) {
        	//keyword for the "contains" pointcut extension
        	lexer.addPointcutKeyword("contains", new LexerAction_c(new Integer(Terminals.PC_CONTAINS)));
        }
        
        // Array set/get pointcut keywords
        lexer.addPointcutKeyword("arrayget", new LexerAction_c(new Integer(Terminals.PC_ARRAYGET)));
        lexer.addPointcutKeyword("arrayset", new LexerAction_c(new Integer(Terminals.PC_ARRAYSET)));


        lexer.addAspectJKeyword("tracematch", new LexerAction_c(
                            new Integer(Terminals.TRACEMATCH)));
        lexer.addAspectJKeyword("sym", new LexerAction_c(
                            new Integer(Terminals.SYM)));
        lexer.addAspectJKeyword("perthread", new LexerAction_c(
                            new Integer(Terminals.PERTHREAD)));
        lexer.addAspectJKeyword("frequent", new LexerAction_c(
                            new Integer(Terminals.FREQUENT)));
        lexer.addAspectJKeyword("filtermatch", new LexerAction_c(
				new Integer(Terminals.FILTERMATCH)));
        lexer.addAspectJKeyword("skipmatch", new LexerAction_c(
				new Integer(Terminals.SKIPMATCH)));
    }
    
    public void addBasicClassesToSoot()
    {
        super.addBasicClassesToSoot();
        // Need to add all standard library classes used in the
        // codegen (minus some default ones)
        final String tmRuntime = "org.aspectbench.tm.runtime.internal.";

        addClassSignature("java.util.Iterator");
        addClassSignature("java.util.LinkedHashSet");
        addClassSignature("java.util.LinkedList");
        addClassSignature("java.lang.ref.WeakReference");
        addClassSignature("java.lang.ThreadLocal");
        addClassSignature("java.util.Set");
        addClassSignature(tmRuntime + "MyWeakRef");
        addClassSignature(tmRuntime + "PersistentWeakRef");
        addClassSignature(tmRuntime + "ClashWeakRef");
        addClassSignature(tmRuntime + "ClashPersistentWeakRef");
        addClassSignature(tmRuntime + "Lock");

        if(abc.main.Debug.v().useCommonsCollections)
            addClassSignature(
                "org.apache.commons.collections.map.ReferenceIdentityMap");
        else {
            addClassSignature("java.util.NoSuchElementException");
            addClassSignature("java.util.Map$Entry");
            addClassSignature(tmRuntime + "IdentityHashMap");
            addClassSignature(tmRuntime + "WeakKeyIdentityHashMap");
            addClassSignature(tmRuntime + "WeakKeyCollectingIdentityHashMap");
        }

        if(Debug.v().dynaInstr || Debug.v().shadowCount) {
            addClassSignature(tmRuntime + "IShadowSwitchInitializer");
            addClassSignature(tmRuntime + "ShadowSwitch");
        }

        if(Debug.v().useITDs) {
            addClassSignature("java.lang.Thread");
            addClassSignature("java.util.BitSet");
            addClassSignature("java.lang.ref.ReferenceQueue");
            addClassSignature(tmRuntime + "IndexTree");
            addClassSignature(tmRuntime + "IndexTreeMap");
            addClassSignature(tmRuntime + "MaybeWeakRef");
        }
    }
 
    private void addClassSignature(String name)
    {
        Scene.v().addBasicClass(name, SootClass.SIGNATURES);
    }

    /** 
     * {@inheritDoc}
     */
    protected void createReweavingPasses(List<ReweavingPass> passes) {
        super.createReweavingPasses(passes);
        
        if (abc.main.Debug.v().useITDs) {
            OptionsParser.v().set_tag_instructions(true);
            passes.add(new ReweavingPass(PASS_ITD_ANALYSIS,
                                         new ITDAnalysis()));
        }
    }
    
    /** within a single tracematch, normal precedence rules apply for recognition of symbols.
         the "some" advice has higher precedence than all symbols in the same tracematch
         if it is after advice; it has lower precedence than all symbols if it is before advice
         
         the "synch" advice always has higher precedence than anything else in the same tracematch */
	public int tmGetPrec(TMAdviceDecl tma,TMAdviceDecl tmb) {
        if (tma.getTraceMatchID().equals(tmb.getTraceMatchID())) {

            int tma_first;
            int tma_second;
            int tmb_first;
            int tmb_second;

            if (tma.getAdviceSpec().isAfter())
            {
                tma_first = GlobalAspectInfo.PRECEDENCE_SECOND;
                tma_second = GlobalAspectInfo.PRECEDENCE_FIRST;
            } else {
                tma_first = GlobalAspectInfo.PRECEDENCE_FIRST;
                tma_second = GlobalAspectInfo.PRECEDENCE_SECOND;
            }

            if (tmb.getAdviceSpec().isAfter())
            {
                tmb_first = GlobalAspectInfo.PRECEDENCE_SECOND;
                tmb_second = GlobalAspectInfo.PRECEDENCE_FIRST;
            } else {
                tmb_first = GlobalAspectInfo.PRECEDENCE_FIRST;
                tmb_second = GlobalAspectInfo.PRECEDENCE_SECOND;
            }

            if (tma.isBody() && !tmb.isBody())
                return tma_second;
            if (!tma.isBody() && tmb.isBody())
                return tmb_first;
            if (tma.isBody() && tmb.isBody())
                // we have tma==tmb, as there is at most one piece
                // of "body" advice
                return GlobalAspectInfo.PRECEDENCE_NONE;

            if (tma.isSynch() && !tmb.isSynch())
                return tma_first;
            if (!tma.isSynch() && tmb.isSynch())
                return tmb_second;
            if (tma.isSynch() && tmb.isSynch())
                // we have tma==tmb, as there is at most one piece
                // of "synch" advice
                return GlobalAspectInfo.PRECEDENCE_NONE;

            if (tma.isSome() && !tmb.isSome())
                return tma_second;
            if (!tma.isSome() && tmb.isSome())
                return tmb_first;
            if (tma.isSome() && tmb.isSome())
                // we have tma==tmb, as there is at most one piece
                // of "some" advice
                return GlobalAspectInfo.PRECEDENCE_NONE;
		    	 
		    	 
		    	 
				int lexicalfirst,lexicalsecond;
				if  (tma.getAdviceSpec().isAfter() || tmb.getAdviceSpec().isAfter() ) {
					lexicalfirst=GlobalAspectInfo.PRECEDENCE_SECOND;
					lexicalsecond=GlobalAspectInfo.PRECEDENCE_FIRST;
				} else {
					lexicalfirst=GlobalAspectInfo.PRECEDENCE_FIRST;
					lexicalsecond=GlobalAspectInfo.PRECEDENCE_SECOND;
				}
	    	    // neither is "some" advice, so just compare positions
				if(tma.getPosition().line() < tmb.getPosition().line())
					return lexicalfirst;
				if(tma.getPosition().line() > tmb.getPosition().line())
					return lexicalsecond;
				// both pieces of advice are on the same line, compare columns
				if(tma.getPosition().column() < tmb.getPosition().column())
					return lexicalfirst;
				if(tma.getPosition().column() > tmb.getPosition().column())
					return lexicalsecond;
				// we have a==b
				return GlobalAspectInfo.PRECEDENCE_NONE;
	       }
	       // do the comparison via the containing tracematches
	       return getPrec(tma,tmb);
	}
	
	
	protected int getPrec(AdviceDecl a,AdviceDecl b) {
			// We know that we are in the same aspect
			// and *not* within the same tracematch

			int lexicalfirst,lexicalsecond;

			// not sure about this: do we want to ignore advice type when it's
			// in a trace match?
			if( (a.getAdviceSpec().isAfter()  && !(a instanceof TMAdviceDecl)) || 
			     (b.getAdviceSpec().isAfter()  && !(b instanceof TMAdviceDecl))) {
				lexicalfirst=GlobalAspectInfo.PRECEDENCE_SECOND;
				lexicalsecond=GlobalAspectInfo.PRECEDENCE_FIRST;
			} else {
				lexicalfirst=GlobalAspectInfo.PRECEDENCE_FIRST;
				lexicalsecond=GlobalAspectInfo.PRECEDENCE_SECOND;
			}
			
			// as a and b are *not* within the same tracematch, we use the positions
			// of the containing tracematches for precedence comparison
			Position ap = ((a instanceof TMAdviceDecl) ? 
			                       ((TMAdviceDecl)a).getTraceMatchPosition() : 
			                       a.getPosition());
			Position bp = ((b instanceof TMAdviceDecl) ? 
			                       ((TMAdviceDecl)b).getTraceMatchPosition() : 
			                       b.getPosition());
        
        
			if(ap.line() < bp.line())
				return lexicalfirst;
			if(ap.line() > bp.line())
				return lexicalsecond;

			if(ap.column() < bp.column())
				return lexicalfirst;
			if(ap.column() > bp.column())
				return lexicalsecond;

			// Trying to compare the same advice, I guess... (modulo inlining behaviour)
			return GlobalAspectInfo.PRECEDENCE_NONE;
    }
	   
	/** amended for tracematches */
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

	       // change for tracematches starts here
			   if (a instanceof TMAdviceDecl && b instanceof TMAdviceDecl)
			   	   return tmGetPrec((TMAdviceDecl)a,(TMAdviceDecl)b);
			   	   
			   if(a instanceof AdviceDecl && b instanceof AdviceDecl)
				   return getPrec((AdviceDecl) a,(AdviceDecl) b);
		   // and ends here

		   if(a instanceof DeclareSoft && b instanceof DeclareSoft)
			   return DeclareSoft.getPrecedence((DeclareSoft) a,(DeclareSoft) b);

		   // We don't care about precedence since these won't ever get woven
		   if(a instanceof DeclareMessage && b instanceof DeclareMessage)
			   return GlobalAspectInfo.PRECEDENCE_NONE;

		   throw new InternalCompilerError
			   ("case not handled when comparing "+a+" and "+b);
	   }
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public CompileSequence createCompileSequence() {
		return new CompileSequence(this) {
			@Override
			public void reset() {
				super.reset();
				//reset static nmembers for tracematches
		        abc.tm.weaving.aspectinfo.TraceMatch.reset();
			}
		};
	}	
   
}
