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
 *  A type pattern expression that is a reference type pattern.
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 */
public class TPERefTypePat_c extends TypePatternExpr_c 
    implements TPERefTypePat
{
    protected RefTypePattern pat;

    public RefTypePattern getPattern() {
	return pat;
    }

    public TPERefTypePat_c(Position pos, RefTypePattern pat)  {
	super(pos);
        this.pat = pat;
    }

    public Precedence precedence() {
	return Precedence.UNARY;
    }
    
	/** Reconstruct the pattern. */
	protected TPERefTypePat_c reconstruct(RefTypePattern pat) {
		if (pat != this.pat) {
			 TPERefTypePat_c n = (TPERefTypePat_c) copy();
			 n.pat = pat;
			 return n;
		}

		return this;
	}

	/** Visit the children of the pattern. */
	public Node visitChildren(NodeVisitor v) {
		RefTypePattern pat = (RefTypePattern)visitChild(this.pat, v);
		return reconstruct(pat);
	}

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	print(pat, w, tr);
    }

    public String toString() {
	return pat.toString();
    }

    public boolean matchesClass(PatternMatcher matcher, PCNode cl) {
	return pat.matchesClass(matcher, cl);
    }

    public boolean matchesClassArray(PatternMatcher matcher, PCNode cl, int dim) {
	return pat.matchesArray(matcher);
    }

    public boolean matchesPrimitive(PatternMatcher matcher, String prim) {
	return false;
    }

    public boolean matchesPrimitiveArray(PatternMatcher matcher, String prim, int dim) {
	return pat.matchesArray(matcher);
    }

    public ClassnamePatternExpr transformToClassnamePattern(AJNodeFactory nf) throws SemanticException {
	return pat.transformToClassnamePattern(nf);
    }

    public boolean equivalent(TypePatternExpr t) {
	if (t.getClass() == this.getClass()) {
	    return (pat.equivalent(((TPERefTypePat)t).getPattern()));
	} else return false;
    }

}
