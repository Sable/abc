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
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 */
public class ThrowsPattern_c extends Node_c implements ThrowsPattern
{
    protected ClassnamePatternExpr type;
    protected boolean positive;

    public ThrowsPattern_c(Position pos, 
			     ClassnamePatternExpr type, 
			     boolean positive)  {
	super(pos);
        this.type = type;
	this.positive = positive;
    }

    protected ThrowsPattern_c reconstruct(ClassnamePatternExpr type) {
	if (type != this.type) {
	    ThrowsPattern_c n = (ThrowsPattern_c) copy();
	    n.type = type;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	ClassnamePatternExpr type = (ClassnamePatternExpr) visitChild(this.type, v);
	return reconstruct(type);
    }

    public ClassnamePatternExpr type() {
	return type;
    }

    public boolean positive() {
	return positive;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	if (!positive)
	    w.write("!");
	print(type,w,tr);
    }

    public String toString() {
	if(positive) return type.toString();
	else return "!"+type.toString();
    }

    public boolean equivalent(ThrowsPattern p) {
	return (   (type.equivalent(p.type()))
	        && (positive == p.positive()));
    }

}
