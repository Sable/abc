package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.Node_c;

public class FieldPattern_c extends Node_c 
                            implements FieldPattern
{
   
    List modifiers;
    TypePatternExpr type;
    ClassTypeDotId name;

    public FieldPattern_c(Position pos,
			  List modifiers,
			  TypePatternExpr type,
			  ClassTypeDotId name) {
        super(pos);
	this.modifiers = modifiers;
	this.type = type;
	this.name = name;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {

       for (Iterator i = modifiers.iterator(); i.hasNext(); ) {
	    ModifierPattern f = (ModifierPattern) i.next();
	    print(f, w, tr);

	    if (i.hasNext()) {
		w.write(",");
		w.allowBreak(0, " ");
	    }
       }

       w.write(" ");
       
       print(type,w,tr);

       w.write(" ");

       print(name,w,tr);

    }


}
