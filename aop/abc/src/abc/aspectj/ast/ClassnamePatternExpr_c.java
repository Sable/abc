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
import abc.aspectj.visit.PCNode;

/** 
 *  Class name pattern expressions can combine name patterns with logical operators.
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 */
public abstract class ClassnamePatternExpr_c extends Node_c 
                                    implements ClassnamePatternExpr
{

    public ClassnamePatternExpr_c(Position pos) {
        super(pos);
    }

    public Precedence precedence() {
	return Precedence.UNKNOWN;
    }

    public void printSubExpr(ClassnamePatternExpr expr, boolean associative,
                             CodeWriter w, PrettyPrinter pp) {
        if (! associative && precedence().equals(expr.precedence()) ||
	    precedence().isTighter(expr.precedence())) {
	    w.write("(");
            printBlock(expr, w, pp);
	    w.write( ")");
	}
        else {
            printBlock(expr, w, pp);
        }
    }
    
    public boolean matches(PCNode cl) {
    	return matches(PatternMatcher.v(),cl);
    }

    public abc.weaving.aspectinfo.ClassnamePattern makeAIClassnamePattern() {
	return PatternMatcher.v().makeAIClassnamePattern(this);
    }
}
