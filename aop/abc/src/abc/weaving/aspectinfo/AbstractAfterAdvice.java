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

import polyglot.util.*;

import abc.weaving.matching.*;
import abc.weaving.residues.*;
import abc.polyglot.util.ErrorInfoFactory;

/** Abstract base class for all forms of "after" advice
 *  @author Ganesh Sittampalam
 */
public abstract class AbstractAfterAdvice extends AbstractAdviceSpec {
    public AbstractAfterAdvice(Position pos) {
        super(pos);
    }

    public Residue matchesAt(WeavingEnv we,ShadowMatch sm,AbstractAdviceDecl ad) {
        if(sm.supportsAfter()) return AlwaysMatch.v();
        // FIXME: should be a multi-position error
        if(ad instanceof AdviceDecl)
            abc.main.Main.v().error_queue.enqueue
                (ErrorInfoFactory.newErrorInfo
                 (ErrorInfo.WARNING,
                  sm.joinpointName()+" join points do not support after advice, but some advice from "+ad.errorInfo()
                  +" would otherwise apply here",
                  sm.getContainer(),
                  sm.getHost()));

        return NeverMatch.v();
    }

}
