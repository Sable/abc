/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *D
 * Copyright (C) 1998-2001  Gerwin Klein <lsf@jflex.de>                    *
 * Copyright (C) 2004 Laurie Hendren (extensions to AspectJ)               *
 *                       <hendren@cs.mcgill.ca>
 * All rights reserved.                                                    *
 *                                                                         *
 * This program is free software; you can redistribute it and/or modify    *
 * it under the terms of the GNU General Public License. See the file      *
 * COPYRIGHT for more information.                                         *
 *                                                                         *
 * This program is distributed in the hope that it will be useful,         *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of          *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           *
 * GNU General Public License for more details.                            *
 *                                                                         *
 * You should have received a copy of the GNU General Public License along *
 * with this program; if not, write to the Free Software Foundation, Inc., *
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA                 *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/* Java 1.2 language lexer specification */

/* Use together with unicode.flex for Unicode preprocesssing */
/* and java12.cup for a Java 1.2 parser                      */

/* Note that this lexer specification is not tuned for speed.
   It is in fact quite slow on integer and floating point literals, 
   because the input is read twice and the methods used to parse
   the numbers are not very fast. 
   For a production quality application (e.g. a Java compiler) 
   this could be optimized */

package abc.aspectj.parse;

import java_cup.runtime.Symbol;
import polyglot.lex.*;
import polyglot.util.Position;
import polyglot.util.ErrorQueue;
import polyglot.util.ErrorInfo;
import java.util.Stack;
import polyglot.ext.jl.parse.*;

//import soot.javaToJimple.jj.DPosition;


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
      { switch (savedState)
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

/* ------------------------------------------------------------------- */

    StringBuffer sb = new StringBuffer();
    String file;
    ErrorQueue eq;

    boolean lastTokenWasDot;

    public Lexer_c(java.io.InputStream in, String file, ErrorQueue eq) {
        this(new java.io.BufferedReader(new java.io.InputStreamReader(in)),
             file, eq);
    }
    
    public Lexer_c(java.io.Reader reader, String file, ErrorQueue eq) {
        this(new EscapedUnicodeReader(reader));
        this.file = file;
        this.eq = eq;
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

    private Token int_token(String s, int radix) {
        lastTokenWasDot = false;
        int x = parseInt(s, radix);
        return new IntegerLiteral(pos(), x, sym.INTEGER_LITERAL);
    }

    private Token long_token(String s, int radix) {
        lastTokenWasDot = false;
        long x = parseLong(s, radix);
        return new LongLiteral(pos(), x, sym.LONG_LITERAL);
    }

    private Token float_token(String s) {
        lastTokenWasDot = false;
        float x = Float.valueOf(s).floatValue();
        return new FloatLiteral(pos(), x, sym.FLOAT_LITERAL);
    }

    private Token double_token(String s) {
        lastTokenWasDot = false;
        double x = Double.valueOf(s).doubleValue();
        return new DoubleLiteral(pos(), x, sym.DOUBLE_LITERAL);
    }
    
    private Token char_token(char x) {
        lastTokenWasDot = false;
        return new CharacterLiteral(pos(), x, sym.CHARACTER_LITERAL);
    }

    private Token boolean_token(boolean x) {
        lastTokenWasDot = false;
        return new BooleanLiteral(pos(), x, sym.BOOLEAN_LITERAL);
    }

    private Token null_token() {
        lastTokenWasDot = false;
        return new NullLiteral(pos(), sym.NULL_LITERAL);
    }

    private Token string_token() {
        lastTokenWasDot = false;
        return new StringLiteral(pos(sb.length()), 
                                 sb.toString(), 
                                 sym.STRING_LITERAL);
    }


  private int parseInt(String s, int radix) {
        int r = (int)(parseLong(s,radix));
        return r;
  }

  /* assumes correct representation of a long value for 
     specified radix in String s */
  private long parseLong(String s, int radix) {
    int max = s.length();
    long result = 0;
    long digit;

    for (int i = 0; i < max; i++) {
      digit = Character.digit(s.charAt(i), radix);
      result *= radix;
      result += digit;
    }

    return result;
  }

  private int comment_count = 0;

%}

%eofval{
        return new EOF(pos(), sym.EOF); 
%eofval}

%state COMMENT

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = {LineTerminator} | [ \t\f]

/* comments */

EndOfLineComment = "//" {InputCharacter}* {LineTerminator}


/* identifiers */
Identifier = [:jletter:][:jletterdigit:]*

/* Used in pointcut names */
IdentifierPattern = 
    ( "*" | [:jletter:] ) ( "*" | [:jletterdigit:] )*

/* integer literals */
DecIntegerLiteral =  (0 | [1-9][0-9]*)
DecLongLiteral    =  {DecIntegerLiteral} [lL]

HexIntegerLiteral =  0 [xX] 0* {HexDigit} {1,8}
HexLongLiteral    =  0 [xX] 0* {HexDigit} {1,16} [lL]
HexDigit          =  [0-9a-fA-F]

OctIntegerLiteral =  0+ [1-3]? {OctDigit} {1,15}
OctLongLiteral    =  0+ 1? {OctDigit} {1,21} [lL]
OctDigit          =  [0-7]
    
/* floating point literals */        
FloatLiteral  = ({FLit1}|{FLit2}|{FLit3}|{FLit4}) [fF]
DoubleLiteral = {FLit1}|{FLit2}|{FLit3}|{FLit4}

FLit1 = [0-9]+ \. [0-9]* {Exponent}?
FLit2 = \. [0-9]+ {Exponent}?
FLit3 = [0-9]+ {Exponent}
FLit4 = [0-9]+ {Exponent}?

Exponent = [eE] [+\-]? [0-9]+

/* string and character literals */
StringCharacter = [^\r\n\"\\]
SingleCharacter = [^\r\n\'\\]

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

%state STRING, CHARLITERAL 

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
  "if"                           { return key(sym.IF); }
  "this"                         { return key(sym.THIS); }
  /* ------------  keyword added to the Java part ------------------ */
  "aspect"                       { yybegin(ASPECTJ); 
                                   nestingStack.push(
                                      new NestingState(
                                         curlyBraceLevel, savedState));
                                   savedState = IN_ASPECTJ;
                                   javaOrAspect = IN_ASPECTJ;  
                                   return key(sym.ASPECT); 
                                 }
  "pointcut"                     { yybegin(POINTCUT);
                                   savedState = IN_POINTCUT;
                                   return key(sym.POINTCUT);
                                 }
  /* ----------------------------------------------------------------*/
}

<YYINITIAL,ASPECTJ,POINTCUTIFEXPR,POINTCUT> {

  /* keywords */
  "abstract"                     { return key(sym.ABSTRACT); }
  "boolean"                      { return key(sym.BOOLEAN); }
  "break"                        { return key(sym.BREAK); }
  "byte"                         { return key(sym.BYTE); }
  "case"                         { return key(sym.CASE); }
  "catch"                        { return key(sym.CATCH); }
  "char"                         { return key(sym.CHAR); }
  "class"                        { if (!lastTokenWasDot) {
					yybegin(YYINITIAL);
                                   	nestingStack.push(
                                      		new NestingState(
                                         	curlyBraceLevel, savedState));
                                   	savedState = YYINITIAL;
                                   }
                                   return key(sym.CLASS); 
                                 }
  "const"                        { return key(sym.CONST); }
  "continue"                     { return key(sym.CONTINUE); }
  "do"                           { return key(sym.DO); }
  "double"                       { return key(sym.DOUBLE); }
  "else"                         { return key(sym.ELSE); }
  "extends"                      { return key(sym.EXTENDS); }
  "final"                        { return key(sym.FINAL); }
  "finally"                      { return key(sym.FINALLY); }
  "float"                        { return key(sym.FLOAT); }
  "for"                          { return key(sym.FOR); }
  "default"                      { return key(sym.DEFAULT); }
  "implements"                   { return key(sym.IMPLEMENTS); }
  "import"                       { return key(sym.IMPORT); }
  "instanceof"                   { return key(sym.INSTANCEOF); }
  "int"                          { return key(sym.INT); }
  "interface"                    { yybegin(YYINITIAL);
                                   nestingStack.push(
                                      new NestingState(
                                         curlyBraceLevel, savedState));
                                   savedState = YYINITIAL;
                                   return key(sym.INTERFACE); 
                                 }
  "long"                         { return key(sym.LONG); }
  "native"                       { return key(sym.NATIVE); }
  "new"                          { return key(sym.NEW); }
  "goto"                         { return key(sym.GOTO); }
  "public"                       { return key(sym.PUBLIC); }
  "short"                        { return key(sym.SHORT); }
  "super"                        { return key(sym.SUPER); }
  "switch"                       { return key(sym.SWITCH); }
  "synchronized"                 { return key(sym.SYNCHRONIZED); }
  "package"                      { return key(sym.PACKAGE); }
  "private"                      { return key(sym.PRIVATE); }
  /* ------------  keyword added to the Java part ------------------ */
  "privileged"                   { return key(sym.PRIVILEGED); }
  /* ----------------------------------------------------------------*/
  "protected"                    { return key(sym.PROTECTED); }
  "transient"                    { return key(sym.TRANSIENT); }
  "return"                       { return key(sym.RETURN); }
  "void"                         { return key(sym.VOID); }
  "static"                       { return key(sym.STATIC); }
  "while"                        { return key(sym.WHILE); }
  "throw"                        { return key(sym.THROW); }
  "throws"                       { return key(sym.THROWS); }
  "try"                          { return key(sym.TRY); }
  "volatile"                     { return key(sym.VOLATILE); }
  "strictfp"                     { return key(sym.STRICTFP); }
  "assert"                       { return key(sym.ASSERT); }

  /* boolean literals */
  "true"                         { return boolean_token(true); }
  "false"                        { return boolean_token(false); }

  /* null literal */
  "null"                         { return null_token(); }

  /* comments */
  "/*"                           { yybegin(COMMENT); comment_count = comment_count + 1; }
  "*/"                           { eq.enqueue(ErrorInfo.LEXICAL_ERROR,"unmatched */",pos()); }

  {EndOfLineComment}             { /* ignore */ }

  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
}

/* ASPECTJ reserved words, these cannot be used as the names for
   any identifiers within aspect code.  */

<ASPECTJ,POINTCUTIFEXPR> {
  "after"                         { yybegin(POINTCUT); 
                                    savedState = IN_POINTCUT;
                                    return key(sym.AFTER); 
                                  } 
  "around"                        { yybegin(POINTCUT);
                                    savedState = IN_POINTCUT;
                                    return key(sym.AROUND); 
                                  }
  "before"                        { yybegin(POINTCUT);
                                    savedState = IN_POINTCUT;
                                    return key(sym.BEFORE); 
                                  }
  "declare"                       { yybegin(POINTCUT);
                                    savedState = IN_POINTCUT;
                                    return key(sym.DECLARE); 
                                  }
  "issingleton"                   { return key(sym.ISSINGLETON); }
  "percflow"                      { yybegin(POINTCUT);
                                    savedState = IN_POINTCUT;
                                    inPerPointcut = true; 
                                    savedPerParenLevel = parenLevel;
                                    return key(sym.PERCFLOW); 
                                  }
  "percflowbelow"                 { yybegin(POINTCUT);
                                    savedState = IN_POINTCUT;
                                    inPerPointcut = true;
                                    savedPerParenLevel = parenLevel; 
                                    return key(sym.PERCFLOWBELOW); 
                                  }
  "pertarget"                     { yybegin(POINTCUT);
                                    savedState = IN_POINTCUT;
                                    inPerPointcut = true;
                                    savedPerParenLevel = parenLevel; 
                                    return key(sym.PERTARGET); 
                                  }
  "perthis"                       { yybegin(POINTCUT);
                                    savedState = IN_POINTCUT;
                                    inPerPointcut = true;
                                    savedPerParenLevel = parenLevel; 
                                    return key(sym.PERTHIS); 
                                  }
  "proceed"                       { return key(sym.PROCEED); }
  "thisEnclosingJoinPointStaticPart"  { return key(sym.THISENCLOSINGJOINPOINTSTATICPART); }
  "thisJoinPoint"                 { return key(sym.THISJOINPOINT); }
  "thisJoinPointStaticPart"       { return key(sym.THISJOINPOINTSTATICPART); }
}


/* Java-ish symbols and literals */
<YYINITIAL,ASPECTJ,POINTCUTIFEXPR> {

  /* separators */
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
                                       nestingStack.pop();
                                     }
                                   else /* we are in some nesting */
                                     /* if curlyBraceLevel is same as
                                          top of nestingStack, then 
                                          exiting a class, interface or
                                          aspect declaration. */
                                     if (curlyBraceLevel == 
                                         ((NestingState) nestingStack.peek()).
                                                                  nestingLevel)
                                       { NestingState s = 
                                           (NestingState) nestingStack.pop();
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
                                   return op(sym.RBRACE); 
                                 }
  "["                            { return op(sym.LBRACK); }
  "]"                            { return op(sym.RBRACK); }
  ";"                            { return op(sym.SEMICOLON); }
  ","                            { return op(sym.COMMA); }
  "."                            { return op(sym.DOT); }

  /* operators */
  "="                            { return op(sym.EQ); }
  ">"                            { return op(sym.GT); }
  "<"                            { return op(sym.LT); }
  "!"                            { return op(sym.NOT); }
  "~"                            { return op(sym.COMP); }
  "?"                            { return op(sym.QUESTION); }
  ":"                            { return op(sym.COLON); }
  "=="                           { return op(sym.EQEQ); }
  "<="                           { return op(sym.LTEQ); }
  ">="                           { return op(sym.GTEQ); }
  "!="                           { return op(sym.NOTEQ); }
  "&&"                           { return op(sym.ANDAND); }
  "||"                           { return op(sym.OROR); }
  "++"                           { return op(sym.PLUSPLUS); }
  "--"                           { return op(sym.MINUSMINUS); }
  "+"                            { return op(sym.PLUS); }
  "-"                            { return op(sym.MINUS); }
  "*"                            { return op(sym.MULT); }
  "/"                            { return op(sym.DIV); }
  "&"                            { return op(sym.AND); }
  "|"                            { return op(sym.OR); }
  "^"                            { return op(sym.XOR); }
  "%"                            { return op(sym.MOD); }
  "<<"                           { return op(sym.LSHIFT); }
  ">>"                           { return op(sym.RSHIFT); }
  ">>>"                          { return op(sym.URSHIFT); }
  "+="                           { return op(sym.PLUSEQ); }
  "-="                           { return op(sym.MINUSEQ); }
  "*="                           { return op(sym.MULTEQ); }
  "/="                           { return op(sym.DIVEQ); }
  "&="                           { return op(sym.ANDEQ); }
  "|="                           { return op(sym.OREQ); }
  "^="                           { return op(sym.XOREQ); }
  "%="                           { return op(sym.MODEQ); }
  "<<="                          { return op(sym.LSHIFTEQ); }
  ">>="                          { return op(sym.RSHIFTEQ); }
  ">>>="                         { return op(sym.URSHIFTEQ); }

  /* string literal */
  \"                             { yybegin(STRING); sb.setLength(0); }

  /* character literal */
  \'                             { yybegin(CHARLITERAL); }

  /* numeric literals */

  {DecIntegerLiteral}            { return int_token(yytext(), 10); }
  {DecLongLiteral}               { return long_token(yytext().substring(0,yylength()-1), 10); }
  
  {HexIntegerLiteral}            { return int_token(yytext().substring(2), 16); }
  {HexLongLiteral}               { return long_token(yytext().substring(2,yylength()-1), 16); }
 
  {OctIntegerLiteral}            { return int_token(yytext(), 8); }  
  {OctLongLiteral}               { return long_token(yytext().substring(0,yylength()-1), 8); }
  
  {FloatLiteral}                 { return float_token(yytext().substring(0,yylength()-1)); }
  {DoubleLiteral}                { return double_token(yytext()); }
  {DoubleLiteral}[dD]            { return double_token(yytext().substring(0,yylength()-1)); }

  /* Identifiers for everything but in pointcuts */
  {Identifier}                   { return id(); }  

}
  
/* ------------- Symbols valid in pointcut designators --------------------
   We have redefined some symbols already existing for the ordinary 
   Java-ish states so that we can define the pointcut grammar independently 
   from the Java grammar and not worry about introducing new conflicts if 
   the pointcut grammar is extended.
   -------------------------------------------------------------------------*/ 
<POINTCUT> {
/* Keywords for pointcuts */

  "adviceexecution"              { return key(sym.PC_ADVICEEXECUTION); }
  "args"                         { return key(sym.PC_ARGS); }
  "call"                         { return key(sym.PC_CALL); } 
  "cflow"                        { return key(sym.PC_CFLOW); }
  "cflowbelow"                   { return key(sym.PC_CFLOWBELOW); }
  "error"                        { return key(sym.PC_ERROR); }
  "execution"                    { return key(sym.PC_EXECUTION); }
  "get"                          { return key(sym.PC_GET); }
  "handler"                      { return key(sym.PC_HANDLER); }
  "if"                           { yybegin(POINTCUTIFEXPR);
                                   savedState = IN_POINTCUTIFEXPR;
                                   savedParenLevel = parenLevel;
                                   return key(sym.PC_IF);  
                                 }
  "initialization"               { return key(sym.PC_INITIALIZATION); }
  "parents"                      { return key(sym.PC_PARENTS); }
  "precedence"                   { return key(sym.PC_PRECEDENCE); }
  "preinitialization"            { return key(sym.PC_PREINITIALIZATION); }
  "returning"                    { return key(sym.PC_RETURNING); }
  "set"                          { return key(sym.PC_SET); }
  "soft"                         { return key(sym.PC_SOFT); }
  "staticinitialization"         { return key(sym.PC_STATICINITIALIZATION); }
  "target"                       { return key(sym.PC_TARGET); }
  "this"                         { return key(sym.PC_THIS); }
  "throwing"                     { return key(sym.PC_THROWING); }
  "warning"                      { return key(sym.PC_WARNING); }
  "within"                       { return key(sym.PC_WITHIN); }
  "withincode"                   { return key(sym.PC_WITHINCODE); }
  
/* Special redefinition of aspect keyword, so that we don't go out of
   ASPECTJ state and remain in POINTCUT state */
  "aspect"                       { return key(sym.ASPECT); }


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
  ":"                            { return op(sym.COLON); }
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


/* Note that if both Identifier and Name Pattern match, then
   Identifier will be chosen first, since it is an earlier rule.
*/
  {Identifier}                   { return id(); }  

/* Identifier patterns,  to handle things like foo.. *foo *1a and so on.
   We don't want to parse them further because it is certainly meaningful
   to say things like *if*while*for  and we don't want to have to include
   reserved words explicitly.
*/
  
  {IdentifierPattern}      { return id_pattern(); }
}


<STRING> {
  \"                             { returnFromStringChar(); return string_token(); }
  
  {StringCharacter}+             { sb.append( yytext() ); }
  
  /* escape sequences */
  "\\b"                          { sb.append( '\b' ); }
  "\\t"                          { sb.append( '\t' ); }
  "\\n"                          { sb.append( '\n' ); }
  "\\f"                          { sb.append( '\f' ); }
  "\\r"                          { sb.append( '\r' ); }
  "\\\""                         { sb.append( '\"' ); }
  "\\'"                          { sb.append( '\'' ); }
  "\\\\"                         { sb.append( '\\' ); }
  \\[0-3]?{OctDigit}?{OctDigit}  { char val = (char) Integer.parseInt(yytext().substring(1),8);
				   sb.append(val); }
  
  /* error cases */
  \\.                            { eq.enqueue(ErrorInfo.LEXICAL_ERROR,
                                              "Illegal escape sequence \""+yytext()+"\"",
					      pos()); }
  {LineTerminator}               { eq.enqueue(ErrorInfo.LEXICAL_ERROR,
					      "Unterminated string at end of line",
					      pos()); }
}

<CHARLITERAL> {
  {SingleCharacter}\'            { returnFromStringChar(); return char_token(yytext().charAt(0)); }
  
  /* escape sequences */
  "\\b"\'                        { returnFromStringChar(); return char_token('\b');}
  "\\t"\'                        { returnFromStringChar(); return char_token('\t');}
  "\\n"\'                        { returnFromStringChar(); return char_token('\n');}
  "\\f"\'                        { returnFromStringChar(); return char_token('\f');}
  "\\r"\'                        { returnFromStringChar(); return char_token('\r');}
  "\\\""\'                       { returnFromStringChar(); return char_token('\"');}
  "\\'"\'                        { returnFromStringChar(); return char_token('\'');}
  "\\\\"\'                       { returnFromStringChar(); return char_token('\\'); }
  \\[0-3]?{OctDigit}?{OctDigit}\' { returnFromStringChar();
				    long val = parseLong(yytext().substring(1,yylength()-1), 8);
			            return char_token((char)val); }
  
  /* error cases */
  \\.                            { eq.enqueue(ErrorInfo.LEXICAL_ERROR,
                                              "Illegal escape sequence \""+yytext()+"\"",
					      pos()); }
  {LineTerminator}               { eq.enqueue(ErrorInfo.LEXICAL_ERROR,
                                              "Unterminated character literal at end of line",
					      pos()); }
}

				     

<COMMENT> { 
  "*/"				 { comment_count = comment_count - 1; 
				   if (comment_count < 0) 
                                     eq.enqueue(ErrorInfo.LEXICAL_ERROR,"unmatched */",pos());
	                           if (comment_count == 0) 
    		                     returnFromStringChar();
	                         }
  "/*"                           { comment_count = comment_count + 1; }
  .|\n                           { /* ignore */ }
}

/* error fallback */
.|\n                             { eq.enqueue(ErrorInfo.LEXICAL_ERROR,
                                              "Illegal character \""+yytext()+"\"",
	                                      pos()); }
