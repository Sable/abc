package polyglot.ext.aspectj.ast;

import polyglot.ext.jl.ast.CharLit_c;
import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

public class FixCharLit_c extends CharLit_c
{

    public FixCharLit_c(Position pos, char value) {
	super(pos,value);
    }

 /** Write character to output file - unicode added by ODM  */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("'");
	w.write(StringUtil.unicodeEscape((char) value));
        w.write("'");
    }

}  
