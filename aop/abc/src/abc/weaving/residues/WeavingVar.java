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
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;
import abc.weaving.weaver.*;
import java.util.*;

/** A variable for use in weaving
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 *  @date 04-May-04
 */

public abstract class WeavingVar {
    /** Set the variable to the value v and return the last statement added 
     *  (or begin if none)
     */
    public abstract Stmt set
	(LocalGeneratorEx localgen,Chain units,Stmt begin,WeavingContext wc,Value val);

    /** Get the soot local corresponding to this variable (only valid once it has been set) */
    public abstract Local get();

    /** Has this variable got a type yet? */
    public abstract boolean hasType();

    /** Get the soot type corresponding to this variable
     */
    public abstract Type getType();
    
    /** Should primitive typed values be boxed if necessary when writing to this variable? */
    public boolean maybeBox() {
	return getType().equals(Scene.v().getSootClass("java.lang.Object").getType());
    }

    /** Should we reject any binding value that isn't the appropriate type to box to this variable? */
    public boolean mustBox() {
	return false;
    }
    
    public abstract void resetForReweaving();

    public abstract WeavingVar inline(ConstructorInliningMap cim);
    public static List/*WeavingVar*/ inline( List/*WeavingVar*/ list,
            ConstructorInliningMap cim) {
        List ret = new ArrayList(list.size());
        for( Iterator wvIt = list.iterator(); wvIt.hasNext(); ) {
            final WeavingVar wv = (WeavingVar) wvIt.next();
            ret.add(wv.inline(cim));
        }
        return ret;
    }
}
