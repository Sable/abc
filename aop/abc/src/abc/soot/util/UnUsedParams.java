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

/*
 * Created on 19-Jul-2005
 *
 
 */
package abc.soot.util;

import soot.*;
import soot.jimple.*;
import java.util.*;

/**
 * @author oege
 *
 
 */
public class UnUsedParams {
	   private static boolean find(Collection c, int n) {
			boolean res = false;
			for (Iterator it = c.iterator(); it.hasNext() && !res; ) {
				Integer i = (Integer) it.next();
				res = (n == i.intValue());
			}
			return res;
		}
    
		/**
		 * return a list of all the formals that are *not* used in the body of a method
		 * 
		 * @param sm
		 * @param formalNames
		 * @return
		 */
		public static Set unusedFormals(SootMethod sm,List formalNames) {
			Body body = sm.getActiveBody();
			// first find what locals are used
			Set usedLocals = new HashSet();
			for (Iterator it=body.getUseBoxes().iterator(); it.hasNext(); ) {
						ValueBox b=(ValueBox)it.next();
						if (b.getValue() instanceof Local)
							usedLocals.add(b.getValue());
			}
			// then look for identity statements of the form "local = parameterRef"
			Set usedParams = new HashSet();
			for (Iterator it=body.getUnits().iterator(); it.hasNext(); ) {
				Unit b = (Unit)it.next();
				if (b instanceof IdentityStmt) {
					IdentityStmt is = (IdentityStmt) b;
					Value lhs = is.getLeftOp();
					Value rhs = is.getRightOp();
					if (rhs instanceof ParameterRef &&
						lhs instanceof Local && 
						usedLocals.contains(lhs)) {
							ParameterRef pr = (ParameterRef) rhs;
							usedParams.add(new Integer(pr.getIndex()));
						}
				}
			}
			// walk over the name list, omitting those that are used
			int count = 0;
			Set unusedFormalNames = new HashSet();
			for (Iterator it = formalNames.iterator(); it.hasNext(); ) {
				String name = (String) it.next();
				if (!find(usedParams,count))
					unusedFormalNames.add(name); 
				count++;
			}
			return unusedFormalNames;
		}
    
}
