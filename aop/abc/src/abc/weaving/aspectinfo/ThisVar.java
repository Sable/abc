/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Damien Sereni
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

import java.util.Hashtable;
import java.util.Set;

import polyglot.util.Position;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.Bind;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.Residue;
import abc.weaving.residues.WeavingVar;

/** Handler for <code>this</code> condition pointcut with a variable argument. 
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 *  @author Damien Sereni
 *  @author Eric Bodden
 */
public class ThisVar extends ThisAny {
	protected Var var;
	protected WeavingVar weavingVar;

    public ThisVar(Var var,Position pos) {
	super(pos);
	this.var = var;
    }

    /** Get the pointcut variable that is bound by this
     *  <code>this</code> pointcut.
     */
    public Var getVar() {
	return var;
    }

    public String toString() {
	return "this("+var+")";
    }

    protected Residue matchesAt(WeavingEnv we,ContextValue cv) {
	weavingVar = we.getWeavingVar(var);
	return Bind.construct
	    (cv,we.getAbcType(var).getSootType(),weavingVar);
    }

    public Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context,
			      int cflowdepth) {
	Var var=this.var.rename(renameEnv);

	if(var==this.var) return this;
	else return new ThisVar(var,getPosition());
    }
    
    public void getFreeVars(Set/*<String>*/ result) {
	result.add(var.getName());
    }
    
	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#unify(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable, java.util.Hashtable, abc.weaving.aspectinfo.Pointcut)
	 */
	public boolean unify(Pointcut otherpc, Unification unification) {

		if (otherpc.getClass() == this.getClass()) {
			Var othervar = ((ThisVar)otherpc).getVar();
			if (var.unify(othervar, unification)) {
				Var unifiedvar = unification.getVar();
				if (unifiedvar == var) {
					unification.setPointcut(this);
					return true;
				} else {
					if (unification.unifyWithFirst())
							throw new RuntimeException("Unfication error: restricted unification failed");
					if (unifiedvar == othervar) {
					unification.setPointcut(otherpc);
					return true;
				} else {
					unification.setPointcut(new ThisVar(unifiedvar, unifiedvar.getPosition()));
					return true;
				} 
				}
			} else return false;
		} else if (otherpc.getClass() == ThisType.class) {
			// If the other pc is a ThisType with the same type as this Var, can unify
			if (abc.main.Debug.v().debugPointcutUnification)
				System.out.println("Trying to unify a ThisVar "+this+" with a ThisType: "+otherpc);
			ThisType othertt = (ThisType)otherpc;
			if (unification.getType1(this.getVar().getName()).equals(othertt.getType())) {
				if (abc.main.Debug.v().debugPointcutUnification)
					System.out.println("Succeeded!");
				unification.setPointcut(this);
				unification.put2(this.getVar(), new VarBox());
				return true;
			} else return false;
		} else // Do the right thing if otherpc was a local vars pc
			return LocalPointcutVars.unifyLocals(this,otherpc,unification);

	}
}
