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

import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.types.AJTypeSystem;

/** Used on a AST node that participates in the transformation
 *  of thisJoinPoint references to thisJoinPointStaticPart
 *  @author Ganesh Sittampalam
 */
public interface TransformsAspectReflection {

    public void enterAspectReflectionInspect(AspectReflectionInspect v,Node parent);
    public void leaveAspectReflectionInspect(AspectReflectionInspect v);

    public void enterAspectReflectionRewrite(AspectReflectionRewrite v,AJTypeSystem ts);
    public Node leaveAspectReflectionRewrite(AspectReflectionRewrite v,AJNodeFactory nf);

}
