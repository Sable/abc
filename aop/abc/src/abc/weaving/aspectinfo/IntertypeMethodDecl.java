/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
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

import polyglot.util.Position;

import soot.*;

/** An intertype method declaration. 
 *  @author Aske Simon Christensen
 *  @author Oege de Moor
 */
public class IntertypeMethodDecl extends InAspect {
    private MethodSig target;
    private MethodSig impl;
    private String origName;

    public IntertypeMethodDecl(MethodSig target, MethodSig impl, Aspect aspct, String origName, Position pos) {
	super(aspct, pos);
	this.target = target;
	this.impl = impl;
	this.origName = origName;
    }

    /** Get the method signature that this intertype method declaration
     *  will end up having when it is woven in.
     */
    public MethodSig getTarget() {
	return target;
    }

    /** Get the signature of the placeholder method that contains the
     *  implementation of this intertype method declaration.
     */
    public MethodSig getImpl() {
	return impl;
    }
    
    public String getOrigName() {
    	return origName;
    }

    public String toString() {
	return "(in aspect "+getAspect().getName()+") "+target+" { ... }";
    }
}
