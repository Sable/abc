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

/**
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 *
 */
public class TPEType_c extends TypePatternExpr_c implements TPEType
{
    protected TypeNode type;

    public TPEType_c(Position pos, TypeNode type)  {
	super(pos);
        this.type = type;
    }

    public TypeNode type() {
	return type;
    }

    public Precedence precedence() {
		return Precedence.UNARY;
    }

	/** Reconstruct the type pattern */
	protected TPEType_c reconstruct(TypeNode type) {
		if (this.type != type) {
			 TPEType_c n = (TPEType_c) copy();
			 n.type = type;
			 return n;
		}
		return this;
	}

	/** Visit the children of the type pattern. */
	public Node visitChildren(NodeVisitor v) {
		TypeNode type = (TypeNode) visitChild(this.type, v);
		return reconstruct(type);
	}
    
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        print(type, w, tr);
    }

    public String toString() {
	return type.toString();
    }

    public boolean matchesClass(PatternMatcher matcher, PCNode cl) {
	return false;
    }

    public boolean matchesClassArray(PatternMatcher matcher, PCNode cl, int dim) {
	return false;
    }

    public boolean matchesPrimitive(PatternMatcher matcher, String prim) {
	return type.toString().equals(prim);
    }

    public boolean matchesPrimitiveArray(PatternMatcher matcher, String prim, int dim) {
	return false;
    }

    public ClassnamePatternExpr transformToClassnamePattern(AJNodeFactory nf) throws SemanticException {
	throw new SemanticException("Primitive type in classname pattern");
    }

    public boolean equivalent(TypePatternExpr t) {
	if (t.getClass() == this.getClass()) {
	    if (type.type().equals(((TPEType)t).type().type()))
			return true;
	    else
		  return false;
	} else return false; 
    }

}
