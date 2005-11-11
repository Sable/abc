/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Neil Ongkingco
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

package abc.om.ast;

public class OpenClassFlags {

	boolean isField;
	boolean isParent;
	boolean isMethod;
	
	public OpenClassFlags() {
		this.isField = false;
		this.isParent = false;
		this.isMethod = false;
	}
	
	public OpenClassFlags(boolean isField, 
			boolean isParent, 
			boolean isMethod) {
		this.isField = isField;
		this.isParent = isParent;
		this.isMethod = isMethod;
	}
	
	public boolean isField() {
		return isField;
	}
	public void setField(boolean isField) {
		this.isField = isField;
	}
	public boolean isMethod() {
		return isMethod;
	}
	public void setMethod(boolean isMethod) {
		this.isMethod = isMethod;
	}
	public boolean isParent() {
		return isParent;
	}
	public void setParent(boolean isParent) {
		this.isParent = isParent;
	}
	
	public OpenClassFlags disjoin(OpenClassFlags flags) {
		isField = isField || flags.isField();
		isParent = isParent || flags.isParent();
		isMethod = isMethod || flags.isMethod();
		return this;
	}
	
}
