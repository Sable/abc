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

import abc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

/** A type pattern expression for array types.
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 */
public class TPEArray_c extends TypePatternExpr_c 
    implements TPEArray
{
    protected TypePatternExpr base;
    protected int dims;

    public TypePatternExpr base() {
	return base;
    }

    public int dims() {
	return dims;
    }

    public TPEArray_c(Position pos, TypePatternExpr base, int dims)  {
	super(pos);
        this.base = base;
	this.dims = dims;
    }

    protected TPEArray_c reconstruct(TypePatternExpr base) {
	if (base != this.base) {
	    TPEArray_c n = (TPEArray_c) copy();
	    n.base = base;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	TypePatternExpr base = (TypePatternExpr) visitChild(this.base, v);
	return reconstruct(base);
    }

    public Precedence precedence() {
	return Precedence.UNARY;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	print(base, w, tr);
	for (int i = 0; i < dims; i++) 
	    w.write("[]");
    }

    public String toString() {
	String s=base.toString();
	for (int i=0;i<dims;i++)
	    s+="[]";
	return s;
    }

    public boolean matchesClass(PatternMatcher matcher, PCNode cl) {
	return false;
    }

    public boolean matchesClassArray(PatternMatcher matcher, PCNode cl, int dim) {
	if (dim == dims) {
	    return base.matchesClass(matcher, cl);
	}
	if (dim > dims) {
	    return base.matchesClassArray(matcher, cl, dim-dims);
	}
	return false;
    }

    public boolean matchesPrimitive(PatternMatcher matcher, String prim) {
	return false;
    }

    public boolean matchesPrimitiveArray(PatternMatcher matcher, String prim, int dim) {
	if (dim == dims) {
	    return base.matchesPrimitive(matcher, prim);
	}
	if (dim > dims) {
	    return base.matchesPrimitiveArray(matcher, prim, dim-dims);
	}
	return false;
    }

    public ClassnamePatternExpr transformToClassnamePattern(AJNodeFactory nf) throws SemanticException {
	throw new SemanticException("Array in classname attern");
    }

    public boolean equivalent(TypePatternExpr t) {
	if (t.getClass() == this.getClass()) {
	    TPEArray tar = (TPEArray)t;
	    return ((base.equivalent(tar.base())) && (dims == tar.dims()));
	} else return false;
    }

}
