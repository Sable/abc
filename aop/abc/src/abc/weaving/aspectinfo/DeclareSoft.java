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
import abc.weaving.matching.*;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.Tagger;
import abc.weaving.weaver.WeavingContext;
import abc.weaving.weaver.SingleValueWeavingContext;
import abc.soot.util.LocalGeneratorEx;

/** A <code>declare soft</code> declaration. 
 *  @author Ganesh Sittampalam
 */
public class DeclareSoft extends AbstractAdviceDecl {
    private AbcType exc;
    private Pointcut pc;

    public DeclareSoft(AbcType exc, Pointcut pc, Aspect aspct, Position pos) {
	super(aspct,new SoftenAdvice(exc,pos),
	      pc, new ArrayList(), pos);
	this.exc = exc;
    }

    // static because otherwise we can't use it in the constructor call of the super class
    public static class SoftenAdvice extends AfterThrowingAdvice {
	AbcType exc;

	public SoftenAdvice(AbcType exc, Position pos) {
	    super(pos);
	    this.exc=exc;
	}

	public String toString() {
	    return "soften exception";
	}

	// We inherit the matchesAt method from AfterThrowingAdvice,
	// because the binding of the formal is best done as a special
	// case in the weaver for after throwing advice

	public RefType getCatchType() {
	    return ((RefType) exc.getSootType());
	}

	public void bindException(WeavingContext wc,AbstractAdviceDecl ad,Local exception) {
	    ((SingleValueWeavingContext) wc).value=exception;
	}
    }

    /** Get the softened exception. */
    public AbcType getException() {
	return exc;
    }


    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+" from aspect: "+getAspect().getName()+"\n");
	sb.append(prefix+" exception: "+exc+"\n");
	sb.append(prefix+" pointcut: "+pc+"\n");
	sb.append(prefix+" special: declare soft\n");
    }

    public WeavingEnv getWeavingEnv() {
	return new EmptyFormals();
    }

    public WeavingContext makeWeavingContext() {
	return new SingleValueWeavingContext();
    }

    public Chain makeAdviceExecutionStmts
	(AdviceApplication adviceappl,LocalGeneratorEx localgen,WeavingContext wc) {

	Chain units=new HashChain();

	SootClass soft=Scene.v().getSootClass("org.aspectj.lang.SoftException");

	Value ex=((SingleValueWeavingContext) wc).value;
	Local softexc=localgen.generateLocal(soft.getType(),"softexception");

	units.addLast(Jimple.v().newAssignStmt(softexc,
					       Jimple.v().newNewExpr(soft.getType())));

	List argsTypeList=new ArrayList(1);
	argsTypeList.add(RefType.v("java.lang.Throwable"));
	SootMethodRef constr=Scene.v().makeConstructorRef(soft,argsTypeList);
	units.addLast(Jimple.v().newInvokeStmt
		      (Jimple.v().newSpecialInvokeExpr(softexc,constr,ex)));


	units.addLast(Jimple.v().newThrowStmt(softexc));
    Tagger.tagChain(units, InstructionKindTag.EXCEPTION_SOFTENER);

	return units;

    }

    public static int getPrecedence(DeclareSoft a,DeclareSoft b) {
	// We know that we are in the same aspect

	if(a.getPosition().line() < b.getPosition().line()) 
	    return GlobalAspectInfo.PRECEDENCE_FIRST;
	if(a.getPosition().line() > b.getPosition().line()) 
	    return GlobalAspectInfo.PRECEDENCE_SECOND;

	if(a.getPosition().column() < b.getPosition().column()) 
	    return GlobalAspectInfo.PRECEDENCE_FIRST;
	if(a.getPosition().column() > b.getPosition().column()) 
	    return GlobalAspectInfo.PRECEDENCE_SECOND;

	// Trying to compare the same advice, I guess... (modulo inlining behaviour)
	return GlobalAspectInfo.PRECEDENCE_NONE;

    }

    public String toString() {
	return "soften "+getException()+" at "+getPointcut();
    }
}
