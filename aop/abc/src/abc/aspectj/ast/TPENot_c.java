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
 *  negation of a type pattern expression.
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 *
 */
public class TPENot_c extends TypePatternExpr_c implements TPENot
{
    protected TypePatternExpr tpe;

    public TPENot_c(Position pos, TypePatternExpr tpe)  {
	super(pos);
        this.tpe = tpe;
    }

    public TypePatternExpr getTpe() {
	return tpe;
    }

    protected TPENot_c reconstruct(TypePatternExpr tpe) {
	if (tpe != this.tpe) {
	    TPENot_c n = (TPENot_c) copy();
	    n.tpe = tpe;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	TypePatternExpr tpe = (TypePatternExpr) visitChild(this.tpe, v);
	return reconstruct(tpe);
    }

    public Precedence precedence() {
	return Precedence.UNARY;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("!");
        printSubExpr(tpe, true, w, tr);
    }

    public String toString() {
	return "!"+tpe;
    }

    public boolean matchesClass(PatternMatcher matcher, PCNode cl) {
	return !tpe.matchesClass(matcher, cl);
    }

    public boolean matchesClassArray(PatternMatcher matcher, PCNode cl, int dim) {
	return !tpe.matchesClassArray(matcher, cl, dim);
    }

    public boolean matchesPrimitive(PatternMatcher matcher, String prim) {
	return !tpe.matchesPrimitive(matcher, prim);
    }

    public boolean matchesPrimitiveArray(PatternMatcher matcher, String prim, int dim) {
	return !tpe.matchesPrimitiveArray(matcher, prim, dim);
    };

    public ClassnamePatternExpr transformToClassnamePattern(AJNodeFactory nf) throws SemanticException {
	ClassnamePatternExpr cpe = tpe.transformToClassnamePattern(nf);
	return nf.CPENot(position, cpe);
    }

    public boolean equivalent(TypePatternExpr t) {
	if (t.getClass() == this.getClass()) {
	    return (tpe.equivalent(((TPENot)t).getTpe()));
	} else return false;
    }

}
