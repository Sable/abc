/* Abc - The AspectBench Compiler
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
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
import polyglot.types.SemanticException;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Pointcut negation. */
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

    public Residue matchesAt(WeavingEnv we,
			     SootClass cls,
			     SootMethod method,
			     ShadowMatch sm)
	throws SemanticException
    {
	return NotResidue.construct(pc.matchesAt(we,cls,method,sm));
    }

    protected Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context) {
	Pointcut pc=this.pc.inline(renameEnv,typeEnv,context);
	if(pc==this.pc) return this;
	else return construct(pc,getPosition());
    }

    protected DNF dnf() {
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
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		// Renaming is useless here b/c guarantees that no bound vsrs in not
		// but no harm done
		if (otherpc instanceof NotPointcut) {
			return pc.equivalent(((NotPointcut)otherpc).getPointcut(), renaming);
		} else return false;
	}

}
