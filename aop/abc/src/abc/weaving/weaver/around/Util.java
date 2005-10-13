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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootMethod;
import soot.Trap;
import soot.Type;
import soot.Unit;
import soot.UnitBox;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.GotoStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.NullConstant;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.weaver.CodeGenException;
import abc.weaving.weaver.CflowCodeGenUtils;
import abc.weaving.weaver.around.AroundWeaver.ObjectBox;
import abc.weaving.weaver.around.soot.JInterfaceInvokeExpr;
import abc.weaving.weaver.around.soot.JSpecialInvokeExpr;
import abc.weaving.weaver.around.soot.JStaticInvokeExpr;
import abc.weaving.weaver.around.soot.JVirtualInvokeExpr;
import abc.weaving.weaver.around.soot.ModifiableInvokeExpr;


public class Util {
	
	
	
	// Returns a representation of the method
	// where the method name and the names of locals are normalized.
	// Hence, if the normalized representations of two methods are equal,
	// the methods should be functionally equal.
	public static String getMethodFingerprint(SootMethod m) {
		String result="FP:\n";
		Body b=m.getActiveBody();
		
		Chain locals=b.getLocals();
		List localNames=new ArrayList(locals.size());
		// save local names
		for (Iterator it=locals.iterator(); it.hasNext();) {
			Local l=(Local)it.next();
			localNames.add(l.getName());
		}
		
		// set normalized local names
		{
			int i=0;		
			for (Iterator it=b.getUseAndDefBoxes().iterator();it.hasNext();i++) {
				ValueBox box=(ValueBox)it.next();
				if (box.getValue() instanceof Local) {
					Local l=(Local)box.getValue();
					l.setName("norm$" + i);
					result += l.getType() + "\n";					
				}
			}
		}
		result += m.getDeclaringClass().toString() + "\n";
		result += m.getReturnType().toString() + "\n";		
		result += m.getParameterTypes()+ "\n";
		result += m.getModifiers()+ "\n";
		result += m.getExceptions()+ "\n";
		
		Chain units=m.getActiveBody().getUnits();
		for(Iterator it=units.iterator(); it.hasNext();) {
			Stmt s=(Stmt)it.next();
			result += s.toString() + "\n";
		}
		
		Map unitPositions=new HashMap();
		{
			int i=0;
			for (Iterator it=units.iterator();it.hasNext();i++) {
				Stmt s=(Stmt)it.next();
				unitPositions.put(s, new Integer(i));
			}
		}
		result += "TRAPS\n"; 
		for(Iterator it=m.getActiveBody().getTraps().iterator(); it.hasNext();) {
			Trap t=(Trap)it.next();			
			result += t.getException() + "\n";
			result += unitPositions.get(t.getBeginUnit()) + "\n";
			result += unitPositions.get(t.getEndUnit()) + "\n";
			result += unitPositions.get(t.getHandlerUnit()) + "\n";
		}
		
		
		// restore local names
		{
			Iterator itL=localNames.iterator();
			for (Iterator it=locals.iterator(); it.hasNext();) {
				Local l=(Local)it.next();
				l.setName((String)itL.next());
			}
		}
		return result;
	}
	public static Stmt newSwitchStmt(Value key, List lookupValues, List targets, Unit defaultTarget) {
		int min=Integer.MAX_VALUE;
		int max=Integer.MIN_VALUE;
		int count=lookupValues.size();
		TreeMap map=new TreeMap();
		Iterator itTg=targets.iterator();
		for (Iterator it=lookupValues.iterator();it.hasNext();) {
			IntConstant intConst=(IntConstant)it.next();
			Unit target=(Unit)itTg.next();
			int val=intConst.value;
			max = (val>max ? val : max);
			min = (val<min ? val : min);
			map.put(new Integer(val), target);
		}
		if (max-min+1==count) {
			targets=new LinkedList(map.values()); /// is this map necessary, or are lookupValues always sorted?
			return Jimple.v().newTableSwitchStmt(key, min, max, targets, defaultTarget);
		} else {
			return Jimple.v().newLookupSwitchStmt(key, lookupValues, targets, defaultTarget);
		}
	}
	
	public static void validateMethod(SootMethod method) {
		if (abc.main.Debug.v().aroundWeaver)
			Restructure.validateMethod(method);
	}
	public static String printMethod(SootMethod m) {
		String result="";//=m + "\n";
		for(Iterator it=m.getActiveBody().getLocals().iterator(); it.hasNext();) {
			Local l=(Local)it.next();
			result += l.getType() + " " + l.getName() + ";\n";			
		}
		result += "\n";
		for(Iterator it=m.getActiveBody().getUnits().iterator(); it.hasNext();) {
			result += it.next().toString() + "\n";
		}
		return result;
	}
	public static boolean isAroundAdviceMethod(SootMethod method) {
		return isAroundAdviceMethodName(method.getName()); // TODO: something more solid
	}
	public static boolean isAroundAdviceMethodName(String methodName) {
		return methodName.startsWith("around$"); // TODO: something more solid
	}
	public static boolean isProceedMethodName(String methodName) {
		return methodName.indexOf("$proceed$")!=-1; // TODO: something much more solid!
	}
	public static boolean chainContainsLocal(Chain locals, String name) {
		Iterator it = locals.iterator();
		while (it.hasNext()) {
			if (((soot.Local) it.next()).getName().equals(name))
				return true;
		}
		return false;
	}
	

	/**
	 * Removes all unused locals from the local chain
	 * @param body
	 */
	public static void cleanLocals(Body body) {

		Chain locals = body.getLocals();

		HashSet usedLocals = new HashSet();

		Iterator it = body.getUseAndDefBoxes().iterator();
		while (it.hasNext()) {
			ValueBox vb = (ValueBox) it.next();
			if (vb.getValue() instanceof Local) {
				usedLocals.add(vb.getValue());
			}
		}

		List removed = new LinkedList();
		it = body.getLocals().iterator();
		while (it.hasNext()) {
			Local local = (Local) it.next();
			if (!usedLocals.contains(local))
				removed.add(local);
		}
		Iterator it2 = removed.iterator();
		while (it2.hasNext())
			locals.remove(it2.next());
	}

	public static List getParameterLocals(Body body) {
		List result = new LinkedList();

		for (int i = 0; i < body.getMethod().getParameterCount(); i++) {
			result.add(body.getParameterLocal(i));
		}
		return result;
	}

	/**
	 * Checks if @param test 
	 * is in ]@param begin, @param end[
	 * 
	 * @param body 
	 * @return
	 */
	public static boolean isInSequence(Body body, Unit begin, Unit end, Unit test) {
		Chain statements = body.getUnits().getNonPatchingChain();

		Iterator it = statements.iterator(begin);
		it.next();
		while (it.hasNext()) {
			Unit ut = (Unit) it.next();
			if (ut == end)
				break;

			if (ut == test)
				return true;
		}
		return false;
	}

	public static String mangleTypeName(String name) {
		return name.replaceAll("_", "__").replaceAll("\\.", "_d_").replaceAll("/", "_s_");

	}

	/**
	 * Removes statements between begin and end, excluding these and skip.
	 */
	public static void removeStatements(Body body, Unit begin, Unit end, Unit skip) {
		Chain units = body.getUnits().getNonPatchingChain();
		List removed = new LinkedList();
		Iterator it = units.iterator(begin);
		if (it.hasNext())
			it.next(); // skip begin
		while (it.hasNext()) {
			Unit ut = (Unit) it.next();
			if (ut == end)
				break;

			if (ut != skip) {
				removed.add(ut);
			}
		}
		Iterator it2 = removed.iterator();
		while (it2.hasNext()) {
			Unit ut=(Unit)it2.next();
			AroundWeaver.debug("******* Removing unit: " + ut);
			units.remove(ut);
		}
	}

	/**
	 * Removes all traps that refer to statements between begin and end.
	 * Throws an exception if traps partially refer to that range.
	 * @param body
	 * @param begin
	 * @param end
	 */
	public static void removeTraps(Body body, Unit begin, Unit end) {
		HashSet range = new HashSet();

		Chain units = body.getUnits().getNonPatchingChain();
		Iterator it = units.iterator(begin);
		if (it.hasNext())
			it.next(); // skip begin
		while (it.hasNext()) {
			Unit ut = (Unit) it.next();
			if (ut == end)
				break;
			range.add(ut);
		}

		List removed = new LinkedList();
		Chain traps = body.getTraps();
		it = traps.iterator();
		while (it.hasNext()) {
			Trap trap = (Trap) it.next();
			if (range.contains(trap.getBeginUnit())) {
				if (!range.contains(trap.getEndUnit()))
					throw new CodeGenException("partial trap in shadow");

				if (!range.contains(trap.getHandlerUnit()))
					throw new CodeGenException("partial trap in shadow");

				removed.add(trap);
			} else {
				if (range.contains(trap.getEndUnit()))
					throw new CodeGenException("partial trap in shadow");

				if (range.contains(trap.getHandlerUnit()))
					throw new CodeGenException("partial trap in shadow");
			}
		}
		it = removed.iterator();
		while (it.hasNext()) {
			traps.remove(it.next());
		}
	}

	public static void reset() {
		id=0;
	}
	private static int id=0;
	/**
	 *  Assigns a suggested name to a local, dealing with possible collisions
	 */
	public static void setLocalName(Chain locals, Local local, String suggestedName) {
		//if (!locals.contains(local))
		//	throw new InternalCompilerError();

		String name = suggestedName + "$$" + (++id);
		//int i = 0;
		//while (Util.chainContainsLocal(locals, name)) {
		//	name = suggestedName + (++id);
		//}
		local.setName(name);
	}

	/**
	 * Creates a new InvokeExpr based on an existing one but with new arguments.
	 */
	public static InvokeExpr createNewInvokeExpr(InvokeExpr old, List newArgs, List newTypes) {
		if (newArgs.size()!=newTypes.size())
			throw new InternalAroundError();
		/*{ // sanity check:
			Iterator it0=newTypes.iterator();
			for (Iterator it=newArgs.iterator(); it.hasNext();) {
				Value val=(Value)it.next();
				Type type=(Type)it0.next();
			}
		}*/
		SootMethod m;
		
		soot.SootMethodRef oldRef=old.getMethodRef();

		soot.SootMethodRef ref=Scene.v().makeMethodRef(
				oldRef.declaringClass(),
				oldRef.name(),
				newTypes,
				oldRef.returnType(),
				oldRef.isStatic()					
				);

		if (old instanceof InstanceInvokeExpr) {
			Local base = (Local) ((InstanceInvokeExpr) old).getBase();
			if (old instanceof InterfaceInvokeExpr)
				return new JInterfaceInvokeExpr(base, ref, newArgs);
			else if (old instanceof SpecialInvokeExpr) {
				return new JSpecialInvokeExpr(base, ref, newArgs);
			} else if (old instanceof VirtualInvokeExpr)
				return new JVirtualInvokeExpr(base, ref, newArgs);
			else
				throw new InternalAroundError();
		} else {
			return new JStaticInvokeExpr(ref, newArgs);
		}
	}
	public static InvokeExpr createModifiedInvokeExpr(InvokeExpr old, List addedArgs, List addedTypes) {
		if (addedArgs.size()!=addedTypes.size())
			throw new InternalAroundError();
	
		if (old instanceof ModifiableInvokeExpr) {
			ModifiableInvokeExpr m=(ModifiableInvokeExpr)old;
			m.addArguments(addedArgs, addedTypes);
			return old;
		} else {
			soot.SootMethodRef oldRef=old.getMethodRef();
			
			List newArgs=old.getArgs(); // returns copy of list, so no need to copy
			List newTypes=new ArrayList(oldRef.parameterTypes()); // need to make copy
			
			newArgs.addAll(addedArgs);
			newTypes.addAll(addedTypes);
			
			soot.SootMethodRef ref=Scene.v().makeMethodRef(
					oldRef.declaringClass(),
					oldRef.name(),
					newTypes,
					oldRef.returnType(),
					oldRef.isStatic()					
					);
			
			if (old instanceof InstanceInvokeExpr) {		
				Local base = (Local) ((InstanceInvokeExpr) old).getBase();
				if (old instanceof InterfaceInvokeExpr)
					return new JInterfaceInvokeExpr(base, ref, newArgs);
				else if (old instanceof SpecialInvokeExpr) {
					return new JSpecialInvokeExpr(base, ref, newArgs);
				} else if (old instanceof VirtualInvokeExpr)
					return new JVirtualInvokeExpr(base, ref, newArgs);
				else
					throw new InternalAroundError();
			} else {
				return new JStaticInvokeExpr(ref, newArgs);
			}
		}
	}


	/*public static InvokeExpr createModifiedInvokeExpr(InvokeExpr old, List addedArgs, List addedTypes) {
		if (addedArgs.size()!=addedTypes.size())
			throw new InternalAroundError();
	
		soot.SootMethodRef oldRef=old.getMethodRef();
		
		
		List newArgs=old.getArgs(); // returns copy of list, so no need to copy
		List newTypes=new ArrayList(oldRef.parameterTypes()); // need to make copy
		
		newArgs.addAll(addedArgs);
		newTypes.addAll(addedTypes);
		
		soot.SootMethodRef ref=Scene.v().makeMethodRef(
				oldRef.declaringClass(),
				oldRef.name(),
				newTypes,
				oldRef.returnType(),
				oldRef.isStatic()					
				);

		if (old instanceof InstanceInvokeExpr) {
			Local base = (Local) ((InstanceInvokeExpr) old).getBase();
			if (old instanceof InterfaceInvokeExpr)
				return Jimple.v().newInterfaceInvokeExpr(base, ref, newArgs);
			else if (old instanceof SpecialInvokeExpr) {
				return Jimple.v().newSpecialInvokeExpr(base, ref, newArgs);
			} else if (old instanceof VirtualInvokeExpr)
				return Jimple.v().newVirtualInvokeExpr(base, ref, newArgs);
			else
				throw new InternalAroundError();
		} else {
			return Jimple.v().newStaticInvokeExpr(ref, newArgs);
		}
	}*/
	public static List getDefaultValues(List types) {
		List result = new LinkedList();
		{
			Iterator it = types.iterator();
			while (it.hasNext()) {
				Type type = (Type) it.next();
				result.add(Restructure.JavaTypeInfo.getDefaultValue(type));
			}
		}
		return result;
	}
	public static List getTypeListFromLocals(List locals) {
		List result=new ArrayList(locals.size());
		for (Iterator it=locals.iterator();it.hasNext();) {
			Local l=(Local)it.next();
			result.add(l.getType());
		}
		return result;
	}
	public static void insertCast(Body body, Stmt stmt, ValueBox source, Type targetType) {
		Chain units = body.getUnits().getNonPatchingChain();
		if (!source.getValue().getType().equals(targetType)) {
			LocalGeneratorEx localgen = new LocalGeneratorEx(body);
			Local castLocal = localgen.generateLocal(source.getValue().getType(), "castTmp");
			//debug("cast: source has type " + source.getValue().getType().toString());
			//debug("cast: target has type " + targetType.toString());
			AssignStmt tmpStmt = Jimple.v().newAssignStmt(castLocal, source.getValue());
			CastExpr castExpr = Jimple.v().newCastExpr(castLocal, targetType);
			//	Jimple.v().newCastExpr()
			units.insertBefore(tmpStmt, stmt);
			if (stmt instanceof AssignStmt) {
				source.setValue(castExpr);
			} else {
				Local tmpLocal = localgen.generateLocal(targetType, "castTarget");
				AssignStmt tmpStmt2 = Jimple.v().newAssignStmt(tmpLocal, castExpr);
				units.insertBefore(tmpStmt2, stmt);
				source.setValue(tmpLocal);
			}
		}
	}
	
	/**Copies a sequence of statements from one method to another.
	 * Copied units exclude begin and end.
	 * Returns bindings (old-unit<->new-unit).
	 * Returns first inserted unit in the destination method in the UnitBox.
	 * 
	 * If returnedLocal is not null, the corresponding new local is returned after the 
	 * copy of the block.
	 * 
	 * The former local "this" is mapped to the new "this". 
	 * 
	 * This is a modified version of Body.importBodyContentsFrom()
	 * */
	public static HashMap copyStmtSequence(
		Body source,
		Unit begin,
		Unit end,
		Body dest,
		Unit insertAfter,
		Local returnedLocal,
		ObjectBox resultingFirstCopy) {
		
		if (returnedLocal!=null && !source.getLocals().contains(returnedLocal))
			throw new InternalAroundError("returnedLocal " + returnedLocal + 
					" not in source method " + source.getMethod());

		boolean bInstance = !source.getMethod().isStatic() && !dest.getMethod().isStatic();
		Local lThisSource = null;
		//Local lThisCopySource = null;
		if (bInstance) {
			lThisSource = source.getThisLocal();
		//	lThisCopySource = Restructure.getThisCopy(source.getMethod());
		}

		Local lThisDest = null;
		//Local lThisCopyDest = null;
		if (bInstance) {
			lThisDest = dest.getThisLocal();
		//	lThisCopyDest = Restructure.getThisCopy(dest.getMethod());
		}

		HashMap bindings = new HashMap();
		//HashMap boxes=new HashMap();

		Iterator it = source.getUnits().getNonPatchingChain().iterator(begin);
		if (it.hasNext())
			it.next(); // skip begin

		Chain unitChain = dest.getUnits().getNonPatchingChain();

		Unit firstCopy = null;
		// Clone units in body's statement list 
		while (it.hasNext()) {
			Unit original = (Unit) it.next();
			if (original == end)
				break;

			Unit copy = (Unit) original.clone();
			copy.addAllTagsOf(original);

			// Add cloned unit to our unitChain.
			unitChain.insertAfter(copy, insertAfter);
			insertAfter = copy;
			if (firstCopy == null)
				firstCopy = insertAfter;
			// Build old <-> new map to be able to patch up references to other units 
			// within the cloned units. (these are still refering to the original
			// unit objects).
			bindings.put(original, copy);
		}

		Chain trapChain = dest.getTraps();

		// Clone trap units.
		it = source.getTraps().iterator();
		while (it.hasNext()) {
			Trap original = (Trap) it.next();
			if (Util.isInSequence(source, begin, end, original.getBeginUnit())
				&& Util.isInSequence(source, begin, end, original.getEndUnit())
				&& Util.isInSequence(source, begin, end, original.getHandlerUnit())) {

				Trap copy = (Trap) original.clone();

				// Add cloned unit to our trap list.
				trapChain.addLast(copy);

				// Store old <-> new mapping.
				bindings.put(original, copy);
			}
		}

		Chain destLocals = dest.getLocals();

		// Clone local units.
		it = source.getLocals().iterator();
		while (it.hasNext()) {
			Local original = (Local) it.next();
			Local copy = (Local) original.clone();

			if (original == lThisSource) {
				bindings.put(lThisSource, lThisDest);						
			} else {
				//copy.setName(copy.getName() + "$abc$" + state.getUniqueID());
				Util.setLocalName(destLocals, copy, original.getName());
				

				// Add cloned unit to our local list.
				destLocals.addLast(copy);

				// Build old <-> new mapping.
				bindings.put(original, copy);
				
				// Cflow thread-local variables are not passed, so we have to
				// initialise them to null
				// DS (from patch by Bruno Harbulot)
				if (CflowCodeGenUtils.isThreadLocalType(copy.getType())) {
					Unit nullInitStmt = Jimple.v().newAssignStmt(copy, NullConstant.v());
					unitChain.insertBefore(nullInitStmt, firstCopy);
				}
				
			}
		}

		// Patch up references within units using our (old <-> new) map.
		it = dest.getAllUnitBoxes().iterator();
		while (it.hasNext()) {
			UnitBox box = (UnitBox) it.next();
			Unit newObject, oldObject = box.getUnit();

			// if we have a reference to an old object, replace it 
			// it's clone.
			if ((newObject = (Unit) bindings.get(oldObject)) != null)
				box.setUnit(newObject);

		}

		// backpatching all local variables.
		it = dest.getUseAndDefBoxes().iterator();
		while (it.hasNext()) {
			ValueBox vb = (ValueBox) it.next();
			if (vb.getValue() instanceof Local) {
				Local oldLocal = (Local) vb.getValue();
				Local newLocal = (Local) bindings.get(oldLocal);

				if (newLocal != null)
					vb.setValue(newLocal);
			}

		}

		// fix the trap destinations
		it = dest.getTraps().iterator();
		while (it.hasNext()) {
			Trap trap = (Trap) it.next();
			List boxes = trap.getUnitBoxes();
			Iterator it2 = boxes.iterator();

			while (it2.hasNext()) {
				UnitBox box = (UnitBox) it2.next();
				Unit ut = box.getUnit();
				Unit newUnit = (Unit) bindings.get(ut);
				if (newUnit != null) {
					box.setUnit(newUnit);
				} /*else {
																									
				}*/
			}
		}

		if (returnedLocal != null) {
			Local newLocal = (Local) bindings.get(returnedLocal);
			if (newLocal == null) {
				if (!source.getLocals().contains(returnedLocal)) {
					AroundWeaver.debug("returnedLocal " + returnedLocal +
					" is not in local chain of source method.");
				}				
				AroundWeaver.debug("Source: " + Util.printMethod(source.getMethod()));
				AroundWeaver.debug("Dest : " + Util.printMethod(dest.getMethod()));
				throw new InternalAroundError("Could not find " + returnedLocal + 
						" in the bindings map. " + 
						"Source: " + source.getMethod() +
						" Dest: " + dest.getMethod());
			}
			LocalGeneratorEx lg = new LocalGeneratorEx(dest);
			Local castLocal = lg.generateLocal(dest.getMethod().getReturnType(), "castLocal");
			AssignStmt s = Jimple.v().newAssignStmt(castLocal, newLocal);
			unitChain.insertAfter(s, insertAfter);
			insertAfter = s;
			ReturnStmt returnStmt = Jimple.v().newReturnStmt(castLocal);
			unitChain.insertAfter(returnStmt, insertAfter);
			Restructure.insertBoxingCast(dest, s, true);
			//insertBoxingCast(dest, returnStmt, returnStmt.getOpBox(), dest.getMethod().getReturnType());
			//JasminClass
			insertAfter = returnStmt;						
		} else {
			if (!dest.getMethod().getReturnType().equals(VoidType.v())) {					
				throw new InternalAroundError(
					"destination method: " + dest.getMethod() + 
					"\nsource method: " + source.getMethod());
			}

			ReturnVoidStmt returnStmt = Jimple.v().newReturnVoidStmt();
			unitChain.insertAfter(returnStmt, insertAfter);
			insertAfter = returnStmt;
			
		}
		if (firstCopy == null)
			firstCopy = insertAfter;
		
		resultingFirstCopy.object = firstCopy;
		return bindings;
	}
}
