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

package abc.weaving.matching;

import soot.SootClass;
import soot.SootMethod;

/**
 * @author Neil Ongkingco
 * Contains the parameters to Pointcut.matchesAt. 
 */
public class MatchingContext {
    protected WeavingEnv env;
    protected SootClass cls;
    protected SootMethod method;
    protected ShadowMatch sm;
    
    public MatchingContext(WeavingEnv env, SootClass cls, SootMethod method, ShadowMatch sm) {
        this.env = env;
        this.cls = cls;
        this.method = method;
        this.sm = sm;
    }
    
    public SootClass getSootClass() {
        return cls;
    }
    public WeavingEnv getWeavingEnv() {
        return env;
    }
    public SootMethod getSootMethod() {
        return method;
    }
    public ShadowMatch getShadowMatch() {
        return sm;
    }
}
