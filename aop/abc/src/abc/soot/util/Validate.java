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

package abc.soot.util;

import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.exceptions.PedanticThrowAnalysis;
import soot.util.cfgcmd.*;
import soot.util.dot.*;

/** Validate a jimple class. Currently checks that:
 *   - all local variables are declared with non-void types
 *   - types are used correctly in assignments and method calls
 *   - local variables are initialised before use
 *  @author Ganesh Sittampalam
 */
public class Validate {

    public static void validate(SootClass cl) {
	// FIXME: temporary until Soot gets fixed
	Scene.v().releaseActiveHierarchy();
	for( Iterator methodIt = cl.getMethods().iterator(); methodIt.hasNext(); ) {
	    final SootMethod method = (SootMethod) methodIt.next();
	    if(!method.isConcrete()) continue;
            method.getActiveBody().validate();
	}
    }
}
