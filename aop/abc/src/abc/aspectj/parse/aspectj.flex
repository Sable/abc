/* Abc - The AspectBench Compiler
 * Copyright (C) 2004 Polyglot project group
 * Copyright (C) 2004 Laurie Hendren
 * Copyright (C) 2004 Pavel Avgustinov
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL; 
 * if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

/* Java 1.4 scanner for JFlex.
 * Based on JLS, 2ed, Chapter 3.
 * Adapted for abc Pavel Avgustinov <pavel.avgustinov@magd.ox.ac.uk>, August 2004.
 */

package abc.aspectj.parse;

import java_cup.runtime.Symbol;
import polyglot.lex.*;
import polyglot.util.Position;
import polyglot.util.ErrorQueue;
import polyglot.util.ErrorInfo;
import java.util.HashMap;
import java.util.Stack;
import polyglot.ext.jl.parse.*;
import java.math.BigInteger;

%%

%public
%class Lexer_c
%implements Lexer
%type Token
%function nextToken

%unicode
%pack

%line
%column

%{

/* -------------------------- added for AspectJ ---------------------- */

    /* Counters added to get out of scanning states for AspectJ */
    private static int curlyBraceLevel = 0; // nesting of {}
    private static int parenLevel = 0; // nesting of ()
    private static int savedParenLevel; // level when entering if in pointcut 
    private static int savedPerParenLevel; // level when entering a per pointcut
    private static boolean inPerPointcut = false; // currently in a per pointcut 
    private static boolean reportedUnclosedComment = false; 
    private static boolean inComment = false;

    /* Remember state to return to when finishing a String or Char */
    /* Just keep own copy of state for the four main states, JAVA (aka
       YYINITIAL), ASPECTJ, POINTCUTEXPR and POINTCUT.  Each time yybegin
       is called to go into one of these states,  the variable
       savedState should be updated as well. */
    private static final int IN_JAVA = 0;
    private static final int IN_ASPECTJ = 1;
    private static final int IN_POINTCUTIFEXPR = 2;
    private static final int IN_POINTCUT = 3;
    private static int savedState = IN_JAVA;

    /* similar to savedState, but to remember whether we're in
       java or in an aspect */

    private static int javaOrAspect = IN_JAVA;

    public void returnFromPointcut() {
        switch (javaOrAspect) {
          case IN_JAVA : yybegin(YYINITIAL); break;
          case IN_ASPECTJ : yybegin(ASPECTJ); break;
        }
        savedState = javaOrAspect;
    }

    /* Go back to correct state from STRING OR CHAR states.  When coming
       out of a STRING or CHAR state, just look at the value in
       savedState and peform the appropriate yybegin action.   Must
       be called on exiting STRING and CHAR states. */
    public void returnFromStringChar()
      { 
      switch (savedState)
          { case IN_JAVA:     yybegin(YYINITIAL); break;
            case IN_ASPECTJ:  yybegin(ASPECTJ); break;
            case IN_POINTCUTIFEXPR: yybegin(POINTCUTIFEXPR); break;
            case IN_POINTCUT: yybegin(POINTCUT); break;
           }
      }

    /* Need a nestingStack to keep track of nesting of class, interface and
       aspect.  

       Each time a class or interface is entered, a stackState
       of (curlyBraceLevel,savedState) is pushed and then savedState becomes
       IN_JAVA. 

       Each time an aspect is entered a stackState of 
       (curlyBraceLevel,savedState) is pushed and then savedStated becomes
       IN_ASPECTJ.   

       Each time a LEFTBRACE is reached,  the curlyBraceLevel is incremented.

       Each time a RIGHTBRACE is reached, the curlyBraceLevel is decremented,
       and the new curlyBraceLevel is checked against the level stored on
       top of the nestingStack.   If the levels are equal, then we are
       exiting a class, interface or aspect declaration, and so we pop
       the top state and put the scanner in that state.
     */

    private static Stack nestingStack = new Stack();

    class NestingState {
         int nestingLevel;  /* current nesting level of { }, should be >= 0 */
         int state;  /* should be one of IN_JAVA or IN_ASPECTJ */

         NestingState(int l, int s)
           { nestingLevel=l;
             state = s;
           }
     }

      public static void reset() {
    	curlyBraceLevel = 0;
    	parenLevel = 0;
    	inPerPointcut = false;
    	reportedUnclosedComment = false;
    	inComment = false;
    	savedState = IN_JAVA;
    	nestingStack = new Stack();
    }

/* ------------------------------------------------------------------- */

    StringBuffer sb = new StringBuffer();
    String file;
    ErrorQueue eq;
    HashMap javaKeywords, pointcutKeywords, aspectJReservedWords;
    boolean lastTokenWasDot;

    public Lexer_c(java.io.InputStream in, String file, ErrorQueue eq) {
        this(new java.io.BufferedReader(new java.io.InputStreamReader(in)),
             file, eq);
    }
    
    public Lexer_c(java.io.Reader reader, String file, ErrorQueue eq) {
        this(new EscapedUnicodeReader(reader));
        this.file = file;
        this.eq = eq;
        this.javaKeywords = new HashMap();
        this.pointcutKeywords = new HashMap();
        this.aspectJReservedWords = new HashMap();
        init_keywords();
    }

    protected void init_keywords() {
        javaKeywords.put("abstract",      new Integer(sym.ABSTRACT));
        javaKeywords.put("assert",        new Integer(sym.ASSERT));
        javaKeywords.put("boolean",       new Integer(sym.BOOLEAN));
        javaKeywords.put("break",         new Integer(sym.BREAK));
        javaKeywords.put("byte",          new Integer(sym.BYTE));
        javaKeywords.put("case",          new Integer(sym.CASE));
        javaKeywords.put("catch",         new Integer(sym.CATCH));
        javaKeywords.put("char",          new Integer(sym.CHAR));
        javaKeywords.put("class",         new Integer(sym.CLASS));
        javaKeywords.put("const",         new Integer(sym.CONST));
        javaKeywords.put("continue",      new Integer(sym.CONTINUE));
        javaKeywords.put("default",       new Integer(sym.DEFAULT));
        javaKeywords.put("do",            new Integer(sym.DO));
        javaKeywords.put("double",        new Integer(sym.DOUBLE));
        javaKeywords.put("else",          new Integer(sym.ELSE));
        javaKeywords.put("extends",       new Integer(sym.EXTENDS));
        javaKeywords.put("final",         new Integer(sym.FINAL));
        javaKeywords.put("finally",       new Integer(sym.FINALLY));
        javaKeywords.put("float",         new Integer(sym.FLOAT));
        javaKeywords.put("for",           new Integer(sym.FOR));
        javaKeywords.put("goto",          new Integer(sym.GOTO));
        // if is handled specifically, as it differs in pointcuts and non-pointcuts.
        //javaKeywords.put("if",            new Integer(sym.IF));
        javaKeywords.put("implements",    new Integer(sym.IMPLEMENTS));
        javaKeywords.put("import",        new Integer(sym.IMPORT));
        javaKeywords.put("instanceof",    new Integer(sym.INSTANCEOF));
        javaKeywords.put("int",           new Integer(sym.INT));
        javaKeywords.put("interface",     new Integer(sym.INTERFACE));
        javaKeywords.put("long",          new Integer(sym.LONG));
        javaKeywords.put("native",        new Integer(sym.NATIVE));
        javaKeywords.put("new",           new Integer(sym.NEW));
        javaKeywords.put("package",       new Integer(sym.PACKAGE));
        javaKeywords.put("private",       new Integer(sym.PRIVATE));
        /* ------------  keyword added to the Java part ------------------ */
        javaKeywords.put("privileged",	  new Integer(sym.PRIVILEGED));
        /* ------------  keyword added to the Java part ------------------ */
        javaKeywords.put("protected",     new Integer(sym.PROTECTED));
        javaKeywords.put("public",        new Integer(sym.PUBLIC));
        javaKeywords.put("return",        new Integer(sym.RETURN));
        javaKeywords.put("short",         new Integer(sym.SHORT));
        javaKeywords.put("static",        new Integer(sym.STATIC));
        javaKeywords.put("strictfp",      new Integer(sym.STRICTFP));
        javaKeywords.put("super",         new Integer(sym.SUPER));
        javaKeywords.put("switch",        new Integer(sym.SWITCH));
        javaKeywords.put("synchronized",  new Integer(sym.SYNCHRONIZED));
        // this is handled explicitly, as it differs in pointcuts and non-pointcuts.
        //javaKeywords.put("this",          new Integer(sym.THIS));
        javaKeywords.put("throw",         new Integer(sym.THROW));
        javaKeywords.put("throws",        new Integer(sym.THROWS));
        javaKeywords.put("transient",     new Integer(sym.TRANSIENT));
        javaKeywords.put("try",           new Integer(sym.TRY));
        javaKeywords.put("void",          new Integer(sym.VOID));
        javaKeywords.put("volatile",      new Integer(sym.VOLATILE));
        javaKeywords.put("while",         new Integer(sym.WHILE));
        
        pointcutKeywords.put("adviceexecution", new Integer(sym.PC_ADVICEEXECUTION));
        pointcutKeywords.put("args", new Integer(sym.PC_ARGS));
        pointcutKeywords.put("call", new Integer(sym.PC_CALL));
        pointcutKeywords.put("cflow", new Integer(sym.PC_CFLOW));
        pointcutKeywords.put("cflowbelow", new Integer(sym.PC_CFLOWBELOW));
        pointcutKeywords.put("error", new Integer(sym.PC_ERROR));
        pointcutKeywords.put("execution", new Integer(sym.PC_EXECUTION));
        pointcutKeywords.put("get", new Integer(sym.PC_GET));
        pointcutKeywords.put("handler", new Integer(sym.PC_HANDLER));
        pointcutKeywords.put("if", new Integer(sym.PC_IF));
        pointcutKeywords.put("initialization", new Integer(sym.PC_INITIALIZATION));
        pointcutKeywords.put("parents", new Integer(sym.PC_PARENTS));
        pointcutKeywords.put("precedence", new Integer(sym.PC_PRECEDENCE));
        pointcutKeywords.put("preinitialization", new Integer(sym.PC_PREINITIALIZATION));
        pointcutKeywords.put("returning", new Integer(sym.PC_RETURNING));
        pointcutKeywords.put("set", new Integer(sym.PC_SET));
        pointcutKeywords.put("soft", new Integer(sym.PC_SOFT));
        pointcutKeywords.put("staticinitialization", new Integer(sym.PC_STATICINITIALIZATION));
        pointcutKeywords.put("target", new Integer(sym.PC_TARGET));
        pointcutKeywords.put("this", new Integer(sym.PC_THIS));
        pointcutKeywords.put("throwing", new Integer(sym.PC_THROWING));
        pointcutKeywords.put("warning", new Integer(sym.PC_WARNING));
        pointcutKeywords.put("within", new Integer(sym.PC_WITHIN));
        pointcutKeywords.put("withincode", new Integer(sym.PC_WITHINCODE));
        
        /* Special redefinition of aspect keyword so that we don't go out of ASPECTJ state
        	and remain in POINTCUT state */
        pointcutKeywords.put("aspect", new Integer(sym.ASPECT));
        
        /* ASPECTJ reserved words - these cannot be used as the names of any identifiers within
           aspect code. */
        aspectJReservedWords.put("after", new Integer(sym.AFTER));
        aspectJReservedWords.put("around", new Integer(sym.AROUND));
        aspectJReservedWords.put("before", new Integer(sym.BEFORE));
        aspectJReservedWords.put("declare", new Integer(sym.DECLARE));
        aspectJReservedWords.put("issingleton", new Integer(sym.ISSINGLETON));
        aspectJReservedWords.put("percflow", new Integer(sym.PERCFLOW));
        aspectJReservedWords.put("percflowbelow", new Integer(sym.PERCFLOWBELOW));
        aspectJReservedWords.put("pertarget", new Integer(sym.PERTARGET));
        aspectJReservedWords.put("perthis", new Integer(sym.PERTHIS));
        aspectJReservedWords.put("proceed", new Integer(sym.PROCEED));
/*        aspectJReservedWords.put("thisEnclosingJoinPointStaticPart", new Integer(sym.THISENCLOSINGJOINPOINTSTATICPART));
        aspectJReservedWords.put("thisJoinPoint", new Integer(sym.THISJOINPOINT));
        aspectJReservedWords.put("thisJoinPointStaticPart", new Integer(sym.THISJOINPOINTSTATIPART));*/
    }

    public String file() {
        return file;
    }

    private Position pos() {
        return new Position(file, yyline+1, yycolumn, yyline+1, yycolumn+yytext().length());
    }
    private Position pos(int len) {
        return new Position(file, yyline+1, yycolumn-len-1, yyline+1, yycolumn+1);
    }

    private Token key(int symbol) {
        lastTokenWasDot = false;
        return new Keyword(pos(), yytext(), symbol);
    }

    private Token op(int symbol) {
        lastTokenWasDot = (symbol == sym.DOT);
        return new Operator(pos(), yytext(), symbol);
    }

    private Token id() {
        lastTokenWasDot = false;
        return new Identifier(pos(), yytext(), sym.IDENTIFIER);
    }

    /* ---- added for id patterns, needed in Pointcuts  --- */
    private Token id_pattern() {
	//System.out.println("ID pattern: " + yytext());
        lastTokenWasDot = false;
        return new Identifier(pos(), yytext(), sym.IDENTIFIERPATTERN);
    }

    private Token int_lit(String s, int radix) {
        lastTokenWasDot = false;
	    BigInteger x = new BigInteger(s, radix);
	    boolean boundary = (radix == 10 && s.equals("2147483648"));
	    int bits = (radix == 10 ? 31 : 32);
	    if (x.bitLength() > bits && !boundary) { 
			eq.enqueue(ErrorInfo.LEXICAL_ERROR, "Integer literal \"" +
			   yytext() + "\" out of range.", pos());
	    }
	    return new IntegerLiteral(pos(), x.intValue(), 
	    		boundary? sym.INTEGER_LITERAL_BD : sym.INTEGER_LITERAL);
    }

    private Token long_lit(String s, int radix) {
        lastTokenWasDot = false;
	    BigInteger x = new BigInteger(s, radix);
	    boolean boundary = (radix == 10 && s.equals("9223372036854775808"));
        int bits = (radix == 10 ? 63 : 64);
	    if (x.bitLength() > bits && !boundary) {
			eq.enqueue(ErrorInfo.LEXICAL_ERROR, "Long literal \"" +
			   yytext() + "\" out of range.", pos());
			return new LongLiteral(pos(), x.longValue(), sym.LONG_LITERAL); // null;
	    }
	    return new LongLiteral(pos(), x.longValue(), 
	    		boundary? sym.LONG_LITERAL_BD : sym.LONG_LITERAL);
    }

    private Token float_lit(String s) {
        lastTokenWasDot = false;
        try {
            Float x = Float.valueOf(s);
		    boolean zero = true;
		    for (int i = 0; i < s.length(); i++) {
				if ('1' <= s.charAt(i) && s.charAt(i) <= '9') {
				    zero = false;
				    break;
				}
				else if(s.charAt(i) == 'e' || s.charAt(i) == 'E') {
					break; // 0e19 is still 0
				}
		    }
		    if (x.isInfinite() || x.isNaN() || (x.floatValue() == 0 && ! zero)) {
				eq.enqueue(ErrorInfo.LEXICAL_ERROR,
				   "Illegal float literal \"" + yytext() + "\"", pos());
				return new FloatLiteral(pos(), 0, sym.FLOAT_LITERAL); // null;
		    }
            return new FloatLiteral(pos(), x.floatValue(), sym.FLOAT_LITERAL);
        }
        catch (NumberFormatException e) {
            eq.enqueue(ErrorInfo.LEXICAL_ERROR,
                       "Illegal float literal \"" + yytext() + "\"", pos());
            return new FloatLiteral(pos(), 0, sym.FLOAT_LITERAL); // null;
        }
    }

    private Token double_lit(String s) {
        lastTokenWasDot = false;
        try {
            Double x = Double.valueOf(s);
		    boolean zero = true;
		    for (int i = 0; i < s.length(); i++) {
				if ('1' <= s.charAt(i) && s.charAt(i) <= '9') {
				    zero = false;
				    break;
				}
				else if(s.charAt(i) == 'e' || s.charAt(i) == 'E') {
					break; // 0e19 is still 0
				}
		    }
		    if (x.isInfinite() || x.isNaN() || (x.doubleValue() == 0 && ! zero)) {
				eq.enqueue(ErrorInfo.LEXICAL_ERROR,
				   "Illegal double literal \"" + yytext() + "\"", pos());
				return new DoubleLiteral(pos(), 0, sym.DOUBLE_LITERAL); // null;
		    }
            return new DoubleLiteral(pos(), x.doubleValue(), sym.DOUBLE_LITERAL);
        }
        catch (NumberFormatException e) {
            eq.enqueue(ErrorInfo.LEXICAL_ERROR,
                       "Illegal double literal \"" + yytext() + "\"", pos());
            return new DoubleLiteral(pos(), 0, sym.DOUBLE_LITERAL); // null;
        }
    }

    private Token char_lit(String s) {
        lastTokenWasDot = false;
        if (s.length() == 1) {
            char x = s.charAt(0);
            return new CharacterLiteral(pos(), x, sym.CHARACTER_LITERAL);
        }
        else {
            eq.enqueue(ErrorInfo.LEXICAL_ERROR,
                       "Illegal character literal \'" + s + "\'", pos(s.length()));
            return new CharacterLiteral(pos(), '\0', sym.CHARACTER_LITERAL);
        }
    }

    private Token boolean_lit(boolean x) {
        lastTokenWasDot = false;
        return new BooleanLiteral(pos(), x, sym.BOOLEAN_LITERAL);
    }

    private Token null_lit() {
        lastTokenWasDot = false;
        return new NullLiteral(pos(), sym.NULL_LITERAL);
    }

    private Token string_lit() {
        lastTokenWasDot = false;
        return new StringLiteral(pos(sb.length()), sb.toString(),
                                 sym.STRING_LITERAL);
    }

	private int comment_count = 0;
	
    private String chop(int i, int j) {
        return yytext().substring(i,yylength()-j);
    }

    private String chop(int j) {
        return chop(0, j);
    }

    private String chop() {
        return chop(0, 1);
    }
%}

%eofval{
    return new EOF(pos(), sym.EOF);
%eofval}

%state COMMENT

/* From Chapter 3 of the JLS: */

/* 3.4 Line Terminators */
/* LineTerminator:
      the ASCII LF character, also known as "newline"
      the ASCII CR character, also known as "return"
      the ASCII CR character followed by the ASCII LF character
*/
LineTerminator = \n|\r|\r\n

/* 3.6 White Space */
/*
WhiteSpace:
    the ASCII SP character, also known as "space"
    the ASCII HT character, also known as "horizontal tab"
    the ASCII FF character, also known as "form feed"
    LineTerminator
*/
WhiteSpace = [ \t\f] | {LineTerminator}

InputCharacter = [^\r\n]

/* comments */

EndOfLineComment = "//" {InputCharacter}* 

/* 3.8 Identifiers */
Identifier = [:jletter:] [:jletterdigit:]*

/* Used in pointcut names */
IdentifierPattern = 
    ( "*" | [:jletter:] ) ( "*" | [:jletterdigit:] )*

/* 3.10.1 Integer Literals */
/* 3.10.1 Integer Literals */
DecimalNumeral = 0 | [1-9][0-9]*
HexNumeral = 0 [xX] [0-9a-fA-F]+
OctalNumeral = 0 [0-7]+

/* 3.10.2 Floating-Point Literals */
FloatingPointLiteral = [0-9]+ "." [0-9]* {ExponentPart}?
                     | "." [0-9]+ {ExponentPart}?
                     | [0-9]+ {ExponentPart}

ExponentPart = [eE] {SignedInteger}
SignedInteger = [-+]? [0-9]+

/* 3.10.4 Character Literals */
OctalEscape = \\ [0-7]
            | \\ [0-7][0-7]
            | \\ [0-3][0-7][0-7]
            
/* string and character literals */
//StringCharacter = [^\r\n\"\\]
//SingleCharacter = [^\r\n\'\\]

/* Note that YYINITIAL is used for state JAVA, we start assuming
   we are parsing a Java declaration. 

   POINTCUTEXPR is the state indicating we are parsing the expression
   part of the "if (Expression)" pointcut designator.   While in a
   POINTCUT state, if we see the keyword "if", go to POINTCUTEXPR state.
   Return from POINTCUT state when the matching ")" is found. 

   Note that ASPECTJ and POINTCUTEXPR state behave the same way for
   grammar productions. 
*/

%state ASPECTJ, POINTCUTIFEXPR 

/* within any of YYINITIAL(JAVA), POINTCUTEXPR, ASPECTJ  or POINTCUT
   we need to deal with strings and char literals.   The end of the
   STRING or CHARLITERAL takes us back to the same state in which 
   we entered.  The state is saved in savedState.  All yybegin statements
   going into one of these four states must assign to savedState.
*/

%state STRING, CHARACTER

/* Pointcut designators have their own set of tokens.  

   A declare POINTCUT starts with the keyword "declare" and ends at the
   first ';'.   It returns to the ASPECTJ state.

   An advice POINTCUT starts with the keyword "before", "after" or "around"
   and ends with the first '{',  which signals the beginning of the
   advice body,  so the state goes to ASPECTJ.

   A pointcut declaration starts with the keyword "pointcut" and ends at
   the first ';', state goes to ASPECTJ.

   A per POINTCUT starts with the keyword "pertarget", "perthis", 
   "percflow" or "percflowbelow",  and ends at the matching ')' at
   which time it returns to the ASPECTJ state.

   The first three cases are easy to find the end,  but for the fourth case
   we need to know two things: (1) we found a ')' at nesting level 0,
   AND (2) we are currently in a PER pointcut.  We use one extra global 
   variable, inPerPointcut, to distinguish the 4th case from the others. 

   inPerPointcut is intially false.  When a pointcut state is entered
   because of seeing a PER* keyword,  inPerPointcut is set to true.  When
   the matching ")" is found, inPerPointcut is reset to false.
*/
%state POINTCUT

%%
/* overloaded keywords, mean different things in POINTCUTS */
<YYINITIAL,ASPECTJ,POINTCUTIFEXPR> {
	"if"						{ return key(sym.IF); }
	"this"						{ return key(sym.THIS); }
	/* ------------  keyword added to the Java part ------------------ */
	"aspect"                       { yybegin(ASPECTJ); 
    	                               nestingStack.push(
                                       new NestingState(
                                           curlyBraceLevel, savedState));
           		                       savedState = IN_ASPECTJ;
                	                   javaOrAspect = IN_ASPECTJ;  
                     	               return key(sym.ASPECT); 
                         	        }
    "pointcut"                      { yybegin(POINTCUT);
    	                              savedState = IN_POINTCUT;
        	                          return key(sym.POINTCUT);
            	                    }
  /* ----------------------------------------------------------------*/
}

<YYINITIAL,ASPECTJ,POINTCUTIFEXPR,POINTCUT> {
    /* 3.7 Comments */
	"/*"                           { yybegin(COMMENT); 
                                     inComment = true; 
                                     comment_count = comment_count + 1; 
                                   }
	"*/"                           { eq.enqueue(ErrorInfo.LEXICAL_ERROR,
                                                  "unmatched */",pos()); }

	{EndOfLineComment}             { /* ignore */ }

	/* whitespace */
	{WhiteSpace}                   { /* ignore */ }

    /* Keywords and identifiers are handled last. */

	/* boolean literals */
	"true"                         { return boolean_lit(true); }
	"false"                        { return boolean_lit(false); }

    /* 3.10.6 Null Literal */
    "null"  { return null_lit(); }
}

/* Java-ish symbols and literals */
<YYINITIAL,ASPECTJ,POINTCUTIFEXPR> {
    /* 3.11 Separators */
  "("                            { parenLevel++; return op(sym.LPAREN); }

  /* if we have finished an expression found in the pointcut if, must
        return to POINTCUT state. */
  ")"                            { parenLevel--; 
                                   if ( (savedState == IN_POINTCUTIFEXPR) &&
                                        (parenLevel == savedParenLevel))
                                      { yybegin(POINTCUT);
                                        savedState = IN_POINTCUT;
                                      } 
                                   return op(sym.RPAREN); 
                                 }
  "{"                            { curlyBraceLevel++; return op(sym.LBRACE); }
  "}"                            { curlyBraceLevel--; 
                                   
                                   /* if level is 0, back to top level
                                        of a compilation unit */
                                   if ( (curlyBraceLevel == 0) &&
                                        (savedState == IN_ASPECTJ) )
                                     { yybegin(YYINITIAL);
                                       savedState = IN_JAVA;
                                       javaOrAspect = IN_JAVA;
                                       // shouldn't the thing on the nesting stack be correct anyway?
                                       if (!nestingStack.isEmpty())
                                         nestingStack.pop();
                                     }
                                   else /* we are in some nesting */
                                     /* if curlyBraceLevel is same as
                                          top of nestingStack, then 
                                          exiting a class, interface or
                                          aspect declaration. */
                                     if (!nestingStack.isEmpty())
                                       { if (curlyBraceLevel == 
                                           ((NestingState) nestingStack.peek()).
                                                                  nestingLevel)
                                           { NestingState s;  

                                             s= (NestingState) nestingStack.pop();
                                             savedState = s.state;
                                             if (savedState == IN_JAVA)
                                               yybegin(YYINITIAL);
                                             else if (savedState == IN_ASPECTJ)
                                               yybegin(ASPECTJ);
                                             else if (savedState == IN_POINTCUT) // syntax error
                                               yybegin(POINTCUT);
                                             else if (savedState == IN_POINTCUTIFEXPR) // syntax error
                                               yybegin(POINTCUTIFEXPR);
                                             else
                                               System.err.println(
     "Invalid state " + savedState + " popped from nestingStack in scanner");
                                             }
                                          }
                                        else // an extra }
                                          { // don't change the state
                                            // hand over token for parser to
                                            // find sytax error
                                            //savedState = IN_JAVA;
                                            //yybegin(YYINITIAL);
                                          }
                                        return op(sym.RBRACE); 
                                 }
    "["    { return op(sym.LBRACK);    }
    "]"    { return op(sym.RBRACK);    }
    ";"    { return op(sym.SEMICOLON); }
    ","    { return op(sym.COMMA);     }
    "."    { return op(sym.DOT);       }

    /* 3.12 Operators */
    "="    { return op(sym.EQ);         }
    ">"    { return op(sym.GT);         }
    "<"    { return op(sym.LT);         }
    "!"    { return op(sym.NOT);        }
    "~"    { return op(sym.COMP);       }
    "?"    { return op(sym.QUESTION);   }
    ":"    { return op(sym.COLON);      }
    "=="   { return op(sym.EQEQ);       }
    "<="   { return op(sym.LTEQ);       }
    ">="   { return op(sym.GTEQ);       }
    "!="   { return op(sym.NOTEQ);      }
    "&&"   { return op(sym.ANDAND);     }
    "||"   { return op(sym.OROR);       }
    "++"   { return op(sym.PLUSPLUS);   }
    "--"   { return op(sym.MINUSMINUS); }
    "+"    { return op(sym.PLUS);       }
    "-"    { return op(sym.MINUS);      }
    "*"    { return op(sym.MULT);       }
    "/"    { return op(sym.DIV);        }
    "&"    { return op(sym.AND);        }
    "|"    { return op(sym.OR);         }
    "^"    { return op(sym.XOR);        }
    "%"    { return op(sym.MOD);        }
    "<<"   { return op(sym.LSHIFT);     }
    ">>"   { return op(sym.RSHIFT);     }
    ">>>"  { return op(sym.URSHIFT);    }
    "+="   { return op(sym.PLUSEQ);     }
    "-="   { return op(sym.MINUSEQ);    }
    "*="   { return op(sym.MULTEQ);     }
    "/="   { return op(sym.DIVEQ);      }
    "&="   { return op(sym.ANDEQ);      }
    "|="   { return op(sym.OREQ);       }
    "^="   { return op(sym.XOREQ);      }
    "%="   { return op(sym.MODEQ);      }
    "<<="  { return op(sym.LSHIFTEQ);   }
    ">>="  { return op(sym.RSHIFTEQ);   }
    ">>>=" { return op(sym.URSHIFTEQ);  }

    /* 3.10.4 Character Literals */
    \'      { yybegin(CHARACTER); sb.setLength(0); }

    /* 3.10.5 String Literals */
    \"      { yybegin(STRING); sb.setLength(0); }

    /* 3.10.1 Integer Literals */
    {OctalNumeral} [lL]          { return long_lit(chop(), 8); }
    {HexNumeral} [lL]            { return long_lit(chop(2,1), 16); }
    {DecimalNumeral} [lL]        { return long_lit(chop(), 10); }
    {OctalNumeral}               { return int_lit(yytext(), 8); }
    {HexNumeral}                 { return int_lit(chop(2,0), 16); }
    {DecimalNumeral}             { return int_lit(yytext(), 10); }

    /* 3.10.2 Floating-Point Literals */
    {FloatingPointLiteral} [fF]  { return float_lit(chop()); }
    {DecimalNumeral} [fF]        { return float_lit(chop()); }
    {FloatingPointLiteral} [dD]  { return double_lit(chop()); }
    {DecimalNumeral} [dD]        { return double_lit(chop()); }
    {FloatingPointLiteral}       { return double_lit(yytext()); }
}

<POINTCUT> {
/* Symbols for pointcuts */

  /* symbols that are in normal Java states too, we repeat them here 
     because either they have a different action when in POINTCUT
     state, or we just need them to parse things like types in
     pointcuts. */
  "("                            { parenLevel++; return op(sym.LPAREN); }
  ")"                            { parenLevel--;
                                   if (inPerPointcut &&
                                       parenLevel == savedPerParenLevel)
                                     { yybegin(ASPECTJ);
                                       savedState = IN_ASPECTJ;
                                       inPerPointcut = false;
                                     }
                                   return op(sym.RPAREN); 
                                 }
  "["                            { return op(sym.LBRACK); }
  "]"                            { return op(sym.RBRACK); } 
  ","                            { return op(sym.COMMA); }
  "."                            { return op(sym.DOT); }
  ":"                            { return op(sym.COLON);}
  ";"                            { returnFromPointcut();
                                   return op(sym.SEMICOLON); 
                                 }
  "{"                            { curlyBraceLevel++;
                                   returnFromPointcut();
                                   return op(sym.LBRACE); 
                                 }
  \"                             { yybegin(STRING); sb.setLength(0); }

  /* symbol specific to pointcuts */
  ".."                           {  return op(sym.PC_DOTDOT); } 

  /* operators specific to pointcuts  */
  "!"                            { return op(sym.PC_NOT); }
  "&&"                           { return op(sym.PC_ANDAND); }
  "||"                           { return op(sym.PC_OROR); }
  "+"                            { return op(sym.PC_PLUS); }
  
/* Note that if both IdentifierPattern and * match, then * will be
   chosen first, since it is an earlier rule.
*/
  
  "*"                            { return op(sym.PC_MULT); }
}

/* We need some cases to distinguish the different lexer states from one another - otherwise
 * they Java, Pointcut and AspectJ get collapsed into the same state by optimisations.
 */
<YYINITIAL> {
	"oege"					{ return id(); }
}

<ASPECTJ> {
	"ganesh"				{ return id(); }
}

<POINTCUTIFEXPR> {
	"pavel"					{ return id(); }
}

<YYINITIAL,ASPECTJ,POINTCUTIFEXPR,POINTCUT> {
	/* Handle keywords and identifiers here. It has to be done last as with the new parser structure,
		whether or not an identifier is a keyword is only determined after it has been consumed, so
		that placing splitting this code between the different lexer states would consume identifiers
		(and hence potential keywords for later state) at the first occurrence. (yes, this comment does
		make sense to me. :-P) */
		
    /* 3.9 Keywords */
    /* 3.8 Identifiers */
    {Identifier}   { 
    	// Keywords common to all states first.
    	Integer i = (Integer) javaKeywords.get(yytext());
    	// System.out.println("J/AJ/PC/PCIE: " + YYINITIAL + "/"+ASPECTJ+"/"+POINTCUT+"/"+POINTCUTIFEXPR+", currernt state: " + yystate() + " for token " + yytext());
		if(i != null) {
			// Some special handling is required...
			if(yytext().equals("class")) {
				if(!lastTokenWasDot) {
					// if in ASPECCTJ state, stay there.
					int newSavedState = (savedState == IN_ASPECTJ ? IN_ASPECTJ : IN_JAVA);
					int newState = (savedState == IN_ASPECTJ ? ASPECTJ : YYINITIAL);
					yybegin(newState);
					nestingStack.push(new NestingState(curlyBraceLevel, savedState));
					savedState = newSavedState;
					}
			}
			else if(yytext().equals("interface")) {
				yybegin(YYINITIAL);
				nestingStack.push(new NestingState(curlyBraceLevel, savedState));
				savedState = YYINITIAL;
			}
			// all other keywords can be handled generically
			return key(i.intValue());
		}
		
		// AspectJ-specific keywords
		if(yystate() == ASPECTJ || yystate() == POINTCUTIFEXPR) {
			i = (Integer) aspectJReservedWords.get(yytext());
			if(i != null) {
				// these keywords require some special handling, as many of them can trigger
				// a lexer state change.
				if(yytext().equals("percflow") || yytext().equals("percflowbelow") ||
						yytext().equals("pertarget") || yytext().equals("perthis")) {
					yybegin(POINTCUT);
					savedState = IN_POINTCUT;
					inPerPointcut = true;
					savedPerParenLevel = parenLevel;
				}
				else if(yytext().equals("after") || yytext().equals("around") ||
						yytext().equals("before") || yytext().equals("declare")) {
					yybegin(POINTCUT);
					savedState = IN_POINTCUT;
				}
				// all other cases handled generically.
				return key(i.intValue());
			}
		}
		
        // pointcut-specific keywords
        if(yystate() == POINTCUT) {
            i = (Integer) pointcutKeywords.get(yytext());
            if(i != null)  {
            	if(yytext().equals("if")) {
            		yybegin(POINTCUTIFEXPR);
            		savedState = IN_POINTCUTIFEXPR;
            		savedParenLevel = parenLevel;
            	}
            	// all other keywords can be handled generically
	            return key(i.intValue());
	        }
        }
		
		// OK, it's not a keyword, so it's either an identifier.
		/* Note that if both Identifier and Name Pattern match, then    
		   Identifier will be chosen first, since it is an earlier rule.
		 */
		return id();
	}
}

<POINTCUT> {
	/* Identifier patterns,  to handle things like foo.. *foo *1a and so on.
	   We don't want to parse them further because it is certainly meaningful
	   to say things like *if*while*for  and we don't want to have to include
	   reserved words explicitly.
	*/
  {IdentifierPattern}      { return id_pattern(); }
}	

<COMMENT> { 
  "*/"				{ 
	  					if(abc.main.Debug.v().noNestedComments) {
	  						inComment = false;
	  						returnFromStringChar();
	  					}
	  					else {
	  						comment_count = comment_count - 1; 
		 			   		if (comment_count < 0) 
	                        	eq.enqueue(ErrorInfo.LEXICAL_ERROR,"unmatched */",pos());
	                        if (comment_count == 0) {
                            	inComment = false;
    		                    returnFromStringChar(); 
                            }
	                    } 
	                }
  "/*"              { 
  						if(!abc.main.Debug.v().noNestedComments) 
  							comment_count = comment_count + 1; 
  					}
  .|\n                           { /* ignore */ }
}

<CHARACTER> {
    /* End of the character literal */
    \'                           { returnFromStringChar();
                                   return char_lit(sb.toString()); }

    /* 3.10.6 Escape Sequences for Character and String Literals */
    "\\b"                        { sb.append('\b'); }
    "\\t"                        { sb.append('\t'); }
    "\\n"                        { sb.append('\n'); }
    "\\f"                        { sb.append('\f'); }
    "\\r"                        { sb.append('\r'); }
    "\\\""                       { sb.append('\"'); }
    "\\'"                        { sb.append('\''); }
    "\\\\"                       { sb.append('\\'); }
    {OctalEscape}                { try {
                                       int x = Integer.parseInt(chop(1,0), 8);
                                       sb.append((char) x);
                                   }
                                   catch (NumberFormatException e) {
                                       eq.enqueue(ErrorInfo.LEXICAL_ERROR,
                                                  "Illegal octal escape \""
                                                  + yytext() + "\"", pos());
                                   }
                                 }

    /* Illegal escape character */
    \\.                          { eq.enqueue(ErrorInfo.LEXICAL_ERROR,
                                              "Illegal escape character \"" +
                                              yytext() + "\"", pos()); }

    /* Unclosed character literal */
    {LineTerminator}             { yybegin(YYINITIAL);
                                  eq.enqueue(ErrorInfo.LEXICAL_ERROR,
                                             "Unclosed character literal",
                                             pos(sb.length())); }

    /* Anything else is okay */
    [^\r\n\'\\]+                 { sb.append( yytext() ); }
}

<STRING> {
    /* End of string */
    \"                           { returnFromStringChar();
                                   return string_lit(); }

    /* 3.10.6 Escape Sequences for Character and String Literals */
    "\\b"                        { sb.append( '\b' ); }
    "\\t"                        { sb.append( '\t' ); }
    "\\n"                        { sb.append( '\n' ); }
    "\\f"                        { sb.append( '\f' ); }
    "\\r"                        { sb.append( '\r' ); }
    "\\\""                       { sb.append( '\"' ); }
    "\\'"                        { sb.append( '\'' ); }
    "\\\\"                       { sb.append( '\\' ); }
    {OctalEscape}                { try {
                                       int x = Integer.parseInt(chop(1,0), 8);
                                       sb.append((char) x);
                                   }
                                   catch (NumberFormatException e) {
                                       eq.enqueue(ErrorInfo.LEXICAL_ERROR,
                                                  "Illegal octal escape \""
                                                  + yytext() + "\"", pos());
                                   }
                                 }

    /* Illegal escape character */
    \\.                          { eq.enqueue(ErrorInfo.LEXICAL_ERROR,
                                              "Illegal escape character \"" +
                                              yytext() + "\"", pos()); }

    /* Unclosed string literal */
    {LineTerminator}             { yybegin(YYINITIAL);
                                   eq.enqueue(ErrorInfo.LEXICAL_ERROR,
                                              "Unclosed string literal",
                                              pos(sb.length())); }

    /* Anything else is okay */
    [^\r\n\"\\]+                 { sb.append( yytext() ); }
}

/* Fallthrough case: anything not matched above is an error */
.|\n                             { eq.enqueue(ErrorInfo.LEXICAL_ERROR,
                                              "Illegal character \"" +
                                              yytext() + "\"", pos()); }

<<EOF>> { Position mypos = pos();

          if (inComment) 
           { // for some reason EOF gets reported twice and only want to issue
             //    error once. 
             if (!reportedUnclosedComment) 
               { eq.enqueue(ErrorInfo.LEXICAL_ERROR, 
                       "unclosed comment at EOF, expecting */", 
                        new Position(file(),mypos.line()-1));
                 reportedUnclosedComment = true;
               }
           }

         return new EOF(new Position(file(),mypos.line()-1), sym.EOF); 

        }
                        
