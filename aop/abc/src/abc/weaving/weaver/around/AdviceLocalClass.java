/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Sascha Kuzins
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

package abc.weaving.weaver.around;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;



public class AdviceLocalClass {
	
	public final AdviceMethod adviceMethod;
	public AdviceLocalClass getEnclosingFirstDegreeClass() {
		if (isAspect() || isFirstDegree())
			throw new InternalAroundError();
		
		AdviceLocalClass enclosing=getEnclosingClass();
		if (enclosing.isFirstDegree())
			return enclosing;
		else
			return enclosing.getEnclosingFirstDegreeClass();
	}
	public AdviceLocalClass getEnclosingClass() {
		if (isAspect())
			throw new InternalAroundError();
		
		return (AdviceLocalClass)this.adviceMethod.adviceLocalClasses.get(enclosingSootClass);
	}
	private final SootClass enclosingSootClass;
	public SootClass getEnclosingSootClass() {
		if (isAspect())
			throw new InternalAroundError();
		return enclosingSootClass;
	}
	
	public void addDefaultParameters() {
		addParameters(null, true);
	}

	boolean firstDegree=false;
	public void generateProceeds(ProceedMethod proceedMethod, String newStaticInvoke) {
		for (Iterator it=this.adviceLocalMethods.iterator(); it.hasNext();) {
			AdviceLocalMethod pm=(AdviceLocalMethod)it.next();
			pm.generateProceeds(proceedMethod, newStaticInvoke);
		}
	}
	
	/*private void modifyInterfaceInvocations(List addedAdviceParameterLocals,
			List addedTypes) {
		for (Iterator it=this.adviceLocalMethods.iterator(); it.hasNext();) {
			AdviceLocalMethod pm=(AdviceLocalMethod)it.next();
			pm.modifyInterfaceInvocations(addedAdviceParameterLocals, addedTypes);
		}
	}*/
	List addedFields=new LinkedList();
	
	public void addParameters(List addedDynArgsTypes, boolean bDefault) {
		
		if (bDefault && addedDynArgsTypes!=null)
			throw new InternalAroundError();
		
		if (bDefault) {
			addedDynArgsTypes=new LinkedList();
			addedDynArgsTypes.add(adviceMethod.interfaceInfo.closureInterface.getType());
			//, "closureInterface"
			addedDynArgsTypes.add(IntType.v());
			addedDynArgsTypes.add(IntType.v());
			addedDynArgsTypes.add(IntType.v());
		}
		if (isAspect()){ 
			// Add the new parameters to the advice method 
			// and keep track of the newly created locals corresponding to the parameters.
			//validateMethod(adviceMethod);
			if (adviceLocalMethods.size()!=1)
				throw new InternalAroundError();
			
			AdviceLocalMethod am=(AdviceLocalMethod)adviceLocalMethods.get(0);
			
			List addedAdviceParameterLocals = new LinkedList();
			AroundWeaver.debug("adding parameters to advice method " + am.sootProceedCallMethod);
			for (Iterator it = addedDynArgsTypes.iterator();
				it.hasNext();) {
				Type type = (Type) it.next();
				AroundWeaver.debug(" " + type);
				Local l;
				
				 l = Restructure.addParameterToMethod(am.sootProceedCallMethod, type, "contextArgFormal");
				
				addedAdviceParameterLocals.add(l);						
			}
			
			am.modifyNestedInits(addedAdviceParameterLocals);
			
			if (bDefault) {
				am.setDefaultParameters(addedAdviceParameterLocals);
			}
			if (!bDefault)
				am.modifyInterfaceInvocations(addedAdviceParameterLocals, addedDynArgsTypes);
			
			for (Iterator it=this.adviceMethod.adviceLocalClasses.values().iterator(); it.hasNext();) {
				AdviceLocalClass pl=(AdviceLocalClass)it.next();
				pl.addedFields.clear();
			}
			
		} else  {
			addedFields=new LinkedList();
			if (isFirstDegree()) {
				for (Iterator it = addedDynArgsTypes.iterator();
					it.hasNext();) {
					Type type = (Type) it.next();
				
					SootField f=new SootField("contextField" + AroundWeaver.v().getUniqueID(), 
								type, Modifier.PUBLIC);
					sootClass.addField(f);
					addedFields.add(f);
				}
			} else {
				addedFields=getEnclosingFirstDegreeClass().addedFields;
			}
			// add locals referencing the fields
			for (Iterator it=adviceLocalMethods.iterator(); it.hasNext();) {
				AdviceLocalMethod pm=(AdviceLocalMethod)it.next();
				
				List addedAdviceParameterLocals = new LinkedList();
				
				Chain statements=pm.methodBody.getUnits().getNonPatchingChain();
				Stmt insertion=pm.nopAfterEnclosingLocal;
				LocalGeneratorEx lg=new LocalGeneratorEx(pm.methodBody);
				
				for (Iterator it0=addedFields.iterator(); it0.hasNext();) {
					SootField f=(SootField)it0.next();
					Local l=lg.generateLocal(f.getType(), "contextFieldLocal");
					Stmt sf=
						Jimple.v().newAssignStmt(l, 
								Jimple.v().newInstanceFieldRef(
										pm.contextArgfieldBaseLocal,
										f.makeRef()));
					statements.insertBefore(sf, insertion);
					addedAdviceParameterLocals.add(l);
				}
				pm.modifyNestedInits(addedAdviceParameterLocals);
				if (bDefault) {
					pm.setDefaultParameters(addedAdviceParameterLocals);
				}
				if (!bDefault)
					pm.modifyInterfaceInvocations(addedAdviceParameterLocals, addedDynArgsTypes);	
			}
		}
		//return addedAdviceParameterLocals;
	}

	
	public final SootClass sootClass;
	//public final SootClass aspectClass;
	public AdviceLocalClass(AdviceMethod method, SootClass sootClass) {
		this.sootClass=sootClass;
		//this.aspectClass=aspectClass;
		this.adviceMethod = method;

		if (isAspect())
			enclosingSootClass=null;
		else	
			enclosingSootClass=((RefType)sootClass.getFieldByName("this$0").getType()).getSootClass();
		
		this.firstDegree=
			!isAspect() && getEnclosingSootClass().equals(this.adviceMethod.getAspect());
		
		AroundWeaver.debug("XXXXXXXXXXXXXXXX" + sootClass + " isAspect: " + isAspect() + " isFirst: " + isFirstDegree());
	}
	public boolean isAspect() {
		return sootClass.equals(this.adviceMethod.getAspect());
	}
	boolean isFirstDegree() {
		return firstDegree;
	}
	
	public void addAdviceLocalMethod(SootMethod m) {
		this.adviceLocalMethods.add(
				new AdviceLocalMethod(this, adviceMethod, m));
	}
	public final List adviceLocalMethods=new LinkedList();
}
