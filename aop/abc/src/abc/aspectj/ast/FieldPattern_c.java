package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.Node_c;

import abc.aspectj.visit.PatternMatcher;

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

    protected FieldPattern_c reconstruct(List modifiers,TypePatternExpr type,ClassTypeDotId name) {
	if(!CollectionUtil.equals(modifiers,this.modifiers) 
	   || type!=this.type 
	   || name!=this.name) {
	    
	    FieldPattern_c n = (FieldPattern_c) copy();
	    n.modifiers=modifiers;
	    n.type=type;
	    n.name=name;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	List modifiers=visitList(this.modifiers,v);
	TypePatternExpr type=(TypePatternExpr) visitChild(this.type,v);
	ClassTypeDotId name=(ClassTypeDotId) visitChild(this.name,v);
	return reconstruct(modifiers,type,name);
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

    public abc.weaving.aspectinfo.FieldPattern makeAIFieldPattern() {
	return PatternMatcher.v().makeAIFieldPattern(modifiers, type, name.base(), name.name());
    }

}
