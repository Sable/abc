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

import abc.ja.om.jrag.AspectDecl;
import abc.ja.om.jrag.TypeDecl;

//not enough code reuseable in MSOpenClassContext to be worth subtyping
//refactor into interfaces and implementing classes if future need
//occurs
//classDecl = class on which the ITD was inserted
//aspectDecl = aspect where the ITD was declared
public abstract class JAOpenClassContext {

	protected TypeDecl classDecl;
	protected AspectDecl aspectDecl;

	public JAOpenClassContext(TypeDecl classDecl, AspectDecl aspectDecl) {
		this.classDecl = classDecl;
		this.aspectDecl = aspectDecl;
	}
	
	public TypeDecl getClassDecl() {
		return classDecl;
	}
	
	public AspectDecl getAspectDecl() {
		return aspectDecl;
	}
}
