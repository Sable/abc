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


/** 
 *  represent  ClassnamePatternExpr.SimpleNamePattern in pointcuts.
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 */
public class ClassTypeDotId_c extends Node_c implements ClassTypeDotId
{
    protected ClassnamePatternExpr base;
    protected SimpleNamePattern name;
   
    public ClassTypeDotId_c(Position pos, 
			    ClassnamePatternExpr base,
			    SimpleNamePattern name)  {
	super(pos);
        this.base = base;
        this.name = name;
    }

    protected ClassTypeDotId_c reconstruct(ClassnamePatternExpr base,
					   SimpleNamePattern name) {
	if(base!=this.base || name!=this.name) {
	    ClassTypeDotId_c n = (ClassTypeDotId_c) copy();
	    n.base=base;
	    n.name=name;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	ClassnamePatternExpr base=(ClassnamePatternExpr) visitChild(this.base,v);
	SimpleNamePattern name=(SimpleNamePattern) visitChild(this.name,v);
	return reconstruct(base,name);
    }

    public ClassnamePatternExpr base() {
	return base;
    }

    public SimpleNamePattern name() {
	return name;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	if (name != null) 
	    w.write("(");
        print(base,w,tr);
        if (name != null) {
	    w.write(").");
	    print(name,w,tr);
        }
    }

    public String toString() {
	String s="";
	if(name !=null) s+="(";
	s+=base;
	if(name!=null) {
	    s+=")."+name;
	}
	return s;
    }

    public boolean equivalent(ClassTypeDotId c) {
	return (base.equivalent(c.base()) && name.equivalent(c.name()));
    }

}
