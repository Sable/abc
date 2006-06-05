/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
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
import soot.PrimType;
import soot.Scene;
import soot.Type;
import abc.weaving.matching.MatchingContext;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.Box;
import abc.weaving.residues.Copy;
import abc.weaving.residues.Residue;
import abc.weaving.residues.WeavingVar;


/** Cast from one pointcut variable to another. 
 *  This can appear after inlining
 *  @author Ganesh Sittampalam
 *  @author Eric Bodden
 */
public class CastPointcutVar extends Pointcut {
	protected Var from;
    protected Var to;
	protected WeavingVar weavingVarTo;

    public CastPointcutVar(Var from,Var to,Position pos) {
	super(pos);
	this.from=from;
	this.to=to;
    }
    

    public Var getFrom() {
	return from;
    }

    public Var getTo() {
	return to;
    }

    public String toString() {
	return "cast("+from+","+to+")";
    }

    public Residue matchesAt(MatchingContext mc) {
    WeavingEnv we = mc.getWeavingEnv();
	
	Type fromType=we.getAbcType(from).getSootType();
	Type toType=we.getAbcType(to).getSootType();
	weavingVarTo = we.getWeavingVar(to);
	if(fromType instanceof PrimType && 
	   toType.equals(Scene.v().getSootClass("java.lang.Object").getType()))
	    return new Box(we.getWeavingVar(from),weavingVarTo);
	
	// no need to cast, because the rules guarantee this is an upcast...
	return new Copy(we.getWeavingVar(from),weavingVarTo);
    }

    public Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context,
			      int cflowdepth) {
	Var from=this.from;
	if(renameEnv.containsKey(from.getName()))
	   from=(Var) renameEnv.get(from.getName());

	Var to=this.to;
	if(renameEnv.containsKey(to.getName()))
	   to=(Var) renameEnv.get(to.getName());

	if(from != this.from || to != this.to)
	    return new CastPointcutVar(from,to,getPosition());
	else return this;
	   
    }
    public void registerSetupAdvice(Aspect context,Hashtable typeMap) {}
    public void getFreeVars(Set/*<String>*/ result) {
	result.add(to.getName());
    }
    
	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#unify(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable, java.util.Hashtable, abc.weaving.aspectinfo.Pointcut)
	 */
	public boolean unify(Pointcut otherpc, Unification unification) {

		if (otherpc.getClass() == this.getClass()) {
			CastPointcutVar othcast = (CastPointcutVar) otherpc;
			
			if (from.unify(othcast.getFrom(), unification)) {
				Var unifiedFrom = unification.getVar();
				if (to.unify(othcast.getTo(), unification)) {
					Var unifiedTo = unification.getVar();
					if ((unifiedFrom == from) && (unifiedTo == to))
						unification.setPointcut(this);
					else {
						if (unification.unifyWithFirst())
							throw new RuntimeException("Unfication error: restricted unification failed");
						if ((unifiedFrom == othcast.getFrom()) && (unifiedTo == othcast.getTo()))
							unification.setPointcut(otherpc);
						else
							unification.setPointcut(
								new CastPointcutVar(unifiedFrom, unifiedTo, 
													getPosition()));
					}
					return true;
				} else return false;
			} else return false;
		} else // Do the right thing if otherpc was a local vars pc
			return LocalPointcutVars.unifyLocals(this,otherpc,unification);

	}
}
