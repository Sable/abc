/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Laurie Hendren
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

package abc.weaving.weaver;

import java.util.*;

import soot.*;
import soot.util.*;
import soot.jimple.*;

import polyglot.util.InternalCompilerError;

import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;
import abc.soot.util.LocalGeneratorEx;

/** A data structure to keep track of the beginning and end points
 * of a pointcut shadow.   Once created, the beginning and end points
 * will alway point to NOP statements.   Weaving will take place just
 * after the beginning NOP and just before the ending NOP.
 * Each abc.weaving.matching.AdviceApplication instance refers to a
 * ShadowPoints instance.   A ShadowPoints instance is shared between all
 * AdviceApplications that apply to a specific pointcut.
 *   @author Laurie Hendren
 *   @date 03-May-04
 */

public class ShadowPoints {

    private final SootMethod container;

 private final Stmt begin;

 private final Stmt end;

 /** Should always get references to NopStmts.  For all types of pointcuts
  *  both b and e will be non-null. Even handler pointcuts have an ending
  *  nop, so they can handle BeforeAfterAdvice for cflow etc; but the nop
  *  will initially be right next to the starting nop.
  */
 public ShadowPoints(SootMethod container,Stmt b, Stmt e){
    if (b == null) 
      throw new InternalCompilerError("Beginning of shadow point must be non-null");
    if (!(b instanceof NopStmt))
      throw new InternalCompilerError("Beginning of shadow point must be NopStmt");
    if (e == null) 
      throw new InternalCompilerError("Ending of shadow point must be non-null");
    if(!(e instanceof NopStmt))
      throw new InternalCompilerError("Ending of shadow point must be NopStmt");
    begin = b;
    end = e;
    this.container=container;
  }
       
  public Stmt getBegin(){
    return begin;
  }

  public Stmt getEnd(){
      return end;
  }
        
  public String toString(){
    return ("ShadowPoint< begin:" + begin + " end:" + end + " >");
   }

    private ShadowMatch shadowmatch=null;
    public void setShadowMatch(ShadowMatch sm) {
	shadowmatch=sm;
    }
    public ShadowMatch getShadowMatch() {
	return shadowmatch;
    }

    public Stmt lazyInitThisJoinPoint(LocalGeneratorEx lg,Chain units,Stmt start) {
	Stmt skip=Jimple.v().newNopStmt();
        Stmt jump=Jimple.v().newIfStmt
            (Jimple.v().newNeExpr(getThisJoinPoint(),NullConstant.v()),skip);
        units.insertAfter(jump,start);
        Stmt init=initThisJoinPoint(lg,units,jump);
        units.insertAfter(skip,init);
        return skip;

    }

    private Stmt initThisJoinPoint(LocalGeneratorEx lg,Chain units,Stmt start) {
	Type object=Scene.v().getSootClass("java.lang.Object").getType();
	WeavingContext wc=new WeavingContext();
	
	WeavingVar sjpVal=new LocalVar(RefType.v("org.aspectj.lang.JoinPoint$StaticPart"),
				       "sjpinfo");
	Stmt bindSJPInfo
	    =new Load(new StaticJoinPointInfo(shadowmatch.getSJPInfo()),sjpVal)
	    .codeGen(container,lg,units,start,null,true,wc);
	
	WeavingVar thisVal=new LocalVar(object,"thisval");
	ContextValue thisCV=shadowmatch.getThisContextValue();
	// Sometimes using this would actually cause a pointcut to fail to match,
	// but here we just want a null value in the JoinPointInfo
	if(thisCV==null) thisCV=new JimpleValue(NullConstant.v());
	Stmt bindThis=Bind
	    .construct(thisCV,object,thisVal)
	    .codeGen(container,lg,units,bindSJPInfo,end,true,wc);
	
	WeavingVar targetVal=new LocalVar(object,"targetval");
	ContextValue targetCV=shadowmatch.getTargetContextValue();
	// Likewise here
	if(targetCV==null) targetCV=new JimpleValue(NullConstant.v());
	Stmt bindTarget=Bind
	    .construct(targetCV,object,targetVal)
	    .codeGen(container,lg,units,bindThis,end,true,wc);
	
	Local argsVal=lg.generateLocal(ArrayType.v(object,1),"argsvals");
	
	List/*<ContextValue>*/ argsCVs=shadowmatch.getArgsContextValues();
	
	Stmt initArgsArray=Jimple.v().newAssignStmt
	    (argsVal,Jimple.v().newNewArrayExpr(object,IntConstant.v(argsCVs.size())));
	units.insertAfter(initArgsArray,bindTarget);
	
	Iterator it=argsCVs.iterator();
	Stmt last=initArgsArray;
	int i=0;
	while(it.hasNext()) {
	    ContextValue argCV=(ContextValue) it.next();
	    WeavingVar argVal=new LocalVar(object,"argval");
	    Stmt bindArg=Bind
		.construct(argCV,object,argVal)
		.codeGen(container,lg,units,last,end,true,wc);
	    
	    last=Jimple.v().newAssignStmt
		(Jimple.v().newArrayRef(argsVal,IntConstant.v(i)),
		 argVal.get());
	    units.insertAfter(last,bindArg);
	    
	    i++;
	}
	
	List/*<Value>*/ constrArgs=new LinkedList();
	List/*<SootType>*/ constrTypes=new LinkedList();
	
	constrTypes.add(RefType.v("org.aspectj.lang.JoinPoint$StaticPart"));
	constrArgs.add(sjpVal.get()); 
	
	constrTypes.add(object);
	constrArgs.add(thisVal.get()); 
	
	constrTypes.add(object);
	constrArgs.add(targetVal.get());
	
	constrTypes.add(ArrayType.v(object,1));
	constrArgs.add(argsVal);
	
	// FIXME: Should this class be delegated to the extension too?
	SootClass factoryclass=Scene.v().getSootClass
	    ("uk.ac.ox.comlab.abc.runtime.reflect.Factory");
	
	Stmt makeJP=Jimple.v().newAssignStmt
	    (getThisJoinPoint(),
	     Jimple.v().newStaticInvokeExpr
	     (Scene.v().makeMethodRef
	      (factoryclass,"makeJP",constrTypes,RefType.v("org.aspectj.lang.JoinPoint"),true),
	      constrArgs));
	units.insertAfter(makeJP,last);

	return makeJP;
    }
    
    private Local thisJoinPoint=null;
    public Local getThisJoinPoint() {
	if(thisJoinPoint==null) {
	    LocalGeneratorEx lg=new LocalGeneratorEx(container.getActiveBody());

	    Chain units=container.getActiveBody().getUnits();

	    thisJoinPoint=lg.generateLocal
		(RefType.v("org.aspectj.lang.JoinPoint"),"thisJoinPoint");

	    Stmt startJP=Jimple.v().newNopStmt();
	    units.insertBefore(startJP,begin);
	    
	    Stmt assignStmt=Jimple.v().newAssignStmt(thisJoinPoint,NullConstant.v());
	    units.insertAfter(assignStmt,startJP);
	    //	    initThisJoinPoint(lg,units,startJP);
	}
	return thisJoinPoint;
    }

}
