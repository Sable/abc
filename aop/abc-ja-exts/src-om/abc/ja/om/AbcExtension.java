/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
 * Copyright (C) 2008 Neil Ongkingco
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

package abc.ja.om;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import polyglot.types.SemanticException;
import polyglot.util.Position;
import soot.SootClass;
import soot.SootMethod;

import abc.aspectj.parse.AbcLexer;
import abc.aspectj.parse.LexerAction_c;
import abc.aspectj.parse.PerClauseLexerAction_c;
import abc.ja.om.modulestruct.JAModuleStructure;
import abc.ja.om.modulestruct.OMGlobalAspectInfo;
import abc.ja.om.parse.OMAbcLexer;
import abc.ja.om.parse.JavaParser.Terminals;
import abc.main.Debug;
import abc.om.AbcExtension.OMDebug;
import abc.om.modulestruct.ModuleStructure;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.Pointcut;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.matching.AdviceApplication.ResidueConjunct;
import abc.weaving.residues.Residue;

/**
 * @author Julian Tibble
 * @author Pavel Avgustinov
 * @author Eric Bodden
 */
public class AbcExtension extends abc.ja.AbcExtension {
	
	public abc.om.ExtensionInfo ext = null;
	public JAModuleStructure moduleStruct;
	
    public static Position generated = new Position("openmod_generated:0");
	
    public static enum OMDebug {
    	PRECEDENCE_DEBUG,
    	COLLECT_MODULE_ASPECTS_DEBUG,
    	MATCHING_DEBUG,
    	AST_PRINT_DEBUG,
    	PARENT_DEBUG,
    	ITD_DEBUG,
    	NORMALIZE_DEBUG,
    	ANY_DEBUG
    };
    protected static Set<OMDebug> debugSet = null;
    
    public static boolean isDebugSet(OMDebug debug) {
    	if (debug == OMDebug.ANY_DEBUG) {
    		return debugSet.size() > 0;
    	}
        return debugSet.contains(debug);
    }
    
    public static void debPrintln(OMDebug debug, String str) {
        if (isDebugSet(debug)) {
            System.out.println(str);
        }
    }

    public static void debPrint(OMDebug debug, String str) {
        if (isDebugSet(debug)) {
            System.out.print(str);
        }
    }

    public AbcExtension() {
    	super();
    	moduleStruct = new JAModuleStructure();
        debugSet = new HashSet<OMDebug>();
        if (Debug.v().omASTPrintDebug) {
            debugSet.add(OMDebug.AST_PRINT_DEBUG);
        }
        if (Debug.v().omCollectModuleAspectsDebug){
            debugSet.add(OMDebug.COLLECT_MODULE_ASPECTS_DEBUG);
        }
        if (Debug.v().omMatchingDebug) {
            debugSet.add(OMDebug.MATCHING_DEBUG);
        }
        if (Debug.v().omPrecedenceDebug) {
            debugSet.add(OMDebug.PRECEDENCE_DEBUG);
        }
        if (Debug.v().omOpenClassParentDebug) {
            debugSet.add(OMDebug.PARENT_DEBUG);
        }
        if (Debug.v().omOpenClassITDDebug) {
            debugSet.add(OMDebug.ITD_DEBUG);
        }
        if (Debug.v().omNormalizeDebug) {
            debugSet.add(OMDebug.NORMALIZE_DEBUG);
        }
    }

	protected void collectVersions(StringBuffer versions) {
		super.collectVersions(versions);
		versions.append(" with OpenModules (JastAdd version) "
				+ new abc.ja.om.Version().toString() + "\n");
	}

	public abc.aspectj.ExtensionInfo makeExtensionInfo(Collection jar_classes,
			Collection aspect_sources) {
		this.ext = new abc.om.ExtensionInfo(jar_classes, aspect_sources, this); 
		return ext;
	}

	/*
	 * public abc.weaving.weaver.AdviceInliner makeAdviceInliner() { return new
	 * abc.om.weaving.weaver.AdviceInliner(); }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see abc.main.AbcExtension#initLexerKeywords(abc.aspectj.parse.AbcLexer)
	 */
	public void initLexerKeywords(AbcLexer abclexer) {
		OMAbcLexer lexer = (OMAbcLexer) abclexer;
        // Cannot call super to add the base keywords unfortunately.

        lexer.addGlobalKeyword("abstract",      new LexerAction_c(new Integer(Terminals.ABSTRACT)));
        if(!abc.main.Debug.v().java13)
        	lexer.addGlobalKeyword("assert",        new LexerAction_c(new Integer(Terminals.ASSERT)));
        lexer.addGlobalKeyword("boolean",       new LexerAction_c(new Integer(Terminals.BOOLEAN)));
        lexer.addGlobalKeyword("break",         new LexerAction_c(new Integer(Terminals.BREAK)));
        lexer.addGlobalKeyword("byte",          new LexerAction_c(new Integer(Terminals.BYTE)));
        lexer.addGlobalKeyword("case",          new LexerAction_c(new Integer(Terminals.CASE)));
        lexer.addGlobalKeyword("catch",         new LexerAction_c(new Integer(Terminals.CATCH)));
        lexer.addGlobalKeyword("char",          new LexerAction_c(new Integer(Terminals.CHAR)));
        lexer.addGlobalKeyword("const",         new LexerAction_c(new Integer(Terminals.EOF))); // Disallow
																								// 'const'
																								// keyword
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
        lexer.addGlobalKeyword("goto",          new LexerAction_c(new Integer(Terminals.EOF))); // disallow
																								// 'goto'
																								// keyword
        // if is handled specifically, as it differs in pointcuts and
		// non-pointcuts.
        // lexer.addGlobalKeyword("if", new LexerAction_c(new
		// Integer(Terminals.IF)));
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
        /* ------------ keyword added to the Java part ------------------ */
        lexer.addGlobalKeyword("privileged",    new LexerAction_c(new Integer(Terminals.PRIVILEGED)));
        /* ------------ keyword added to the Java part ------------------ */
        lexer.addGlobalKeyword("protected",     new LexerAction_c(new Integer(Terminals.PROTECTED)));
        lexer.addGlobalKeyword("public",        new LexerAction_c(new Integer(Terminals.PUBLIC)));
        lexer.addGlobalKeyword("return",        new LexerAction_c(new Integer(Terminals.RETURN)));
        lexer.addGlobalKeyword("short",         new LexerAction_c(new Integer(Terminals.SHORT)));
        lexer.addGlobalKeyword("static",        new LexerAction_c(new Integer(Terminals.STATIC)));
        lexer.addGlobalKeyword("strictfp",      new LexerAction_c(new Integer(Terminals.STRICTFP)));
        lexer.addGlobalKeyword("super",         new LexerAction_c(new Integer(Terminals.SUPER)));
        lexer.addGlobalKeyword("switch",        new LexerAction_c(new Integer(Terminals.SWITCH)));
        lexer.addGlobalKeyword("synchronized",  new LexerAction_c(new Integer(Terminals.SYNCHRONIZED)));
        // this is handled explicitly, as it differs in pointcuts and
		// non-pointcuts.
        // lexer.addGlobalKeyword("this", new LexerAction_c(new
		// Integer(Terminals.THIS)));
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

        /*
		 * Special redefinition of aspect keyword so that we don't go out of
		 * ASPECTJ state and remain in POINTCUT state
		 */
        lexer.addPointcutKeyword("aspect", new LexerAction_c(new Integer(Terminals.ASPECT)));

        /*
		 * ASPECTJ reserved words - these cannot be used as the names of any
		 * identifiers within aspect code.
		 */
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

        // Overloaded keywords - they mean different things in pointcuts, hence
		// have to be
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
        
        
        //open module keywords
        
        lexer.addJavaKeyword("root", 
        		new LexerAction_c(new Integer(Terminals.ROOT))
        );
        lexer.addJavaKeyword("module", 
        		new LexerAction_c(
        				new Integer(Terminals.MODULE), 
        				new Integer(lexer.module_state())
        				)
        );
        lexer.addModuleKeyword("module", new LexerAction_c(new Integer(Terminals.MODULE)));
        // replace class to include class keyword in module state
        lexer.addGlobalKeyword("class",new LexerAction_c(new Integer(Terminals.CLASS)) {
            public int getToken(AbcLexer abclexer) {
            	OMAbcLexer lexer = (OMAbcLexer) abclexer;
                if(!lexer.getLastTokenWasDot()) {
                	int next_state;
                	if (lexer.currentState()==lexer.aspectj_state()){
                		next_state = lexer.aspectj_state();
                	} else if (lexer.currentState() == lexer.module_state()) {
                		next_state = lexer.pointcut_state();
                	} else {
                		next_state = lexer.java_state();
                	}
                    lexer.enterLexerState(next_state);
                }
                return token.intValue();
            }
        });
        lexer.addModuleKeyword("expose", 
        		new LexerAction_c(
        				new Integer(Terminals.EXPOSE), 
        				new Integer(lexer.pointcut_state())
        				)
        );
        lexer.addModuleKeyword("advertise", 
        		new LexerAction_c(
        				new Integer(Terminals.ADVERTISE), 
        				new Integer(lexer.pointcut_state())
        				)
        );
        lexer.addPointcutKeyword("to", 
        		new LexerAction_c(new Integer(Terminals.TO))
        );
        lexer.addModuleKeyword("open", 
        		new LexerAction_c(new Integer(Terminals.OPEN))
        );
        lexer.addModuleKeyword("constrain", 
        		new LexerAction_c(new Integer(Terminals.CONSTRAIN))
        );
        lexer.addModuleKeyword("friend", 
        		new LexerAction_c(new Integer(Terminals.FRIEND))
        );

        lexer.addModuleKeyword("openclass", 
        		new LexerAction_c(
        				new Integer(Terminals.OPENCLASS), 
        				new Integer(lexer.pointcut_state())
        				)
        );
        lexer.addPointcutKeyword("field", 
        		new LexerAction_c(new Integer(Terminals.FIELD))
        );
        lexer.addPointcutKeyword("parent", 
        		new LexerAction_c(new Integer(Terminals.PARENT))
        );
        lexer.addPointcutKeyword("method", 
        		new LexerAction_c(new Integer(Terminals.METHOD))
        );

        
        if(!abc.main.Debug.v().pureJava) {
            lexer.addJavaKeyword("aspect", new LexerAction_c(new Integer(Terminals.ASPECT),
            					new Integer(lexer.aspectj_state())));
            lexer.addJavaKeyword("pointcut", new LexerAction_c(new Integer(Terminals.POINTCUT),
                                new Integer(lexer.pointcut_state())));
        }
    }

	public abc.ja.om.CompileSequence createCompileSequence() {
		//add creation of extension info. as extension info is in 
		return new CompileSequence(this);
	}
	
    public List residueConjuncts(final AbstractAdviceDecl ad, 
            final Pointcut pc,
            final ShadowMatch sm, 
            final SootMethod method, 
            final SootClass cls, 
            final WeavingEnv we) {
        //complete rewrite, so that warnings will still be generated by
        //openModMatchesAt()
        List result = new ArrayList();
        result.add(new ResidueConjunct() {
        	             public Residue run() throws SemanticException {
        	             	return ad.preResidue(sm);
        	             }});
        //replace matchesAt with openModMatchesAt
        result.add(new ResidueConjunct() {
        	             public Residue run() throws SemanticException {
							return moduleStruct.openModMatchesAt(
							        ad.getPointcut(),
							        sm,
							        ad.getAspect(),
							        we,
							        cls,
							        method,
							        ad);
        	             }
                        });

       result.add(new ResidueConjunct() {
                        public Residue run() throws SemanticException {
                        	return ad.getAdviceSpec().matchesAt(we,sm,ad);
                        }
                       });
        result.add(new ResidueConjunct() {
        		        public Residue run() throws SemanticException {
        		        	return ad.postResidue(sm);
        		        }
                       });
        return result;
    }
    
    protected GlobalAspectInfo createGlobalAspectInfo() {
        return new OMGlobalAspectInfo();
    }

}
