/* abc - The AspectBench Compiler
 * Copyright (C) 2009 Eric Bodden
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
 
package abc.da.weaving.weaver.tracing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.ArrayType;
import soot.Local;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.ParameterRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.StringConstant;
import abc.da.weaving.aspectinfo.AdviceDependency;
import abc.da.weaving.aspectinfo.DAInfo;
import abc.main.Main;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.matching.SJPInfo;

public class Dumper {
	
    public static void replaceDependentAdviceBodiesAndExtendSJPInfos(DAInfo daInfo, Aspect aspect) {
    	SootClass aspectClass = aspect.getInstanceClass().getSootClass();
    	for(AdviceDependency dep: daInfo.getAdviceDependencies()) {
    		if (dep.getContainer().equals(aspect)) {
    			for (SootMethod method : aspectClass.getMethods()) {
    				if(daInfo.isDependentAdviceMethod(aspectClass, method)) {
    					replaceBody(method);
    				}					
				}
			}
    	}
    	
    	Set<SJPInfo> visited = new HashSet<SJPInfo>();
		Set<AbcClass> weavableClasses = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses();
		for (AbcClass abcClass : weavableClasses) {
			SootClass sootClass = abcClass.getSootClass();
			for(SootMethod m: sootClass.getMethods()) {
				if(MethodCategory.weaveInside(m)) {
	    			MethodAdviceList adviceList = Main.v().getAbcExtension().getGlobalAspectInfo().getAdviceList(m);
	    			if(adviceList!=null) {
		    			List<AdviceApplication> adviceApplications = adviceList.allAdvice();
		    			for (AdviceApplication aa : adviceApplications) {
		    				if(aa.advice.getAspect().equals(aspect)) {
								SJPInfo info = aa.shadowmatch.getSJPInfo();
								if(!visited.contains(info)) {
									info.overrideKind(info.getKind()+"$shadowId$"+aa.shadowmatch.shadowId+"$endShadowId$");
									visited.add(info);
								}
		    				}
						}
	    			}	    						
				}
			}
		}
    }

	private static void replaceBody(SootMethod method) {
		JimpleBody body = Jimple.v().newBody();
		LocalGenerator localGen = new LocalGenerator(body);
		
		RefType baseType = Scene.v().getObjectType();
		ArrayType arrayType = ArrayType.v(baseType, 1);
		Local arrayLocal = localGen.generateLocal(arrayType);
		int numActualParams = method.getParameterCount()-1;
		PatchingChain<Unit> units = body.getUnits();
		
		RefType declType = RefType.v(method.getDeclaringClass());
		Local thisLocal = localGen.generateLocal(declType);
		units.add(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(declType)));
		
		List<Type> parameterTypes = method.getParameterTypes();
		int num=-1;
		int spParamNum = -1;
		Local[] paramLocals = new Local[method.getParameterCount()];
		for (Type t : parameterTypes) {
			num++;
			if(t instanceof RefType) {
				if(((RefType) t).getClassName().equals("org.aspectj.lang.JoinPoint$StaticPart")) {
					spParamNum = num;
				}
			}
			
			Local paramLocal = localGen.generateLocal(t);
			ParameterRef paramRef = Jimple.v().newParameterRef(t, num);
			IdentityStmt paramAssignToLocal = Jimple.v().newIdentityStmt(paramLocal, paramRef);
			units.add(paramAssignToLocal);
			
			paramLocals[num] = paramLocal;
			
		}
		
		if(spParamNum<0) {
			throw new InternalError("StaticPart not found!");
		}
		
		AssignStmt arrayInitStmt = Jimple.v().newAssignStmt(arrayLocal, Jimple.v().newNewArrayExpr(baseType, IntConstant.v(numActualParams)));
		units.add(arrayInitStmt);
		
		num=-1;
		for (Type t : parameterTypes) {
			num++;
			if(num!=spParamNum) {
				AssignStmt assignToArray = Jimple.v().newAssignStmt(Jimple.v().newArrayRef(arrayLocal, IntConstant.v(num)), paramLocals[num]);
				units.add(assignToArray);
			}
		}
		
		Local methodNameLocal = localGen.generateLocal(RefType.v("java.lang.String"));
		AssignStmt methodNameAssign = Jimple.v().newAssignStmt(methodNameLocal, StringConstant.v(method.getDeclaringClass().getName()+"."+method.getName()));
		units.add(methodNameAssign);
		
		
		SootClass dumperClass = Scene.v().getSootClass("org.aspectbench.tm.runtime.internal.Dumper");
		SootMethod dumpMethod = dumperClass.getMethodByName("dump");
		StaticInvokeExpr invoke = Jimple.v().newStaticInvokeExpr(dumpMethod.makeRef(), arrayLocal, paramLocals[spParamNum], methodNameLocal);
		InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(invoke);
		units.add(invokeStmt);
		
		//TODO won't work for around advice
		units.add(Jimple.v().newReturnVoidStmt());
		
		body.setMethod(method);
		method.setActiveBody(body);
		
		body.validate();
		
	}

}
