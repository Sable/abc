/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Neil Ongkingco
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

package abc.ja.om.modulestruct;

import polyglot.util.InternalCompilerError;
import soot.SootClass;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.ja.om.jrag.Pattern;
import abc.weaving.aspectinfo.ClassnamePattern;

public class JAOMClassnamePattern implements ClassnamePattern {
	Pattern pattern;
	
	public JAOMClassnamePattern(Pattern pat) {
		this.pattern = pat;
	}
	
	public boolean equivalent(ClassnamePattern p) {
		return false;
	}

	public ClassnamePatternExpr getPattern() {
        throw new InternalCompilerError("Can not get polyglot frontend pattern from JastAdd");
    }

	public boolean matchesClass(SootClass cl) {
    	if(abc.main.Debug.v().patternMatches) {
    		System.err.println("Matching classname pattern " + pattern + " against "
    				+ cl + ": " + pattern.matchesType(cl.getType()));
    	}
        return pattern.matchesType(cl);	}
	
	public String toString() {
		return pattern.toString();
	}
}
