package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import abc.aspectj.visit.*;

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

    public List/*<ModifierPattern>*/ getModifiers() { return modifiers; }
    public TypePatternExpr getType() { return type; }
    public ClassTypeDotId getName() { return name; }
    public List/*<FormalPattern>*/ getFormals() { return formals; }
    public ClassnamePatternExpr getThrowPat() { return throwpat; }

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

    protected MethodPattern_c reconstruct(List/*<ModifierPattern>*/ modifiers,
					  TypePatternExpr type,
					  ClassTypeDotId name,
					  List/*<FormalPattern>*/ formals,
					  ClassnamePatternExpr throwpat) {
	if(modifiers != this.modifiers || type != this.type || name != this.name
	   || formals != this.formals || throwpat != this.throwpat) {
	    
	    MethodPattern_c n = (MethodPattern_c) copy();
	    n.modifiers=modifiers;
	    n.type=type;
	    n.name=name;
	    n.formals=formals;
	    n.throwpat=throwpat;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	List/*<ModifierPattern>*/ modifiers = visitList(this.modifiers,v);
	TypePatternExpr type = (TypePatternExpr) visitChild(this.type,v);
	ClassTypeDotId name = (ClassTypeDotId) visitChild(this.name,v);
	List/*<FormalPattern>*/ formals = visitList(this.formals,v);
	ClassnamePatternExpr throwpat = (ClassnamePatternExpr) visitChild(this.throwpat,v);
	return reconstruct(modifiers,type,name,formals,throwpat);
    }

    public String toString() {
	String s=modifiers+" "+type+" "+name+" "+formals;
	if(throwpat!=null) s+=" throws "+throwpat;
	return s;
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

    public abc.weaving.aspectinfo.MethodPattern makeAIMethodPattern() {
	return PatternMatcher.v().makeAIMethodPattern(this);
    }
}
