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

/** ClassnamePatternExpr that is just a name pattern.
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 */
public class CPEName_c extends ClassnamePatternExpr_c 
    implements CPEName, ContainsNamePattern
{
    protected NamePattern pat;

    public CPEName_c(Position pos, NamePattern pat)  {
	super(pos);
        this.pat = pat;
    }

    protected CPEName_c reconstruct(NamePattern pat) {
	if (pat != this.pat) {
	    CPEName_c n = (CPEName_c) copy();
	    n.pat = pat;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	NamePattern pat = (NamePattern) visitChild(this.pat, v);
	return reconstruct(pat);
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	print(pat,w,tr);
    }

    public String toString() {
	return pat.toString();
    }

    public NamePattern getNamePattern() {
	return pat;
    }

    public boolean matches(PatternMatcher matcher, PCNode cl) {
	return matcher.matchesName(pat, cl);
    }

    public boolean equivalent(ClassnamePatternExpr otherexp) {
	if (otherexp.getClass() == this.getClass()) {
	    return (pat.equivalent(((CPEName)otherexp).getNamePattern()));
	} else return false;
    }

}
