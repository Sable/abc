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

/** ClassnamePatternExpr that matches anything.
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
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
    
    public List/*<ClassnamePatternExpr>*/ getExcludes() {
    	return excludes;
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
	if (otherexp.getClass() == this.getClass()) {
		// Check that the excludes are the same
		// Could be a little too restrictive, but is correct
		
		List/*<ClassnamePatternExpr>*/ otherexcludes = 
				((CPEUniversal)otherexp).getExcludes();
		
		// this.excludes SUBSET OF otherexp.excludes
		Iterator it1 = excludes.iterator();
		while (it1.hasNext()) {
			ClassnamePatternExpr e = (ClassnamePatternExpr)it1.next();
			Iterator it2 =  otherexcludes.iterator();
			boolean found = false;
			while (it2.hasNext() && !found) {
				ClassnamePatternExpr othere = (ClassnamePatternExpr)it2.next(); 
				if (e.equivalent(othere)) {
					found = true;
				}
			}
			if (!found) return false;
		}
		
		// this.excludes SUPERSET OF otherexp.excludes
		it1 = otherexcludes.iterator();
		while (it1.hasNext()) {
			ClassnamePatternExpr e = (ClassnamePatternExpr)it1.next();
			Iterator it2 =  excludes.iterator();
			boolean found = false;
			while (it2.hasNext() && !found) {
				ClassnamePatternExpr othere = (ClassnamePatternExpr)it2.next(); 
				if (e.equivalent(othere)) {
					found = true;
				}
			}
			if (!found) return false;
		}
		
	    return true;
	} else return false;
    }
}
