/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
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

package abc.aspectj.visit;

import java.util.*;

import polyglot.ast.Node;
import polyglot.ast.JL;
import polyglot.visit.NodeVisitor;

/** Check whether all instances of thisJoinPoint in an advice
 *  body can be transformed to thisJoinPointStaticPart
 *  @author Ganesh Sittampalam
 */
public class AspectReflectionInspect extends NodeVisitor {

    private Stack/*<Boolean>*/ canTransform; /* Can we transform thisJoinPoint to 
                                                thisJoinPointStaticPart in the current advice body? */

    public AspectReflectionInspect() {
	super();
	this.canTransform=new Stack();
    }
    
    public void enterAdvice() {
	canTransform.push(new Boolean(true));
    }

    public void disableTransform() {
	if(((Boolean) canTransform.peek()).booleanValue()) {
	    canTransform.pop();
	    canTransform.push(new Boolean(false));
	}
    }

    public boolean inspectingLocals() {
	return !canTransform.empty();
    }

    public boolean leaveAdvice() {
	return ((Boolean) canTransform.pop()).booleanValue();
    }

    public NodeVisitor enter(Node parent, Node n) {
	JL del=n.del();
	if(del instanceof TransformsAspectReflection) 
	    ((TransformsAspectReflection) del).enterAspectReflectionInspect(this,parent);
	return this;
    }

    public Node leave(Node old,Node n,NodeVisitor v) {
	JL del=n.del();
	if(del instanceof TransformsAspectReflection)
	    ((TransformsAspectReflection) del).leaveAspectReflectionInspect(this);
	return n;
    }
}
