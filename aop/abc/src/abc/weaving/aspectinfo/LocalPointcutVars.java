/* Abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Damien Sereni
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

/** Declare local pointcut variables. These can appear
 *  after inlining
 *  @author Ganesh Sittampalam
 *  @author Damien Sereni
 */
public class LocalPointcutVars extends Pointcut {
    private Pointcut pc;
    private List/*<Formal>*/ formals;

    public LocalPointcutVars(Pointcut pc,List/*<Formal>*/ formals, Position pos) {
	super(pos);
	this.pc = pc;
	this.formals = formals;
    }

    public Pointcut getPointcut() {
	return pc;
    }

    public List getFormals() {
	return formals;
    }


    public Residue matchesAt(WeavingEnv we,
			     SootClass cls,
			     SootMethod method,
			     ShadowMatch sm) 
	throws SemanticException
    {
	WeavingEnv lwe=new LocalsDecl(formals,we);
	return pc.matchesAt(lwe,cls,method,sm);
    }

    protected Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context) {

	List/*<Formal>*/ newFormals=new ArrayList(formals.size());

	Hashtable newRenameEnv=new Hashtable(renameEnv);

	Iterator it=formals.iterator();
	while(it.hasNext()) {
	    Formal old=(Formal) it.next();

	    String newName=Pointcut.freshVar();
	    newFormals.add(new Formal(old.getType(),newName,old.getPosition()));

	    newRenameEnv.put(old.getName(),new Var(newName,old.getPosition()));
	}
	
	Pointcut pc=this.pc.inline(newRenameEnv,typeEnv,context);
	if(pc==this.pc) return this;
	else return new LocalPointcutVars(pc,newFormals,getPosition());
    }

    protected DNF dnf() {
	return DNF.declare(pc.dnf(),formals);
    }



    public String toString() {
	return "local"+formals+" ("+pc+")";
    }

    public void registerSetupAdvice(Aspect context,Hashtable typeMap) {
	Hashtable newTypeMap=new Hashtable(typeMap);
	Iterator it=formals.iterator();
	while(it.hasNext()) {
	    Formal f=(Formal) it.next();
	    newTypeMap.put(f.getName(),f.getType());
	}
	pc.registerSetupAdvice(context,newTypeMap);
    }

    public void getFreeVars(Set/*<String>*/ result) {
	pc.getFreeVars(result);
	Iterator it=formals.iterator();
	while(it.hasNext()) result.remove(((Formal) (it.next())).getName());
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean canRenameTo(Pointcut otherpc, Hashtable renaming) {
		if (otherpc.getClass() == this.getClass()) {
			LocalPointcutVars other = (LocalPointcutVars) otherpc; 
			if (pc.canRenameTo(other.getPointcut(), renaming)) {
				// The inner pcs are equivalent, and we have the renaming
				// Are the variables to be abstracted the same?
				// ie require that corresponding elements in the lists of formals:
				//   - have the same type
				//   - are related by the substitution
				
				// FIXME This is much too restrictive for comparing pcs with different bound vars
				
				Iterator it1 = formals.iterator();
				Iterator it2 = other.getFormals().iterator();
				while (it1.hasNext() && it2.hasNext()) {
					Formal form1 = (Formal) it1.next();
					Formal form2 = (Formal) it2.next();
					
					if (!form1.canRenameTo(form2, renaming)) 
							return false;	
				}
				if (it1.hasNext() || it2.hasNext()) return false;
				// The lists have the same length and corresponding elements are related
				// We are done
				return true;
			} else return false;
		} else return false;
	}

}
