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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.AdviceSpec;
import abc.weaving.aspectinfo.AroundAdvice;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.weaver.AdviceInliner;

/** Handle around weaving.
 * @author Sascha Kuzins 
 * @date May 6, 2004
 * 
 * The around weaver builds the following object hierarchy:
 * AroundWeaver
 *  AdviceMethod
 *   AdviceLocalClass
 *    AdviceLocalMethod
 *     ProceedInvocation
 *     NestedInitCall
 *   ProceedMethod
 *    AdviceApplicationInfo
 * 
 */

public class AroundWeaver {
	/**
	 * @return Returns the singleton instance.
	 */
	public static AroundWeaver v() {
		return instance;
	}

	private static AroundWeaver instance = new AroundWeaver();

	private AroundWeaver() {
	} // private constructor

	public static void reset() {
		instance = new AroundWeaver();
		AdviceApplicationInfo.reset();
	}

	public static void debug(String message) {
		if (abc.main.Debug.v().aroundWeaver)
			System.err.println("ARD*** " + message);
	}

	public static class ObjectBox {
		Object object;
	}

	public void doWeave(SootClass shadowClass, SootMethod shadowMethod,
			LocalGeneratorEx localgen, AdviceApplication adviceAppl) {

		AdviceInliner.v().addShadowMethod(shadowMethod);

		debug("Weaving advice application: " + adviceAppl);
		if (abc.main.Debug.v().aroundWeaver) {
			// uncomment to skip around weaving (for debugging)
			// if (shadowClass!=null)	return;
			//throw new RuntimeException();
		}

		if (abc.main.Debug.v().aroundWeaver) {
			try {
				//				UnreachableCodeEliminator.v().transform(shadowMethod.getActiveBody());
				shadowMethod.getActiveBody().validate();
			} catch (RuntimeException e) {
				debug("shadow method: " + Util.printMethod(shadowMethod));
				throw e;
			}
		}

		SootMethod adviceMethod = null;
		//try {

		AdviceDecl adviceDecl = (AdviceDecl) adviceAppl.advice;

		AdviceSpec adviceSpec = adviceDecl.getAdviceSpec();
		AroundAdvice aroundSpec = (AroundAdvice) adviceSpec;
		SootClass theAspect = adviceDecl.getAspect().getInstanceClass()
				.getSootClass();
		SootMethod method = adviceDecl.getImpl().getSootMethod();
		adviceMethod = method;
		if (abc.main.Debug.v().aroundWeaver) {
			try {
				//				UnreachableCodeEliminator.v().transform(method.getActiveBody());
				method.getActiveBody().validate();
			} catch (RuntimeException e) {
				debug("advice method: " + Util.printMethod(method));
				throw e;
			}
		}
		AdviceMethod adviceMethodInfo = v().getAdviceMethod(method);
		List sootLocalAdviceMethods = new LinkedList();
		sootLocalAdviceMethods.addAll(adviceDecl.getLocalSootMethods());
		if (!sootLocalAdviceMethods.contains(method))
			sootLocalAdviceMethods.add(method);

		if (adviceMethodInfo == null) {
			adviceMethodInfo = new AdviceMethod(this, method, AdviceMethod
					.getOriginalAdviceFormals(adviceDecl),
					sootLocalAdviceMethods);
			v().setAdviceMethod(method, adviceMethodInfo);
		} else {
			if (AdviceMethod.getOriginalAdviceFormals(adviceDecl).size() != adviceMethodInfo.originalAdviceFormalTypes
					.size())
				throw new InternalAroundError(
						"Expecting consistent adviceDecl each time for same advice method");
			// if this occurs, fix getOriginalAdviceFormals()

			//if (adviceMethodInfo.proceedMethods.size()!=adviceDecl.getSootProceeds().size())
			//	throw new InternalAroundError("" + adviceMethodInfo.proceedMethods.size() +
			//				" : " + adviceDecl.getSootProceeds().size());
		}

		adviceMethodInfo.doWeave(adviceAppl, shadowMethod);

		/*} catch (InternalAroundError e) {
		 throw e;
		 } catch (Throwable e) {
		 System.err.println(" " + e.getClass().getName() + " " + e.getCause());
		 
		 StackTraceElement[] els=e.getStackTrace();
		 for (int i=0; i<els.length; i++) {
		 System.err.println(e.getStackTrace()[i].toString());
		 }			
		 throw new InternalAroundError("", e);
		 }*/

		if (abc.main.Debug.v().aroundWeaver) {
			validate();
			//validate();
			//abc.soot.util.Validate.validate(Scene.v().getSootClass("org.aspectj.runtime.reflect.Factory"));
		}
		if (abc.main.Debug.v().aroundWeaver) {
			try {
				//			UnreachableCodeEliminator.v().transform(shadowMethod.getActiveBody());
				shadowMethod.getActiveBody().validate();
			} catch (RuntimeException e) {
				debug("shadow method: " + Util.printMethod(shadowMethod));
				throw e;
			}
		}
		if (abc.main.Debug.v().aroundWeaver) {
			try {
				//		UnreachableCodeEliminator.v().transform(adviceMethod.getActiveBody());
				adviceMethod.getActiveBody().validate();
			} catch (RuntimeException e) {
				debug("advice method: " + Util.printMethod(adviceMethod));
				throw e;
			}
		}
	}

	public static void validate() {
		for (Iterator clIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses()
				.iterator(); clIt.hasNext();) {
			final AbcClass cl = (AbcClass) clIt.next();
			abc.soot.util.Validate.validate(cl.getSootClass());
		}
	}

	public static class AdviceMethodInlineInfo {

		public int proceedInvocations = 0;

		public boolean nestedClasses = false;

		public int originalSize = 0;

		public int internalLocalCount = 0;

		public int applications = 0;
	}

	public static class ShadowInlineInfo {
		public ShadowInlineInfo(int size, int internalLocals, boolean weavingRequiredUnBoxing) {
			this.size = size;
			this.internalLocals = internalLocals;
			this.weavingRequiredUnBoxing=weavingRequiredUnBoxing;
		}

		public final int size;

		public final int internalLocals;
		
		public final boolean weavingRequiredUnBoxing;
	}

	public static class ProceedMethodInlineInfo {
		public int shadowIDParamIndex = -1;

		public Map shadowInformation;
	}

	public static void updateSavedReferencesToStatements(HashMap bindings) {
		Collection values = v().adviceMethods.values();
		Iterator it = values.iterator();
		// all advice methods
		while (it.hasNext()) {
			AdviceMethod adviceMethodInfo = (AdviceMethod) it.next();
			Set keys2 = bindings.keySet();
			Iterator it2 = keys2.iterator();
			// all bindings
			while (it2.hasNext()) {
				Object old = it2.next();
				if (!(old instanceof Value) && !(old instanceof Stmt))
					continue;
				if (adviceMethodInfo.adviceMethodInvocationStmts.contains(old)) {
					adviceMethodInfo.adviceMethodInvocationStmts.remove(old);
					adviceMethodInfo.adviceMethodInvocationStmts.add(bindings
							.get(old));
					// replace with new
				}
				// this is only necessary if proceed calls are ever part of a shadow,
				// for example if the advice body were to be matched by an adviceexecution pointcut. 
				// TODO: does this kind of thing ever happen?
				// Doesn't matter. Once an advice method has been woven into,
				// the proceeds aren't changed anymore anyways.
				// So we might as well keep all these references updated.
				// (or delete them otherwise).
				for (Iterator it3 = adviceMethodInfo.adviceLocalClasses
						.values().iterator(); it3.hasNext();) {
					AdviceLocalClass pl = (AdviceLocalClass) it3.next();

					for (Iterator it0 = pl.adviceLocalMethods.iterator(); it0
							.hasNext();) {
						AdviceLocalMethod pm = (AdviceLocalMethod) it0.next();

						if (pm.interfaceInvocationStmts.contains(old)) {
							pm.interfaceInvocationStmts.remove(old);
							pm.interfaceInvocationStmts.add(bindings.get(old));
							// replace with new
						}
					}
				}
				if (adviceMethodInfo.directInvocationStmts.contains(old)) {
					adviceMethodInfo.directInvocationStmts.remove(old);
					adviceMethodInfo.directInvocationStmts.add(bindings
							.get(old));
				}
			}
		}
	}

	Map buildAroundAdviceLocalMethodMap() {
		Map result = new HashMap();
		List adviceDecls = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getAdviceDecls();
		for (Iterator it = adviceDecls.iterator(); it.hasNext();) {
			AbstractAdviceDecl absdecl = (AbstractAdviceDecl) it.next();
			if (!(absdecl instanceof AdviceDecl))
				continue;

			AdviceDecl decl = (AdviceDecl) absdecl;
			SootMethod adviceMethod = decl.getImpl().getSootMethod();
			if (Util.isAroundAdviceMethod(adviceMethod)) {
				List localMethods = decl.getLocalSootMethods();
				for (Iterator itM = localMethods.iterator(); itM.hasNext();) {
					SootMethod localMethod = (SootMethod) itM.next();
					result.put(localMethod, adviceMethod);
				}
			}
		}
		return result;
	}

	private Map aroundAdviceLocalMethods = null;

	public SootMethod getEnclosingAroundAdviceMethod(SootMethod m) {
		if (aroundAdviceLocalMethods == null) {
			aroundAdviceLocalMethods = buildAroundAdviceLocalMethodMap();
		}
		return (SootMethod) aroundAdviceLocalMethods.get(m);
	}

	public Map proceedMethods = new HashMap();

	private Map proceedMethodInlineInfos = new HashMap();

	public ProceedMethodInlineInfo getProceedMethodInlineInfo(SootMethod method) {
		ProceedMethodInlineInfo result = (ProceedMethodInlineInfo) proceedMethodInlineInfos
				.get(method);
		if (result != null)
			return result;

		proceedMethodInlineInfos.put(method, result);

		result = new ProceedMethodInlineInfo();

		ProceedMethod pm = (ProceedMethod) proceedMethods.get(method);

		result.shadowIDParamIndex = pm.shadowIDParamIndex;
		result.shadowInformation = pm.shadowInformation;

		return result;
	}

	private Map adviceMethodInlineInfos = new HashMap();

	public AdviceMethodInlineInfo getAdviceMethodInlineInfo(SootMethod method) {

		AdviceMethodInlineInfo result = (AdviceMethodInlineInfo) adviceMethodInlineInfos
				.get(method);
		if (result != null)
			return result;

		adviceMethodInlineInfos.put(method, result);

		result = new AdviceMethodInlineInfo();

		AdviceMethod adviceMethod = getAdviceMethod(method);

		if (adviceMethod == null) {
			throw new RuntimeException("Could not find information on "
					+ method.getName());
		}

		if (adviceMethod.adviceLocalClasses.size() > 1) {
			result.nestedClasses = true;
		} else {
			AdviceLocalClass cl = (AdviceLocalClass) adviceMethod.adviceLocalClasses
					.values().iterator().next();
			AdviceLocalMethod m = (AdviceLocalMethod) cl.adviceLocalMethods
					.get(0);
			result.proceedInvocations = m.proceedInvocations.size();

			result.originalSize = m.originalSize;
			result.internalLocalCount = m.internalLocalCount;

			for (Iterator it = adviceMethod.getAllProceedMethods().iterator(); it
					.hasNext();) {
				ProceedMethod pm = (ProceedMethod) it.next();
				result.applications += pm.adviceApplications.size();
			}
		}
		return result;
	}

	//public Set shadowMethods = new HashSet();

	private void validateState() {
		Iterator it = adviceMethods.values().iterator();
		while (it.hasNext()) {
			AdviceMethod method = (AdviceMethod) it.next();
			method.validate();
		}
	}

	public int getUniqueID() {
		return currentUniqueID++;
	}

	int currentUniqueID;

	//final private HashMap /* AdviceApplication,  */ adviceApplications = new HashMap();

	final private HashMap /* SootMethod, AdviceMethod */adviceMethods = new HashMap();

	void setAdviceMethod(SootMethod adviceMethod, AdviceMethod m) {
		adviceMethods.put(adviceMethod, m);
	}

	public AdviceMethod getAdviceMethod(SootMethod adviceMethod) {
		if (!adviceMethods.containsKey(adviceMethod)) {
			return null;
		}
		return (AdviceMethod) adviceMethods.get(adviceMethod);
	}
	public static class LookupStmtTag implements Tag {
		public final static String name="LookupStmtTag";
	    
	    public String getName() {
		return name;
	    }

	    public byte[] getValue() {
		throw new AttributeValueException();
	    }
	    public LookupStmtTag(int ID, boolean start) {
	    	this.ID=ID;
	    	this.start=start;
	    }
	    public final int ID;
	    public boolean start;
	}
	
}
