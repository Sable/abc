package abc.impact.utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import abc.impact.impact.ReferredPlace;

import soot.Hierarchy;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.SpecialInvokeExpr;

public class ExtendedHierarchy {

	private Hierarchy hierarchy;
	
	public ExtendedHierarchy(Hierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}
	
	public Hierarchy getHierarchy() {
		return hierarchy;
	}

	/**
	 * Get direct sub classes in the same package of a class
	 * Note: Class is needed, not interface
	 * @param currentClass
	 * @return
	 */
	public List<SootClass> getDirectPackageSubClassesOf(SootClass currentClass) {
		LinkedList<SootClass> ret = new LinkedList<SootClass>();
		
		if (currentClass.isInterface()) {
			throw new RuntimeException("Class is needed.");
		}
		List<SootClass> l = hierarchy.getDirectSubclassesOf(currentClass);
		
		for (Iterator<SootClass> lIt = l.iterator(); lIt.hasNext(); ) {
			SootClass s = lIt.next();
			if (currentClass.getPackageName().equals(s.getPackageName()))
				ret.add(s);
		}
		
		return Collections.unmodifiableList(ret);
	}
	
//	/**
//	 * Get direct sub classes depending on referred palce
//	 * Note: class is needed, not interface
//	 * @param arg0
//	 * @return
//	 */
//	public List<SootClass> getDirectSubClassesOf(SootClass currentClass, ReferredPlace referredPlace) {
//		
//		if (referredPlace == ReferredPlace.CLASS) {
//			return new LinkedList<SootClass>();
//		}
//		
//		if (referredPlace == ReferredPlace.PACKAGE) {
//			return getDirectPackageSubClassesOf(currentClass);
//		}
//		
//		if (referredPlace == ReferredPlace.CHILDERN ||
//			referredPlace == ReferredPlace.ALL) {
//			return hierarchy.getDirectSubclassesOf(currentClass);
//		}
//		
//		throw new RuntimeException("Unsupported place.");
//	}
	
	//---------------delegate methods -------------
	
	public List getDirectImplementersOf(SootClass arg0) {
		return hierarchy.getDirectImplementersOf(arg0);
	}

	public List getDirectSubclassesOf(SootClass arg0) {
		return hierarchy.getDirectSubclassesOf(arg0);
	}

	public List getDirectSubclassesOfIncluding(SootClass arg0) {
		return hierarchy.getDirectSubclassesOfIncluding(arg0);
	}

	public List getDirectSubinterfacesOf(SootClass arg0) {
		return hierarchy.getDirectSubinterfacesOf(arg0);
	}

	public List getDirectSubinterfacesOfIncluding(SootClass arg0) {
		return hierarchy.getDirectSubinterfacesOfIncluding(arg0);
	}

	public List getDirectSuperclassesOf(SootClass arg0) {
		return hierarchy.getDirectSuperclassesOf(arg0);
	}

	public List getDirectSuperinterfacesOf(SootClass arg0) {
		return hierarchy.getDirectSuperinterfacesOf(arg0);
	}

	public List getImplementersOf(SootClass arg0) {
		return hierarchy.getImplementersOf(arg0);
	}

	public SootClass getLeastCommonSuperclassOf(SootClass arg0, SootClass arg1) {
		return hierarchy.getLeastCommonSuperclassOf(arg0, arg1);
	}

	public List getSubclassesOf(SootClass arg0) {
		return hierarchy.getSubclassesOf(arg0);
	}

	public List getSubclassesOfIncluding(SootClass arg0) {
		return hierarchy.getSubclassesOfIncluding(arg0);
	}

	public List getSubinterfacesOf(SootClass arg0) {
		return hierarchy.getSubinterfacesOf(arg0);
	}

	public List getSubinterfacesOfIncluding(SootClass arg0) {
		return hierarchy.getSubinterfacesOfIncluding(arg0);
	}

	public List getSuperclassesOf(SootClass arg0) {
		return hierarchy.getSuperclassesOf(arg0);
	}

	public List getSuperclassesOfIncluding(SootClass arg0) {
		return hierarchy.getSuperclassesOfIncluding(arg0);
	}

	public List getSuperinterfacesOf(SootClass arg0) {
		return hierarchy.getSuperinterfacesOf(arg0);
	}

	public boolean isClassDirectSubclassOf(SootClass arg0, SootClass arg1) {
		return hierarchy.isClassDirectSubclassOf(arg0, arg1);
	}

	public boolean isClassSubclassOf(SootClass arg0, SootClass arg1) {
		return hierarchy.isClassSubclassOf(arg0, arg1);
	}

	public boolean isClassSubclassOfIncluding(SootClass arg0, SootClass arg1) {
		return hierarchy.isClassSubclassOfIncluding(arg0, arg1);
	}

	public boolean isClassSuperclassOf(SootClass arg0, SootClass arg1) {
		return hierarchy.isClassSuperclassOf(arg0, arg1);
	}

	public boolean isClassSuperclassOfIncluding(SootClass arg0, SootClass arg1) {
		return hierarchy.isClassSuperclassOfIncluding(arg0, arg1);
	}

	public boolean isInterfaceDirectSubinterfaceOf(SootClass arg0, SootClass arg1) {
		return hierarchy.isInterfaceDirectSubinterfaceOf(arg0, arg1);
	}

	public boolean isInterfaceSubinterfaceOf(SootClass arg0, SootClass arg1) {
		return hierarchy.isInterfaceSubinterfaceOf(arg0, arg1);
	}

	public boolean isVisible(SootClass arg0, SootMethod arg1) {
		return hierarchy.isVisible(arg0, arg1);
	}

	public List resolveAbstractDispatch(List arg0, SootMethod arg1) {
		return hierarchy.resolveAbstractDispatch(arg0, arg1);
	}

	public List resolveAbstractDispatch(SootClass arg0, SootMethod arg1) {
		return hierarchy.resolveAbstractDispatch(arg0, arg1);
	}

	public List resolveConcreteDispatch(List arg0, SootMethod arg1) {
		return hierarchy.resolveConcreteDispatch(arg0, arg1);
	}

	public SootMethod resolveConcreteDispatch(SootClass arg0, SootMethod arg1) {
		return hierarchy.resolveConcreteDispatch(arg0, arg1);
	}

	public SootMethod resolveSpecialDispatch(SpecialInvokeExpr arg0, SootMethod arg1) {
		return hierarchy.resolveSpecialDispatch(arg0, arg1);
	}
	
	
}
