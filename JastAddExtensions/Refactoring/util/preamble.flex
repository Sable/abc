package scanner;

import beaver.Symbol;
import beaver.Scanner;
import parser.JavaParser.Terminals;
import java.io.*;
import java.util.HashMap;
import AST.FileRange;

%%

%public 
%final 
%class JavaScanner
%extends Scanner

%type Symbol 
%function nextToken 
%yylexthrow Scanner.Exception

%unicode
%line %column %char

%{
  StringBuffer strbuf = new StringBuffer(128);
  int sub_line;
  int sub_column;
  int strlit_start_line, strlit_start_column;

  private Symbol sym(short id) {
    return new Symbol(id, yyline + 1, yycolumn + 1, len(), str());
  }

  private Symbol sym(short id, String value) {
    return new Symbol(id, yyline + 1, yycolumn + 1, len(), value);
  }

  private Symbol sym(short id, String value, int start_line, int start_column, int len) {
    return new Symbol(id, start_line, start_column, len, value);
  }

  private String str() { return yytext(); }
  private int len() { return yylength(); }

  private void error(String msg) throws Scanner.Exception {
    throw new Scanner.Exception(yyline + 1, yycolumn + 1, msg);
  }
  
  private HashMap<FileRange, String> comments = new HashMap<FileRange, String>();
  public HashMap<FileRange, String> comments() { return comments; }
  private void registerComment() {
    String comment = str();
    int startline = yyline + 1;
    int startcol = yycolumn + 1;
    int endline;
    int endcol;
    String eol = System.getProperty("line.separator");     
    String[] lines = comment.split(eol);
    int n = lines.length;
    if(n > 0 && lines[n-1].equals(""))
    	--n;
    endline = startline + n - 1;
    if(n == 1)
    	endcol = startcol + lines[0].length() - 1;
    else
    	endcol = lines[n-1].length();
    comments.put(new FileRange("", startline, startcol, endline, endcol), str());
  }

  private HashMap offsets = new java.util.LinkedHashMap();
  public HashMap offsets() { return offsets; }
  private void registerOffset() {
    Integer key = new Integer(yyline + 2);
    Integer value = new Integer(yychar + len());
    offsets.put(key, value);
  }
  
%}


