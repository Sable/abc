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

/** negation of a ClassnamePatternExpr.
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 */
public class CPENot_c extends ClassnamePatternExpr_c implements CPENot
{
    protected ClassnamePatternExpr cpe;

    public CPENot_c(Position pos, ClassnamePatternExpr cpe)  {
	super(pos);
        this.cpe = cpe;
    }

    public ClassnamePatternExpr getCpe() {
	return cpe;
    }

    protected CPENot_c reconstruct(ClassnamePatternExpr cpe) {
	if (cpe != this.cpe) {
	    CPENot_c n = (CPENot_c) copy();
	    n.cpe = cpe;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	ClassnamePatternExpr cpe = (ClassnamePatternExpr) visitChild(this.cpe, v);
	return reconstruct(cpe);
    }

    public Precedence precedence() {
	return Precedence.UNARY;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("!");
        printSubExpr(cpe, true, w, tr);
    }

    public String toString() {
	return "!"+cpe;
    }

    public boolean matches(PatternMatcher matcher, PCNode cl) {
	return !cpe.matches(matcher, cl);
    }

    public boolean equivalent(ClassnamePatternExpr otherexp) {
	if (otherexp.getClass() == this.getClass()) {
	    return (cpe.equivalent(((CPENot)otherexp).getCpe()));
	} else return false;
    }

}
