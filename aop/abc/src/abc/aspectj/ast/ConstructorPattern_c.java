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

    List modifiers;       // of ModifierPattern
    ClassTypeDotNew name;
    List formals;         // of FormalPattern
    List throwspats;        // of ThrowsPattern

    public ConstructorPattern_c(Position pos,
                                List modifiers,
                                ClassTypeDotNew name,
			        List formals,
                                List throwspats) {
        super(pos);
	this.modifiers = modifiers;
	this.name = name;
	this.formals = formals;
        this.throwspats = throwspats;
    }

    protected ConstructorPattern_c reconstruct(List/*<ModifierPattern>*/ modifiers,
					       ClassTypeDotNew name,
					       List/*<FormalPattern>*/ formals,
					       List/*<ThrowsPattern>*/ throwspats) {
	if(modifiers != this.modifiers || name != this.name
	   || formals != this.formals || throwspats != this.throwspats) {
	    
	    ConstructorPattern_c n = (ConstructorPattern_c) copy();
	    n.modifiers=modifiers;
	    n.name=name;
	    n.formals=formals;
	    n.throwspats=throwspats;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	List/*<ModifierPattern>*/ modifiers = visitList(this.modifiers,v);
	ClassTypeDotNew name = (ClassTypeDotNew) visitChild(this.name,v);
	List/*<FormalPattern>*/ formals = visitList(this.formals,v);
	List/*<ThrowsPattern>*/ throwspats = visitList(this.throwspats,v);
	return reconstruct(modifiers,name,formals,throwspats);
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
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();

       for (Iterator i = modifiers.iterator(); i.hasNext(); ) {
	    ModifierPattern f = (ModifierPattern) i.next();
	    sb.append(f);
       }

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

    public List/*<ModifierPattern>*/ getModifiers() {
	return modifiers;
    }

    public ClassTypeDotNew getName() {
	return name;
    }

    public List/*<FormalPattern>*/ getFormals() {
	return formals;
    }

    public List/*<ThrowsPattern>*/ getThrowspats() { return throwspats; }

    public abc.weaving.aspectinfo.ConstructorPattern makeAIConstructorPattern() {
	return PatternMatcher.v().makeAIConstructorPattern(this);
    }
}
