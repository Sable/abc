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
 *  A type pattern expression that matches everything.
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 *
 */
public class TPEUniversal_c extends TypePatternExpr_c implements TPEUniversal
{
    public TPEUniversal_c(Position pos)  {
	super(pos);
    }

    public Precedence precedence() {
	return Precedence.UNARY;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("*");
    }

    public String toString() {
	return "*";
    }

    public boolean matchesClass(PatternMatcher matcher, PCNode cl) {
	return true;
    }

    public boolean matchesClassArray(PatternMatcher matcher, PCNode cl, int dim) {
	return true;
    }

    public boolean matchesPrimitive(PatternMatcher matcher, String prim) {
	return true;
    }

    public boolean matchesPrimitiveArray(PatternMatcher matcher, String prim, int dim) {
	return true;
    }

    public ClassnamePatternExpr transformToClassnamePattern(AJNodeFactory nf) throws SemanticException {
	return nf.CPEUniversal(position);
    }

    public boolean equivalent(TypePatternExpr t) {
	if (t.getClass() == this.getClass()) {
	    return true;
	} else return false;
    }

}
