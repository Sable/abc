/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
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

package abc.weaving.aspectinfo;

/**
 * represent super dispatch calls in target of intertype declarations
 * @author Oege de Moor
 */
public class SuperDispatch {
	private String name;
	private MethodSig methodsig;
	private AbcClass target;
	
	public String getName() {
		return name;
	}
	
	public MethodSig getMethodSig() {
		return methodsig;
	}
	
	public AbcClass getTarget() {
		return target;
	}
	
	public SuperDispatch(String name, MethodSig methodsig, AbcClass target) {
		this.name = name;
		this.methodsig = methodsig;
		this.target = target;
	}
}
