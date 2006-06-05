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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import abc.weaving.matching.MatchingContext;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.CflowResidue;
import abc.weaving.residues.Residue;

/** Handler for <code>cflowbelow</code> condition pointcut. 
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 *  @author Damien Sereni
 *  @author Eric Bodden
 */
public class CflowBelow extends CflowPointcut {
    public int depth=-1;

    public CflowBelow(Pointcut pc,Position pos) {
	super(pos);
	setPointcut(pc);
    }

    protected CflowBelow(Pointcut pc,Position pos,int depth) {
	super(pos);
	setPointcut(pc);
	this.depth=depth;
    }

    public int getDepth() {
	return depth;
    }

    public DNF dnf() {
	CflowBelow ret=new CflowBelow
	    (getPointcut().dnf().makePointcut(getPointcut().getPosition()),getPosition(),depth);
	return new DNF(ret);
    }

    public String toString() {
	return "cflowbelow("+getPointcut()+")";
    }

    public Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context,
			      int cflowdepth) {
	Pointcut pc=this.getPointcut().inline(renameEnv,typeEnv,context,cflowdepth+1);
	CflowBelow ret;
	if(pc==this.getPointcut()) ret=this;
	else ret=new CflowBelow(pc,getPosition(),depth);
	if(depth==-1) ret.depth=cflowdepth;
	return ret;
    }

    public void registerSetupAdvice(Aspect context,Hashtable typeMap) {
   	if(depth==-1) 
	    throw new InternalCompilerError("uninlined cflowbelow registered",getPosition());   
	GlobalCflowSetupFactory.CfsContainer cfsCont = 
	    GlobalCflowSetupFactory.construct(context,getPointcut(),true,typeMap,getPosition(),depth);
	setCfs(cfsCont.getCfs());
	setRenaming(cfsCont.getRenaming());
	setTypeMap(typeMap);
	getCfs().addUse(this);
    }

    public Residue matchesAt(MatchingContext mc) {
    WeavingEnv env = mc.getWeavingEnv();

	List/*<Var>*/ actuals=getCfs().getActuals();
	// List of actuals for the Cflow setup advice
	// These are NOT necessarily the same as the actuals for
	// this (inlined) pointcut, but we have the renaming
	List/*<WeavingVar>*/ weavingActuals=new LinkedList();
	Iterator it=actuals.iterator();
	while(it.hasNext()) {
		Var setupvar = (Var) it.next();
		VarBox inlinedvar = 
			(VarBox) getRenaming().get(setupvar);
		if (inlinedvar == null) {
			throw new RuntimeException("Internal error: Could not find variable "+
					setupvar.getName() + " in cflow renaming from cflowbelow:\n"+getPointcut()+
					"\nwith CFS pointcut\n:"+getCfs().getPointcut());
		}
		if (inlinedvar.hasVar())
			weavingActuals.add(env.getWeavingVar(inlinedvar.getVar()));
		else
			weavingActuals.add(null);
	}
	return new CflowResidue(getCfs(),weavingActuals);
    }

    public void getFreeVars(Set result) {
    	getPointcut().getFreeVars(result);
    }
	
    /* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#unify(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable, java.util.Hashtable, abc.weaving.aspectinfo.Pointcut)
	 */
	public boolean unify(Pointcut otherpc, Unification unification) {

		if (otherpc.getClass() == this.getClass()) {
			if (getPointcut().unify(((CflowBelow)otherpc).getPointcut(), unification)) {
				if (unification.getPointcut() == getPointcut())
					unification.setPointcut(this);
				else {
					if (unification.unifyWithFirst())
						throw new RuntimeException("Unfication error: restricted unification failed");
					if (unification.getPointcut() == ((CflowBelow)otherpc).getPointcut())
						unification.setPointcut(otherpc);
					else
						unification.setPointcut(new CflowBelow(getPointcut(), getPosition(), depth));
				}
				return true;
			} else return false;
		} else // Do the right thing if otherpc was a local vars pc
			return LocalPointcutVars.unifyLocals(this,otherpc,unification);

	}
}
