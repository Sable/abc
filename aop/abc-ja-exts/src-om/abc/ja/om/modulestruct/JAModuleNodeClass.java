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
import polyglot.util.Position;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.ast.ClassnamePatternExpr_c;
import abc.ja.om.jrag.Pattern;
import abc.om.modulestruct.ModuleNodeClass;

public class JAModuleNodeClass extends ModuleNodeClass {

	protected Pattern pat;
	
	public JAModuleNodeClass(String parentName, Pattern pat, Position pos) {
		this.cpe = null;
		this.pat = pat;
		name = parentName + "." + pat.toString();
		this.pos = pos;
	}
	
	@Override
	public ClassnamePatternExpr getCPE() {
		throw new InternalCompilerError("Attempt to get Polyglot CPE from JAModuleNodeClass");
	}

	public Pattern getCPEPattern() {
		return pat;
	}
}
