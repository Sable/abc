/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
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

package abc.weaving.residues;

import soot.*;
import abc.weaving.weaver.*;
import java.util.*;

/** The base class defining a value to be extracted from the context
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 *  @date 30-Apr-04
 */ 

public abstract class ContextValue {
    /** Force subclasses to implement toString */
    public abstract String toString();

    public abstract Type getSootType();

    /** get a soot value corresponding to this contextvalue */
    public abstract Value getSootValue();

    public abstract ContextValue inline(ConstructorInliningMap cim);
    public static List/*ContextValue*/ inline( List/*ContextValue*/ list,
            ConstructorInliningMap cim) {
        List ret = new ArrayList(list.size());
        for( Iterator cvIt = list.iterator(); cvIt.hasNext(); ) {
            final ContextValue cv = (ContextValue) cvIt.next();
            ret.add(cv.inline(cim));
        }
        return ret;
    }
}
