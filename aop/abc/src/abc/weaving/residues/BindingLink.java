/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Sascha Kuzins
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

/**
 * 
 Implemented by residues that know how one weaving var is linked to another,
 namely Copy, Box, and And (because it may contain Copy and Box residues).
 
 This is needed to determine the binding between the local and the advice-formal in residues like
  (((bind(jimplevalue(o),polylocalvar(box:null))) && 
  (box(polylocalvar(box:null)->polylocalvar(boxed:null)))) && 
  (copy(polylocalvar(boxed:null)->advicearg(0:java.lang.Object)))) 
  
  
  @author Sascha Kuzins
 
 */
interface BindingLink {
	public abstract WeavingVar getAdviceFormal(WeavingVar var);
}
