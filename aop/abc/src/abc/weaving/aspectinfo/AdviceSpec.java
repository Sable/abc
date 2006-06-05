/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2006 Eric Bodden
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

import soot.SootMethod;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.Residue;


/** An advice specification.
 *  @author Aske Simon Christensen
 *  @author Eric Bodden
 */
public interface AdviceSpec {
    
    /** Is this advice spec for "after" advice? 
     *  This affects the precedence of the advice.
     */
    public boolean isAfter();

    /** Does this kind of advice match at the given join point shadow?
     *  @param we The weaving environment
     *  @param sm The shadow match structure
     *  @param ad The advice declaration being matched, for use in generating a good error message
     *            if appropriate
     */
    public Residue matchesAt(WeavingEnv we,ShadowMatch sm,AbstractAdviceDecl ad);

    /** Weave a specific advice application into the given method
     *  using the given local generator. The AdviceSpec is used to
     *  dispatch to the correct weaving method for the advice type.
     *  @author Ganesh Sittampalam
     */
    public void weave(SootMethod method,
		      LocalGeneratorEx localgen,
		      AdviceApplication adviceappl);
}
