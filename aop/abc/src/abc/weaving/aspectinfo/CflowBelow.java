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
import soot.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Handler for <code>cflowbelow</code> condition pointcut. */
public class CflowBelow extends Pointcut {
    private Pointcut pc;
    int depth;
	private Hashtable/*<String,Var>*/ renaming;

    public CflowBelow(Pointcut pc,Position pos,int depth) {
	super(pos);
	this.pc = pc;
	this.depth=depth;
    }

    public Pointcut getPointcut() {
	return pc;
    }

    public int getDepth() {
	return depth;
    }

    protected DNF dnf() {
	return new DNF
	    (new CflowBelow
	     (pc.dnf().makePointcut(pc.getPosition()),getPosition(),depth));
    }

    public String toString() {
	return "cflowbelow("+pc+")";
    }

    protected Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context) {
	Pointcut pc=this.pc.inline(renameEnv,typeEnv,context);
	if(pc==this.pc) return this;
	else return new CflowBelow(pc,getPosition(),depth);
    }

    private CflowSetup setupAdvice;

    public void registerSetupAdvice(Aspect context,Hashtable typeMap) {

	GlobalCflowSetupFactory.CflowSetupContainer cfsCont = 
	    GlobalCflowSetupFactory.construct(context,pc,true,typeMap,getPosition(),depth);

	setupAdvice = cfsCont.getCfs();
	renaming = cfsCont.getRenaming();

	// Should only do this if the advice has not already been added.

	if (cfsCont.isFresh()) {
	    GlobalAspectInfo.v().addAdviceDecl(setupAdvice);
	}

    }

    public Residue matchesAt
	(WeavingEnv env,SootClass cls,
	 SootMethod method,ShadowMatch sm) {

	List/*<Var>*/ actuals=setupAdvice.getActuals();
	// List of actuals for the Cflow setup advice
	// These are NOT necessarily the same as the actuals for
	// this (inlined) pointcut, but we have the renaming
	List/*<WeavingVar>*/ weavingActuals=new LinkedList();
	Iterator it=actuals.iterator();
	while(it.hasNext()) {
		Var setupvar = (Var) it.next();
		Var inlinedvar = (Var) renaming.get(setupvar);
		if (inlinedvar == null) {
			throw new RuntimeException("Internal error: Could not find variable "+
					setupvar.getName() + " in cflow renaming");
		}
		weavingActuals.add(env.getWeavingVar(inlinedvar));
	}
	return new CflowResidue(setupAdvice,weavingActuals);
    }

    public void getFreeVars(Set result) {
	pc.getFreeVars(result);
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		if (otherpc instanceof CflowBelow) {
			return pc.equivalent(((CflowBelow)otherpc).getPointcut(), renaming);
		} else return false;
	}

}
