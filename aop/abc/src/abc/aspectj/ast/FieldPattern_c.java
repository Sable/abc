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

import abc.aspectj.visit.PatternMatcher;

/**
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 *
 */
public class FieldPattern_c extends Node_c 
                            implements FieldPattern
{
   
    List modifiers;
    TypePatternExpr type;
    ClassTypeDotId name;

    public List/*<ModifierPattern>*/ getModifiers() { return modifiers; }
    public TypePatternExpr getType() { return type; }
    public ClassTypeDotId getName() { return name; }

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

    public String toString() {
	StringBuffer sb = new StringBuffer();

       for (Iterator i = modifiers.iterator(); i.hasNext(); ) {
	    ModifierPattern f = (ModifierPattern) i.next();
	    sb.append(f);
       }

       sb.append(type);
       sb.append(" ");
       sb.append(name);

       return sb.toString();
    }

    public abc.weaving.aspectinfo.FieldPattern makeAIFieldPattern() {
	return PatternMatcher.v().makeAIFieldPattern(this);
    }

    public boolean equivalent(FieldPattern p) {

    if (!name.equivalent(p.getName())) return false;

    if (!type.equivalent(p.getType())) return false;

    Iterator it1 = modifiers.iterator();
    Iterator it2 = p.getModifiers().iterator();

	while (it1.hasNext() && it2.hasNext()) {
		ModifierPattern mp1 = (ModifierPattern)it1.next();
		ModifierPattern mp2 = (ModifierPattern)it2.next();
		if (!mp1.equivalent(mp2)) return false;
	}
	if (it1.hasNext() || it2.hasNext()) return false;

    return true;

    }


}
