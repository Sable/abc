package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.Node_c;

import abc.aspectj.visit.*;

public class ConstructorPattern_c extends Node_c 
                                  implements  ConstructorPattern
{

    Position pos;
    List modifiers;       // of ModifierPattern
    ClassTypeDotNew name;
    List formals;         // of FormalPattern
    ClassnamePatternExpr throwpat;

    public ConstructorPattern_c(Position pos,
                                List modifiers,
                                ClassTypeDotNew name,
			        List formals,
                                ClassnamePatternExpr throwpat) {
        super(pos);
	this.pos = pos;
	this.modifiers = modifiers;
	this.name = name;
	this.formals = formals;
        this.throwpat = throwpat;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {

       for (Iterator i = modifiers.iterator(); i.hasNext(); ) {
	    ModifierPattern f = (ModifierPattern) i.next();
	    print(f, w, tr);
       }


       print(name,w,tr);

       w.write("(");
       w.begin(0);
       for (Iterator i = formals.iterator(); i.hasNext(); ) {
	    FormalPattern f = (FormalPattern) i.next();
	    print(f, w, tr);

	    if (i.hasNext()) {
		w.write(",");
		w.allowBreak(0, " ");
	    }
       }
       w.end();
       w.write(")");
       
       if (throwpat != null) {
	   w.write(" throws ");
	   print(throwpat,w,tr);
       }
    }

    public abc.weaving.aspectinfo.ConstructorPattern makeAIConstructorPattern() {
	return PatternMatcher.v().makeAIConstructorPattern(this);
    }
}
