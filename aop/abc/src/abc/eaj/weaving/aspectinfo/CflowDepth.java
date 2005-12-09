/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Ondrej Lhotak
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

package abc.eaj.weaving.aspectinfo;

import polyglot.ast.Local;
import abc.weaving.aspectinfo.*;

import java.util.*;

import polyglot.util.Position;
import polyglot.util.InternalCompilerError;
import soot.*;
import abc.weaving.matching.*;
import abc.eaj.weaving.residues.*;
import abc.weaving.residues.*;

/** Handler for <code>cflowdepth</code> pointcut. 
 *  @author Ondrej Lhotak
 */
public class CflowDepth extends abc.weaving.aspectinfo.Cflow {
    private Var depth_var;

    private CflowDepth(Pointcut pc,Position pos,int depth,Var depth_var) {
        super(pc,pos,depth);
        this.depth_var = depth_var;
    }

    public CflowDepth(Pointcut pc,Position pos,Var depth_var) {
	super(pc,pos);
        this.depth_var = depth_var;
    }

    public String toString() {
	return "cflowdepth("+depth_var+", "+getPointcut()+")";
    }

    public void getFreeVars(Set result) {
        super.getFreeVars(result);
        result.add(depth_var.getName());
    }

    public Residue matchesAt(MatchingContext mc) {
        WeavingEnv env = mc.getWeavingEnv();
	SootClass cls = mc.getSootClass();
	SootMethod method = mc.getSootMethod();
	ShadowMatch sm = mc.getShadowMatch();

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
					setupvar.getName() + " in cflow renaming from cflow:\n"+getPointcut());
		}
		if (inlinedvar.hasVar())
		    weavingActuals.add(env.getWeavingVar(inlinedvar.getVar()));
		else
			weavingActuals.add(null);
	}
        WeavingVar wv = env.getWeavingVar(depth_var);
	return new CflowDepthResidue(getCfs(),weavingActuals,wv);
    }
    public Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context,
			      int cflowdepth) {
        Var depth_var = this.depth_var.rename(renameEnv);
	Pointcut pc=this.getPointcut().inline(renameEnv,typeEnv,context,cflowdepth+1);
	CflowDepth ret;
	if(pc==this.getPointcut()) ret=this;
	else ret=new CflowDepth(pc,getPosition(),depth,depth_var);
	if(depth==-1) ret.depth=cflowdepth;
	return ret;
    }
    public DNF dnf() {
	Cflow ret=new CflowDepth(getPointcut().dnf().makePointcut(getPointcut().getPosition()),getPosition(),depth,depth_var);
	return new DNF(ret);
    }
    	public boolean unify(Pointcut otherpc, Unification unification) {

		if (otherpc.getClass() == this.getClass()) {
			if (getPointcut().unify(((Cflow)otherpc).getPointcut(), unification)) {
				if (unification.getPointcut() == getPointcut())
					unification.setPointcut(this);
				else {
				if (unification.unifyWithFirst())
					throw new RuntimeException("Unfication error: restricted unification failed");
				if (unification.getPointcut() == ((Cflow)otherpc).getPointcut())
					unification.setPointcut(otherpc);
				else
					unification.setPointcut(new CflowDepth(getPointcut(), getPosition(), depth, depth_var));
			}
				return true;
			} else return false;
		} else // Do the right thing if otherpc was a local vars pc
			return LocalPointcutVars.unifyLocals(this,otherpc,unification);

	}

}
