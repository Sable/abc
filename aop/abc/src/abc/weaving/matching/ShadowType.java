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
 */
public abstract class ShadowType {
    /** Find out if there is a join point shadow of the relevant type at the
     *  given MethodPosition, and if so return it
     */
    public abstract ShadowMatch matchesAt(MethodPosition pos);
}
