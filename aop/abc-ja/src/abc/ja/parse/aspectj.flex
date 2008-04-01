/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Pavel Avgustinov
 * Copyright (C) 2008 Torbjörn Ekman
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

package abc.ja.parse;

import beaver.Symbol;
import beaver.Scanner;
import abc.ja.parse.JavaParser.Terminals;
import abc.aspectj.parse.AbcLexer;
import abc.aspectj.parse.LexerAction;

import polyglot.lex.*;
import polyglot.util.Position;
import polyglot.util.ErrorQueue;
import polyglot.util.ErrorInfo;
import java.util.HashMap;
import java.util.Stack;
import polyglot.ext.jl.parse.*;
import java.math.BigInteger;

import java.io.*;

%%

%public
%class JavaScanner
%extends Scanner
%implements AbcLexer
%type Symbol
%function nextToken
%yylexthrow Scanner.Exception

%unicode
%pack

%line
%column

%{
  private Symbol sym(int id) {
    lastTokenWasDot = (str().equals("."));
    return new Symbol((short)id, yyline + 1, yycolumn + 1, len(), str());
  }

  private Symbol sym(int id, String value) {
    lastTokenWasDot = (str().equals("."));
    return new Symbol((short)id, yyline + 1, yycolumn + 1, len(), value);
  }

  private String str() { return yytext(); }
  private int len() { return yylength(); }

  private void error(String msg) throws Scanner.Exception {
    throw new Scanner.Exception(yyline + 1, yycolumn + 1, msg);
  }

/* -------------------------- added for AspectJ ---------------------- */

    /* Counters added to get out of scanning states for AspectJ */
    private static int curlyBraceLevel = 0; // nesting of {}
    private static int parenLevel = 0; // nesting of ()
    private static boolean inPerPointcut = false; // currently in a per pointcut 
    private static boolean reportedUnclosedComment = false; 
    private static boolean inComment = false;

	/* Any change of lexer state should be done using this method rather than yybegin() - it
		keeps track of the necessary information on the stack. */
	public void enterLexerState(int state) {
		nestingStack.push(new NestingState(curlyBraceLevel, parenLevel, yystate()));
		// System.out.println("Pushing state [" + curlyBraceLevel+ ", " + parenLevel + ", " + yystate() + "] onto stack and switching to " + state + " after token '" + yytext() + "'.");
		yybegin(state);
	}
	
	/* Any change of lexer state to an enclosing state should be done using this method, as it
		pops the necessary information from the stack. */
	public void returnToPrevState() {
		if(nestingStack.isEmpty()) throw new polyglot.util.InternalCompilerError("Stack underflow while lexing.");
		NestingState ns = (NestingState)nestingStack.pop();
		// System.out.println("Popped state " + ns + " from stack and switching away from " + yystate() + " after token '" + yytext() + "'.");
		yybegin(ns.state);
	}

    /* Need a nestingStack to keep track of the nesting of lexer states.

       Each time new state is entered, a stackState
       of (curlyBraceLevel,parenLevel,yystate()) is pushed and then current
       state becomes the state in question.

       Each time a LEFTBRACE is reached,  the curlyBraceLevel is incremented.

       Each time a RIGHTBRACE is reached, the curlyBraceLevel is decremented,
       and the new curlyBraceLevel is checked against the level stored on
       top of the nestingStack for the lexer states that are terminated by a curly
       brace, i.e. YYINITIAL and AspectJ.   If the levels are equal, then we are
       exiting a class, interface or aspect declaration, and so we pop
       the top state and put the scanner in that state.
       
       Each time a LEFTPAREN is reached, the parenLevel is incremented.
       
       Each time a RIGHTPAREN is reached, the parenLevel is decremented, and
       the new parenLevel is checked against the level stored on the top of the
       nestingStack for the lexer states that are terminated by a round parenthesis,
       i.e. POINTCUT and POINTCUTIFEXPR. If the levels are equal, pop the top state
       and return to it.
       
       A 'declare' POINTCUT state pops the preceding state from the stack when it 
       encounters a ';'.
       
       The lexer states for comments, string and char literals are responsible for
       popping the relevant lexer state off the stack themselves, when they encounter
       the closing token.
     */

    private static Stack nestingStack = new Stack();

    class NestingState {
         int nestingLevel;  /* current nesting level of { }, should be >= 0 */
         int parenLevel;    /* Same for the nesting of ( ), should be >= 0 */
         int state;  /* should be one of the lexer states */

         NestingState(int l, int p, int s)
           { nestingLevel=l;
           	 parenLevel = p;
             state = s;
           }
         
         public String toString() {
         	return "BraceLevel: " + nestingLevel + ", ParenLevel: " + parenLevel + ", state: " + state;
         }
     }

      public static void reset() {
    	curlyBraceLevel = 0;
    	parenLevel = 0;
    	inPerPointcut = false;
    	reportedUnclosedComment = false;
    	inComment = false;
    	// currentState = YYINITIAL; // PA: I'm not sure where this actually alters anything.
    				// Really it should be yybegin(YYINITIAL), but that can't be called from
    				// a static context...
    	nestingStack = new Stack();
    }

/* ------------------------------------------------------------------- */

    StringBuffer sb = new StringBuffer();
    String file;
    ErrorQueue eq;
    HashMap javaKeywords, pointcutKeywords, aspectJKeywords, pointcutIfExprKeywords;
    boolean lastTokenWasDot;

	public void addJavaKeyword(String keyword, LexerAction la) {
		javaKeywords.put(keyword, la);
	}
	
	public void addAspectJKeyword(String keyword, LexerAction la) {
		aspectJKeywords.put(keyword, la);
	}
	
	public void addPointcutKeyword(String keyword, LexerAction la) {
		pointcutKeywords.put(keyword, la);
	}
	
	public void addPointcutIfExprKeyword(String keyword, LexerAction la) {
		pointcutIfExprKeywords.put(keyword, la);
	}
	
	public void addGlobalKeyword(String keyword, LexerAction la) {
		addJavaKeyword(keyword, la);
		addAspectJKeyword(keyword, la);
		addPointcutKeyword(keyword, la);
		addPointcutIfExprKeyword(keyword, la);
	}
	
	public void addAspectJContextKeyword(String keyword, LexerAction la) {
		addAspectJKeyword(keyword, la);
		addPointcutIfExprKeyword(keyword, la);
	}

    public JavaScanner(java.io.InputStream in, String file, ErrorQueue eq) {
        this(new java.io.BufferedReader(new java.io.InputStreamReader(in)),
             file, eq);
    }

	{
	    this.javaKeywords = new HashMap();
        this.pointcutKeywords = new HashMap();
        this.aspectJKeywords = new HashMap();
        this.pointcutIfExprKeywords = new HashMap();
        abc.main.Main.v().getAbcExtension().initLexerKeywords(this);
	}
    
    public JavaScanner(java.io.Reader reader, String file, ErrorQueue eq) {
        this(new abc.ja.parse.Unicode(reader));
        this.file = file;
        this.eq = eq;
    }

	// These methods should be used to access the values of the state constants,
	// which could be required when constructing LexerAction objects.
	public int java_state() { return YYINITIAL; }
	public int aspectj_state() { return ASPECTJ; }
	public int pointcut_state() { return POINTCUT; }
	public int pointcutifexpr_state() { return POINTCUTIFEXPR; }

	public void setInPerPointcut(boolean b) {
		inPerPointcut = b;
	}
	
	public int currentState() {
		return yystate();
	}
	
	public boolean getLastTokenWasDot() {
		return lastTokenWasDot;
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

    private Symbol key(int symbol) {
        lastTokenWasDot = false;
        return sym(symbol);
    }

    private Symbol op(int symbol) {
        lastTokenWasDot = (symbol == Terminals.DOT);
        return sym(symbol);
    }

    private Symbol id() {
        lastTokenWasDot = false;
        return sym(Terminals.IDENTIFIER);
    }

    /* ---- added for id patterns, needed in Pointcuts  --- */
    private Symbol id_pattern() {
	//System.out.println("ID pattern: " + yytext());
        lastTokenWasDot = false;
        return sym(Terminals.IDENTIFIERPATTERN);
    }

    private Symbol char_lit(String s) {
        lastTokenWasDot = false;
        if (s.length() == 1) {
            char x = s.charAt(0);
            return sym(Terminals.CHARACTER_LITERAL, s);
        }
        else {
            eq.enqueue(ErrorInfo.LEXICAL_ERROR,
                       "Illegal character literal \'" + s + "\'", pos(s.length()));
            return sym(Terminals.CHARACTER_LITERAL, s);
        }
    }

    private Symbol boolean_lit(boolean x) {
        lastTokenWasDot = false;
        return sym(Terminals.BOOLEAN_LITERAL);
    }

    private Symbol null_lit() {
        lastTokenWasDot = false;
        return sym(Terminals.NULL_LITERAL);
    }

    private Symbol string_lit() {
        lastTokenWasDot = false;
        return sym(Terminals.STRING_LITERAL, sb.toString());
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
    return sym(Terminals.EOF);
%eofval}

%state NESTABLECOMMENT, NONNESTABLECOMMENT

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


HexadecimalFloatingPointLiteral = {HexSignificand} {BinaryExponent}

HexSignificand = {HexNumeral} [\.]?
 | 0 [xX] [0-9a-fA-F]* \. [0-9a-fA-F]+

BinaryExponent = [pP] [+-]? [0-9]+
           
            
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
   we entered. 
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
<YYINITIAL,ASPECTJ,POINTCUTIFEXPR,POINTCUT> {
    /* 3.7 Comments */
	"/*"                       { if(abc.main.options.OptionsParser.v().nested_comments()) 
						enterLexerState(NESTABLECOMMENT);
				     else 
						enterLexerState(NONNESTABLECOMMENT); 
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
  "("                            { parenLevel++; return op(Terminals.LPAREN); }

  /* if we have finished an expression found in the pointcut if, must
        return to POINTCUT state. */
  ")"                            { parenLevel--; 
                                   if ( (yystate() == POINTCUTIFEXPR) &&
                                        (parenLevel == ((NestingState)nestingStack.peek()).parenLevel))
                                      { returnToPrevState();
                                      } 
                                   return op(Terminals.RPAREN); 
                                 }
  "{"                            { curlyBraceLevel++; return op(Terminals.LBRACE); }
  "}"                            { curlyBraceLevel--; 
                                   
                                     /* if curlyBraceLevel is same as
                                          top of nestingStack, then 
                                          exiting a class, interface or
                                          aspect declaration. */
                                          
                                     if (!nestingStack.isEmpty())
                                       { if (curlyBraceLevel == 
                                           ((NestingState) nestingStack.peek()).
                                                                  nestingLevel)
                                           { returnToPrevState();
                                             }
                                          }
                                        else // an extra }
                                          { // don't change the state
                                            // hand over token for parser to
                                            // find sytax error
                                          }
                                        return op(Terminals.RBRACE); 
                                 }
    "["    { return op(Terminals.LBRACK);    }
    "]"    { return op(Terminals.RBRACK);    }
    ";"    { return op(Terminals.SEMICOLON); }
    ","    { return op(Terminals.COMMA);     }
    "."    { return op(Terminals.DOT);       }

    /* 3.12 Operators */
    "="    { return op(Terminals.EQ);         }
    ">"    { return op(Terminals.GT);         }
    "<"    { return op(Terminals.LT);         }
    "!"    { return op(Terminals.NOT);        }
    "~"    { return op(Terminals.COMP);       }
    "?"    { return op(Terminals.QUESTION);   }
    ":"    { return op(Terminals.COLON);      }
    "=="   { return op(Terminals.EQEQ);       }
    "<="   { return op(Terminals.LTEQ);       }
    ">="   { return op(Terminals.GTEQ);       }
    "!="   { return op(Terminals.NOTEQ);      }
    "&&"   { return op(Terminals.ANDAND);     }
    "||"   { return op(Terminals.OROR);       }
    "++"   { return op(Terminals.PLUSPLUS);   }
    "--"   { return op(Terminals.MINUSMINUS); }
    "+"    { return op(Terminals.PLUS);       }
    "-"    { return op(Terminals.MINUS);      }
    "*"    { return op(Terminals.MULT);       }
    "/"    { return op(Terminals.DIV);        }
    "&"    { return op(Terminals.AND);        }
    "|"    { return op(Terminals.OR);         }
    "^"    { return op(Terminals.XOR);        }
    "%"    { return op(Terminals.MOD);        }
    "<<"   { return op(Terminals.LSHIFT);     }
    ">>"   { return op(Terminals.RSHIFT);     }
    ">>>"  { return op(Terminals.URSHIFT);    }
    "+="   { return op(Terminals.PLUSEQ);     }
    "-="   { return op(Terminals.MINUSEQ);    }
    "*="   { return op(Terminals.MULTEQ);     }
    "/="   { return op(Terminals.DIVEQ);      }
    "&="   { return op(Terminals.ANDEQ);      }
    "|="   { return op(Terminals.OREQ);       }
    "^="   { return op(Terminals.XOREQ);      }
    "%="   { return op(Terminals.MODEQ);      }
    "<<="  { return op(Terminals.LSHIFTEQ);   }
    ">>="  { return op(Terminals.RSHIFTEQ);   }
    ">>>=" { return op(Terminals.URSHIFTEQ);  }
    "@"    { return op(Terminals.AT); }
    "..."  { return op(Terminals.ELLIPSIS); }


    /* 3.10.4 Character Literals */
    \'      { enterLexerState(CHARACTER); sb.setLength(0); }

    /* 3.10.5 String Literals */
    \"      { enterLexerState(STRING); sb.setLength(0); }

 // 3.10.1 Integer Literals
  {DecimalNumeral}               { return sym(Terminals.INTEGER_LITERAL); }
  {DecimalNumeral} [lL]          { return sym(Terminals.LONG_LITERAL, str().substring(0,len()-1)); }

  {HexNumeral}                   { return sym(Terminals.INTEGER_LITERAL); }
  {HexNumeral} [lL]              { return sym(Terminals.LONG_LITERAL, str().substring(0, len()-1)); }

  {OctalNumeral}                 { return sym(Terminals.INTEGER_LITERAL); }
  {OctalNumeral} [lL]            { return sym(Terminals.LONG_LITERAL, str().substring(0, len()-1)); }

  // 3.10.2 Floating-Point Literals
  {FloatingPointLiteral} [fF]    { return sym(Terminals.FLOATING_POINT_LITERAL, str().substring(0,len()-1)); }
  {FloatingPointLiteral} [dD]    { return sym(Terminals.DOUBLE_LITERAL, str().substring(0,len()-1)); }
  {FloatingPointLiteral}         { return sym(Terminals.DOUBLE_LITERAL); }
  [0-9]+ {ExponentPart}? [fF]    { return sym(Terminals.FLOATING_POINT_LITERAL, str().substring(0,len()-1)); }
  [0-9]+ {ExponentPart}? [dD]    { return sym(Terminals.DOUBLE_LITERAL, str().substring(0,len()-1)); }

  {HexadecimalFloatingPointLiteral} [fF]    { return sym(Terminals.FLOATING_POINT_LITERAL, str().substring(0,len()-1)); }
  {HexadecimalFloatingPointLiteral} [dD]    { return sym(Terminals.DOUBLE_LITERAL, str().substring(0,len()-1)); }
  {HexadecimalFloatingPointLiteral}         { return sym(Terminals.DOUBLE_LITERAL); }

}

<POINTCUT> {
/* Symbols for pointcuts */

  /* symbols that are in normal Java states too, we repeat them here 
     because either they have a different action when in POINTCUT
     state, or we just need them to parse things like types in
     pointcuts. */
  "("                            { parenLevel++; return op(Terminals.LPAREN); }
  ")"                            { parenLevel--;
                                   if (inPerPointcut &&
                                       parenLevel == ((NestingState)nestingStack.peek()).parenLevel)
                                     { returnToPrevState();
                                       //currentState = IN_ASPECTJ;
                                       inPerPointcut = false;
                                     }
                                   return op(Terminals.RPAREN); 
                                 }
  "["                            { return op(Terminals.LBRACK); }
  "]"                            { return op(Terminals.RBRACK); } 
  ","                            { return op(Terminals.COMMA); }
  "."                            { return op(Terminals.DOT); }
  ":"                            { return op(Terminals.COLON);}
  ";"                            { returnToPrevState();
                                   return op(Terminals.SEMICOLON); 
                                 }
  "{"                            { curlyBraceLevel++;
                                   returnToPrevState();
                                   return op(Terminals.LBRACE); 
                                 }
  \"                             { enterLexerState(STRING); sb.setLength(0); }

  /* symbol specific to pointcuts */
  ".."                           {  return op(Terminals.PC_DOTDOT); } 

  /* operators specific to pointcuts  */
  "!"                            { return op(Terminals.PC_NOT); }
  "&&"                           { return op(Terminals.PC_ANDAND); }
  "||"                           { return op(Terminals.PC_OROR); }
  "+"                            { return op(Terminals.PC_PLUS); }
  
/* Note that if both IdentifierPattern and * match, then * will be
   chosen first, since it is an earlier rule.
*/
  
  "*"                            { return op(Terminals.PC_MULT); }
}

/* We need some cases to distinguish the different lexer states from one another - otherwise
 * they Java, Pointcut and AspectJ get collapsed into the same state by optimisations.
 */
<YYINITIAL> {
	"java_state"					{ return id(); }
}

<ASPECTJ> {
	"aspectj_state"				{ return id(); }
}

<POINTCUTIFEXPR> {
	"pointcutifexpr_state"			{ return id(); }
}

<YYINITIAL,ASPECTJ,POINTCUTIFEXPR,POINTCUT> {
	/* Handle keywords and identifiers here. It has to be done last because with the new parser structure,
		whether or not an identifier is a keyword is only determined after it has been consumed, so
		that splitting this code between the different lexer states would consume identifiers
		(and hence potential keywords for a later state) at the first occurrence. (yes, this comment does
		make sense to me. :-P) */
		
    /* 3.9 Keywords */
    /* 3.8 Identifiers */
    {Identifier}   { 
    	// Keywords common to all states first.
    	LexerAction la;
    	switch(yystate()) {
    		case YYINITIAL:
    			la = (LexerAction) javaKeywords.get(yytext());
				break;
				
    		case ASPECTJ:
    			la = (LexerAction) aspectJKeywords.get(yytext());
				break;
				
    		case POINTCUT:
    			la = (LexerAction) pointcutKeywords.get(yytext());
				break;
				
    		case POINTCUTIFEXPR:
    			la = (LexerAction) pointcutIfExprKeywords.get(yytext());
				break;
			
			default:
				la = null; // Will never happen - this pattern only matches if state is one of the 4.
		}
		
		if(la != null) {
			return key(la.getToken(this));
		}
		
		// OK, it's not a keyword, so it's an identifier.
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

<NESTABLECOMMENT> { 
  "*/"			{ 
				comment_count = comment_count - 1; 
		 	   	if (comment_count < 0) 
	                	       	eq.enqueue(ErrorInfo.LEXICAL_ERROR,"unmatched */",pos());
	                        if (comment_count == 0) {
                            		inComment = false;
    		            	       	returnToPrevState(); 
                            	}
			}
  "/*"              { 
				comment_count = comment_count + 1; 
		    }
  .|\n                           { /* ignore */ }
}

<NONNESTABLECOMMENT> { 
  "*/"				{ 
					inComment = false;
					returnToPrevState();
				}
  .|\n                           { /* ignore */ }
}

<CHARACTER> {
    /* End of the character literal */
    \'                           { returnToPrevState();
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
    {LineTerminator}             { returnToPrevState();
                                  eq.enqueue(ErrorInfo.LEXICAL_ERROR,
                                             "Unclosed character literal",
                                             pos(sb.length())); }

    /* Anything else is okay */
    [^\r\n\'\\]+                 { sb.append( yytext() ); }
}

<STRING> {
    /* End of string */
    \"                           { returnToPrevState();
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
    {LineTerminator}             { returnToPrevState();
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

//		if(!eq.hasErrors())
//			if(!nestingStack.isEmpty()) System.out.println("XXXXXXXXX: Failed to consume entire stack while lexing: " + nestingStack);

         return sym(Terminals.EOF); 

        }
                        
