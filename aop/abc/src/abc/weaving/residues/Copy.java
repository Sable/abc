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
import soot.jimple.*;
import soot.util.Chain;
import abc.weaving.weaver.WeavingContext;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.*;

/** Copy a weaving variable into another one
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 */ 

public class Copy extends Residue implements BindingLink {
    public WeavingVar from;
    public WeavingVar to;

    public Residue optimize() { return this; }
    public Residue inline(ConstructorInliningMap cim) {
        return new Copy(from.inline(cim), to.inline(cim));
    }
    
	public WeavingVar getAdviceFormal(WeavingVar var) {
		if (var==from)
			return to;
		
		return null;
	}
    public Copy(WeavingVar from,WeavingVar to) {
	this.from=from;
	this.to=to;
    }
    public Residue resetForReweaving() {
    	from. resetForReweaving();
    	to.resetForReweaving();
    	return this;
    }
    public String toString() {
	return "copy("+from+"->"+to+")";
    }

    public Stmt codeGen
	(SootMethod method,LocalGeneratorEx localgen,
	 Chain units,Stmt begin,Stmt fail,boolean sense,
	 WeavingContext wc) {

	return succeed(units,to.set(localgen,units,begin,wc,from.get()),fail,sense);
    }
}
