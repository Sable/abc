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
import java.util.regex.*;

/**
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 *
 */
public class DotNamePattern_c extends NamePattern_c 
                              implements DotNamePattern
{
    NamePattern init;
    SimpleNamePattern last;

    public DotNamePattern_c(Position pos,NamePattern init,SimpleNamePattern last) {
        super(pos);
        this.init = init;
	this.last = last;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	print(init,w,tr);
	w.write(".");
	print(last,w,tr);
    }

    public String toString() {
	return init+"."+last;
    }

    public NamePattern getInit() {
	return init;
    }
    public SimpleNamePattern getLast() {
	return last;
    }

    public Set/*<PCNode>*/ match(PCNode context, Set/*<PCNode>*/ classes, Set/*<PCNode>*/ packages) {
	Set/*<PCNode>*/ init_matches = init.match(context, classes, packages);
	Set/*<PCNode>*/ result = new HashSet();
	Pattern lp = PatternMatcher.v().compileNamePattern(((SimpleNamePattern_c)last).pat);
	Iterator imi = init_matches.iterator();
	while (imi.hasNext()) {
	    PCNode im = (PCNode)imi.next();
	    result.addAll(im.matchClass(lp));
	}
	return result;
    }

    public boolean universal() {
	return false;
    }

    public boolean equivalent(NamePattern p) {
	if (p.getClass() == this.getClass()) {
	    DotNamePattern dnp = (DotNamePattern) p;
	    return (init.equivalent(dnp.getInit()) && last.equivalent(dnp.getLast()));
	} else return false;
    }

}
