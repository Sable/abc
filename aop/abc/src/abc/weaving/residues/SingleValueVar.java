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
import soot.Value;
import soot.Local;
import soot.Type;
import soot.jimple.Stmt;
import soot.jimple.Jimple;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;
import abc.weaving.weaver.SingleValueWeavingContext;
import abc.weaving.weaver.*;

/** For cases where there is just one value needed in the weaving context
 *  Don't ever use it in a context where a get() might be done on it
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 */ 

public class SingleValueVar extends WeavingVar {
    public Type type;
    public Value value;

    public WeavingVar inline(ConstructorInliningMap cim) {
        if( value != null ) throw new InternalCompilerError("can't inline once value has been set");
        WeavingVar ret = cim.map(this);
        if(ret == null) {
            ret = new SingleValueVar(type);
            cim.add(this, ret);
        }
        return ret;
    }
    public void resetForReweaving() {}
    
    public SingleValueVar(Type type) {
	this.type=type;
    }

    public String toString() {
	return "singlevaluevar("+type+")";
    }

    public Stmt set(LocalGeneratorEx localgen,Chain units,Stmt begin,WeavingContext wc,Value val) {
	((SingleValueWeavingContext) wc).value=val;
	return begin;
    }

    public Local get() {
	throw new InternalCompilerError("Can't read from a SingleValueVar");
    }

    public boolean hasType() {
	return true;
    }

    public Type getType() {
	return type;
    }

}
