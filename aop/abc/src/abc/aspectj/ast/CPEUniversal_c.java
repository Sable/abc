/* Abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 * Copyright (C) 2004 Aske Simon Christensen
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
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

/** ClassnamePatternExpr that matches anything.
 * 
 * @author Oege de Moor
 * @author Aske Simon Christensen
 */

public class CPEUniversal_c extends ClassnamePatternExpr_c implements CPEUniversal
{
    private List excludes = new ArrayList();

    public CPEUniversal_c(Position pos)  {
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

    public void addExclude(ClassnamePatternExpr pat) {
	excludes.add(pat);
    }

    public void setExcludes(List excludes) {
	this.excludes = excludes;
    }

    public boolean matches(PatternMatcher matcher, PCNode cl) {
	Iterator ei = excludes.iterator();
	while (ei.hasNext()) {
	    ClassnamePatternExpr e = (ClassnamePatternExpr)ei.next();
	    if (e.matches(matcher, cl)) return false;
	}
	return true;
    }

    public boolean equivalent(ClassnamePatternExpr otherexp) {
	if (otherexp instanceof CPEUniversal) {
	    return true;
	} else return false;
    }
}
