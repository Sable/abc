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

public class DotDotNamePattern_c extends NamePattern_c 
                                 implements DotDotNamePattern
{
    NamePattern init;

    public DotDotNamePattern_c(Position pos,NamePattern init) {
        super(pos);
        this.init = init;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	print(init,w,tr);
	w.write(".");
	// This node will always appear to the left of a dot.
	// Print one extra dot here - that makes two of them.
    }

    public String toString() {
	return init+".";
    }

    public NamePattern getInit() {
	return init;
    }

     public Set/*<PCNode>*/ match(PCNode context, Set/*<PCNode>*/ classes, Set/*<PCNode>*/ packages) {
	Set/*<PCNode>*/ matches = init.match(context, classes, packages);
	LinkedList worklist = new LinkedList(matches);
	while (!worklist.isEmpty()) {
	    PCNode n = (PCNode)worklist.removeFirst();
	    Iterator ii = n.getInners().iterator();
	    while (ii.hasNext()) {
		PCNode inner = (PCNode)ii.next();
		if (!matches.contains(inner)) {
		    matches.add(inner);
		    worklist.addLast(inner);
		}
	    }
	}
	return matches;
    }

    public boolean universal() {
	return false;
    }

    public boolean equivalent(NamePattern p) {
	if (p.getClass() == this.getClass()) {
	    return (init.equivalent(((DotDotNamePattern)p).getInit()));
	} else return false;
    }

}
