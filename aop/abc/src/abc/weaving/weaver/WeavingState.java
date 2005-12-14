/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ondrej Lhotak
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

package abc.weaving.weaver;

import java.util.HashMap;
import java.util.Map;

import soot.Local;
import abc.weaving.residues.JoinPointInfo;
import abc.weaving.matching.ShadowMatch;

/** State used during weaving that needs to be reset for reweaving.
 * @author Ondrej Lhotak
 * @date November 8, 2004
 */

public class WeavingState {
    private static WeavingState instance = new WeavingState();
    public static WeavingState v() { return instance; }
    public static void reset() { instance = new WeavingState(); }
    private Map JoinPointInfo_thisJoinPoint = new HashMap();
    public Local get_JoinPointInfo_thisJoinPoint(ShadowMatch key) {
        return (Local) JoinPointInfo_thisJoinPoint.get(key);
    }
    public void set_JoinPointInfo_thisJoinPoint(ShadowMatch key, Local value) {
        JoinPointInfo_thisJoinPoint.put(key, value);
    }
}
