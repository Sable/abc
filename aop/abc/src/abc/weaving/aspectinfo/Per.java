/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
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

import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** A per clause. 
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
*/
public abstract class Per extends Syntax {
    public Per(Position pos) {
	super(pos);
    }

    public abstract String toString();

    /** Register any advice declarations required to setup the aspect instances */
    public abstract void registerSetupAdvice(Aspect aspct);

    // These are separate because we want to check for the aspect first (if appropriate), 
    // but bind the local last. They are residues because in the case of proper per-advice,
    // we need shadow-specific stuff like the target.
    public abstract Residue matchesAt(Aspect aspct,ShadowMatch sm);
    public abstract Residue getAspectInstance(Aspect aspct,ShadowMatch sm);
}
