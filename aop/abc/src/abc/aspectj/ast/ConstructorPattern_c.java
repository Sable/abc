/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 * Copyright (C) 2004 Aske Simon Christensen
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

package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.Node_c;

import abc.aspectj.visit.*;

/** patterns to capture constructor joinpoints.
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 */
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

    public boolean equivalent(ConstructorPattern p) {

    if (!name.equivalent(p.getName())) return false;

    // COMPARE MODIFIERS

    Iterator it1 = modifiers.iterator();
    Iterator it2 = p.getModifiers().iterator();

    while (it1.hasNext() && it2.hasNext()) {
	if (!((ModifierPattern)it1.next()).equivalent(
		  (ModifierPattern)it2.next())) return false;
    }
    if (it1.hasNext() || it2.hasNext()) return false;

    //COMPARE FORMALS

    it1 = formals.iterator();
    it2 = p.getFormals().iterator();

    while (it1.hasNext() && it2.hasNext()) {
	if (!((FormalPattern)it1.next()).equivalent(
		  (FormalPattern)it2.next())) return false;
    }
    if (it1.hasNext() || it2.hasNext()) return false;

    //COMPARE THROWSPATTERNS

    it1 = throwspats.iterator();
    it2 = p.getThrowspats().iterator();

    while (it1.hasNext() && it2.hasNext()) {
	if (!((ThrowsPattern)it1.next()).equivalent(
		  (ThrowsPattern)it2.next())) return false;
    }
    if (it1.hasNext() || it2.hasNext()) return false;

    return true;

    }

    public boolean canMatchEmptyArgumentList() {
	if(formals.size()==0) return true;
	if(formals.size()>1) return false;
	return (formals.get(0) instanceof DotDotFormalPattern);
    }

}
