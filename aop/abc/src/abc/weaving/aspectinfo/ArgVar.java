/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Damien Sereni
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

import java.util.*;

import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.*;

/** An argument pattern denoting a pointcut variable. 
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 *  @author Damien Sereni
 */
public class ArgVar extends ArgAny {
    private Var var;

    public ArgVar(Var var, Position pos) {
	super(pos);
	this.var = var;
    }

    public Var getVar() {
	return var;
    }

    public String toString() {
	return var.toString();
    }

    public Residue matchesAt(WeavingEnv we,ContextValue cv) {
	return Bind.construct
	    (cv,we.getAbcType(var).getSootType(),we.getWeavingVar(var));
    }

    public Var substituteForPointcutFormal
	(Hashtable/*<String,Var>*/ renameEnv,
	 Hashtable/*<String,AbcType>*/ typeEnv,
	 Formal formal,
	 List/*<Formal>*/ newLocals,
	 List/*<CastPointcutVar>*/ newCasts,
	 Position pos) {

	Var oldvar=this.var.rename(renameEnv);
    
	AbcType actualType=(AbcType) typeEnv.get(var.getName());
	
	if(actualType==null) throw new RuntimeException(var.getName());

	if(actualType.getSootType().equals
	   (formal.getType().getSootType())) {

	    return oldvar;
	}

	String name=Pointcut.freshVar();
	Var newvar=new Var(name,pos);
	
	newLocals.add(new Formal(formal.getType(),name,pos));
	newCasts.add(new CastPointcutVar(newvar,oldvar,pos));

	return newvar;
    }

    public void getFreeVars(Set/*<String>*/ result) {
	result.add(var.getName());
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.ArgPattern#unify(abc.weaving.aspectinfo.ArgPattern, abc.weaving.aspectinfo.Unification)
	 */
	public boolean unify(ArgPattern other, Unification unification) {
		if (other.getClass() == this.getClass()) {
			Var othervar = ((ArgVar)other).getVar();
			if (var.unify(othervar, unification)) {
				Var unifiedvar = unification.getVar();
				if (unifiedvar == var) {
					unification.setArgPattern(this);
					return true;
				} else {
					if (unification.unifyWithFirst())
						throw new RuntimeException("Unfication error: restricted unification failed");
					if (unifiedvar == othervar) {
					unification.setArgPattern(other);
					return true;
				} else {
					unification.setArgPattern(new ArgVar(unifiedvar, unifiedvar.getPosition()));
					return true;
				} 
				}
			} else return false;
		} else if (other.getClass() == ArgType.class) {
			// If the other pc is an ArgType with the same type as this Var, can unify
			if (abc.main.Debug.v().debugPointcutUnification)
				System.out.println("Trying to unify "+this+" with an ArgType: "+other);
			ArgType otherargtype = (ArgType)other;
			if (unification.getType1(this.getVar().getName()).equals(otherargtype.getType())) {
				if (abc.main.Debug.v().debugPointcutUnification)
					System.out.println("Succeeded!");
				unification.setArgPattern(this);
				unification.put2(this.getVar(), new VarBox());
				return true;
			} else return false;
		} else return false;
	}
}
