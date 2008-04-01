/* abc - The AspectBench Compiler
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

package abc.ja;

import polyglot.util.ErrorInfo;
import polyglot.util.Position;
import abc.aspectj.parse.*;
import abc.ja.parse.JavaParser.Terminals;

public class AbcExtension extends abc.main.AbcExtension {
	public void reportError(ErrorInfo ei) {
    getErrorQueue().enqueue(ei);
	}

	public void reportError(int level, String s, Position pos) {
    getErrorQueue().enqueue(level, s, pos);
	}

    /**
     * Override this method to add the version information
     * for this extension, calling the same method in the
     * super-class to ensure that all extensions are
     * reported.
     */
    protected void collectVersions(StringBuffer versions)
    {
    	super.collectVersions(versions);
        versions.append("... with JastAdd frontend " +
                        new abc.ja.Version().toString() +
                        "\n");
    }

	public CompileSequence createCompileSequence() {
    	return new CompileSequence(this);
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
    }

	
}
