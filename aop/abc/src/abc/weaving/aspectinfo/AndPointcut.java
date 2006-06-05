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

import polyglot.types.SemanticException;
import polyglot.util.Position;
import abc.weaving.matching.MatchingContext;
import abc.weaving.residues.AndResidue;
import abc.weaving.residues.Residue;

/** Pointcut conjunction. 
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 *  @author Damien Sereni
 *  @author Eric Bodden
 */
public class AndPointcut extends Pointcut {
    private Pointcut pc1;
    private Pointcut pc2;

    private AndPointcut(Pointcut pc1, Pointcut pc2, Position pos) {
	super(pos);
	this.pc1 = pc1;
	this.pc2 = pc2;
    }

    public static Pointcut construct(Pointcut pc1, Pointcut pc2, Position pos) {
	if(pc1 instanceof EmptyPointcut || pc2 instanceof FullPointcut) return pc1;
	if(pc2 instanceof EmptyPointcut || pc1 instanceof FullPointcut) return pc2;
	return new AndPointcut(pc1,pc2,pos);
    }

    public Pointcut getLeftPointcut() {
	return pc1;
    }

    public Pointcut getRightPointcut() {
	return pc2;
    }

    public Residue matchesAt(MatchingContext mc)
	throws SemanticException
    {
	return AndResidue.construct(pc1.matchesAt(mc),
				    pc2.matchesAt(mc));
    }
    
    public Pointcut inline(Hashtable renameEnv,Hashtable typeEnv, 
			      Aspect context,int cflowdepth) {
	Pointcut pc1=this.pc1.inline(renameEnv,typeEnv,context,cflowdepth);
	Pointcut pc2=this.pc2.inline(renameEnv,typeEnv,context,cflowdepth);
	if(pc1==this.pc1 && pc2==this.pc2) return this;
	else return construct(pc1,pc2,getPosition());
    }

    public DNF dnf() {
	DNF dnf1=pc1.dnf();
	DNF dnf2=pc2.dnf();
	return DNF.and(dnf1,dnf2);
    }

    public String toString() {
	return "("+pc1+") && ("+pc2+")";
    }

    public void registerSetupAdvice(Aspect context,Hashtable typeMap) {
	pc1.registerSetupAdvice(context,typeMap);
	pc2.registerSetupAdvice(context,typeMap);
    }

    public void getFreeVars(Set result) {
	pc1.getFreeVars(result);
	pc2.getFreeVars(result);
    }

    /* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#unify(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable, java.util.Hashtable, abc.weaving.aspectinfo.Pointcut)
	 */
	public boolean unify(Pointcut otherpc, Unification unification) {

		if (otherpc.getClass() == this.getClass()) {
			AndPointcut oth = (AndPointcut)otherpc;
			if (pc1.unify(oth.getLeftPointcut(), unification)) {
				Pointcut pc1new = unification.getPointcut();
				if (pc2.unify(oth.getRightPointcut(), unification)) {
					Pointcut pc2new = unification.getPointcut();
					if ((pc1new == pc1) && (pc2new == pc2))
						unification.setPointcut(this);
					else 
					{
						if (unification.unifyWithFirst())
							throw new RuntimeException("Unfication error: restricted unification failed");
						if ((pc1new == oth.getLeftPointcut()) && (pc2new == oth.getRightPointcut()))
							unification.setPointcut(otherpc);
						else
							unification.setPointcut(AndPointcut.construct(pc1new, pc2new, getPosition()));
					} 
					return true;
				} else return false;
			} else  return false;
		} else // Do the right thing if otherpc was a local vars pc
			return LocalPointcutVars.unifyLocals(this,otherpc,unification); 
			

	}
}
