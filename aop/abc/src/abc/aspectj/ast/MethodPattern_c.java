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
    List throwspats;      // of ThrowsPattern

    public List/*<ModifierPattern>*/ getModifiers() { return modifiers; }
    public TypePatternExpr getType() { return type; }
    public ClassTypeDotId getName() { return name; }
    public List/*<FormalPattern>*/ getFormals() { return formals; }
    public List/*<ThrowsPattern>*/ getThrowspats() { return throwspats; }

    public MethodPattern_c(Position pos,
                           List modifiers,
                           TypePatternExpr type,
                           ClassTypeDotId name,
			   List formals,
                           List throwspats) {
        super(pos);
	this.modifiers = modifiers;
	this.type = type;
	this.name = name;
        this.formals = formals;
        this.throwspats = throwspats;
    }

    protected MethodPattern_c reconstruct(List/*<ModifierPattern>*/ modifiers,
					  TypePatternExpr type,
					  ClassTypeDotId name,
					  List/*<FormalPattern>*/ formals,
					  List throwspats) {
	if(modifiers != this.modifiers || type != this.type || name != this.name
	   || formals != this.formals || throwspats != this.throwspats) {
	    
	    MethodPattern_c n = (MethodPattern_c) copy();
	    n.modifiers=modifiers;
	    n.type=type;
	    n.name=name;
	    n.formals=formals;
	    n.throwspats=throwspats;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	List/*<ModifierPattern>*/ modifiers = visitList(this.modifiers,v);
	TypePatternExpr type = (TypePatternExpr) visitChild(this.type,v);
	ClassTypeDotId name = (ClassTypeDotId) visitChild(this.name,v);
	List/*<FormalPattern>*/ formals = visitList(this.formals,v);
	List throwspats = visitList(this.throwspats,v);
	return reconstruct(modifiers,type,name,formals,throwspats);
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
       
       if (throwspats.size() != 0) {
	   w.write(" throws ");
	   for (Iterator ti = throwspats.iterator(); ti.hasNext(); ) {
	       ThrowsPattern t = (ThrowsPattern) ti.next();
	       print(t,w,tr);
	       if (ti.hasNext()) {
		   w.write(", ");
	       }
	   }
       }
       w.end();
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();

       for (Iterator i = modifiers.iterator(); i.hasNext(); ) {
	    ModifierPattern f = (ModifierPattern) i.next();
	    sb.append(f);
       }

       sb.append(type);
       sb.append(" ");
       sb.append(name);

       sb.append("(");
       for (Iterator i = formals.iterator(); i.hasNext(); ) {
	    FormalPattern f = (FormalPattern) i.next();
	    sb.append(f);

	    if (i.hasNext()) {
		sb.append(",");
	    }
       }
       sb.append(")");
       
       if (throwspats.size() != 0) {
	   sb.append(" throws ");
	   for (Iterator ti = throwspats.iterator(); ti.hasNext(); ) {
	       ThrowsPattern t = (ThrowsPattern) ti.next();
	       sb.append(t);
	       if (ti.hasNext()) {
		   sb.append(", ");
	       }
	   }
       }
       return sb.toString();
    }

    public abc.weaving.aspectinfo.MethodPattern makeAIMethodPattern() {
	return PatternMatcher.v().makeAIMethodPattern(this);
    }
}
