/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
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
import soot.jimple.*;
import soot.util.*;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.residues.*;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.InstructionShadowTag;
import abc.weaving.tagkit.InstructionSourceTag;
import abc.weaving.tagkit.Tagger;
import abc.weaving.weaver.WeavingContext;
import abc.soot.util.LocalGeneratorEx;

/** Synthetic advice to support instantiation of percflow aspects
 *  @author Ganesh Sittampalam
 */
public class PerCflowSetup extends PerSetupAdvice {

    private boolean isBelow;

    public PerCflowSetup(Aspect aspct,Pointcut pc,
			   boolean isBelow,Position pos) {
	super(new BeforeAfterAdvice(pos),aspct,pc,pos);
	this.isBelow=isBelow;
    }

    public boolean isBelow() {
	return isBelow;
    }

    public static class PerCflowSetupWeavingContext 
	extends WeavingContext
	implements BeforeAfterAdvice.ChoosePhase {

	public boolean doBefore;
	public void setBefore() { doBefore=true; }
	public void setAfter() { doBefore=false; }

    }

    public WeavingContext makeWeavingContext() {
	return new PerCflowSetupWeavingContext();
    }



    public Chain makeAdviceExecutionStmts
	 (AdviceApplication adviceappl,LocalGeneratorEx localgen,WeavingContext wc) {

	PerCflowSetupWeavingContext cswc=(PerCflowSetupWeavingContext) wc;

	if(cswc.doBefore) {

	    Chain c = new HashChain();
	    Type object=Scene.v().getSootClass("java.lang.Object").getType();
	    
	    SootClass aspectClass=getAspect().getInstanceClass().getSootClass();

	    SootMethodRef push=Scene.v().makeMethodRef
		(aspectClass,"abc$perCflowPush",new ArrayList(),VoidType.v(),true);

	    c.addLast(Jimple.v().newInvokeStmt
		      (Jimple.v().newStaticInvokeExpr(push)));
        Tagger.tagChain(c, InstructionKindTag.PERCFLOW_ENTRY);
        Tagger.tagChain(c, new InstructionSourceTag(adviceappl.advice.sourceId));
        Tagger.tagChain(c, new InstructionShadowTag(adviceappl.shadowmatch.shadowId));
	    return c;
	} else {
	    Chain c=new HashChain();
	    SootClass stackClass=Scene.v()
		.getSootClass("org.aspectbench.runtime.internal.CFlowStack");
	    SootClass aspectClass=getAspect().getInstanceClass().getSootClass();

	    SootMethodRef pop=Scene.v().makeMethodRef(stackClass,"pop",new ArrayList(),VoidType.v(),false);
	    SootFieldRef perCflowStackField
		=Scene.v().makeFieldRef(aspectClass,"abc$perCflowStack",stackClass.getType(),true);

	    Local perCflowStackLoc=localgen.generateLocal(stackClass.getType(),"perCflowStack");
	    c.addLast(Jimple.v().newAssignStmt
		      (perCflowStackLoc,Jimple.v().newStaticFieldRef(perCflowStackField)));
	    c.addLast(Jimple.v().newInvokeStmt
		      (Jimple.v().newVirtualInvokeExpr(perCflowStackLoc,pop)));
        Tagger.tagChain(c, InstructionKindTag.PERCFLOW_EXIT);
        Tagger.tagChain(c, new InstructionSourceTag(adviceappl.advice.sourceId));
        Tagger.tagChain(c, new InstructionShadowTag(adviceappl.shadowmatch.shadowId));
	    return c;
	}
    }

    public Residue postResidue(ShadowMatch sm) {
	return AlwaysMatch.v();
    }


    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+" from aspect: "+getAspect().getName()+"\n");
	sb.append(prefix+" type: "+spec+"\n");
	sb.append(prefix+" pointcut: "+pc+"\n");
	sb.append(prefix+" special: percflow"+(isBelow?"below":"")+" instantiation\n");
    }
}
