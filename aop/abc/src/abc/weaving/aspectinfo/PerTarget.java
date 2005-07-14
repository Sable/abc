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
import abc.weaving.matching.ShadowMatch;
import abc.weaving.residues.*;

/** A <code>pertarget</code> per clause.
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 */
public class PerTarget extends PerPointcut {
    public PerTarget(Pointcut pc, Position pos) {
        super(pc, pos);
    }

    public String toString() {
        return "pertarget("+getPointcut()+")";
    }

    public void registerSetupAdvice(Aspect aspct) {
        abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().addAdviceDecl(new PerTargetSetup(aspct,getPointcut(),getPosition()));
    }


    public Residue matchesAt(Aspect aspct,ShadowMatch sm) {
        ContextValue targetCV=sm.getTargetContextValue();
        if(targetCV==null) return NeverMatch.v();
        return new HasAspect(aspct.getInstanceClass().getSootClass(),targetCV);
    }

    public Residue getAspectInstance(Aspect aspct,ShadowMatch sm) {
        ContextValue targetCV=sm.getTargetContextValue();
        if(targetCV==null) return NeverMatch.v();
        return new AspectOf(aspct.getInstanceClass().getSootClass(),targetCV);
    }
}
