package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.Node_c;

public class MethodPattern_c extends Node_c 
                             implements  MethodPattern
{

   
    List modifiers;       // of ModifierPattern
    TypePatternExpr type;
    ClassTypeDotId name;
    List formals;         // of FormalPattern
    ClassnamePatternExpr throwpat;

    public MethodPattern_c(Position pos,
                           List modifiers,
                           TypePatternExpr type,
                           ClassTypeDotId name,
			   List formals,
                           ClassnamePatternExpr throwpat) {
        super(pos);
	this.modifiers = modifiers;
	this.type = type;
	this.name = name;
        this.formals = formals;
        this.throwpat = throwpat;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {

       w.begin(2);
       for (Iterator i = modifiers.iterator(); i.hasNext(); ) {
	    ModifierPattern f = (ModifierPattern) i.next();
	    print(f, w, tr);
       }

       print(type,w,tr);

       w.allowBreak(0, " ");

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
       w.end();
    }
}
