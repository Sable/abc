package abc.impact.impact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.SootMethod;
import soot.Type;
import abc.impact.analysis.ITDNoiseAnalysis;
import abc.impact.utils.ImpactUtil;
import abc.weaving.aspectinfo.InAspect;
import abc.weaving.aspectinfo.IntertypeConstructorDecl;
import abc.weaving.aspectinfo.IntertypeFieldDecl;
import abc.weaving.aspectinfo.IntertypeMethodDecl;

public final class MethodSignature {

	private final String methodName;
	private final ArrayList<Type> paramTypes;
	
	public MethodSignature(final String methodName, final List<Type> params) {
		this.methodName = methodName;
		if (params == null) this.paramTypes = new ArrayList<Type>();
		else this.paramTypes = new ArrayList<Type>(params);
	}
	
//	/**
//	 * Create the MethodSignature of a SootMethod
//	 * @param sm
//	 */
//	public MethodSignature(final SootMethod sm) {
//		this(sm.getName(), sm.getParameterTypes());
//	}
	
	/**
	 * Create the MethodSignature of a SootMethod, but take the affect of
	 * itd method injection into account (fix method name)
	 * @param sm
	 * @param targets
	 */
	public MethodSignature(final SootMethod sm) {

		Map<SootMethod, InAspect> targets = ITDNoiseAnalysis.v().getTargetMethods();
		Map<SootMethod, InAspect> noise = ITDNoiseAnalysis.v().getNoiseMethods();
		
		if (targets.containsKey(sm)) {
			InAspect ia = targets.get(sm);
			if (ia instanceof IntertypeMethodDecl) {
				IntertypeMethodDecl md = (IntertypeMethodDecl) ia;
				this.methodName = md.getOrigName();
				this.paramTypes = new ArrayList<Type>(sm.getParameterTypes());
			} else if (ia instanceof IntertypeConstructorDecl) {
				IntertypeConstructorDecl cd = (IntertypeConstructorDecl) ia;
				this.methodName = SootMethod.constructorName;
				this.paramTypes = new ArrayList<Type>(sm.getParameterTypes());
				if (cd.hasMangleParam()) {
					//remove the last one
					this.paramTypes.remove(paramTypes.size()-1);
				}
			} else
				throw new RuntimeException("Unknown " + ia);
		} else if (noise.containsKey(sm)) {
			InAspect ia = noise.get(sm);
			if (ia instanceof IntertypeMethodDecl) {
				IntertypeMethodDecl md = (IntertypeMethodDecl) ia;
				this.methodName = md.getOrigName();
				this.paramTypes = new ArrayList<Type>(sm.getParameterTypes());
			} else if (ia instanceof IntertypeConstructorDecl) {
				IntertypeConstructorDecl cd = (IntertypeConstructorDecl) ia;
				this.methodName = SootMethod.constructorName;
				this.paramTypes = new ArrayList<Type>(sm.getParameterTypes());
				if (cd.hasMangleParam()) {
					//remove the last one
					this.paramTypes.remove(paramTypes.size()-1);
				}
			} else if (ia instanceof IntertypeFieldDecl) {
				this.methodName = sm.getName();
				this.paramTypes = new ArrayList<Type>(sm.getParameterTypes());
			} else
				throw new RuntimeException("Unknown " + ia);
		} else {
			this.methodName = sm.getName();
			this.paramTypes = new ArrayList<Type>(sm.getParameterTypes());
		}
	}

	/**
	 * return the method name. return SootMethod.constructorName for consturctors.
	 * @return
	 */
	public String getMethodName() {
		return methodName;
	}

	public List<Type> getParams() {
		return Collections.unmodifiableList(paramTypes);
	}

	@Override
	/**
	 * The hash code depends on methodName and the size of paramTypes.
	 */
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((methodName == null) ? 0 : methodName.hashCode());
		result = PRIME * result + ((paramTypes == null) ? 0 : paramTypes.size());
		return result;
	}

	@Override
	/**
	 * 1. the methodName should be equal
	 * 2. the size of paramTypes should be equal
	 * 3. each Type in the same position in paramTypes should be equal
	 * otherwise, return false
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MethodSignature other = (MethodSignature) obj;
		
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!methodName.equals(other.methodName))
			return false;
		
		if (paramTypes == null) {
			if (other.paramTypes != null)
				return false;
		} else if (paramTypes.size() != other.paramTypes.size()) {
			return false;
		} else {
			for (int i = 0; i < paramTypes.size(); i++) {
				if (!paramTypes.get(i).equals(other.paramTypes.get(i)))
					return false;
			}
		}
		return true;
	}
	
	/**
	 * test if this is applicable for a invocation with the given MethodSignature
	 * JLS v2 15.12.2.1
	 * @param invocation
	 * @return true if applicable
	 */
	public boolean isApplicableFor(MethodSignature invocation) {
		if (this == invocation)
			return true;
		if (invocation == null)
			return false;
		
		if (methodName == null) {
			if (invocation.methodName != null)
				return false;
		} else if (!methodName.equals(invocation.methodName))
			return false;
		
		if (paramTypes == null) {
			if (invocation.paramTypes != null)
				return false;
		} else if (paramTypes.size() != invocation.paramTypes.size()) {
			return false;
		} else {
			for (int i = 0; i < paramTypes.size(); i++) {
				if (! ImpactUtil.isMethodInvocationConversion(invocation.paramTypes.get(i), paramTypes.get(i)))
					return false;
			}
		}
		return true;
	}
	
//	/**
//	 * test if the SootMethod is applicable for a invocation with the given MethodSignature
//	 * @param sm
//	 * @param invocation
//	 * @return true if applicable
//	 */
//	public static boolean isApplicableSootMethod(SootMethod sm, MethodSignature invocation) {
//		return (new MethodSignature(sm)).isApplicableFor(invocation);
//	}
	
	/**
	 * Select SootMethod applicable for this, name of injected method is fixed
	 * Note: ITDNoiseAnalysis.v().getTargetMethods() is used
	 * @param candidates
	 * @return
	 */
	public List<SootMethod> getApplicableMethods(List<SootMethod> candidates) {
		List<SootMethod> applicableMethods = new LinkedList<SootMethod>();
		
		for (Iterator<SootMethod> methodIt = candidates.iterator(); methodIt.hasNext();) {
			SootMethod sm = methodIt.next();
			
			MethodSignature ms = new MethodSignature(sm);

			if (ms.isApplicableFor(this))
				applicableMethods.add(sm);
		}
		return Collections.unmodifiableList(applicableMethods);
	}
	
//	/**
//	 * test if the SootMethod is applicable for a invocation with the given SootMethod
//	 * @param sm
//	 * @param invocation
//	 * @return
//	 */
//	public static boolean isApplicableSootMethod(SootMethod sm, SootMethod invocation) {
//		return (new MethodSignature(sm))
//				.isApplicableFor(new MethodSignature(invocation));
//	}
	
	/**
	 * Test if this is more specific than other
	 * JLS v2 15.12.2.2, rule 2 --
	 *   Tj can be converted to Uj by method invocation conversion, for all j from 1 to n.
	 * @param other
	 * @return true if this is more specific than other
	 *         false if this is not more specific than other, but it does not mean
	 *               other is more specific than this 
	 */
	public boolean isMoreSpecificThan(MethodSignature other) {
		
		if (!this.methodName.equals(other.methodName))
			throw new RuntimeException("Incompatible, method name must be equal.");
		List<Type> thisParams = this.paramTypes;
		List<Type> otherParams = other.paramTypes;
		
		if (thisParams.size() != otherParams.size())
			throw new RuntimeException("Incompatible, method parameters' size must be equal.");
		
		for (int i = 0; i < thisParams.size(); i++) {
			if (! ImpactUtil.isMethodInvocationConversion(thisParams.get(i), otherParams.get(i)))
				return false;
		}
		
		return true;
	}

	@Override
	public String toString() {
		
		return methodName + paramTypes;
	}
}
