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
import abc.weaving.residues.NotResidue;
import abc.weaving.residues.Residue;

/** Pointcut negation. 
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 *  @author Damien Sereni
 *  @author Eric Bodden
 */
public class NotPointcut extends Pointcut {
    private Pointcut pc;

    private NotPointcut(Pointcut pc, Position pos) {
	super(pos);
	this.pc = pc;
    }

    public static Pointcut construct(Pointcut pc, Position pos) {
	if(pc instanceof EmptyPointcut) return new FullPointcut(pos);
	if(pc instanceof FullPointcut) return new EmptyPointcut(pos);
	return new NotPointcut(pc,pos);
    }
    
    public Pointcut getPointcut() {
	return pc;
    }

    public Residue matchesAt(MatchingContext mc)
	throws SemanticException
    {
	return NotResidue.construct(pc.matchesAt(mc));
    }

    public Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context,
			      int cflowdepth) {
	Pointcut pc=this.pc.inline(renameEnv,typeEnv,context,cflowdepth);
	if(pc==this.pc) return this;
	else return construct(pc,getPosition());
    }

    public DNF dnf() {
	return new DNF(new NotPointcut
		       (pc.dnf().makePointcut(pc.getPosition()),getPosition()));
    }

    public String toString() {
	return "!("+pc+")";
    }

    public void registerSetupAdvice(Aspect context,Hashtable typeMap) {
	pc.registerSetupAdvice(context,typeMap);
    }


    public void getFreeVars(Set result) {
	pc.getFreeVars(result);
    }

    /* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#unify(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable, java.util.Hashtable, abc.weaving.aspectinfo.Pointcut)
	 */
	public boolean unify(Pointcut otherpc, Unification unification) {

		if (otherpc.getClass() == this.getClass()) {
			if (pc.unify(((NotPointcut)otherpc).getPointcut(), unification)) {
				if (unification.getPointcut() == pc)
					unification.setPointcut(this);
				else {
					if (unification.unifyWithFirst())
						throw new RuntimeException("Unfication error: restricted unification failed");
				if (unification.getPointcut() == ((NotPointcut)otherpc).getPointcut())
					unification.setPointcut(otherpc);
				else
					unification.setPointcut(new NotPointcut(pc, getPosition()));
				}
				return true;
			} else return false;
		} else // Do the right thing if otherpc was a local vars pc
			return LocalPointcutVars.unifyLocals(this,otherpc,unification);

	}
}
