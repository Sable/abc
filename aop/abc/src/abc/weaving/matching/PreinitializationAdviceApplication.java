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

package abc.weaving.matching;

import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.Residue;
import abc.weaving.weaver.*;

/** Application of advice at a preinitialization joinpoint
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 */
public class PreinitializationAdviceApplication 
    extends ConstructorAdviceApplication {
    public PreinitializationAdviceApplication(AbstractAdviceDecl advice,Residue residue) {
	super(advice,residue);
    }

    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+"preinitialization"+"\n");
	super.debugInfo(prefix,sb);
    }
    public AdviceApplication inline( ConstructorInliningMap cim ) {
        PreinitializationAdviceApplication ret = new PreinitializationAdviceApplication(advice, getResidue().inline(cim));
        ret.shadowmatch = shadowmatch.inline(cim);
        return ret;
    }
}
