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

package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import abc.weaving.matching.*;
import abc.weaving.weaver.WeavingContext;

/*  Base class for synthetic advice used to instantiate aspects with per clauses
 *  @author Ganesh Sittampalam
 */
public abstract class PerSetupAdvice extends AbstractAdviceDecl {

    protected PerSetupAdvice(AdviceSpec spec,Aspect aspct,Pointcut pc,Position pos) {
	super(aspct,spec,pc,new ArrayList(),pos);
    }

    public WeavingEnv getWeavingEnv() {
	return new EmptyFormals();
    }

    public String toString() {
	return "setup advice for "+getAspect();
    }


}
