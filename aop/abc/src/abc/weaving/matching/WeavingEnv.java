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

import abc.weaving.aspectinfo.*;
import abc.weaving.residues.WeavingVar;

/** Provides the mapping from named variables in pointcuts
 *  to the weaving position and type. It is generated
 *  from the advice declaration, and used during pointcut
 *  matching to check the declared type of pointcut variables
 *  and to construct residues which bind values to these
 *  variables.
 *  @author Ganesh Sittampalam
 */
public interface WeavingEnv {

    /** Return the weaving variable corresponding to the given named
     *  pointcut variable
     *  @param v The pointcut variable
     *  @author Ganesh Sittampalam
     */
    public WeavingVar getWeavingVar(Var v);

    /** Return the declared type of the given named pointcut variable
     *  @param v The pointcut variable
     *  @author Ganesh Sittampalam
     */
    public AbcType getAbcType(Var v);
}
