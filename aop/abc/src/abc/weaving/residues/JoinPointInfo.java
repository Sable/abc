/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Ondrej Lhotak
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

package abc.weaving.residues;

import soot.*;
import soot.util.Chain;
import soot.jimple.*;
import abc.weaving.matching.ShadowMatch;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.InstructionShadowTag;
import abc.weaving.tagkit.Tagger;
import abc.weaving.weaver.*;
import java.util.*;

/** A value that will become a thisJoinPoint structure at runtime
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 */
public class JoinPointInfo extends ContextValue {

    private ShadowMatch sm;

    public ContextValue inline(ConstructorInliningMap cim) {
        return new JoinPointInfo(sm.inline(cim),
                (StaticJoinPointInfo) sjp.inline(cim),
                thisCV.inline(cim), targetCV.inline(cim),
                ContextValue.inline(argsCVs, cim));
    }
    public JoinPointInfo(ShadowMatch sm, StaticJoinPointInfo sjp,
            ContextValue thisCV, ContextValue targetCV,
            List/*ContextValue*/ argsCVs) {
        this.sm = sm;
        this.sjp = sjp;
        this.thisCV = thisCV;
        this.targetCV = targetCV;
        this.argsCVs = argsCVs;
    }
    public JoinPointInfo(ShadowMatch sm) {
        this.sm = sm;
        this.sjp = new StaticJoinPointInfo(sm.getSJPInfo());

        // Sometimes using this would actually cause a pointcut to fail to match,
        // but here we just want a null value in the JoinPointInfo
        ContextValue thisCVtmp = sm.getThisContextValue();
        if(thisCVtmp == null) thisCVtmp = new JimpleValue(NullConstant.v());
        this.thisCV = thisCVtmp;

        // Likewise here
        ContextValue targetCVtmp = sm.getTargetContextValue();
        if(targetCVtmp == null) targetCVtmp = new JimpleValue(NullConstant.v());
        this.targetCV = targetCVtmp;

        this.argsCVs = sm.getArgsContextValues();
    }
    private final StaticJoinPointInfo sjp;
    private final ContextValue thisCV;
    private final ContextValue targetCV;
    private final List/*<ContextValue>*/ argsCVs;

    public String toString() {
	return "thisJoinPoint";
    }

    public Type getSootType() {
	return sootType();
    }

    // This possibly should be moved elsewhere
    public static RefType sootType() {
	if (abc.main.Debug.v().thisJoinPointObject) return RefType.v("java.lang.Object");
	return RefType.v("org.aspectj.lang.JoinPoint");
    }

    public Value getSootValue() {
	return getThisJoinPoint();
    }

    public Stmt doInit(LocalGeneratorEx lg,Chain units,Stmt begin) {
	return lazyInitThisJoinPoint(lg,units,begin);
    }

    public ShadowMatch shadowMatch() { return sm; }


    private Stmt initThisJoinPointStatic(LocalGeneratorEx lg,Chain units,Stmt start) {
        Type object=Scene.v().getSootClass("java.lang.Object").getType();
        WeavingContext wc=new WeavingContext();
        wc.setShadowTag(new InstructionShadowTag(sm.shadowId));

        WeavingVar sjpVal=new LocalVar(RefType.v("org.aspectj.lang.JoinPoint$StaticPart"),
                "sjpinfo");
        Stmt bindSJPInfo
            =new Load(sjp,sjpVal).codeGen(sm.getContainer(),lg,units,start,null,true,wc);

        Stmt ret=Jimple.v().newAssignStmt(
                getThisJoinPoint(),
                Jimple.v().newCastExpr(sjpVal.get(), getSootType()));

        if(wc.getKindTag() == null) {
            wc.setKindTag(InstructionKindTag.THISJOINPOINT);
        }
        Tagger.tagStmt(ret, wc);

        units.insertAfter(ret,bindSJPInfo);

        return ret;
    }
    private Stmt initThisJoinPoint(LocalGeneratorEx lg,Chain units,Stmt start) {
        Type object=Scene.v().getSootClass("java.lang.Object").getType();
        WeavingContext wc=new WeavingContext();
        wc.setShadowTag(new InstructionShadowTag(sm.shadowId));
        wc.setKindTag(InstructionKindTag.THISJOINPOINT);

	if (abc.main.Debug.v().thisJoinPointObject) {
	    Local temp = lg.generateLocal(object, "temp");
	    AssignStmt makeObjectJP = Jimple.v().newAssignStmt
		(temp, 
		 Jimple.v().newNewExpr((RefType)object));
	    units.insertAfter(makeObjectJP, start);

	    InvokeStmt initObjectJP = Jimple.v().newInvokeStmt
		(Jimple.v().newSpecialInvokeExpr
		 (temp, Scene.v().makeConstructorRef(Scene.v().getSootClass("java.lang.Object"), new LinkedList())));
	    units.insertAfter(initObjectJP, makeObjectJP);

	    AssignStmt copyObjectJP = Jimple.v().newAssignStmt(getThisJoinPoint(), temp);
	    units.insertAfter(copyObjectJP, initObjectJP);

	    return copyObjectJP;
	}

	if (abc.main.Debug.v().thisJoinPointDummy) {
	    Local temp = lg.generateLocal(RefType.v("DummyJP"), "temp");

	    AssignStmt makeDummyJP = Jimple.v().newAssignStmt
		(temp, 
		 Jimple.v().newNewExpr(RefType.v("DummyJP")));
	    units.insertAfter(makeDummyJP, start);

	    InvokeStmt initDummyJP = Jimple.v().newInvokeStmt
		(Jimple.v().newSpecialInvokeExpr
		 (temp, Scene.v().makeConstructorRef(Scene.v().getSootClass("DummyJP"), new LinkedList())));
	    units.insertAfter(initDummyJP, makeDummyJP);

	    AssignStmt copyDummyJP = Jimple.v().newAssignStmt(getThisJoinPoint(), temp);
	    units.insertAfter(copyDummyJP, initDummyJP);

	    return copyDummyJP;
	}

        WeavingVar sjpVal=new LocalVar(RefType.v("org.aspectj.lang.JoinPoint$StaticPart"),
                "sjpinfo");
        Stmt bindSJPInfo
            =new Load(sjp, sjpVal).codeGen(sm.getContainer(),lg,units,start,null,true,wc);
        Tagger.tagStmt(bindSJPInfo, InstructionKindTag.THISJOINPOINT);
        
        WeavingVar thisVal=new LocalVar(object,"thisval");
        Stmt bindThis=Bind
            .construct(thisCV,object,thisVal)
            .codeGen(sm.getContainer(),lg,units,bindSJPInfo,sm.sp.getEnd(),true,wc);
        Tagger.tagStmt(bindThis, InstructionKindTag.THISJOINPOINT);
        
        WeavingVar targetVal=new LocalVar(object,"targetval");
        Stmt bindTarget=Bind
            .construct(targetCV,object,targetVal)
            .codeGen(sm.getContainer(),lg,units,bindThis,sm.sp.getEnd(),true,wc);
        Tagger.tagStmt(bindTarget, InstructionKindTag.THISJOINPOINT);
        
        Local argsVal=lg.generateLocal(ArrayType.v(object,1),"argsvals");

        Stmt initArgsArray=Jimple.v().newAssignStmt
            (argsVal,Jimple.v().newNewArrayExpr(object,IntConstant.v(argsCVs.size())));
        Tagger.tagStmt(initArgsArray, wc);
        units.insertAfter(initArgsArray,bindTarget);

        Iterator it=argsCVs.iterator();
        Stmt last=initArgsArray;
        int i=0;
        while(it.hasNext()) {
            ContextValue argCV=(ContextValue) it.next();
            WeavingVar argVal=new LocalVar(object,"argval");
            Stmt bindArg=Bind
                .construct(argCV,object,argVal)
                .codeGen(sm.getContainer(),lg,units,last,sm.sp.getEnd(),true,wc);
            Tagger.tagStmt(bindArg, InstructionKindTag.THISJOINPOINT);
            
            last=Jimple.v().newAssignStmt
                (Jimple.v().newArrayRef(argsVal,IntConstant.v(i)),
                 argVal.get());
            Tagger.tagStmt(last, wc);
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
            ("org.aspectbench.runtime.reflect.Factory");

        AssignStmt makeJP=Jimple.v().newAssignStmt
            (getThisJoinPoint(),
             Jimple.v().newStaticInvokeExpr
             (Scene.v().makeMethodRef
              (factoryclass,"makeJP",constrTypes,RefType.v("org.aspectj.lang.JoinPoint"),true),
              constrArgs));
        Tagger.tagStmt(makeJP, wc);
        units.insertAfter(makeJP,last);

        return makeJP;
    }

    private Local get_thisJoinPoint() {
        return abc.weaving.weaver.WeavingState.v().
            get_JoinPointInfo_thisJoinPoint(sm);
    }
    private void set_thisJoinPoint(Local val) {
        abc.weaving.weaver.WeavingState.v().
            set_JoinPointInfo_thisJoinPoint(sm, val);
    }
    private Local getThisJoinPoint() {
        if(get_thisJoinPoint()==null) {
            LocalGeneratorEx lg=new LocalGeneratorEx(sm.getContainer().getActiveBody());

            Chain units=sm.getContainer().getActiveBody().getUnits();

            set_thisJoinPoint
		(lg.generateLocal(getSootType(), "thisJoinPoint"));

            Stmt startJP=Jimple.v().newNopStmt();
            units.insertBefore(startJP,sm.sp.getBegin());
	    if (abc.main.Debug.v().tagWeavingCode)
		startJP.addTag(new soot.tagkit.StringTag("^^ nop for thisJoinPoint = null for " + sm));

            Stmt assignStmt=Jimple.v().newAssignStmt(get_thisJoinPoint(),NullConstant.v());
            Tagger.tagStmt(assignStmt, InstructionKindTag.THISJOINPOINT);
            Tagger.tagStmt(assignStmt, new InstructionShadowTag(sm.shadowId));
            units.insertAfter(assignStmt,startJP);
            //      initThisJoinPoint(lg,units,startJP);
        }
        return get_thisJoinPoint();
    }
    private Stmt lazyInitThisJoinPoint(LocalGeneratorEx lg,Chain units,Stmt start) {
        Stmt skip=Jimple.v().newNopStmt();
        Stmt jump=Jimple.v().newIfStmt
            (Jimple.v().newNeExpr(getThisJoinPoint(),NullConstant.v()),skip);
        Tagger.tagStmt(jump, InstructionKindTag.THISJOINPOINT);
        Tagger.tagStmt(jump, new InstructionShadowTag(sm.shadowId));
        units.insertAfter(jump,start);
        Stmt init = initThisJoinPoint(lg,units,jump);
        units.insertAfter(skip,init);
        return skip;
    }
}
