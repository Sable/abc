package abc.weaving.weaver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import soot.Body;
import soot.BooleanType;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
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
import soot.jimple.Constant;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.AdviceSpec;
import abc.weaving.aspectinfo.AroundAdvice;
import abc.weaving.aspectinfo.Formal;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.ConstructorAdviceApplication;
import abc.weaving.matching.ExecutionAdviceApplication;
import abc.weaving.matching.HandlerAdviceApplication;
import abc.weaving.matching.NewStmtAdviceApplication;
import abc.weaving.matching.StmtAdviceApplication;
import abc.weaving.residues.AdviceFormal;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.AndResidue;
import abc.weaving.residues.AspectOf;
import abc.weaving.residues.Bind;
import abc.weaving.residues.Box;
import abc.weaving.residues.CflowResidue;
import abc.weaving.residues.CheckType;
import abc.weaving.residues.Copy;
import abc.weaving.residues.HasAspect;
import abc.weaving.residues.IfResidue;
import abc.weaving.residues.Load;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.NotResidue;
import abc.weaving.residues.OrResidue;
import abc.weaving.residues.Residue;

/** Handle around weaving.
 * @author Sascha Kuzins 
 * @date May 6, 2004
 */

public class AroundWeaver {

	private static class InternalError extends RuntimeException {
		InternalError(String message) {
			super("ARD around weaver internal error: " + message);
		}
		InternalError() {
			super("ARD around weaver internal error");
		}
	}
	/** set to false to disable debugging messages for Around Weaver */
	public static boolean debugflag = true;

	public static void reset() {
		state = new State();
	}
	private static void debug(String message) {
		if (abc.main.Debug.v().aroundWeaver)
			System.err.println("ARD*** " + message);
	}

	public static class ObjectBox {
		Object object;
	}

	private static class Util {

		private static boolean chainContainsLocal(Chain locals, String name) {
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
		private static void cleanLocals(Body body) {

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

		private static List getParameterLocals(Body body) {
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
		private static boolean isInSequence(Body body, Unit begin, Unit end, Unit test) {
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

		private static String mangleTypeName(String name) {
			return name.replaceAll("_", "__").replaceAll("\\.", "_d_").replaceAll("/", "_s_");

		}

		/**
		 * Removes statements between begin and end, excluding these and skip.
		 */
		private static void removeStatements(Body body, Unit begin, Unit end, Unit skip) {
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
			while (it2.hasNext())
				units.remove(it2.next());
		}

		/**
		 * Removes all traps that refer to statements between begin and end.
		 * Throws an exception if traps partially refer to that range.
		 * @param body
		 * @param begin
		 * @param end
		 */
		private static void removeTraps(Body body, Unit begin, Unit end) {
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

		/**
		 *  Assigns a suggested name to a local, dealing with possible collisions
		 */
		private static void setLocalName(Chain locals, Local local, String suggestedName) {
			//if (!locals.contains(local))
			//	throw new RuntimeException();

			String name = suggestedName;
			int i = 0;
			while (AroundWeaver.Util.chainContainsLocal(locals, name)) {
				name = suggestedName + (++i);
			}
			local.setName(name);
		}

		/*private static ValueBox getInvokeExprBox(Stmt stmt) {
			stmt.getInvokeExprBox()
			if (stmt instanceof AssignStmt)
				return ((AssignStmt)stmt).getInvokeExprBox()
				
		}*/
		/**
		 * Creates a new InvokeExpr based on an existing one but with new arguments.
		 */
		private static InvokeExpr createNewInvokeExpr(InvokeExpr old, List newArgs) {
			if (old instanceof InstanceInvokeExpr) {
				Local base = (Local) ((InstanceInvokeExpr) old).getBase();
				if (old instanceof InterfaceInvokeExpr)
					return Jimple.v().newInterfaceInvokeExpr(base, old.getMethod(), newArgs);
				else if (old instanceof SpecialInvokeExpr) {
					return Jimple.v().newSpecialInvokeExpr(base, old.getMethod(), newArgs);
				} else if (old instanceof VirtualInvokeExpr)
					return Jimple.v().newVirtualInvokeExpr(base, old.getMethod(), newArgs);
				else
					throw new AroundWeaver.InternalError();
			} else {
				return Jimple.v().newStaticInvokeExpr(old.getMethod(), newArgs);
			}
		}

		private static boolean isBaseClass(SootClass baseClass, SootClass subClass) {
			SootClass sub = subClass;
		
			while (sub.hasSuperclass()) {
				SootClass superClass = sub.getSuperclass();
				if (superClass.equals(baseClass))
					return true;
		
				sub = superClass;
			}
			return false;
		}
	}
	public static class AdviceApplicationInfo {
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
		private static HashMap copyStmtSequence(
			Body source,
			Unit begin,
			Unit end,
			Body dest,
			Unit insertAfter,
			Local returnedLocal,
			ObjectBox resultingFirstCopy) {
	
			boolean bInstance = !source.getMethod().isStatic() && !dest.getMethod().isStatic();
			Local lThisSource = null;
			Local lThisCopySource = null;
			if (bInstance) {
				lThisSource = source.getThisLocal();
				lThisCopySource = Restructure.getThisCopy(source.getMethod());
			}
	
			Local lThisDest = null;
			Local lThisCopyDest = null;
			if (bInstance) {
				lThisDest = dest.getThisLocal();
				lThisCopyDest = Restructure.getThisCopy(dest.getMethod());
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
					firstCopy = copy;
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
				} else if (original == lThisCopySource) {
					bindings.put(lThisCopySource, lThisCopyDest);
				} else {
					//copy.setName(copy.getName() + "$abc$" + state.getUniqueID());
					Util.setLocalName(destLocals, copy, original.getName());
					// TODO: can comment this line out in release build
	
					// Add cloned unit to our local list.
					destLocals.addLast(copy);
	
					// Build old <-> new mapping.
					bindings.put(original, copy);
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
				if (newLocal == null)
					throw new InternalError();
	
				LocalGeneratorEx lg = new LocalGeneratorEx(dest);
				Local castLocal = lg.generateLocal(dest.getMethod().getReturnType());
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
					throw new InternalError(
						"destination method: " + dest.getMethod() + 
						"\nsource method: " + source.getMethod());
				}
	
				ReturnVoidStmt returnStmt = Jimple.v().newReturnVoidStmt();
				unitChain.insertAfter(returnStmt, insertAfter);
				insertAfter = returnStmt;
			}
			resultingFirstCopy.object = firstCopy;
			return bindings;
		}

		/**
		 * Checks that:
		 * 	No units outside the shadow point to units inside the shadow
		 *  No units inside the shadow point to units outside the shadow, including end and start
		 * @param body
		 * @param begin
		 * @param end
		 */
		private static void validateShadow(Body body, Stmt begin, Stmt end) {
			Chain statements = body.getUnits().getNonPatchingChain();
	
			if (!statements.contains(begin))
				throw new RuntimeException();
	
			if (!statements.contains(end))
				throw new RuntimeException();
	
			boolean insideRange = false;
	
			Iterator it = statements.iterator();
			while (it.hasNext()) {
				Stmt s = (Stmt) it.next();
				if (s == begin) {
					if (insideRange)
						throw new RuntimeException();
	
					insideRange = true;
				}
	
				if (s == end) {
					if (!insideRange)
						throw new RuntimeException();
	
					insideRange = false;
				}
	
				List unitBoxes = s.getUnitBoxes();
				Iterator it2 = unitBoxes.iterator();
				while (it2.hasNext()) {
					UnitBox box = (UnitBox) it2.next();
					if (insideRange) {
						if (!Util.isInSequence(body, begin, end, box.getUnit())) {
							if (box.getUnit() == end) {
								throw new InternalError("Unit in shadow points to endshadow");
							} else if (box.getUnit() == begin) {
								throw new InternalError("Unit in shadow points to beginshadow");
							} else
								throw new InternalError("Unit in shadow points outside of the shadow" + body.toString());
						}
					} else {
						if (Util.isInSequence(body, begin, end, box.getUnit())) {
							throw new InternalError("Unit outside of shadow points inside the shadow");
						}
					}
				}
			}
		}
		/**
		 * 
		 * Algorithm:
		 * 	Find all locals which are used in the range.
		 *  Intersect with all locals written to outside the range.
		 * 
		 * @param body
		 * @param begin
		 * @param end
		 */
		private static List findLocalsGoingIn(Body body, Stmt begin, Stmt end, Local additionallyUsed) {
			Chain statements = body.getUnits().getNonPatchingChain();
	
			if (!statements.contains(begin))
				throw new RuntimeException();
	
			if (!statements.contains(end))
				throw new RuntimeException();
	
			Set usedInside = new HashSet();
			if (additionallyUsed!=null)
				usedInside.add(additionallyUsed);
				
			Set definedOutside = new HashSet();
	
			boolean insideRange = false;
			{
				Iterator it = statements.iterator();
				while (it.hasNext()) {
					Stmt s = (Stmt) it.next();
					if (s == begin) {
						if (insideRange)
							throw new RuntimeException();
	
						insideRange = true;
					}
	
					if (s == end) {
						if (!insideRange)
							throw new RuntimeException();
	
						insideRange = false;
					}
	
					if (insideRange) {
						List useBoxes = s.getUseBoxes();
						Iterator it2 = useBoxes.iterator();
						while (it2.hasNext()) {
							ValueBox box = (ValueBox) it2.next();
							if (box.getValue() instanceof Local) {
								Local l = (Local) box.getValue();
								usedInside.add(l);
							}
						}
					} else {
						List defBoxes = s.getDefBoxes();
						Iterator it2 = defBoxes.iterator();
						while (it2.hasNext()) {
							ValueBox box = (ValueBox) it2.next();
							if (box.getValue() instanceof Local) {
								Local l = (Local) box.getValue();
								definedOutside.add(l);
							}
						}
					}
				}
			}
			usedInside.retainAll(definedOutside);
			List result=new LinkedList();
			result.addAll(usedInside);
			return result;
		}

		private void weaveDynamicResidue(
			Local returnedLocal,
			List dynamicActuals,
			int shadowID,
			WeavingContext wc,
			Stmt failPoint) {
			{
				LocalGeneratorEx localgen = new LocalGeneratorEx(joinpointBody);	
				//Stmt failPoint = Jimple.v().newNopStmt();
				
				joinpointStatements.insertBefore(failPoint, end);
				//joinpointStatements.insertBefore(beforeFailPoint, failPoint);
	
				// weave in residue
				Stmt endResidue = adviceAppl.residue.codeGen(joinpointMethod, localgen, joinpointStatements, begin, failPoint, wc);
				
				//((AdviceWeavingContext) wc).arglist.get()
	
				// debug("weaving residue: " + adviceAppl.residue);
				if (!(adviceAppl.residue instanceof AlwaysMatch)) {
					InvokeExpr directInvoke;
					List directParams = new LinkedList();
					//directParams.add(targetLocal);
					
					List defaultValues = getDefaultValues(adviceMethod.originalAdviceFormalTypes);
					directParams.addAll(defaultValues);
					directParams.add(IntConstant.v(shadowID));
					directParams.add(IntConstant.v(1)); // skipAdvice parameter
					directParams.addAll(dynamicActuals);
					if (bStaticJoinPoint || adviceMethod.bAllwaysStaticAccessMethod) {
						directInvoke = Jimple.v().newStaticInvokeExpr(accessMethod.method, directParams);
					} else {
						// TODO: can this call be replaced with an InvokeSpecial?
						directInvoke = Jimple.v().newInterfaceInvokeExpr(Restructure.getThisCopy(joinpointMethod), adviceMethod.interfaceInfo.abstractAccessMethod, directParams);
					}
					{
						Stmt skipAdvice;
						if (returnedLocal != null) {
							AssignStmt assign = Jimple.v().newAssignStmt(returnedLocal, directInvoke);
							joinpointStatements.insertAfter(assign, failPoint);
							Restructure.insertBoxingCast(joinpointBody, assign, true);
							skipAdvice = assign;
						} else {
							skipAdvice = Jimple.v().newInvokeStmt(directInvoke);
							joinpointStatements.insertAfter(skipAdvice, failPoint);
						}
						adviceMethod.directInvokationStmts.add(skipAdvice);
					}
				}
			}
		}
		
		// mapping formal-position => binding (local)
		private ArrayList getStaticBinding() {
			List bindings = getBindList(adviceAppl.residue);
			debug("getStaticBinding: Binds found:" + bindings.size());
			
			ArrayList arrayList=new ArrayList();
			arrayList.ensureCapacity(bindings.size());
			
			Iterator it = bindings.iterator();
			while (it.hasNext()) {
				Bind bind = (Bind) it.next();
				if (bind.variable instanceof AdviceFormal) {
					AdviceFormal formal = (AdviceFormal) bind.variable;
					Value value = bind.value.getSootValue();
					if (value instanceof Local) {
						Local local = (Local) value;
						debug(" Binding: " + local.getName() + " => " + formal.pos);
						
						// resize
						while (arrayList.size()<formal.pos+1)
							arrayList.add(null);
							
						if (arrayList.get(formal.pos) != null)
							throw new RuntimeException("Ambiguous variable binding");
						// TODO: fix this message.
	
						arrayList.set(formal.pos, local);
					} else {
						throw new InternalError(
						"Expecting bound values to be of type Local: "
							+ value
							+ " (came from: "
							+ bind
							+ ")");

					}
				} else {
				//	throw new InternalError("Expecting bound variables to be of type adviceFormal: " + bind.variable );
				}
			}			
			return arrayList;
		}
		// this is not used anymore. arguments bound by cflow are not 
		// changeable and are thus not needed in the list.
		private static void verifyBindings(ArrayList bindings) {
			int i=0;
			for (Iterator it=bindings.iterator(); it.hasNext();i++) {
				if (it.next() == null)
					throw new InternalError("Argument "+i+" is not bound"); 
				// TODO:
			}
		}
		public final String accessMethodName;
		
		public void doWeave() {		
			
			// find returned local
			Local returnedLocal = findReturnedLocal();

			List /*ValueBox*/context=findLocalsGoingIn(joinpointBody, begin, end, returnedLocal);
			{ // print debug information
				debug("Locals going in: ");
				debug(" Method: " + joinpointMethod.toString());
				debug(" Application: " + adviceAppl.toString());
				//debug("Method + " + joinpointMethod.toString());
				Iterator it = context.iterator();
				while (it.hasNext()) {
					Local l = (Local) it.next();
					debug("  " + l.toString());
				}
			}

			validateShadow(joinpointBody, begin, end);
			
			List dynamicActuals;
			ObjectBox dynamicActualsBox=new ObjectBox();
			int [] argIndex=adviceMethod.modifyAdviceMethod(context,accessMethod, dynamicActualsBox, bStaticJoinPoint);
			dynamicActuals=(List)dynamicActualsBox.object;
			
			// copy shadow into access method with a return returning the relevant local.
			Stmt first;
			HashMap bindings;
			Stmt switchTarget;
			{ // copy shadow into access method
				ObjectBox result = new ObjectBox();
				bindings = copyStmtSequence(joinpointBody, begin, end, accessMethod.body, accessMethod.lookupStmt, returnedLocal, result);
				first = (Stmt) result.object;
				switchTarget = Jimple.v().newNopStmt();
				accessMethod.statements.insertBefore(switchTarget, first);
			}
			updateSavedReferencesToStatements(bindings);

			{ // remove old shadow
				// remove any traps from the shadow before removing the shadow
				Util.removeTraps(joinpointBody, begin, end);
				// remove statements except original assignment
				Util.removeStatements(joinpointBody, begin, end, null);
				StmtAdviceApplication stmtAppl = null;
				if (adviceAppl instanceof StmtAdviceApplication) {
					stmtAppl = (StmtAdviceApplication) adviceAppl;
					stmtAppl.stmt = null;
					/// just for sanity, because we deleted that stmt
				}
			}

			Local lThis = null;
			if (!bStaticJoinPoint)
				lThis = Restructure.getThisCopy(joinpointMethod);

			int shadowID;
			{ // determine shadow ID
				if (bStaticJoinPoint || adviceMethod.bAllwaysStaticAccessMethod) {
					shadowID = accessMethod.nextID++;
				} else {
					shadowID = adviceMethod.getUniqueShadowID();
				}
			}

		
			ArrayList staticBindings = getStaticBinding();
			//verifyBindings(staticBindings);
		
			Stmt failPoint = Jimple.v().newNopStmt();
			WeavingContext wc = PointcutCodeGen.makeWeavingContext(adviceAppl);
			
			
			weaveDynamicResidue(
				returnedLocal,
				dynamicActuals,
				shadowID,
				wc,					
				failPoint);
		
			
			accessMethod.assignCorrectParametersToLocals(
				context,
				argIndex,
				first,
				bindings,
				staticBindings);
			accessMethod.modifyLookupStatement(switchTarget, shadowID);
			
			makeAdviceInvokation(returnedLocal, dynamicActuals, lThis, shadowID, failPoint, wc);
			accessMethod.method.getActiveBody().validate();

			
		}

		

		private void makeAdviceInvokation(Local returnedLocal, List dynamicActuals, Local lThis, int shadowID, Stmt insertionPoint, WeavingContext wc) {
			LocalGeneratorEx lg = new LocalGeneratorEx(joinpointBody);
			Chain invokeStmts = adviceMethod.adviceDecl.makeAdviceExecutionStmts(adviceAppl, lg, wc);
			
			VirtualInvokeExpr invokeEx = (VirtualInvokeExpr) ((InvokeStmt) invokeStmts.getLast()).getInvokeExpr();
			Local aspectRef = (Local) invokeEx.getBase();
			
			{
				Stmt last = (Stmt) invokeStmts.getLast();
				NopStmt nop = Jimple.v().newNopStmt();
				invokeStmts.insertBefore(nop, last);
				last.redirectJumpsToThisTo(nop);
				invokeStmts.removeLast();
			}
			for (Iterator stmtlist = invokeStmts.iterator(); stmtlist.hasNext();) {
				Stmt nextstmt = (Stmt) stmtlist.next();
				if (nextstmt == null)
					throw new RuntimeException();
				if (insertionPoint == null)
					throw new RuntimeException();
				if (joinpointStatements == null)
					throw new RuntimeException();
				if (!joinpointStatements.contains(insertionPoint))
					throw new RuntimeException();
				joinpointStatements.insertBefore(nextstmt, insertionPoint);
			}
			
			// we need to add some of our own parameters to the invokation
			List params = new LinkedList();
			if (lThis == null) {
				params.add(NullConstant.v());
			} else {
				params.add(lThis); // pass the closure
			}
			//params.add(targetLocal);
			params.add(IntConstant.v(shadowID));
			if (bStaticJoinPoint || adviceMethod.bAllwaysStaticAccessMethod) { // pass the static class id
				params.add(IntConstant.v(state.getStaticDispatchTypeID(joinpointClass.getType())));
			} else {
				params.add(IntConstant.v(0));
			}
			// and add the original parameters 
			params.addAll(0, invokeEx.getArgs());
			
			params.addAll(dynamicActuals);
			
			// generate a new invoke expression to replace the old one
			VirtualInvokeExpr invokeEx2 = Jimple.v().newVirtualInvokeExpr(aspectRef, adviceMethod.method, params);
			
			Stmt invokeStmt;
			if (returnedLocal == null) {
				invokeStmt = Jimple.v().newInvokeStmt(invokeEx2);
				joinpointStatements.insertBefore(invokeStmt, insertionPoint);
			} else {
				AssignStmt assign = Jimple.v().newAssignStmt(returnedLocal, invokeEx2);
				joinpointStatements.insertBefore(assign, insertionPoint);
				Restructure.insertBoxingCast(joinpointMethod.getActiveBody(), assign, true);
				invokeStmt = assign;
			}
			Stmt beforeEnd=Jimple.v().newNopStmt();
			joinpointStatements.insertBefore(beforeEnd, end);
			joinpointStatements.insertBefore(Jimple.v().newGotoStmt(beforeEnd), insertionPoint);
			
			if (invokeStmt == null)
				throw new InternalError();
			
			adviceMethod.adviceMethodInvokationStmts.add(invokeStmt);
			
			joinpointBody.validate();
		}

		
		private Local findReturnedLocal() {
			Body joinpointBody = joinpointMethod.getActiveBody();
			joinpointBody.validate();
			Chain joinpointStatements = joinpointBody.getUnits().getNonPatchingChain();
			boolean bStatic = joinpointMethod.isStatic();
	
			Stmt begin = adviceAppl.shadowpoints.getBegin();
			Stmt end = adviceAppl.shadowpoints.getEnd();
	
			Local returnedLocal = null;
			
			Type objectType=Scene.v().getRefType("java.lang.Object");
	
			if (adviceAppl instanceof ExecutionAdviceApplication || 
				adviceAppl instanceof ConstructorAdviceApplication) {
				//ExecutionAdviceApplication ea = (ExecutionAdviceApplication) adviceAppl;
				
				if (adviceAppl instanceof ConstructorAdviceApplication) {
					if (!joinpointMethod.getReturnType().equals(VoidType.v()))
						throw new InternalError("Constructor must have void return type: " + 
							joinpointMethod);
				}
				//if (joinpointMethod.getName().startsWith("around$"))
				//	throw new CodeGenException("Execution pointcut matching advice method.");
	
				if (!bStatic) {
					Local lThisCopy = Restructure.getThisCopy(joinpointMethod);
					Stmt succ = (Stmt) joinpointStatements.getSuccOf(begin);
					if (succ instanceof AssignStmt) {
						AssignStmt s = (AssignStmt) succ;
						if (s.getLeftOp() == lThisCopy) {
							debug("moving 'thisCopy=this' out of execution shadow.");
							// TODO: fix thisCopy strategy.
							joinpointStatements.remove(s);
							joinpointStatements.insertBefore(s, begin);
						}
					}
				}
				if (joinpointMethod.getReturnType().equals(VoidType.v())) {
					if (
						! adviceMethod.getReturnType().equals(VoidType.v())					 
						) { 
						// make dummy local to be returned. assign default value.
						LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);
						Local l=lg.generateLocal(adviceMethod.getReturnType(), "returnedLocal");
						Stmt s=Jimple.v().newAssignStmt(l, 
							Restructure.JavaTypeInfo.getDefaultValue(adviceMethod.getReturnType()));
						joinpointStatements.insertAfter(s, begin);
						returnedLocal=l;			
					}
				} else {
					ReturnStmt returnStmt;
					try {
						returnStmt = (ReturnStmt) joinpointStatements.getSuccOf(end);
					} catch (Exception ex) {
						throw new CodeGenException("Expecting return statement after shadow " + "for execution advice in non-void method");
					}
					returnedLocal = (Local) returnStmt.getOp();
					// TODO: could return constant?
				}
			} else if (adviceAppl instanceof HandlerAdviceApplication) {
				throw new RuntimeException("Semantic error: Cannot apply around advice to exception handler"); // TODO: fix message
			} else if (adviceAppl instanceof StmtAdviceApplication ||
					   adviceAppl instanceof NewStmtAdviceApplication) { 
			
				Stmt applStmt=null;
				{				
					if (adviceAppl instanceof StmtAdviceApplication) {
						StmtAdviceApplication stmtAppl = (StmtAdviceApplication) adviceAppl;
						applStmt=stmtAppl.stmt;	
					} else if (adviceAppl instanceof NewStmtAdviceApplication) {
						NewStmtAdviceApplication stmtAppl = (NewStmtAdviceApplication) adviceAppl;
						applStmt=stmtAppl.stmt;
					} else {
						throw new RuntimeException(); 
					}
				}
					
				if (applStmt instanceof AssignStmt) {					   
					AssignStmt assignStmt = (AssignStmt) applStmt;
					Value leftOp = assignStmt.getLeftOp();
					Value rightOp = assignStmt.getRightOp();
					if (leftOp instanceof Local) {
						// get
						returnedLocal = (Local) leftOp;
					} else if ((leftOp instanceof FieldRef && rightOp instanceof Local) ||	
							   (leftOp instanceof FieldRef && rightOp instanceof Constant)) {
						// set
						
						// special case: with return type object, set() returns null.
						if (adviceMethod.getReturnType().equals(objectType)) {
							LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);
							Local l=lg.generateLocal(objectType, "nullValue");
							Stmt s=Jimple.v().newAssignStmt(l, NullConstant.v());
							joinpointStatements.insertAfter(s, begin);
							returnedLocal=l;
						}				
					} else {
						// unexpected statement type
						throw new InternalError();
					}
				} else if (applStmt instanceof InvokeStmt) {
					InvokeStmt invStmt=(InvokeStmt)applStmt;
					
					// if advice method is non-void, we have to return something
					// TODO: type checking to throw out invalid cases?
					if (
						! adviceMethod.getReturnType().equals(VoidType.v())					 
						) { 
						// make dummy local to be returned. assign default value.
						LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);
						Local l=lg.generateLocal(adviceMethod.getReturnType(), "returnedLocal");
						Stmt s=Jimple.v().newAssignStmt(l, 
							Restructure.JavaTypeInfo.getDefaultValue(adviceMethod.getReturnType()));
						joinpointStatements.insertAfter(s, begin);
						returnedLocal=l;			
					}
				} else {
					// unexpected statement type
					throw new InternalError();
				}
			}
			return returnedLocal;
		}
		AdviceApplicationInfo(AdviceApplication adviceAppl, SootMethod joinpointMethod) {
			this.adviceAppl=adviceAppl;
			AdviceDecl adviceDecl = (AdviceDecl) adviceAppl.advice;
			AdviceSpec adviceSpec = adviceDecl.getAdviceSpec();
			AroundAdvice aroundSpec = (AroundAdvice) adviceSpec;
			SootClass theAspect = adviceDecl.getAspect().getInstanceClass().getSootClass();
			SootMethod method = adviceDecl.getImpl().getSootMethod();
			
			AdviceMethod adviceMethodInfo = state.getAdviceMethod(method);
			if (adviceMethodInfo == null) {
				adviceMethodInfo = new AdviceMethod(method, adviceDecl);
				state.setAdviceMethod(method, adviceMethodInfo);
			} else {
				if (adviceMethodInfo.adviceDecl != adviceDecl)
					throw new InternalError("Expecting same adviceDecl each time for same advice method");
			}
			
			this.adviceMethod=adviceMethodInfo;
			this.joinpointMethod=joinpointMethod;
			this.joinpointClass=joinpointMethod.getDeclaringClass();
			this.joinpointBody=joinpointMethod.getActiveBody();
			this.joinpointStatements=joinpointBody.getUnits().getNonPatchingChain();
			
			this.bStaticJoinPoint = joinpointMethod.isStatic();
			this.begin = adviceAppl.shadowpoints.getBegin();
			this.end = adviceAppl.shadowpoints.getEnd();

			if (bStaticJoinPoint || adviceMethod.bAllwaysStaticAccessMethod) {
				accessMethodName = "abc$static$proceed$" + adviceMethod.adviceMethodIdentifierString;
			} else {
				accessMethodName = adviceMethod.dynamicAccessMethodName;
			}
	
	
			AccessMethod accessMethod;
			accessMethod = adviceMethod.getAccessMethod(joinpointMethod.getDeclaringClass().getName(), bStaticJoinPoint || adviceMethod.bAllwaysStaticAccessMethod);
			if (accessMethod == null) {
				accessMethod = new AccessMethod(adviceMethod, joinpointMethod.getDeclaringClass(), bStaticJoinPoint || adviceMethod.bAllwaysStaticAccessMethod, accessMethodName);
				adviceMethod.setAccessMethod(joinpointMethod.getDeclaringClass().getName(), bStaticJoinPoint || adviceMethod.bAllwaysStaticAccessMethod, accessMethod);
			}
			this.accessMethod=accessMethod;
		}
		public final Stmt begin;
		public final Stmt end;
		public final AdviceApplication adviceAppl;
		public final AdviceMethod adviceMethod;
		public final SootClass joinpointClass;
		public final SootMethod joinpointMethod;
		public final Chain joinpointStatements;
		public final Body joinpointBody;
		public final boolean bStaticJoinPoint;
		public final AccessMethod accessMethod;	
	}
	
	
	public static class AdviceMethod {
		private void validate() {
			{
				Iterator it=adviceMethodInvokationStmts.iterator();
				
				while (it.hasNext()) {
					Stmt stmt=(Stmt)it.next();
					if (stmt.getInvokeExpr().getArgCount()!=method.getParameterCount()) {
						throw new RuntimeException(
							"Call to advice method " + method +
							" has wrong number of arguments: " + stmt						
							);
					}
				}
			}
			{
				Iterator it=interfaceInvokationStmts.iterator();
				
				while (it.hasNext()) {
					Stmt stmt=(Stmt)it.next();
					if (stmt.getInvokeExpr().getArgCount()!=interfaceInfo.abstractAccessMethod.getParameterCount() ) {
						throw new RuntimeException(
							"Call to interface method in advice method " + method + 
							" has wrong number of arguments: " + stmt
							);
					}
				}
			}
			{
				List accessMethodImplementations = getAllAccessMethods();
				Iterator it = accessMethodImplementations.iterator();
				while (it.hasNext()) {
					AccessMethod info = (AccessMethod) it.next();
					if (info.method.getParameterCount()!=interfaceInfo.abstractAccessMethod.getParameterCount()) {
						throw new RuntimeException(
							"Access method " + info.method + 
							" has wrong number of arguments."
							);
					}
				}	
			}
			
		}
		
		/**
		 * Called when a new access method has been added to a class.
		 * Looks at all other access methods of the advice method and 
		 * adds/changes super() calls where necessary
		 * @param interfaceName
		 * @param newAccessClass
		 */
		private void fixAccessMethodSuperCalls(SootClass newAccessClass) {
			//AdviceMethod adviceMethodInfo = state.getInterfaceInfo(interfaceName);
			
			if (!accessMethodImplementations.containsKey(newAccessClass.getName()))
				throw new InternalError();
			
			Set keys = accessMethodImplementations.keySet();
	
			boolean bAddSuperToNewMethod = false;
			{ // determine if the class that houses the new access method has any base classes 
				// that implement the method 
				Iterator it = keys.iterator();
				while (!bAddSuperToNewMethod && it.hasNext()) {
					String className = (String) it.next();
					SootClass cl = Scene.v().getSootClass(className);
					if (Util.isBaseClass(cl, newAccessClass)) {
						bAddSuperToNewMethod = true;
					}
				}
			}
			
			// Iterate over all classes that implement the interface
			Iterator it = keys.iterator();
			while (it.hasNext()) {
				String className = (String) it.next();
				AccessMethod accessInfo = (AccessMethod) accessMethodImplementations.get(className);
	
				SootClass cl = Scene.v().getSootClass(className);
				// if the class is a sub-class of the new class or 
				// if this is the new class and we need to add a super to the new class
				if (Util.isBaseClass(newAccessClass, cl) || 
					(className.equals(newAccessClass.getName()) && bAddSuperToNewMethod)) {
					if (accessInfo.superCallTarget == null
						|| // if the class has no super() call 
						Util.isBaseClass(accessInfo.superCallTarget, newAccessClass)) { // or if it's invalid
	
						// generate new super() call
						Body body = accessInfo.method.getActiveBody();
						Chain statements = body.getUnits().getNonPatchingChain();
						Type returnType = accessInfo.method.getReturnType();
	
						// find super class that implements the interface.
						// This is the target class of the super call.
						accessInfo.superCallTarget = cl.getSuperclass();
						while (!keys.contains(accessInfo.superCallTarget.getName())) {
							try {
								accessInfo.superCallTarget = accessInfo.superCallTarget.getSuperclass();
							} catch (RuntimeException e) {
								System.err.println("Class: " + accessInfo.superCallTarget);
								throw e;
							}
						}
	
						Util.removeStatements(body, accessInfo.defaultTarget, accessInfo.defaultEnd, null);
						LocalGeneratorEx lg = new LocalGeneratorEx(body);
						Local lThis = body.getThisLocal();
	
						String accessMethodName = accessInfo.method.getName();
						Restructure.validateMethod(accessInfo.method);
						SpecialInvokeExpr ex =
							Jimple.v().newSpecialInvokeExpr(lThis, accessInfo.superCallTarget.getMethodByName(accessMethodName), Util.getParameterLocals(body));
	
						if (returnType.equals(VoidType.v())) {
							Stmt s = Jimple.v().newInvokeStmt(ex);
							statements.insertBefore(s, accessInfo.defaultEnd);
							statements.insertBefore(Jimple.v().newReturnVoidStmt(), accessInfo.defaultEnd);
	
							accessInfo.superInvokeStmt = s;
						} else {
							Local l = lg.generateLocal(returnType, "retVal");
							AssignStmt s = Jimple.v().newAssignStmt(l, ex);
							statements.insertBefore(s, accessInfo.defaultEnd);
							statements.insertBefore(Jimple.v().newReturnStmt(l), accessInfo.defaultEnd);
	
							accessInfo.superInvokeStmt = s;
						}
						//accessInfo.hasSuperCall=true;	
					}
				}
	
			}
		}

		private void addParametersToAccessMethodImplementations(List addedDynArgsTypes) 
		{
			//Set keys=adviceMethodInfo.accessMethodImplementations.keySet();
			List accessMethodImplementations = getAllAccessMethods();
			Iterator it = accessMethodImplementations.iterator();
			while (it.hasNext()) {
				AccessMethod info = (AccessMethod) it.next();
	
				info.addParameters(addedDynArgsTypes);
			}
		}

		public int[] modifyAdviceMethod(List contextParameters, AccessMethod accessMethod, ObjectBox dynamicActualsResult, boolean bStatic) {
			//		determine parameter mappings and necessary additions
			List /*Type*/
			addedDynArgsTypes = new LinkedList();
			int[] argIndex = determineContextParameterMappings(contextParameters, addedDynArgsTypes);

			List dynamicActuals = getContextActualsList(contextParameters, argIndex);

			// create list of default values for the added arguments
			// (for invokations at other locations)
			List addedDynArgs = getDefaultValues(addedDynArgsTypes);

			addContextParamsToInterfaceDefinition(addedDynArgsTypes);
			modifyAdviceMethodInvokations(addedDynArgs);
			modifyDirectInterfaceInvokations(addedDynArgs);

			body.validate();

			List addedAdviceParameterLocals = addParameters(addedDynArgsTypes);

			body.validate();

			generateProceedCalls(bStatic, bAllwaysStaticAccessMethod, accessMethod);

			body.validate();

			modifyInterfaceInvokations(addedAdviceParameterLocals);

			body.validate();

			// add parameters to all access method implementations
			addParametersToAccessMethodImplementations(addedDynArgsTypes);
			
			dynamicActualsResult.object=dynamicActuals;
			return argIndex;			
		}
		private List addParameters(List addedDynArgsTypes) {
			List addedAdviceParameterLocals = new LinkedList();
			{ // Add the new parameters to the advice method 
				// and keep track of the newly created locals corresponding to the parameters.
				//validateMethod(adviceMethod);
				debug("adding parameters to advice method " + method);
				Iterator it = addedDynArgsTypes.iterator();
				while (it.hasNext()) {
					Type type = (Type) it.next();
					debug(" " + type);
					Local l = Restructure.addParameterToMethod(method, type, "dynArgFormal");
					addedAdviceParameterLocals.add(l);
				}
			}
			return addedAdviceParameterLocals;
		}
		private int[] determineContextParameterMappings(List context, List addedDynArgsTypes) {
			int[] argIndex = new int[context.size()];
			{
				int[] currentIndex = new int[Restructure.JavaTypeInfo.typeCount];
				Iterator it = context.iterator();
				int i = 0;
				while (it.hasNext()) {
					Local local = (Local) it.next();
					Type type = local.getType();
					// pass all reference types as java.lang.Object
					if (Restructure.JavaTypeInfo.sootTypeToInt(type) == Restructure.JavaTypeInfo.refType) {
						type = Scene.v().getRefType("java.lang.Object");
						if (type == null)
							throw new InternalError();
					}
					int typeNum = Restructure.JavaTypeInfo.sootTypeToInt(type);
					if (currentIndex[typeNum] < dynamicArgsByType[typeNum].size()) {
						Integer dynArgID = (Integer) dynamicArgsByType[typeNum].get(currentIndex[typeNum]);
						++currentIndex[typeNum];
						argIndex[i] = dynArgID.intValue();
					} else {
						addedDynArgsTypes.add(type);
						dynamicArguments.add(type);
						int newIndex = dynamicArguments.size() - 1;
						dynamicArgsByType[typeNum].add(new Integer(newIndex));
						argIndex[i] = newIndex;
						++currentIndex[typeNum];
					}
					i++;
				}
			}
			return argIndex;
		}

		private List getContextActualsList(List context, int[] argIndex) {
			List dynamicActuals = new LinkedList();
			{ // create list of dynamic actuals to add (including default values)
				Value[] parameters = new Value[dynamicArguments.size()];

				for (int i = 0; i < argIndex.length; i++) {
					parameters[argIndex[i]] = (Local) context.get(i);
				}
				for (int i = 0; i < parameters.length; i++) {
					if (parameters[i] == null) {
						parameters[i] = Restructure.JavaTypeInfo.getDefaultValue((Type) dynamicArguments.get(i));
					}
					dynamicActuals.add(parameters[i]);
				}
			}
			return dynamicActuals;
		}
		private SootClass createAccessInterface(List accessMethodParameters) {
			SootClass accessInterface;
			// create access interface if it doesn't exist
			if (Scene.v().containsClass(interfaceName)) {
				debug("found access interface in scene");
				accessInterface = Scene.v().getSootClass(interfaceName);
				//abstractAccessMethod=accessInterface.getMethodByName(dynamicAccessMethodName);
			} else {
				debug("generating access interface type " + interfaceName);

				accessInterface = new SootClass(interfaceName, Modifier.INTERFACE | Modifier.PUBLIC);

				accessInterface.setSuperclass(Scene.v().getSootClass("java.lang.Object"));

				SootMethod abstractAccessMethod =
					new SootMethod(dynamicAccessMethodName, accessMethodParameters, getReturnType(), Modifier.ABSTRACT | Modifier.PUBLIC);

				accessInterface.addMethod(abstractAccessMethod);
				//signature.setActiveBody(Jimple.v().newBody(signature));

				Scene.v().addClass(accessInterface);
				accessInterface.setApplicationClass();

				//GlobalAspectInfo.v().getGeneratedClasses().add(interfaceName);						 
			}
			return accessInterface;
		}

		private void generateProceedCalls(boolean bStatic, boolean bAllwaysStaticAccessMethod, AccessMethod accessMethod) {

			//AdviceMethod adviceMethodInfo = state.getInterfaceInfo(interfaceName);

			String newStaticInvoke = null;
			if (bStatic || bAllwaysStaticAccessMethod) {
				if (!staticProceedTypes.contains(accessMethod.joinpointClass.getName())) {
					newStaticInvoke = accessMethod.joinpointClass.getName();
					staticProceedTypes.add(accessMethod.joinpointClass.getName());
				} else
					return;
			} else {
				if (hasDynamicProceed)
					return;
				else
					hasDynamicProceed = true;
			}

			Iterator it = proceedInvokations.iterator();
			while (it.hasNext()) {
				AdviceMethod.ProceedInvokation invokation = (AdviceMethod.ProceedInvokation) it.next();

				Util.removeStatements(body, invokation.begin, invokation.end, null);

				List parameters = new LinkedList();
				parameters.addAll(invokation.originalActuals);
				parameters.addAll(implicitProceedParameters);
				if (invokation.dynamicInvoke == null && hasDynamicProceed) {
					InvokeExpr newInvokeExpr = Jimple.v().newInterfaceInvokeExpr(interfaceLocal, interfaceInfo.abstractAccessMethod, parameters);
					Stmt s;
					if (invokation.lhs == null) {
						s = Jimple.v().newInvokeStmt(newInvokeExpr);
					} else {
						s = Jimple.v().newAssignStmt(invokation.lhs, newInvokeExpr);
					}
					invokation.dynamicInvoke = s;
					interfaceInvokationStmts.add(s);
				}

				//List staticInvokes=new LinkedList();
				//List targets=new LinkedList();
				//Iterator it2=info.staticProceedTypes.iterator();
				if (newStaticInvoke != null) {
					SootClass cl = Scene.v().getSootClass(newStaticInvoke);
					SootMethod m = cl.getMethodByName(accessMethod.method.getName());

					invokation.staticLookupValues.add(IntConstant.v(state.getStaticDispatchTypeID(cl.getType())));

					InvokeExpr newInvokeExpr = Jimple.v().newStaticInvokeExpr(m, parameters);
					Stmt s;
					if (invokation.lhs == null) {
						s = Jimple.v().newInvokeStmt(newInvokeExpr);
					} else {
						s = Jimple.v().newAssignStmt(invokation.lhs, newInvokeExpr);
					}
					invokation.staticInvokes.add(s);
					interfaceInvokationStmts.add(s);
				}
				if (invokation.defaultTargetStmts == null) {
					//				generate exception code (default target)
					invokation.defaultTargetStmts = new LinkedList();
					LocalGeneratorEx lg = new LocalGeneratorEx(method.getActiveBody());
					SootClass exception = Scene.v().getSootClass("java.lang.RuntimeException");
					Local ex = lg.generateLocal(exception.getType(), "exception");
					Stmt newExceptStmt = Jimple.v().newAssignStmt(ex, Jimple.v().newNewExpr(exception.getType()));
					Stmt initEx = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(ex, exception.getMethod("<init>", new ArrayList())));
					Stmt throwStmt = Jimple.v().newThrowStmt(ex);
					invokation.defaultTargetStmts.add(newExceptStmt);
					invokation.defaultTargetStmts.add(initEx);
					invokation.defaultTargetStmts.add(throwStmt);
				}

				if (staticProceedTypes.isEmpty()) {
					statements.insertAfter(invokation.dynamicInvoke, invokation.begin);
				} else if (hasDynamicProceed == false && staticProceedTypes.size() == 1) {
					statements.insertAfter(invokation.staticInvokes.get(0), invokation.begin);
				} else {
					List targets = new LinkedList();
					List lookupValues = new LinkedList();
					if (invokation.dynamicInvoke != null) {
						targets.add(invokation.dynamicInvoke);
						lookupValues.add(IntConstant.v(0));
					}
					targets.addAll(invokation.staticInvokes);
					lookupValues.addAll(invokation.staticLookupValues);

					Local key = staticDispatchLocal; ///
					LookupSwitchStmt lookupStmt = Jimple.v().newLookupSwitchStmt(key, lookupValues, targets, (Unit) invokation.defaultTargetStmts.get(0));

					statements.insertBefore(lookupStmt, invokation.end);
					if (invokation.dynamicInvoke != null) {
						statements.insertBefore(invokation.dynamicInvoke, invokation.end);
						statements.insertBefore(Jimple.v().newGotoStmt(invokation.end), invokation.end);
					}

					Iterator it2 = invokation.staticInvokes.iterator();
					while (it2.hasNext()) {
						Stmt stmt = (Stmt) it2.next();
						statements.insertBefore(stmt, invokation.end);
						statements.insertBefore(Jimple.v().newGotoStmt(invokation.end), invokation.end);
					}
					it2 = invokation.defaultTargetStmts.iterator();
					while (it2.hasNext()) {
						Stmt stmt = (Stmt) it2.next();
						statements.insertBefore(stmt, invokation.end);
					}
					// just in case:
					statements.insertBefore(Jimple.v().newGotoStmt(invokation.end), invokation.end);
				}
			}
		}

		private void modifyInterfaceInvokations(List addedAdviceParameterLocals) {
			{ // Modify the interface invokations. These must all be in the advice method.
				// This constraint is violated by adviceexecution() pointcuts.
				Iterator it = interfaceInvokationStmts.iterator();
				while (it.hasNext()) {
					Stmt stmt = (Stmt) it.next();
					if (!statements.contains(stmt))
						throw new InternalError();
					//addEmptyDynamicParameters(method, addedDynArgsTypes, accessMethodName);
					//stmt.
					InvokeExpr intfInvoke = stmt.getInvokeExpr();
					List params = intfInvoke.getArgs();
					Iterator it2 = addedAdviceParameterLocals.iterator();
					while (it2.hasNext()) {
						Local l = (Local) it2.next();
						params.add(l);
					}
					InvokeExpr newInvoke = Util.createNewInvokeExpr(intfInvoke, params);
					//debug("newInvoke: " + newInvoke);
					stmt.getInvokeExprBox().setValue(newInvoke);
					//debug("newInvoke2" + stmt.getInvokeExpr());
				}

				AdviceMethod adviceMethodInfo1 = state.getAdviceMethod(method);

				adviceMethodInfo1.implicitProceedParameters.addAll(addedAdviceParameterLocals);
			}
		}

		private void modifyDirectInterfaceInvokations(List addedDynArgs) {
			{ // modify all existing direct interface invokations by adding the default parameters
				Iterator it = directInvokationStmts.iterator();
				while (it.hasNext()) {
					Stmt stmt = (Stmt) it.next();
					//addEmptyDynamicParameters(method, addedDynArgs, accessMethodName);
					InvokeExpr invoke = (InvokeExpr) stmt.getInvokeExprBox().getValue();
					List newParams = invoke.getArgs();
					newParams.addAll(addedDynArgs); /// should we do deep copy?	
					InvokeExpr newInvoke = Util.createNewInvokeExpr(invoke, newParams);
					stmt.getInvokeExprBox().setValue(newInvoke);
				}
			}
		}
		private void modifyAdviceMethodInvokations(List addedDynArgs) {
			{ // modify all existing advice method invokations by adding the default parameters
				Iterator it = adviceMethodInvokationStmts.iterator();
				while (it.hasNext()) {
					Stmt stmt = (Stmt) it.next();
					//addEmptyDynamicParameters(method, addedDynArgs, accessMethodName);
					InvokeExpr invoke = (InvokeExpr) stmt.getInvokeExprBox().getValue();
					List newParams = invoke.getArgs();
					newParams.addAll(addedDynArgs); /// should we do deep copy?	
					InvokeExpr newInvoke = Util.createNewInvokeExpr(invoke, newParams);
					stmt.getInvokeExprBox().setValue(newInvoke);
				}
			}
		}
		private void addContextParamsToInterfaceDefinition(List addedDynArgsTypes) {
			{ // modify the interface definition
				SootMethod m = interfaceInfo.abstractAccessMethod;
				List p = m.getParameterTypes();
				p.addAll(addedDynArgsTypes);
				m.setParameterTypes(p);
			}
		}
		public Type getReturnType() {
			return method.getReturnType();
		}
		private void doInitialAdviceMethodModification() {
			debug("modifying advice method: " + method.toString());

			Restructure.validateMethod(method);

			Local lInterface = Restructure.addParameterToMethod(method, interfaceInfo.accessInterface.getType(), "accessInterface");
			//Local lTarget=Restructure.addParameterToMethod(adviceMethod, 
			//		Scene.v().getSootClass("java.lang.Object").getType(), "targetArg");
			Local lShadowID = Restructure.addParameterToMethod(method, IntType.v(), "shadowID");
			Local lStaticClassID = Restructure.addParameterToMethod(method, IntType.v(), "staticClassID");

			Restructure.validateMethod(method);

			//adviceMethodInfo.proceedParameters.add(lTarget);
			implicitProceedParameters.add(lShadowID);
			implicitProceedParameters.add(IntConstant.v(0));
			// skipAdvice parameter

			interfaceLocal = lInterface;
			//adviceMethodInfo.targetLocal=lTarget;
			idLocal = lShadowID;
			staticDispatchLocal = lStaticClassID;

			Set proceedActuals = new HashSet();

			Iterator it = statements.snapshotIterator();
			while (it.hasNext()) {
				Stmt s = (Stmt) it.next();
				InvokeExpr invokeEx;
				try {
					invokeEx = s.getInvokeExpr();
				} catch (Exception ex) {
					invokeEx = null;
				}

				if (invokeEx != null) {
					if (invokeEx.getMethod().getName().startsWith("proceed$")) {

						// check if changed values are passed to proceed.
						for (int i = 0; i < invokeEx.getArgCount(); i++) {
							Value v = invokeEx.getArg(i);
							debug("proceed$ arg " + i + ": " + v);
							proceedActuals.add(v);
							/*if (!v.equals(adviceBody.getParameterLocal(i))) {
								throw new CodeGenException(
									"Passing modified values to proceed is not yet implemented. \n" +
									" Aspect: " + adviceMethod.getDeclaringClass().getName() + "\n" +
									" Advice method: " + adviceMethod.getName() + "\n" +  
									" Argument " + i + "\n" + 
									" Statement: " + s.toString() 
									 );
							}*/
						}
						AdviceMethod.ProceedInvokation invokation = new AdviceMethod.ProceedInvokation();
						proceedInvokations.add(invokation);

						invokation.originalActuals.addAll(invokeEx.getArgs());

						invokation.begin = Jimple.v().newNopStmt();
						invokation.end = Jimple.v().newNopStmt();
						if (s instanceof AssignStmt) {
							invokation.lhs = (Local) (((AssignStmt) s).getLeftOp());
						}
						statements.insertBefore(invokation.begin, s);
						statements.insertAfter(invokation.end, s);
						s.redirectJumpsToThisTo(invokation.begin);
						statements.remove(s);
					}
				}
			}
			it = statements.iterator();
			while (it.hasNext()) {
				Stmt s = (Stmt) it.next();

				if (s instanceof AssignStmt) {
					//AssignStmt assign = (AssignStmt) s;
					/*if (proceedActuals.contains(assign.getLeftOp())) {
						throw new CodeGenException(
							"Passing modified values to proceed is not yet implemented. \n" +
							"Found assignment to local passed to proceed. \n" +
							" Aspect: " + adviceMethod.getDeclaringClass().getName() + "\n" +
							" Advice method: " + adviceMethod.getName() + "\n" +  
							" Statement: " + s.toString() 
							 );		
					}*/
				}
			}
		}

		
		public static class InterfaceInfo {
			SootClass accessInterface;
			SootMethod abstractAccessMethod;
		}
		InterfaceInfo interfaceInfo = null;

		final String dynamicAccessMethodName;
		final String interfaceName;
		final String adviceMethodIdentifierString;
		final List /*type*/ accessMethodParameterTypes=new LinkedList();

		SootClass getAspect() {
			return method.getDeclaringClass();
		}

		public final SootMethod method;
		public final AdviceDecl adviceDecl;
		public final Body body;
		public final Chain statements;

		final public List originalAdviceFormalTypes = new LinkedList();
		final public List /*Value*/ implicitProceedParameters = new LinkedList();
		public Local interfaceLocal;
		//public Local targetLocal;
		public Local idLocal;
		public Local staticDispatchLocal;
		final public HashSet /*String*/ staticProceedTypes = new HashSet();
		public boolean hasDynamicProceed = false;
		public final boolean bAllwaysStaticAccessMethod = false;//true; //false;

		public static class ProceedInvokation {
			public Local lhs;
			public NopStmt begin;
			public NopStmt end;

			//List lookupValues=new LinkedList();
			List defaultTargetStmts;
			//Stmt lookupStmt;
			List staticInvokes = new LinkedList();
			List staticLookupValues = new LinkedList();

			Stmt dynamicInvoke;

			List originalActuals = new LinkedList();
		}
		final private List proceedInvokations = new LinkedList();

		final public Set interfaceInvokationStmts = new HashSet();
		final public Set adviceMethodInvokationStmts = new HashSet();
		final public Set directInvokationStmts = new HashSet();
		//public Set superInvokationStmts=new HashSet();
		//public HashMap /*InvokeExpr, ValueBox*/ invokationBoxes=new HashMap();
		final List /*Type*/ dynamicArguments = new LinkedList();

		final List[] dynamicArgsByType = new List[Restructure.JavaTypeInfo.typeCount];

		AdviceMethod(SootMethod method, AdviceDecl adviceDecl) {
			this.method = method;
			this.body = method.getActiveBody();
			this.statements = body.getUnits().getNonPatchingChain();
			this.adviceDecl = adviceDecl;
			for (int i = 0; i < dynamicArgsByType.length; i++) {
				dynamicArgsByType[i] = new LinkedList();
			}

			//String adviceReturnTypeName = getReturnType().toString();
			//String adviceMangledReturnTypeName = Util.mangleTypeName(adviceReturnTypeName);
			//String accessTypeString= bGet ? "get" : "set";

			String aspectName = getAspect().getName();
			String mangledAspectName = Util.mangleTypeName(aspectName);

			adviceMethodIdentifierString = mangledAspectName + "$" + method.getName();

			interfaceName = "abc$access$" + adviceMethodIdentifierString;

			dynamicAccessMethodName = "abc$proceed$" + adviceMethodIdentifierString;
			;

			{ // store original advice formals in adviceMethodInfo

				if (!originalAdviceFormalTypes.isEmpty())
					throw new InternalError();

				Iterator it = adviceDecl.getImpl().getFormals().iterator();
				while (it.hasNext()) {
					Formal formal = (Formal) it.next();
					originalAdviceFormalTypes.add(formal.getType().getSootType());
					//formal.
				}
				// TODO: clean up the following 7 lines
				int size = originalAdviceFormalTypes.size();
				if (adviceDecl.hasEnclosingJoinPoint())
					originalAdviceFormalTypes.remove(--size);
				if (adviceDecl.hasJoinPoint())
					originalAdviceFormalTypes.remove(--size);
				if (adviceDecl.hasJoinPointStaticPart())
					originalAdviceFormalTypes.remove(--size);

				//adviceMethodInfo.originalAdviceFormals.addAll(adviceMethod.getParameterTypes());
			}

			accessMethodParameterTypes.add(IntType.v()); // the shadow id
			accessMethodParameterTypes.add(BooleanType.v()); // the skip flag

			{
				List allAccessMethodParameters = new LinkedList();
				allAccessMethodParameters.addAll(originalAdviceFormalTypes);
				allAccessMethodParameters.addAll(accessMethodParameterTypes);

				interfaceInfo = new InterfaceInfo();

				interfaceInfo.accessInterface = createAccessInterface(allAccessMethodParameters);
				interfaceInfo.abstractAccessMethod = interfaceInfo.accessInterface.getMethodByName(dynamicAccessMethodName);
			}			
			//			Change advice method: add parameters and replace proceed				
			doInitialAdviceMethodModification();
		}
		
		public List getAllAccessMethods() {
			List result = new LinkedList();
			result.addAll(accessMethodImplementations.values());
			result.addAll(accessMethodImplementationsStatic.values());
			return result;
		}
		public AccessMethod getAccessMethod(String className, boolean bStatic) {
			if (bStatic) {
				return (AccessMethod) accessMethodImplementationsStatic.get(className);
			} else {
				return (AccessMethod) accessMethodImplementations.get(className);
			}
		}
		public void setAccessMethod(String className, boolean bStatic, AccessMethod accessMethod) {
			if (bStatic) {
				accessMethodImplementationsStatic.put(className, accessMethod);
			} else {
				accessMethodImplementations.put(className, accessMethod);
			}
		}
		final private HashMap /*String, AccessMethodInfo*/ accessMethodImplementations = new HashMap();
		final private HashMap /*String, AccessMethodInfo*/ accessMethodImplementationsStatic = new HashMap();

		public int getUniqueShadowID() {
			return currentUniqueShadowID++;
		}
		int currentUniqueShadowID;
	}
	public static class AccessMethod {
		public void modifyLookupStatement(Stmt switchTarget, int shadowID) {
			// modify the lookup statement in the access method
			lookupValues.add(IntConstant.v(shadowID));
			targets.add(switchTarget);
			// generate new lookup statement and replace the old one
			Stmt newLookupStmt =
				Jimple.v().newLookupSwitchStmt(
					idParamLocal,
					lookupValues,
					targets,
					defaultTarget);
			statements.insertAfter(newLookupStmt, lookupStmt);
			statements.remove(lookupStmt);
			lookupStmt = newLookupStmt;

			if (!bStaticAccessMethod) {
				 adviceMethod.fixAccessMethodSuperCalls(joinpointClass);
			}

			Util.cleanLocals(body);			
		}
		//HashMap /*String, Integer*/ fieldIDs=new HashMap();
		private void addParameters(List addedDynArgsTypes)  {
			debug("adding parameters to access method " + method);
			Restructure.validateMethod(method);

			Iterator it2 = addedDynArgsTypes.iterator();
			while (it2.hasNext()) {
				Type type = (Type) it2.next();
				debug(" " + type);
				Local l = Restructure.addParameterToMethod(method, type, "dynArgFormal");
				dynParamLocals.add(l);
			}

			//				modify existing super call in the access method		
			Stmt stmt = superInvokeStmt;
			if (stmt != null) {
				//addEmptyDynamicParameters(method, addedDynArgs, accessMethodName);
				InvokeExpr invoke = (InvokeExpr) stmt.getInvokeExprBox().getValue();
				List newParams = new LinkedList();
				newParams.addAll(Util.getParameterLocals(method.getActiveBody()));
				/// should we do deep copy?	
				InvokeExpr newInvoke = Util.createNewInvokeExpr(invoke, newParams);
				stmt.getInvokeExprBox().setValue(newInvoke);
			}
		}
		private void assignCorrectParametersToLocals(
			List context,
			int[] argIndex,
			Stmt first,
			HashMap bindings,
			ArrayList staticBindings) {
		
			debug("Access method: assigning correct parameters to locals*********************");
		
			// Assign the correct access parameters to the locals 
			Stmt insertionPoint = (Stmt) first;
			Stmt skippedCase = Jimple.v().newNopStmt();
			Stmt nonSkippedCase = Jimple.v().newNopStmt();
			Stmt neverBoundCase = Jimple.v().newNopStmt();
			Stmt gotoStmt = Jimple.v().newGotoStmt(neverBoundCase);
			Stmt ifStmt = Jimple.v().newIfStmt(Jimple.v().newEqExpr(skipParamLocal, IntConstant.v(1)), skippedCase);
			statements.insertBefore(ifStmt, insertionPoint);
			statements.insertBefore(nonSkippedCase, insertionPoint);
			statements.insertBefore(gotoStmt, insertionPoint);
			statements.insertBefore(skippedCase, insertionPoint);
			statements.insertBefore(neverBoundCase, insertionPoint);
			int i=0;
			for (Iterator it=context.iterator(); it.hasNext(); i++) {
				Local actual = (Local) it.next(); // context.get(i);
				Local actual2 = (Local) bindings.get(actual);
				if (!body.getLocals().contains(actual2))
					throw new InternalError();
				if (actual2 == null)
					throw new InternalError();

				Restructure.validateMethod(method);
				
				

				Local paramLocal;
				if (staticBindings.contains(actual)) {
					debug(" static binding: " + actual.getName());
					// We use lastIndexOf here to mimic ajc's behavior:
					// When binding the same value multiple times, ajc's
					// proceed only regards the last one passed to it.
					// Can be changed to indexOf to pick the first one 
					// (which would seem more reasonable). 
					int index = staticBindings.lastIndexOf(actual);

					{ // non-skipped case: assign advice formal
						paramLocal = (Local) adviceFormalLocals.get(index);
						AssignStmt s = Jimple.v().newAssignStmt(actual2, paramLocal);
						statements.insertAfter(s, nonSkippedCase);
						Restructure.insertBoxingCast(method.getActiveBody(), s, true);
						/// allow boxing?
					}
					{ // skipped case: assign dynamic argument
						paramLocal = (Local) dynParamLocals.get(argIndex[i]);
						AssignStmt s = Jimple.v().newAssignStmt(actual2, paramLocal);
						statements.insertAfter(s, skippedCase);
						Restructure.insertBoxingCast(method.getActiveBody(), s, true);
						/// allow boxing?
					}
				} else {
					debug(" no binding: " + actual.getName());
					// no binding
					paramLocal = (Local) dynParamLocals.get(argIndex[i]);
					AssignStmt s = Jimple.v().newAssignStmt(actual2, paramLocal);
					statements.insertAfter(s, neverBoundCase);
					insertCast(method.getActiveBody(), s, s.getRightOpBox(), actual2.getType());
				}
			}
		}
	

		public final AdviceMethod adviceMethod;
		public final SootClass joinpointClass;
		public final boolean bStaticAccessMethod;
		public final Body body;
		public final Chain statements;
			
		AccessMethod(AdviceMethod parent, SootClass joinpointClass, boolean bStaticAccessMethod, String accessMethodName) {
			this.bStaticAccessMethod=bStaticAccessMethod;
			this.adviceMethod = parent;
			this.joinpointClass = joinpointClass;

			String interfaceName = adviceMethod.interfaceInfo.accessInterface.getName();

			if (bStaticAccessMethod) {
					method = new SootMethod(accessMethodName, new LinkedList(), adviceMethod.getReturnType(), Modifier.PUBLIC | Modifier.STATIC);
			} else {
				debug("adding interface " + interfaceName + " to class " + joinpointClass.getName());

				joinpointClass.addInterface(adviceMethod.interfaceInfo.accessInterface);

				// create new method					
				method = new SootMethod(accessMethodName, new LinkedList(), adviceMethod.getReturnType(), Modifier.PUBLIC);
			}
			Body accessBody = Jimple.v().newBody(method);

			method.setActiveBody(accessBody);
			debug("adding method " + method.getName() + " to class " + joinpointClass.getName());
			joinpointClass.addMethod(method);

			Chain accessStatements = accessBody.getUnits().getNonPatchingChain();

			// generate this := @this
			LocalGeneratorEx lg = new LocalGeneratorEx(accessBody);
			Local lThis = null;
			if (!bStaticAccessMethod) {
				lThis = lg.generateLocal(joinpointClass.getType(), "this");
				accessStatements.addFirst(Jimple.v().newIdentityStmt(lThis, Jimple.v().newThisRef(RefType.v(joinpointClass))));
			}
			Restructure.validateMethod(method);
			//accessMethodInfo.targetLocal=Restructure.addParameterToMethod(
			//	accessMethod, (Type)accessMethodParameters.get(0), "targetArg");

			{
				Iterator it = adviceMethod.originalAdviceFormalTypes.iterator();
				while (it.hasNext()) {
					Type type = (Type) it.next();
					Local l = Restructure.addParameterToMethod(method, type, "orgAdviceFormal");
					adviceFormalLocals.add(l);
				}
			}
			Restructure.validateMethod(method);

			idParamLocal = Restructure.addParameterToMethod(method, (Type) adviceMethod.accessMethodParameterTypes.get(0), "shadowID");
			skipParamLocal = Restructure.addParameterToMethod(method, (Type) adviceMethod.accessMethodParameterTypes.get(1), "skipAdvice");

			if (adviceMethod.accessMethodParameterTypes.size() != 2)
				throw new InternalError();

			Stmt lastIDStmt = Restructure.getParameterIdentityStatement(method, method.getParameterCount() - 1);

			// generate exception code (default target)
			SootClass exception = Scene.v().getSootClass("java.lang.RuntimeException");
			Local ex = lg.generateLocal(exception.getType(), "exception");
			Stmt newExceptStmt = Jimple.v().newAssignStmt(ex, Jimple.v().newNewExpr(exception.getType()));
			Stmt initEx = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(ex, exception.getMethod("<init>", new ArrayList())));
			Stmt throwStmt = Jimple.v().newThrowStmt(ex);

			defaultTarget = Jimple.v().newNopStmt();
			accessStatements.add(defaultTarget);
			accessStatements.add(newExceptStmt);
			accessStatements.add(initEx);
			accessStatements.add(throwStmt);
			defaultEnd = Jimple.v().newNopStmt();
			accessStatements.add(defaultEnd);

			// just generate a nop for now.
			lookupStmt = Jimple.v().newNopStmt();

			accessStatements.insertAfter(lookupStmt, lastIDStmt);

			//AdviceMethod adviceMethodInfo = state.getInterfaceInfo(interfaceName);
			Iterator it = adviceMethod.dynamicArguments.iterator();
			while (it.hasNext()) {
				Type type = (Type) it.next();
				Local l = Restructure.addParameterToMethod(method, type, "dynArgFormal");
				dynParamLocals.add(l);
			}

			// fixSuperCalls used to be here...

			Restructure.validateMethod(method);

			body=method.getActiveBody();
			statements=body.getUnits().getNonPatchingChain();
		}

		final List targets = new LinkedList();
		final List lookupValues = new LinkedList();
		final NopStmt defaultTarget;
		final NopStmt defaultEnd;
		Stmt lookupStmt;
		int nextID;
		public final Local idParamLocal;
		public final Local skipParamLocal;
		//Local targetLocal;

		final List dynParamLocals = new LinkedList();
		final List adviceFormalLocals = new LinkedList();

		public final SootMethod method;

		//boolean hasSuperCall=false;
		SootClass superCallTarget = null;
		Stmt superInvokeStmt = null;

		/*public boolean implementsField(String field) {
			return fieldIDs.containsKey(field);
		}*/
	}

	//private HashMap /*ClassInterfacePair, AccessMethodInfo*/ 
	//	accessInterfacesImplementations=new HashMap();
	/*public boolean hasInterface(String className, String interfaceName) {
		ClassInterfacePair key=new ClassInterfacePair(className, interfaceName);
		return accessInterfacesGet.containsKey(key);
	}*/

	public static class State {
		private void validate() {
			Iterator it=adviceMethods.values().iterator();
			while (it.hasNext()) {
				AdviceMethod method=(AdviceMethod) it.next();
				method.validate();
			}
		}
		/*private static class ClassInterfacePair {
			String className;
			String interfaceName;		
			ClassInterfacePair(String className, String interfaceName) {
				this.className=className;
				this.interfaceName=interfaceName;
			}
		
			public boolean equals(Object arg0) {
				if (!(arg0 instanceof ClassInterfacePair))
					return false;
				ClassInterfacePair rhs=(ClassInterfacePair)arg0;
				return className.equals(rhs.className) && 
						interfaceName.equals(rhs.interfaceName);
			}
			public int hashCode() {
				return className.hashCode()+interfaceName.hashCode();
			}
		}*/

		/*public AccessMethodInfo getAccessMethodInfo(
			String aspectName,
			String 
			String className,
			String interfaceName,
			boolean bStatic) {
			AdviceMethod info = getAdviceMethod(className, interfaceName, bStatic);
			return info.getAccessMethodInfo(className, bStatic);
		}
		*/

		//public HashMap /*String, AdviceMethod*/
		//interfaces = new HashMap();
		/*public AdviceMethod getInterfaceInfo(String interfaceName) {
			if (!interfaces.containsKey(interfaceName)) {
				interfaces.put(interfaceName, new AdviceMethod());
			}
			return (AdviceMethod) interfaces.get(interfaceName);
		}*/
		public int getUniqueID() {
			return currentUniqueID++;
		}
		int currentUniqueID;

		final private HashMap /* AdviceApplication,  */ adviceApplications = new HashMap();
		/*AdviceApplicationInfo getApplicationInfo(AdviceApplication app) {
			if (!adviceApplications.containsKey(app)) {
				adviceApplications.put(app, new AdviceApplicationInfo());
			}
			return (AdviceApplicationInfo) adviceApplications.get(app);
		}*/
		public int getStaticDispatchTypeID(Type type) {
			String name = type.toString();
			if (!staticDispatchTypeIDs.containsKey(name)) {
				staticDispatchTypeIDs.put(name, new Integer(nextStaticTypeDispatchID++));
			}
			return ((Integer) staticDispatchTypeIDs.get(name)).intValue();
		}
		int nextStaticTypeDispatchID = 1; // 0 is a special value
		final HashMap /*String, int*/ staticDispatchTypeIDs = new HashMap();

		final private HashMap /* String, AdviceMethod */ adviceMethods = new HashMap();
		void setAdviceMethod(SootMethod adviceMethod, AdviceMethod m) {
			adviceMethods.put(adviceMethod, m);
		}
		AdviceMethod getAdviceMethod(SootMethod adviceMethod) {
			if (!adviceMethods.containsKey(adviceMethod)) {
				return null;
			}
			return (AdviceMethod) adviceMethods.get(adviceMethod);
		}
	}
	public static State state = new State();

	public static void doWeave(SootClass joinpointClass, SootMethod joinpointMethod, LocalGeneratorEx localgen, AdviceApplication adviceAppl) {
		debug("Weaving advice application: " + adviceAppl);
		//if (joinpointClass!=null)
		//	return;		
		AdviceApplicationInfo adviceApplication=new AdviceApplicationInfo(adviceAppl, joinpointMethod);
		adviceApplication.doWeave();
		//state.validate();
	}

	
	
	


	private static void insertCast(Body body, Stmt stmt, ValueBox source, Type targetType) {
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

	private static void updateSavedReferencesToStatements(HashMap bindings) {
		Collection values = state.adviceMethods.values();
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
				if (adviceMethodInfo.adviceMethodInvokationStmts.contains(old)) {
					adviceMethodInfo.adviceMethodInvokationStmts.remove(old);
					adviceMethodInfo.adviceMethodInvokationStmts.add(bindings.get(old));
					// replace with new
				}
				// this is only necessary if proceed calls are ever part of a shadow,
				// for example if the advice body were to be matched by an adviceexecution pointcut. 
				// TODO: does this kind of thing ever happen?
				if (adviceMethodInfo.interfaceInvokationStmts.contains(old)) {
					adviceMethodInfo.interfaceInvokationStmts.remove(old);
					adviceMethodInfo.interfaceInvokationStmts.add(bindings.get(old));
					// replace with new
				}
				if (adviceMethodInfo.directInvokationStmts.contains(old)) {
					adviceMethodInfo.directInvokationStmts.remove(old);
					adviceMethodInfo.directInvokationStmts.add(bindings.get(old));
				}
			}
		}
	}

	private static List getDefaultValues(List types) {
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

	private static List /*Residue*/ getBindList(Residue r) {
		// explicitly go through all the options to force early
		// errors when a new one gets added, to make sure we have
		// thought about it properly. This should all be delegated
		// in the future.
		if (r instanceof AlwaysMatch) {

		} else if (r instanceof AndResidue) {
			List l = getBindList(((AndResidue) r).getLeftOp());
			l.addAll(getBindList(((AndResidue) r).getRightOp()));
			return l;
		} else if (r instanceof AspectOf) {

		} else if (r instanceof Bind) {
			Bind bind = (Bind) r;
			List l = new LinkedList();
			l.add(r);
			return l;
		} else if (r instanceof Box) {

		} else if (r instanceof Load) {

		} else if (r instanceof CheckType) {

		} else if (r instanceof Copy) {

		} else if (r instanceof HasAspect) {

		} else if (r instanceof IfResidue) {

		} else if (r instanceof NeverMatch) {

		} else if (r instanceof NotResidue) {
			return getBindList(((NotResidue) r).getOp());
		} else if (r instanceof OrResidue) {
			List l = getBindList(((OrResidue) r).getLeftOp());
			l.addAll(getBindList(((OrResidue) r).getRightOp()));
			return l;
		} else if (r instanceof CflowResidue) {
			
		} else {
			throw new InternalError("Unknown residue type: " + r.getClass().getName());
		}
		return new LinkedList();
	}

	
}

// find out what kind of pointcut 
/*if (adviceAppl instanceof StmtAdviceApplication) {
	debug("found statement advice application");
	StmtAdviceApplication stmtAdv=(StmtAdviceApplication) adviceAppl;

	if (adviceAppl.sjpInfo.kind.equals("field-get") ||
		adviceAppl.sjpInfo.kind.equals("method-call")  ) {
		debug("found " + adviceAppl.sjpInfo.kind);
		if (!(stmtAdv.stmt instanceof AssignStmt)) {
			throw new CodeGenException(
				"StmtAdviceApplication.stmt is expected to be instanceof AssignStmt");  
		}
		debug("found assignment statement");
		AssignStmt assignStmt=(AssignStmt)stmtAdv.stmt;				
								
	} else {
		debug("NYI: type of stmt advice application " + adviceAppl);
	}			
} else if (adviceAppl instanceof ExecutionAdviceApplication) {
	debug("NYI: execution advice application: " + adviceAppl);
} else {
	debug("NYI: advice application: " + adviceAppl);
}*/

/*
adviceMethodParameters.add();
int interfaceParam=adviceMethodParameters.size()-1;
adviceMethodParameters.add(IntType.v());			
int idParam=adviceMethodParameters.size()-1;	
adviceMethodParameters.add(IntType.v());
int staticDispatchParam=adviceMethodParameters.size()-1;
		
adviceMethod.setParameterTypes(adviceMethodParameters);
		
		
debug("id1: " + interfaceParam);
debug("id2: " + idParam);
debug("count:" + adviceMethod.getParameterCount());
	
Stmt lastExistingIDStmt=(Stmt)statements.getFirst();
	
if (adviceAppl.advice.numFormals()>0){		
	Iterator it=statements.iterator();
	for (int i=0; i<adviceAppl.advice.numFormals()+1; i++) {
		lastExistingIDStmt=(Stmt)it.next();
	}
	if (lastExistingIDStmt==null)
		throw new InternalError();

	IdentityStmt id=(IdentityStmt)lastExistingIDStmt;
	
	Local local=(Local)id.getLeftOp();

	// local.

	if ( local!=aroundBody.getParameterLocal(0))
		throw new InternalError();

	debug("param:" + aroundBody.getParameterLocal(0).getType());
	//if (((IdentityStmt)lastExistingIDStmt).getRightOp().)
}
		
LocalGeneratorEx localgen2 = new LocalGeneratorEx(aroundBody);
Local lInterface=localgen2.generateLocal(accessInterface.getType());//, "accessIntf");
// insert id for first param (interface reference)
Stmt intRefIDstmt=Jimple.v().newIdentityStmt(lInterface, 
		Jimple.v().newParameterRef(	
				accessInterface.getType(),interfaceParam));
statements.insertAfter(intRefIDstmt, lastExistingIDStmt);
// id for second param (id of field accessed)
Local l2=localgen2.generateLocal(IntType.v());//, "id");
Stmt fieldIDStmt=Jimple.v().newIdentityStmt(l2, 
		Jimple.v().newParameterRef(IntType.v(),idParam));
statements.insertAfter(fieldIDStmt, intRefIDstmt);
// id for third param (value for set operation)
Local l3=localgen2.generateLocal(IntType.v());//, "id");
Stmt staticDispatchStmt=Jimple.v().newIdentityStmt(l3, 
		Jimple.v().newParameterRef(IntType.v(),staticDispatchParam));
statements.insertAfter(staticDispatchStmt, fieldIDStmt);
*/
// set from constant.
// Add an assignment to a local before stmt 
/*LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);
Local l=lg.generateLocal(((Constant)rightOp).getType(), "setTmp");
AssignStmt s=Jimple.v().newAssignStmt(l, rightOp);
joinpointChain.insertBefore(s, adviceAppl.shadowpoints.getBegin());
assignStmt.setRightOp(l);
actuals.add(assignStmt.getRightOpBox());
actualsTypes.add(((FieldRef)leftOp).getType());
//throw new CodeGenException("Can't handle assignment from constant yet");
if (leftOp instanceof InstanceFieldRef) {
	InstanceFieldRef ix=(InstanceFieldRef) leftOp;
	theTarget=ix.getBaseBox();
}*/

/*if (invokeTarget!=null) {
			invokeTarget.setValue(invokeEx2);
		} else {
			if (assignStmt==null)
				throw new InternalError(); // must/should never be reached
			// set()
			// replace old "fieldref=local" with invokation 
			invokeStmt=Jimple.v().newInvokeStmt(invokeEx2);
			joinpointStatements.insertAfter(invokeStmt, assignStmt);
			joinpointStatements.remove(assignStmt);			
			stmtAppl.stmt=invokeStmt;
			invokeTarget=invokeStmt.getInvokeExprBox();
			//Jimple.v().newEqExpr()
		}*/
//				call (void)
/*invokeStmt=(InvokeStmt) applStmt;
InvokeExpr invokeEx=invokeStmt.getInvokeExpr();
for (int i=0; i<invokeEx.getArgCount(); i++)
	 actuals.add(invokeEx.getArgBox(i));
invokeTarget=invokeStmt.getInvokeExprBox();
actualsTypes=invokeEx.getMethod().getParameterTypes();
if (invokeEx instanceof InstanceInvokeExpr) {
	 InstanceInvokeExpr ix=(InstanceInvokeExpr) invokeEx;
	 theTarget=ix.getBaseBox();
}*/
/*Local targetLocal;
		 { // create a local for the target
			 LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);
			 AssignStmt s;
			 if (theTarget!=null) {
				 targetLocal=lg.generateLocal(theTarget.getValue().getType(), "targetArg");
				 s=Jimple.v().newAssignStmt(targetLocal, theTarget.getValue());
				 theTarget.setValue(targetLocal);
			 } else {
				 targetLocal=lg.generateLocal(
					 Scene.v().getSootClass("java.lang.Object").getType(),
						  "targetArg");
				 s=Jimple.v().newAssignStmt(targetLocal, NullConstant.v());
			 }
			 joinpointStatements.insertBefore(s, stmtAppl.shadowpoints.getBegin());
		 }*/

/*Local interfaceLocal=null;
if (!bStatic) {
	 LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);

	 interfaceLocal=lg.generateLocal(accessInterface.getType(), "closureType");
	 AssignStmt s=Jimple.v().newAssignStmt(interfaceLocal, 
		 Jimple.v().newCastExpr(Restructure.getThisCopy(joinpointMethod),
					 accessInterface.getType()));
	 joinpointStatements.insertBefore(s, begin);
}*/
/* if (rightOp instanceof InvokeExpr) {
					 // call
					 InvokeExpr invokeEx=(InvokeExpr)rightOp;
					 for (int i=0; i<invokeEx.getArgCount(); i++)
						 actuals.add(invokeEx.getArgBox(i));

					 actualsTypes=invokeEx.getMethod().getParameterTypes();

					 if (rightOp instanceof InstanceInvokeExpr) {
						 InstanceInvokeExpr ix=(InstanceInvokeExpr) rightOp;
						 theTarget=ix.getBaseBox();
					 }
				  } else if (rightOp instanceof FieldRef) {
					 // get
					 // no actuals
					 if (rightOp instanceof InstanceFieldRef) {
						 InstanceFieldRef ix=(InstanceFieldRef) rightOp;
						 theTarget=ix.getBaseBox();
					 }
				  }*/
//									At the advice application statement, extract any parameters into locals.
/*Local[] generatedLocals=new Local[actuals.size()];
{  
	Iterator it=actuals.iterator();
	//Iterator it2=actualsTypes.iterator();
	LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);
	int i=0;
	while (it.hasNext()) {
		//ValueBox box=(ValueBox)it.next();
		Local local=(Local)it.next();
		Type type=local.getType();//(Type)it2.next();
		//Value val=box.getValue();
		String name="dynArg" + argIndex[i] + "act";
		Local l=lg.generateLocal(type, name);
		AssignStmt s=Jimple.v().newAssignStmt(l, local);
		joinpointStatements.insertBefore(s, stmtAppl.shadowpoints.getBegin());	
		box.setValue(l);
		generatedLocals[i]=l;
		i++;
	} 			
}*/
