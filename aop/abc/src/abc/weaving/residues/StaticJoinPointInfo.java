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

import polyglot.util.InternalCompilerError;
import soot.*;
import soot.jimple.Jimple;
import abc.weaving.matching.SJPInfo;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.weaver.*;

/** A value that will become a thisJoinPointStaticPart structure at runtime
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 */
public class StaticJoinPointInfo extends ContextValue {

    private SJPInfo sjpInfo;

    public ContextValue inline(ConstructorInliningMap cim) {
        return new StaticJoinPointInfo(sjpInfo);
    }
    public StaticJoinPointInfo(SJPInfo sjpInfo) {
	if(sjpInfo==null) 
	    throw new InternalCompilerError("StaticJoinPointInfo constructed with null argument");
	this.sjpInfo=sjpInfo;
    }

    public String toString() {
	return "thisJoinPointStaticPart";
    }

    public Type getSootType() {
	return RefType.v("org.aspectj.lang.JoinPoint$StaticPart");
    }

    public Value getSootValue() {
	return Jimple.v().newStaticFieldRef(sjpInfo.sjpfield().makeRef());
    }
}
