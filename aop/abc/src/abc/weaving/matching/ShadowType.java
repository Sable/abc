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

package abc.weaving.matching;

import java.util.*;

import soot.*;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.*;

/** Each possible joinpoint shadow type extends this class and registers a
 *  singleton instance with it; the sole purpose of the hierarchy is to 
 *  provide something for the matcher to iterate over. For each
 *  ShadowType class there is a ShadowMatch class that is used to hold
 *  individual matching results.
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public abstract class ShadowType {
    /** Could a given MethodPosition match here? */
    public abstract ShadowMatch matchesAt(MethodPosition pos);

    private static List/*<ShadowType>*/ allShadowTypes=new LinkedList();

    public static void reset() {
	allShadowTypes=new LinkedList();
    }
    

    /** Call this for each shadow type we want to be active */
    public static void register(ShadowType st) {
        allShadowTypes.add(st);
    }

    public static Iterator shadowTypesIterator() {
        return allShadowTypes.iterator();
    }

}
