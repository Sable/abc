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

/** represent (ClassNamePatternExpr.new) in pointcuts
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen 
 */
public class ClassTypeDotNew_c extends Node_c implements ClassTypeDotNew
{
    protected ClassnamePatternExpr base;
   
    public ClassTypeDotNew_c(Position pos, 
			      ClassnamePatternExpr base)  {
	super(pos);
        this.base = base;
    }

    protected ClassTypeDotNew_c reconstruct(ClassnamePatternExpr base) {
	if(base!=this.base) {
	    ClassTypeDotNew_c n = (ClassTypeDotNew_c) copy();
	    n.base=base;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	ClassnamePatternExpr base=(ClassnamePatternExpr) visitChild(this.base,v);
	return reconstruct(base);
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	if (base != null) {
	    w.write("(");
	    print(base,w,tr);
	    w.write(").");
	}
	w.write("new");
    }

    public ClassnamePatternExpr base() {
	return base;
    }

    public String toString() {
	if(base != null) return "("+base+")."+"new";
	else return "new";
    }

    public boolean equivalent(ClassTypeDotNew c) {
	return base.equivalent(c.base());
    }

}
