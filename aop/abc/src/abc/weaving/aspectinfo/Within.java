/* Abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Ganesh Sittampalam
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

package abc.weaving.aspectinfo;

import java.util.Hashtable;

import soot.*;
import polyglot.util.Position;
import abc.weaving.residues.*;

/** Handler for <code>within</code> condition pointcut. 
 *  The within(ClassPattern) pointcut matches any join point lexically contained
 *  within a class matching ClassPattern.
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 */
public class Within extends LexicalPointcut {
    private ClassnamePattern pattern;

    public Within(ClassnamePattern pattern,Position pos) {
	super(pos);
	this.pattern = pattern;
    }

    public ClassnamePattern getPattern() {
	return pattern;
    }


    final protected Residue matchesAt(SootClass cls,SootMethod method) {
	return matchesAt(cls);
    }
    
    protected Residue matchesAt(SootClass cls) {
	if(getPattern().matchesClass(cls)) return AlwaysMatch.v;
	if(cls.hasOuterClass()) return matchesAt(cls.getOuterClass());
	return null;
    }

    public String toString() {
	return "within("+pattern+")";
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		//FIXME Within.equivalent(DirectlyWithin, ren) returns true, is this OK?
		if (otherpc instanceof Within) {
			return pattern.equivalent(((Within)otherpc).getPattern());
		} else return false;
	}

}
