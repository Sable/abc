/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Damien Sereni
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

import soot.*;
import soot.util.*;
import soot.jimple.*;

import java.util.*;

import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import abc.main.Debug;
import abc.main.options.OptionsParser;

/**
 * A class for generating code for the cflow operations. The cflow operations are:
 * <p> push(locals)
 * <p> pop()
 * <p> peek()
 * <p> isValid()
 * <p> The nested (abstract) class CflowCodeGen encapsulates the code generation
 *     functions for these operations. Instances of CflowCodeGen are constructed
 *     using the nested CflowCodeGenFactory class, which picks the appropriate 
 *     flavour of cflow codegen. 
 *
 * @author Damien Sereni
 */
public class CflowCodeGenUtils {

	// Cflow Classes
	// Old-style runtime library
	private static final String CLASS_OLD_STACK = 
		"org.aspectbench.runtime.internal.CFlowStack";
	private static final String CLASS_OLD_COUNTER = 
		"org.aspectbench.runtime.internal.CFlowCounter";
	// New runtime library
	private static final String CLASS_NEW_STACK_GLOBAL = 
		"org.aspectbench.runtime.internal.CflowStackGlobal";
	private static final String CLASS_NEW_COUNTER_GLOBAL =
		"org.aspectbench.runtime.internal.CflowCounterGlobal";
	private static final String CLASS_NEW_STACK_FACTORY = 
		"org.aspectbench.runtime.internal.CflowStackFactory";
	private static final String CLASS_NEW_COUNTER_FACTORY = 
		"org.aspectbench.runtime.internal.CflowCounterFactory";
	private static final String CLASS_NEW_COUNTER_INTERFACE = 
		"org.aspectbench.runtime.internal.CflowCounterInterface";
	private static final String CLASS_NEW_COUNTER_THREADLOCAL = 
		"org.aspectbench.runtime.internal.cflowinternal.Counter";
	// Typed Cflow Stacks
	private static final String[] types = {"Ref", "Int", "Long", "Float", "Double"};
	private static final String[] CLASS_NEW_STACK_INTERFACE_TYPED = new String[types.length];
	private static final String[] CLASS_NEW_STACK_GLOBAL_TYPED = new String[types.length];
	private static final String[] CLASS_NEW_STACK_TYPED = new String[types.length];
	private static final String[] CLASS_NEW_STACK_CELL_TYPED = new String[types.length];
	static {
		// Initialise the typed cflow stack class names
		for (int i = 0; i < types.length; i++) {
			CLASS_NEW_STACK_INTERFACE_TYPED[i] = 
				"org.aspectbench.runtime.internal.CflowStackInterface$" + types[i];
			CLASS_NEW_STACK_GLOBAL_TYPED[i] = 
				"org.aspectbench.runtime.internal.CflowStackGlobal$CflowStack" + types[i];
			CLASS_NEW_STACK_TYPED[i] = 
				"org.aspectbench.runtime.internal.cflowinternal.Stack" + types[i];
			CLASS_NEW_STACK_CELL_TYPED[i] = 
				"org.aspectbench.runtime.internal.cflowinternal.Stack" + types[i] + "$Cell";
		}
	}

	/** Register all the classes that Cflow codegen might use with Soot
	 */
	public static void addBasicClassesToSoot() {
		// Add all classes except typed stack classes
		String[] classes = {
			CLASS_OLD_STACK, CLASS_OLD_COUNTER, 
			CLASS_NEW_STACK_GLOBAL, CLASS_NEW_COUNTER_GLOBAL,
			CLASS_NEW_STACK_FACTORY, CLASS_NEW_COUNTER_FACTORY,
			CLASS_NEW_COUNTER_INTERFACE, CLASS_NEW_COUNTER_THREADLOCAL};
		for (int i = 0; i < classes.length; i++) {
			Scene.v().addBasicClass(classes[i], SootClass.SIGNATURES);
		}	
		
		// Add typed stack classes
		for (int i = 0; i < types.length; i++) {
			Scene.v().addBasicClass(CLASS_NEW_STACK_INTERFACE_TYPED[i], 
							SootClass.SIGNATURES);
			Scene.v().addBasicClass(CLASS_NEW_STACK_GLOBAL_TYPED[i], 
							SootClass.SIGNATURES);
			Scene.v().addBasicClass(CLASS_NEW_STACK_TYPED[i], 
							SootClass.SIGNATURES);
			Scene.v().addBasicClass(CLASS_NEW_STACK_CELL_TYPED[i], 
							SootClass.SIGNATURES);	
		}
		
	}
	
	/** Test whether a method is a Cflow Counter or Stack Factory method
	 */
	public static boolean isFactoryMethod(SootMethodRef m) {
		String className = m.declaringClass().getName();
		String methodName = m.name();
		if (className.equals(CLASS_OLD_STACK) &&
			methodName.equals("getStack"))
			return true;
		if (className.equals(CLASS_OLD_COUNTER) &&
			methodName.equals("getCounter"))
			return true;
		if (className.equals(CLASS_NEW_STACK_FACTORY) &&
			methodName.startsWith("makeStack"))
			return true;
		if (className.equals(CLASS_NEW_COUNTER_FACTORY) &&
			methodName.equals("makeCflowCounter"))
			return true;
			 
		// Not a cflow factory method
		return false;
			
	}
	
	/** Test whether a type is a cflow thread-local type
	 */
	public static boolean isThreadLocalType(Type t) {
		if (!(t instanceof RefType))
			return false;
		RefType rt = (RefType)t;
		String name = rt.getClassName();
		
		if (name.equals(CLASS_NEW_COUNTER_THREADLOCAL))
			return true;
		for (int i = 0; i < types.length; i++) 
			if (name.equals(CLASS_NEW_STACK_TYPED[i]))
				return true;
				
		// FIXME: what about thread locals in the old runtime?
		// The problem is that these are given the type Object,
		// so a simple test based on the type cannot suffice
		// The current workaround is to disable thread-local sharing
		// in the old runtime (in CflowCodeGenFactory.v())
				
		return false;
	}
	
	/** A few code generation utility functions */
	private static class CodeGenUtils {
		private CodeGenUtils() { }
		private static CodeGenUtils instance = new CodeGenUtils();
		private static CodeGenUtils v() { return instance; }
		

		/* SOME CODEGEN UTILITY FUNCTIONS */
		
		/** Copy one local to another, autoboxing/unboxing and inserting a cast if necessary
		 */
		private Chain genCopy(LocalGeneratorEx localgen, Local from, Local to) {
			Chain c = new HashChain();
			Type tfrom = from.getType();
			Type tto = to.getType();
			if ((tfrom instanceof RefType && tto instanceof RefType) ||
				(tfrom instanceof PrimType && tto instanceof PrimType)) {
				c.addAll(genCopyCast(localgen, from, to));
				return c;
			} else if (tfrom instanceof RefType) {
				// UNBOX
				SootClass boxClass = Restructure.JavaTypeInfo.getBoxingClass(tto);
				SootMethodRef unboxMethod = 
					Scene.v().makeMethodRef(
							boxClass,
							Restructure.JavaTypeInfo.getSimpleTypeBoxingClassMethodName(tto),
		                     new ArrayList(),
		                     tto,
		                     false);
				
				// Cast the variable if necessary
				Local castval = genCast(localgen, c, from, boxClass.getType(), null);
                
                Stmt putstmt = Jimple.v().newAssignStmt(
                		to,
						Jimple.v().newVirtualInvokeExpr(castval,unboxMethod));
                c.add(putstmt);
                return c;
			} else {
				// BOX
				SootClass boxClass = Restructure.JavaTypeInfo.getBoxingClass(tfrom);
				Stmt putstmt = 
					Jimple.v().newAssignStmt(
							to,
							Jimple.v().newNewExpr(boxClass.getType()));
				List typeList = new ArrayList(1);
				List valueList = new ArrayList(1);
				typeList.add(tfrom);
				valueList.add(from);
				Stmt initstmt = 
					Jimple.v().newInvokeStmt(
							Jimple.v().newSpecialInvokeExpr(
									to,
									Scene.v().makeConstructorRef(boxClass, typeList),
									valueList));
				c.add(putstmt);
				c.add(initstmt);
				return c;
			}
		}
		
		/** Copy an array cell into a local, autoboxing/unboxing
		 */
		private Chain genCopyFromArray(LocalGeneratorEx localgen, Local fromArray, int index, Local to) {
			Type elemType = ((ArrayType)fromArray.getType()).baseType;
			Local temp = localgen.generateLocal(elemType, "arrayTemp");
			Stmt getstmt =
				Jimple.v().newAssignStmt(
						temp,
						Jimple.v().newArrayRef(fromArray, IntConstant.v(index)));
			Chain c = genCopy(localgen, temp, to);
			c.addFirst(getstmt);
			return c;
		}
		
		/** Copy a local into an array cell, autoboxing/unboxing
		 */
		private Chain genCopyToArray(LocalGeneratorEx localgen, Local from, Local toArray, int index) {
			Type elemType = ((ArrayType)toArray.getType()).baseType;
			Local temp = localgen.generateLocal(elemType);
			Chain c = genCopy(localgen, from, temp);
			Stmt putstmt =
				Jimple.v().newAssignStmt(
						Jimple.v().newArrayRef(toArray, IntConstant.v(index)),
						temp);
			c.addLast(putstmt);
			return c;
		}		
		
		/** Is a cast necessary (for PrimType assignments) ?
		 */
		private boolean primTypeNeedCast(PrimType from, PrimType to) {
			// Deal with common cases
			if (to.equals(IntType.v())) 
				return ((from.equals(LongType.v()) || from.equals(FloatType.v()) || from.equals(DoubleType.v())));
			if (to.equals(LongType.v()))
				return ((from.equals(FloatType.v()) || from.equals(DoubleType.v())));
			// Otherwise, give up...
			return !from.equals(to);
		}
		
		/** Is a cast necessary when assigning a value of type from to an lhs of type to?
		 */
		private boolean needCast(Type from, Type to) {
			if (from instanceof RefType && to instanceof RefType) {
				Type superType = to.merge(from, Scene.v());
				// No need to cast iff
				// to is a supertype of from, ie iff
				// from `merge` to equals to
				return !superType.equals(to);
			}
			if (from instanceof PrimType && to instanceof PrimType) {
				return primTypeNeedCast((PrimType)from, (PrimType)to);
			}
			if (from instanceof ArrayType && to instanceof ArrayType) {
				return
					needCast(((ArrayType)from).baseType,
							 ((ArrayType)to).baseType);
			}
			if (from instanceof ArrayType && to instanceof RefType) {
				if (((RefType)to).getClassName().equals("java.lang.Object") ||
					((RefType)to).getClassName().equals("java.lang.Cloneable") ||
					((RefType)to).getClassName().equals("java.io.Serializable"))
					return false;
				else
					throw new RuntimeException(
							"Incompatible types: cannot assign array type "+from+
							" to type "+to);
			}
			if (from instanceof RefType && to instanceof ArrayType) {
				if (((RefType)from).getClassName().equals("java.lang.Object") ||
					((RefType)from).getClassName().equals("java.lang.Cloneable") ||
					((RefType)from).getClassName().equals("java.io.Serializable"))
					return true;
				else
					throw new RuntimeException(
							"Incompatible types: cannot cast type "+from+
							" to array type "+to);
			}
			// Remaining cases: 
			// Ref->Prim, Prim->Ref, Array->Prim, Prim->Array
			// and types not in Prim,Ref,Array - ie Null and Void
			
			throw new RuntimeException(
					"Invalid types in needCast: "+from+" -> "+to);
		}
		
		/** Coerce a local to a given type. Returns the local holding the
		 *  coerced value, and inserts the cast (if necessary) after stmt in c.
		 *  Pass null as stmt to insert at the beginning
		 */
		private Local genCast(LocalGeneratorEx localgen, Chain c, Local from, Type toType, Stmt stmt) {
			Type fromType = from.getType();
			
			if (needCast(fromType, toType)) {
				Local castval = localgen.generateLocal(toType, "castTemp");
				Stmt castStmt = 
					Jimple.v().newAssignStmt(
							castval,
							Jimple.v().newCastExpr(
									from, toType)
							);
				if (stmt != null)
					c.insertAfter(castStmt, stmt);
				else
					c.addFirst(castStmt);
				return castval;
			} else {
				return from;
			}

		}
		
		/** Copy one local to another, inserting a cast if necessary
		 */
		private Chain genCopyCast(LocalGeneratorEx localgen, Local from, Local to) {
			Type fromType = from.getType();
			Type toType = to.getType();
			Chain c = new HashChain();
			
			Stmt copyStmt;
			
			if (needCast(fromType, toType)) 
				copyStmt = 
					Jimple.v().newAssignStmt(
						to, Jimple.v().newCastExpr(from, toType));
			else
				copyStmt = 
					Jimple.v().newAssignStmt(
						to, from);
			
			c.add(copyStmt);
			return c;
		}
		
		/** Set a boolean value to the result of a test. In addition, jump to 
		 *  succeed (resp. fail) if true (resp. false), or just fall through if
		 *  the corresponding parameter is null
		 */
		private Chain genDecision(LocalGeneratorEx localgen, Expr testIfTrue, Local result, Stmt succeed, Stmt fail) {
			Stmt resfalse =
				Jimple.v().newAssignStmt(
						result,
						IntConstant.v(0));
			Stmt restrue = 
				Jimple.v().newAssignStmt(
						result,
						IntConstant.v(1));
			Stmt nop = 
				Jimple.v().newNopStmt();
			Stmt jumpToSetTrueIfValid = 
				Jimple.v().newIfStmt(
						testIfTrue,
						restrue);
			
			// After the result is set, generate a JUMP, either to the nop
			// or to succeed / fail if not null
			
			Stmt jumpAfterSetIfValid = 
				(succeed == null) ? 
				Jimple.v().newGotoStmt(nop) : 
				Jimple.v().newGotoStmt(succeed);
			Stmt jumpAfterSetIfNotValid = 
				(fail == null) ? 
				Jimple.v().newGotoStmt(nop) :
				Jimple.v().newGotoStmt(fail);
				
			Chain c = new HashChain();

			c.add(jumpToSetTrueIfValid);
			c.add(resfalse);
			c.add(jumpAfterSetIfNotValid);
			c.add(restrue);
			c.add(jumpAfterSetIfValid);
			c.add(nop);
			return c;
		}
		
	}
	
	/** Storing the classes and methods for the CflowCounterGlobal implementation
	 */
	private static class CflowCounterGlobalUtils {
		private CflowCounterGlobalUtils() {}
		
		private SootClass factoryClass = null;
		private SootClass factoryClass() {
			if (factoryClass == null) factoryClass = 
				Scene.v().getSootClass("org.aspectbench.runtime.internal.CflowCounterFactory");
			return factoryClass;
		}
		private Type factoryType() { return factoryClass().getType(); }
		
		private SootClass counterClass = null;
		private SootClass counterClass() { 
			if (counterClass == null) counterClass = 
				Scene.v().getSootClass("org.aspectbench.runtime.internal.CflowCounterInterface");
			return counterClass;
		}
		private Type counterType() { return counterClass().getType(); }
		
		private SootClass threadCounterClass = null;
		private SootClass threadCounterClass() { 
			if (threadCounterClass == null) threadCounterClass = 
				Scene.v().getSootClass("org.aspectbench.runtime.internal.cflowinternal.Counter");
			return threadCounterClass;
		}
		private Type threadCounterType() { return threadCounterClass().getType(); }
		
		private SootMethodRef getMethod() {
			return Scene.v().makeMethodRef(
					counterClass(),
					"getThreadCounter",
					new ArrayList(),
					threadCounterType(),
					false);
		}
		
		private SootMethodRef makeMethod() {
			return Scene.v().makeMethodRef(
					factoryClass(),
					"makeCflowCounter",
					new ArrayList(),
					counterType(),
					true);
		}
		
		private SootFieldRef threadCountField() {
			return Scene.v().makeFieldRef(
					threadCounterClass(),
					"count",
					IntType.v(),
					false);
		}
		
		// The counter field for the single-threaded case
		private SootFieldRef singleCounterField() {
			return Scene.v().makeFieldRef(
					threadCounterClass(),
					"count",
					IntType.v(),
					false);
		}
	}
	
	/** Storing the classes and methods for the CflowStackGlobal implementation
	 */
	private static class CflowStackGlobalUtils {
		private CflowStackGlobalUtils(String elemType) { this.elemType = elemType; }
		String elemType;
		
		private SootClass factoryClass = null;
		private SootClass factoryClass() { 
			if (factoryClass == null) factoryClass = 
				Scene.v().getSootClass("org.aspectbench.runtime.internal.CflowStackFactory");
			return factoryClass;
		}
		private Type factoryType() { return factoryClass().getType(); }

		private Type objectType = null;
		private Type objectType() {
			if (objectType == null) objectType = 
				Scene.v().getRefType("java.lang.Object");
			return objectType;
		}
		
		private final String STACKCELL_REF    = "Ref";
		private final String STACKCELL_INT    = "Int";
		private final String STACKCELL_LONG   = "Long";
		private final String STACKCELL_FLOAT  = "Float";
		private final String STACKCELL_DOUBLE = "Double";
		private String fromType(Type t) {
			if (t instanceof RefType) return STACKCELL_REF; else
			if (t instanceof BooleanType) return STACKCELL_INT; else
			if (t instanceof IntType) return STACKCELL_INT; else
			if (t instanceof ByteType) return STACKCELL_INT; else
			if (t instanceof ShortType) return STACKCELL_INT; else
			if (t instanceof LongType) return STACKCELL_LONG; else
			if (t instanceof CharType) return STACKCELL_INT; else
			if (t instanceof FloatType) return STACKCELL_FLOAT; else
			if (t instanceof DoubleType) return STACKCELL_DOUBLE; else
				throw new RuntimeException("Unknown type in CflowCodeGen");
		}
		private Type toType(String s) {
			// Note that boolean,byte,char,short all map to int
			if (s.equals(STACKCELL_REF)) return objectType(); else
			if (s.equals(STACKCELL_INT)) return IntType.v(); else
			if (s.equals(STACKCELL_LONG)) return LongType.v(); else
			if (s.equals(STACKCELL_FLOAT)) return FloatType.v(); else
			if (s.equals(STACKCELL_DOUBLE)) return DoubleType.v(); else
				throw new RuntimeException("Unknown Type string in CflowCodeGen");
		}
		
		private SootClass typedStackClass(Type t) { return typedStackClass(fromType(t)); }
		private SootClass typedStackClass(String celltype) {
			return Scene.v().getSootClass("org.aspectbench.runtime.internal.CflowStackInterface$"
					+ celltype);
		}
		private RefType typedStackType(Type t) { return typedStackType(fromType(t)); }
		private RefType typedStackType(String celltype) { return typedStackClass(celltype).getType(); }
		private SootClass threadStackClass(Type t)  { return threadStackClass(fromType(t)); }
		private SootClass threadStackClass(String celltype) { 
			return Scene.v().getSootClass("org.aspectbench.runtime.internal.cflowinternal.Stack"
					+ celltype);
		}
		private RefType threadStackType(Type t) { return threadStackType(fromType(t)); }
		private RefType threadStackType(String celltype) { return threadStackClass(celltype).getType(); }
		private SootClass threadStackCellClass(Type t) { return threadStackCellClass(fromType(t)); }
		private SootClass threadStackCellClass(String celltype) {
			return Scene.v().getSootClass("org.aspectbench.runtime.internal.cflowinternal.Stack"
					+ celltype + "$Cell");
		}
		private RefType threadStackCellType(Type t) { return threadStackCellType(fromType(t)); }
		private RefType threadStackCellType(String celltype) { return threadStackCellClass(celltype).getType(); }
		private SootMethodRef threadStackCellConstructor(Type t) { return threadStackCellConstructor(fromType(t)); }
		private SootMethodRef threadStackCellConstructor(String celltype) {
			List types = new ArrayList(2);
			types.add(threadStackCellType(celltype));
			types.add(toType(celltype));
			return Scene.v().makeConstructorRef(threadStackCellClass(celltype),types);
		}
		
		private SootMethodRef makeMethod(Type t) { return makeMethod(fromType(t)); }
		private SootMethodRef makeMethod(String celltype) {
			return Scene.v().makeMethodRef(
					factoryClass(),
					"makeStack"+celltype,
					new ArrayList(),
					typedStackType(celltype),
					true);
		}
		
		private SootMethodRef getMethod(Type t) { return getMethod(fromType(t)); }
		private SootMethodRef getMethod(String celltype) {
			return Scene.v().makeMethodRef(
					typedStackClass(celltype),
					"getThreadStack",
					new ArrayList(),
					threadStackType(celltype),
					false);
		}
		
		private SootFieldRef threadStackField(Type t) { return threadStackField(fromType(t)); }
		private SootFieldRef threadStackField(String celltype) {
			return Scene.v().makeFieldRef(
					threadStackClass(celltype),
					"top",
					threadStackCellType(celltype),
					false);
		}

		private SootFieldRef threadStackPrevField(Type t) { return threadStackPrevField(fromType(t)); }
		private SootFieldRef threadStackPrevField(String celltype) {
			return Scene.v().makeFieldRef(
					threadStackCellClass(celltype),
					"prev",
					threadStackCellType(celltype),
					false);
		}
		
		private SootFieldRef threadStackElemField(Type t) { return threadStackElemField(fromType(t)); }
		private SootFieldRef threadStackElemField(String celltype) {
			return Scene.v().makeFieldRef(
					threadStackCellClass(celltype),
					"elem",
					toType(celltype),
					false);
		}
		
		// The stack field for the single-threaded case
		private SootFieldRef singleThreadedStackField(Type t) { return singleThreadedStackField(fromType(t)); }
		private SootFieldRef singleThreadedStackField(String celltype) {
			return Scene.v().makeFieldRef(
					threadStackClass(celltype),
					"top",
					threadStackCellType(celltype),
					false);
		}
	}
	
	public static abstract class CflowCodeGen {
		/** Get the Type of the (global) cflow class
		 * @return The type of the cflow class 
		 */
		public abstract Type getCflowType();
		/** Get the SootClass for the thread-local cflow class. Must not be called 
		 *  before the formals have been set.
		 * @return The thread-local cflow class, or null if not applicable
		 */
		public abstract SootClass getCflowInstanceClass();
		/** Get the Type of the thread-local cflow class. Must not be called before the
		 * formals have been set.
		 * @return The type of the thread-local cflow class 
		 */
		public Type getCflowInstanceType() { return getCflowInstanceClass().getType(); }
		/** Get a name to denote fields, methods etc that involve cflow classes.
		 * @return The name to use as a base for values of this class.
		 */
		public abstract String chooseName();
		
		protected CodeGenUtils cgu = CodeGenUtils.v();

		/** Generate code to initialise a cflow instance local to a dummy value if necessary
		 * @param l The cflow local variable to initialise
		 * @return The chian of statements initialising the local
		 */
		public Chain genInitLocalToNull(LocalGeneratorEx localgen, Local l) {
			Chain c = new HashChain();
			Stmt init = Jimple.v().newAssignStmt(l, NullConstant.v());
			c.add(init);
			return c;
		}
		
		protected SootFieldRef field = null;
		/** Set the field containing the reference to this Cflow
		 * @param field The field (in an aspect) containing the cflow 
		 */
		public void setCflowField(SootFieldRef field) { this.field = field; }
		/** Set the types of the cflow bound vars. Must be called before code gen, used
		 * to decide on code gen strategy
		 * @param types The list of types of the cflow bound variables
		 */
		public abstract void setFormals(List/*<Type>*/ types);
		/** Generate code to initialise a local variable to the cflow thread-local
		 * @param cFlowLocal The local variable to contain the thread-local Cflow bookkeeping class
		 * @param cFlowInstance The cflow bookkeeping instance to get the thread-local from 
		 * @return The chain of statements initialising the cflow local
		 */
		public abstract Chain genInitLocal(LocalGeneratorEx localgen, Local cFlowLocal, Local cFlowInstance);
		/** Generate code to initialise a local variable to the cflow thread-local, if non-null
		 * @param cFlowLocal The local variable to contain the thread-local Cflow bookkeeping class
		 * @param cFlowInstance The cflow bookkeeping instance to get the thread-local from 
		 * @return The chain of statements initialising the cflow local
		 */
		public Chain genInitLocalLazily(LocalGeneratorEx localgen, Local cFlowLocal, Local cFlowInstance) {
			Chain c = genInitLocal(localgen, cFlowLocal, cFlowInstance);
			Stmt nop = Jimple.v().newNopStmt();
			c.addLast(nop);
			Stmt jumpIfNull = 
				Jimple.v().newIfStmt(
						Jimple.v().newNeExpr(cFlowLocal, NullConstant.v()),
						nop);
			c.addFirst(jumpIfNull);
			return c;
		}
		/** Generate code for a push operation.
		 * @param cFlowLocal The local variable containing the thread-local Cflow bookkeeping class
		 * @param values the list of values to push onto the cflow stack
		 * @return The chain of statements generated for the push operation, together with a single
		 * statement representing it for analyses (cf. conditions for this stmt).
		 */
		public abstract ChainStmtBox genPush(LocalGeneratorEx localgen, Local cFlowLocal, List/*<Value>*/ values);
		/** Generate code for a pop operation.
		 * @param cFlowLocal The local variable containing the thread-local Cflow bookkeeping class
		 * @return The chain of statements generated for the pop operation together with a single
		 * statement representing it for analyses (cf. conditions for this stmt).
		 */
		public abstract ChainStmtBox genPop(LocalGeneratorEx localgen, Local cFlowLocal);
		/** Generate code for a peek operation.
		 * @param cFlowLocal The local variable containing the thread-local Cflow bookkeeping class
		 * @param targets The list of target variables to save the cflow context into
		 * @return The chain of statements generated for the peek operation
		 */
		public abstract Chain genPeek(LocalGeneratorEx localgen, Local cFlowLocal, List/*<Local>*/ targets);
		/** Generate code for an isValid test. This sets a boolean variable to the result. In addition,
		 *  if succeed (resp. fail) is non-null, then control flow jumps to succeed (resp. fail) if the
		 *  test succeeds (resp. fails). Both succeed and fail should occur *after* the point at which
		 *  the chain of statements returned by genIsValid is to be inserted.
		 * @param cFlowLocal The local variable containing the thread-local Cflow bookkeeping class
		 * @param result The local variable to store the result of the test into
		 * @param succeed The stmt to jump to if the test succeeds, or null if none required
		 * @param fail The stmt to jump to if the test fails, or null if none required
		 * @return The chain of statements generated for the isValid test, together with a single
		 * statement representing it for analyses (cf. conditions for this stmt).
		 */
		public abstract ChainStmtBox genIsValid(LocalGeneratorEx localgen, Local cFlowLocal, Local result, Stmt succeed, Stmt fail);

		/** Generate code for a cflowdepth query.
		 * @param cFlowLocal The local variable containing the thread-local Cflow bookkeeping class
		 * @param result The local variable to store the result of the query into
		 * @return The chain of statements generated for the depth query
		 */
		public abstract Chain genDepth(LocalGeneratorEx localgen, Local cFlowLocal, Local result);
		
		/** Generate code to initialise a field to contain the cflow state (in an aspect) 
		 * @param field The field to contain the cflow state (of type getCflowType())
		 * @return The chain of statements generated to initialise the field
		 */
		public Chain genInitCflowField(LocalGeneratorEx localgen, SootFieldRef field) {
			// Default implementation : appropriate for all implementations in which the cflow 
			// state is contained in an instance of a class
			Chain c = new HashChain();
			Local temp = localgen.generateLocal(getCflowType(), chooseName());
			
			Stmt makeNew =
				Jimple.v().newAssignStmt(
						temp,
						Jimple.v().newNewExpr((RefType)getCflowType()));
			c.add(makeNew);
			
			SootClass cflowClass = ((RefType)getCflowType()).getSootClass();
			
			Stmt initNew = 
				Jimple.v().newInvokeStmt(
						Jimple.v().newSpecialInvokeExpr(
								temp,
								Scene.v().makeConstructorRef(cflowClass, new ArrayList())));
			c.add(initNew);
			Stmt saveNew = 
				Jimple.v().newAssignStmt(
						Jimple.v().newStaticFieldRef(field),
						temp);
			c.add(saveNew);
			
			return c;
		}
	}

	private static class CflowCounterGlobalCodeGen extends CflowCodeGen {
	
		private CflowCounterGlobalCodeGen() {} 
		public static CflowCounterGlobalCodeGen v() { 
			return new CflowCounterGlobalCodeGen(); }

		private CflowCounterGlobalUtils util = new CflowCounterGlobalUtils();

		public Type getCflowType() { return util.counterType(); }
		public SootClass getCflowInstanceClass() { return util.threadCounterClass(); }
		
		public String chooseName() { return "cflowCounter"; }
		
		public void setFormals(List/*<Type>*/ types) {
			if (types.size() > 0) 
				throw new RuntimeException("CflowCounter codegen attempted with"+
						" nonempty cflow bound vars list");
		}
		
		public Chain genInitCflowField(LocalGeneratorEx localgen, SootFieldRef field) {
			
			// override default to use a factory method
			Chain c = new HashChain();
			Local l = localgen.generateLocal(util.counterType(), chooseName());
			
			Stmt init = 
				Jimple.v().newAssignStmt(l, 
						Jimple.v().newStaticInvokeExpr(
								util.makeMethod(),
								new ArrayList()));
			Stmt set = 
				Jimple.v().newAssignStmt(
						Jimple.v().newStaticFieldRef(field), l);
			c.add(init);
			c.add(set);
			
			return c;
		}
		
		public Chain genInitLocal(LocalGeneratorEx localgen, Local cFlowLocal, Local cFlowInstance) {
			
			Stmt init = 
				Jimple.v().newAssignStmt(
						cFlowLocal,
						Jimple.v().newInterfaceInvokeExpr(
								cFlowInstance,
								util.getMethod(),
								new ArrayList()));
			Chain c = new HashChain();
			c.add(init);
			
			return c;
		}
		public ChainStmtBox genPush(LocalGeneratorEx localgen, Local cFlowLocal, List/*<Value>*/ values) {
			
			// Check that no formals bound
			if (values.size() > 0) throw new 
				RuntimeException("Cflow Counter used with nonempty formals list");
			
			Local temp = localgen.generateLocal(IntType.v(), "cflowCount");
			Stmt get = 
				Jimple.v().newAssignStmt(
						temp,
						Jimple.v().newInstanceFieldRef(
								cFlowLocal,
								util.threadCountField()));
			Stmt inc = 
				Jimple.v().newAssignStmt(
						temp,
						Jimple.v().newAddExpr(temp, IntConstant.v(1)));
			Stmt put = 
				Jimple.v().newAssignStmt(
						Jimple.v().newInstanceFieldRef(
								cFlowLocal,
								util.threadCountField()),
						temp);
			Chain c = new HashChain();
			c.add(get);
			c.add(inc);
			c.add(put);
			
			return new ChainStmtBox(c,inc);
		}
		public ChainStmtBox genPop(LocalGeneratorEx localgen, Local cFlowLocal) {
			
			Local temp = localgen.generateLocal(IntType.v(), "cflowCount");
			Stmt get = 
				Jimple.v().newAssignStmt(
						temp,
						Jimple.v().newInstanceFieldRef(
								cFlowLocal,
								util.threadCountField()));
			Stmt dec = 
				Jimple.v().newAssignStmt(
						temp,
						Jimple.v().newSubExpr(temp, IntConstant.v(1)));
			Stmt put = 
				Jimple.v().newAssignStmt(
						Jimple.v().newInstanceFieldRef(
								cFlowLocal,
								util.threadCountField()),
						temp);
			Chain c = new HashChain();
			c.add(get);
			c.add(dec);
			c.add(put);
			
			return new ChainStmtBox(c, dec);
		}
		public Chain genPeek(LocalGeneratorEx localgen, Local cFlowLocal, List/*<Local>*/ targets) {
			// Should never be used on counters
			throw new RuntimeException("peek operation attempted on a Cflow counter.");
		}
		public ChainStmtBox genIsValid(LocalGeneratorEx localgen, Local cFlowLocal, Local result, Stmt succeed, Stmt fail) {
			
			Local temp = localgen.generateLocal(IntType.v(), "cflowCount");
			Stmt get = 
				Jimple.v().newAssignStmt(
						temp,
						Jimple.v().newInstanceFieldRef(
								cFlowLocal,
								util.threadCountField()));
			Expr testExpr = 
				Jimple.v().newGtExpr(temp, IntConstant.v(0));
			Chain c = cgu.genDecision(localgen, testExpr, result, succeed, fail);
			c.addFirst(get);
			
			return new ChainStmtBox (c, get);
		}
		public Chain genDepth(LocalGeneratorEx localgen, Local cFlowLocal, Local result) {
			
                    Chain c = new HashChain();
			Stmt get = 
				Jimple.v().newAssignStmt(
						result,
						Jimple.v().newInstanceFieldRef(
								cFlowLocal,
								util.threadCountField()));
			c.addFirst(get);
                        return c;
		}
	}
	
	private static class CflowCounterSingleThreadedCodeGen extends CflowCodeGen {
		private CflowCounterSingleThreadedCodeGen() {} 
		public static CflowCounterSingleThreadedCodeGen v() { 
			return new CflowCounterSingleThreadedCodeGen(); }

		private CflowCounterGlobalUtils util = new CflowCounterGlobalUtils();

		public Type getCflowType() { return util.threadCounterClass().getType(); }
		public SootClass getCflowInstanceClass() { 
			// No thread-specific instance, use the global class
			return util.threadCounterClass(); 
			}
		
		public String chooseName() { return "cflowCounter"; }
		
		public void setFormals(List/*<Type>*/ types) {
			if (types.size() > 0) 
				throw new RuntimeException("CflowCounter codegen attempted with"+
						" nonempty cflow bound vars list");
		}
		
		// Override the lazy init code in this case - not needed 
		// The dummy assignments cFlowInstance->cFlowLocal should have no effect on bytecode
		public Chain genInitLocalLazily(LocalGeneratorEx localgen, Local cFlowLocal, Local cFlowInstance) {
			
			
			return genInitLocal(localgen, cFlowLocal, cFlowInstance);
		}
		
		public Chain genInitLocal(LocalGeneratorEx localgen, Local cFlowLocal, Local cFlowInstance) {
			// There is nothing to do (no thread-local instance to get)
			// Set the cflowLocal to cFlowInstance to keep a handle on it
			
			Chain c = new HashChain();
			c.addAll(cgu.genCopy(localgen, cFlowInstance, cFlowLocal));
			
			return c;
		}

		public ChainStmtBox genIsValid(LocalGeneratorEx localgen, Local cFlowLocal, Local result, Stmt succeed,
				Stmt fail) {
			
			Local temp = localgen.generateLocal(IntType.v(), "cflowCounter");
			Stmt get =
				Jimple.v().newAssignStmt(
						temp,
						Jimple.v().newInstanceFieldRef(
								cFlowLocal,
								util.singleCounterField()));
			Expr test = 
				Jimple.v().newGtExpr(temp, IntConstant.v(0));
			
			Chain c = cgu.genDecision(localgen, test, result, succeed, fail);
			c.addFirst(get);
			
			return new ChainStmtBox(c, get);
		}

		public Chain genDepth(LocalGeneratorEx localgen, Local cFlowLocal, Local result) {
                    Chain c = new HashChain();
			Stmt get =
				Jimple.v().newAssignStmt(
						result,
						Jimple.v().newInstanceFieldRef(
								cFlowLocal,
								util.singleCounterField()));
			c.addFirst(get);
                        return c;
		}

		public Chain genPeek(LocalGeneratorEx localgen, Local cFlowLocal, List targets) {
			// Invalid Operation
			throw new RuntimeException("CflowCounter peek operation");
		}

		public ChainStmtBox genPop(LocalGeneratorEx localgen, Local cFlowLocal) {
			
			Chain c = new HashChain();
			
			Local temp = localgen.generateLocal(IntType.v(), "cflowCounter");
			Stmt get =
				Jimple.v().newAssignStmt(
						temp,
						Jimple.v().newInstanceFieldRef(
								cFlowLocal,
								util.singleCounterField()));
			c.add(get);
			
			Stmt dec = 
				Jimple.v().newAssignStmt(
						temp,
						Jimple.v().newSubExpr(temp, IntConstant.v(1)));
			c.add(dec);
			
			Stmt put =
				Jimple.v().newAssignStmt(
						Jimple.v().newInstanceFieldRef(
								cFlowLocal,
								util.singleCounterField()),
						temp);
			c.add(put);
			
			return new ChainStmtBox(c, dec);
		}

		public ChainStmtBox genPush(LocalGeneratorEx localgen, Local cFlowLocal, List values) {
			Chain c = new HashChain();
			
			Local temp = localgen.generateLocal(IntType.v(), "cflowCounter");
			Stmt get =
				Jimple.v().newAssignStmt(
						temp,
						Jimple.v().newInstanceFieldRef(
								cFlowLocal,
								util.singleCounterField()));
			c.add(get);
			
			Stmt inc = 
				Jimple.v().newAssignStmt(
						temp,
						Jimple.v().newAddExpr(temp, IntConstant.v(1)));
			c.add(inc);
			
			Stmt put =
				Jimple.v().newAssignStmt(
						Jimple.v().newInstanceFieldRef(
								cFlowLocal,
								util.singleCounterField()),
						temp);
			c.add(put);
			
			return new ChainStmtBox(c, inc);
		}

	}
	
	private static class CflowCounterSingleThreadedStaticFieldCodeGen extends CflowCodeGen {
		
		// Note: to create an instance of this codegen, need to specify the field to contain the cflow.
		private CflowCounterSingleThreadedStaticFieldCodeGen() { } 
		public static CflowCounterSingleThreadedStaticFieldCodeGen v() { 
			return new CflowCounterSingleThreadedStaticFieldCodeGen(); }
		
		public String chooseName() {
			return "cflowCounter";
		}
		public Chain genInitCflowField(LocalGeneratorEx localgen, SootFieldRef field) {
			// Initialise the field to 0
			Chain c = new HashChain();
			Stmt init = 
				Jimple.v().newAssignStmt(
						Jimple.v().newStaticFieldRef(field),
						IntConstant.v(0));
			return c;
		}
		public Chain genInitLocal(LocalGeneratorEx localgen, Local cFlowLocal, Local cFlowInstance) {
			// Nothing to do
			return new HashChain();
		}
		public Chain genInitLocalLazily(LocalGeneratorEx localgen, Local cFlowLocal, Local cFlowInstance) {
			// Override the laziness in this case, not useful
			return genInitLocal(localgen, cFlowLocal, cFlowInstance);
		}
		public Chain genInitLocalToNull(LocalGeneratorEx localgen, Local l) {
			// Override default behaviour. Nothing to initialise here
			return new HashChain();
		}
		public ChainStmtBox genIsValid(LocalGeneratorEx localgen, Local cFlowLocal, Local result, Stmt succeed,
				Stmt fail) {
			
			Local temp = localgen.generateLocal(IntType.v(), "cflowCounterTemp");
			Stmt get = Jimple.v().newAssignStmt(temp, 
					Jimple.v().newStaticFieldRef(field));
			Expr test = Jimple.v().newGtExpr(temp, IntConstant.v(0));
			Chain c = cgu.genDecision(localgen, test, result, succeed, fail);
			c.addFirst(get);
			
			return new ChainStmtBox(c, get);
		}
		public Chain genDepth(LocalGeneratorEx localgen, Local cFlowLocal, Local result) {
			
                    Chain c = new HashChain();
			Stmt get = Jimple.v().newAssignStmt(result, 
					Jimple.v().newStaticFieldRef(field));
			c.addFirst(get);
			
                        return c;
		}
		public Chain genPeek(LocalGeneratorEx localgen, Local cFlowLocal, List targets) {
			// Not valid
			throw new RuntimeException("Peek on cflow counter");
		}
		public ChainStmtBox genPop(LocalGeneratorEx localgen, Local cFlowLocal) {
			
			Chain c = new HashChain();
			Local temp = localgen.generateLocal(IntType.v(), "cflowCounterTemp");
			Stmt get = Jimple.v().newAssignStmt(temp, 
					Jimple.v().newStaticFieldRef(field));
			Stmt dec = Jimple.v().newAssignStmt(
					temp,
					Jimple.v().newSubExpr(temp, IntConstant.v(1)));
			Stmt put = Jimple.v().newAssignStmt(
					Jimple.v().newStaticFieldRef(field), temp);
			c.add(get);
			c.add(dec);
			c.add(put);
			
			return new ChainStmtBox(c, dec);
		}
		public ChainStmtBox genPush(LocalGeneratorEx localgen, Local cFlowLocal, List values) {
			
			Chain c = new HashChain();
			Local temp = localgen.generateLocal(IntType.v(), "cflowCounterTemp");
			Stmt get = Jimple.v().newAssignStmt(temp, 
					Jimple.v().newStaticFieldRef(field));
			Stmt inc = Jimple.v().newAssignStmt(
					temp,
					Jimple.v().newAddExpr(temp, IntConstant.v(1)));
			Stmt put = Jimple.v().newAssignStmt(
					Jimple.v().newStaticFieldRef(field), temp);
			c.add(get);
			c.add(inc);
			c.add(put);
			
			return new ChainStmtBox(c, inc);
		}
		public SootClass getCflowInstanceClass() {
			return null;
		}
		public Type getCflowInstanceType() {
			return IntType.v();
		}
		public Type getCflowType() {
			return IntType.v();
		}
		public void setFormals(List types) {
			if (types.size() > 0)
				throw new RuntimeException(
						"Cflow counter codegen with nonempty formals list");
		}
	}
	
	/** Somme functions common to both global and single-threaded stack codegen
	 */
	private static abstract class CflowStackCommonCodeGen extends CflowCodeGen {
		protected CflowStackGlobalUtils util = null;
		
		public Type getCflowType() { return util.typedStackType(elemType); }
		public String chooseName() { return "cflowStack"; }
		
		protected boolean formalsSet = false;
		protected String elemType = null;
		protected Type actualElemType = null;	// The actual type of values stored in the cflow stack
		protected boolean useArray = false;
		protected Type arrayElemType = null;
		protected boolean useBoxing = false;
		protected List/*<Type>*/ actualTypes = null;
		
		
		/* CHOOSING THE CODEGEN STRATEGY:
		 * Possible strategies:
		 *   1 param of type T: stack of type T
		 *   >1 params all of the same prim type T: stack of type T[]
		 *   >1 params, diff types: stack of type Object[], box prim types
		 */
		public void setFormals(List/*<Type>*/ types) {
			{	// Copy the types into actualTypes
				actualTypes = new ArrayList(types.size());
				Iterator it = types.iterator();
				while (it.hasNext()) { actualTypes.add(it.next()); }
			}
			formalsSet = true;
			
			// Set util to a dummy value to get eg. the Object type
			this.util = new CflowStackGlobalUtils(null);
			
			if (types.size() == 0)  {  
				elemType = util.STACKCELL_REF;
				useArray = true;
				arrayElemType = util.objectType();
				actualElemType = util.objectType().getArrayType();
			} else 
			if (types.size() == 1) {
				elemType = util.fromType((Type)types.get(0));
				actualElemType = (Type)types.get(0);
				useArray = false;
				useBoxing = false;
			} else {
				useArray = true;
				elemType = util.STACKCELL_REF;
				
				// Attempt to find the closest possible type for the array elements
				// Two cases: all the same primitive type, so use this type
				// OR some ref types, so use the closest possible supertype of all 
				// types in the list, boxing primitive types.
				
				Type t = (Type)types.get(0);
				Iterator it;
				
				// All the same primitive type?
				it = types.iterator();
				boolean allSamePrimType = t instanceof PrimType;
				Type trepresentation = 
					util.toType(util.fromType(t));
				while (allSamePrimType && it.hasNext()) {
					Type t1 = (Type)it.next();
					Type t1representation = 
						util.toType(util.fromType(t1));
					allSamePrimType = t1representation.equals(t);
				}
				
				if (allSamePrimType) {
					arrayElemType = trepresentation;
					actualElemType = trepresentation.getArrayType();
					useBoxing = false;
				} else {
					it = types.iterator();
					if (t instanceof PrimType)
						t = Restructure.JavaTypeInfo.getBoxingClass(t).getType();
					while (it.hasNext()) {
						Type t1 = (Type)it.next();
						if (t1 instanceof PrimType)
							t1 = Restructure.JavaTypeInfo.getBoxingClass(t1).getType();
						t = t1.merge(t, Scene.v());
					}
					arrayElemType = t;
					actualElemType = t.getArrayType();
					useBoxing = true;
				}
				
			}
			this.util = new CflowStackGlobalUtils(elemType);
		}
	
		// CODEGEN
		
		private SootFieldRef getStackField(boolean isSingleThreaded, boolean useStaticField) {
			if (isSingleThreaded) {
				if (useStaticField)
					return field;
				else
					return util.singleThreadedStackField(elemType);
			}
			else
				return util.threadStackField(elemType);
		}
		
		private Value getStackFieldInstance(boolean isSingleThreaded, boolean useStaticField, Local cFlowLocal) {
			if (useStaticField)
				return Jimple.v().newStaticFieldRef(field);
			else
				return Jimple.v().newInstanceFieldRef(
					cFlowLocal, getStackField(isSingleThreaded, useStaticField));
		}
		
		protected ChainStmtBox protogenIsValid(LocalGeneratorEx localgen, 
				Local cFlowLocal, Local result, Stmt succeed, Stmt fail, 
				boolean isSingleThreaded, boolean useStaticField) {
			
			Local topCellLocal = 
				localgen.generateLocal(
						util.threadStackCellType(elemType), "cflowStackCell");
			
			Stmt getTopCell = 
				Jimple.v().newAssignStmt(
						topCellLocal,
						getStackFieldInstance(isSingleThreaded, useStaticField, cFlowLocal));
			Expr testExpr = 
				Jimple.v().newNeExpr(topCellLocal, NullConstant.v());
			
			Chain c = cgu.genDecision(localgen, testExpr, result, succeed, fail);
			
			c.addFirst(getTopCell);
			
			return new ChainStmtBox(c, getTopCell);
		}
		
		protected Chain protogenDepth(LocalGeneratorEx localgen, 
				Local cFlowLocal, Local result, 
				boolean isSingleThreaded, boolean useStaticField) {
			
                        Chain c = new HashChain();
			Local topCellLocal = 
				localgen.generateLocal(
						util.threadStackCellType(elemType), "cflowStackCell");
			
			Stmt getTopCell = 
				Jimple.v().newAssignStmt(
						topCellLocal,
						getStackFieldInstance(isSingleThreaded, useStaticField, cFlowLocal));
                        c.add(getTopCell);
			Stmt testStmt = 
				Jimple.v().newAssignStmt(
                                    result,
                                    Jimple.v().newVirtualInvokeExpr(
                                        topCellLocal,
                                        Scene.v().makeMethodRef(
                                            util.threadStackCellClass(elemType),
                                            "depth",
                                            new ArrayList(),
                                            IntType.v(),
                                            false),
                                        new ArrayList()));
			c.add(testStmt);
			
			return c;
		}
		
		protected ChainStmtBox protogenPop(LocalGeneratorEx localgen, 
				Local cFlowLocal, boolean isSingleThreaded, boolean useStaticField) {
			
			Local topCell = 
				localgen.generateLocal(
						util.threadStackCellType(elemType), "cflowStackCell");
			Local prevCell = 
				localgen.generateLocal(
						util.threadStackCellType(elemType), "cflowStackCell");
			
			Stmt getCell = 
				Jimple.v().newAssignStmt(
						topCell,
						getStackFieldInstance(isSingleThreaded, useStaticField, cFlowLocal));
			Stmt getPrevCell = 
				Jimple.v().newAssignStmt(
						prevCell,
						Jimple.v().newInstanceFieldRef(
								topCell,
								util.threadStackPrevField(elemType)));
			Stmt setTopCell = 
				Jimple.v().newAssignStmt(
						getStackFieldInstance(isSingleThreaded, useStaticField, cFlowLocal),
						prevCell);
			Chain c = new HashChain();
			c.add(getCell);
			c.add(getPrevCell);
			c.add(setTopCell);
			
			return new ChainStmtBox(c, getPrevCell);
		}
		
		protected Chain protogenPeek(LocalGeneratorEx localgen, 
				Local cFlowLocal, List/*<Local>*/ targets, boolean isSingleThreaded
				, boolean useStaticField) {
			
			Chain c = new HashChain();
			
			// Get the top element of the stack
			Local topCell = localgen.generateLocal(
					util.threadStackCellType(elemType), "cflowStackCell");
			Local topElem = localgen.generateLocal(
					util.toType(elemType), "cflowStackElem");
			Stmt getTopCell = 
				Jimple.v().newAssignStmt(
						topCell,
						getStackFieldInstance(isSingleThreaded, useStaticField, cFlowLocal));
			c.add(getTopCell);
			Stmt getTopElem = 
				Jimple.v().newAssignStmt(
						topElem,
						Jimple.v().newInstanceFieldRef(
								topCell,
								util.threadStackElemField(elemType)));
			c.add(getTopElem);
			
			// Cast the top elem if necessary
			Local cflowBound = cgu.genCast(localgen, c, topElem, actualElemType, getTopElem);
			
			// Add a nop to separate the two
			Stmt nop = Jimple.v().newNopStmt();
			c.add(nop);
			// Is it an array of values?
			if (useArray) {
				Iterator it = targets.iterator();
				int i = 0;
				while (it.hasNext()) {
					Local to = (Local)it.next();
					Chain copy = cgu.genCopyFromArray(localgen, cflowBound, i, to);
					c.addAll(copy);
					i++;
				}
			} else {
				Local to = (Local)targets.get(0);
				Chain copy = cgu.genCopyCast(localgen, cflowBound, to);
				c.addAll(copy);
			}
			
			return c;
		}
		
		protected ChainStmtBox protogenPush(LocalGeneratorEx localgen, Local cFlowLocal, List/*<Value>*/ values, boolean isSingleThreaded
				, boolean useStaticField) {
			
			Chain c = new HashChain();
			
			// Create the new value to store at the top of the cflow stack
			Local newElemValue = localgen.generateLocal(
					actualElemType, "cflowStackBounds");
			// Initialise the new value
			
			// Should it be an array?
			if (useArray) {
				// Create the array
				Stmt arrayInitStmt = 
					Jimple.v().newAssignStmt(
							newElemValue,
							Jimple.v().newNewArrayExpr(
									arrayElemType,
									IntConstant.v(values.size())));
				c.add(arrayInitStmt);
				// Copy each cflow variable into the array
				Iterator it = values.iterator();
				int i = 0;
				while (it.hasNext()) {
					Value v = (Value)it.next();
					Local temp = localgen.generateLocal(
							v.getType(), "cflowBoundTemp");
					Stmt copyToTemp = 
						Jimple.v().newAssignStmt(
								temp,
								v);
					c.add(copyToTemp);
					Chain copy = cgu.genCopyToArray(localgen, temp, newElemValue, i);
					c.addAll(copy);
					i++;
				}
			} else {
				// If not an array, no boxing or cast necessary
				Stmt copyStmt =
					Jimple.v().newAssignStmt(
							newElemValue,
							(Value)values.get(0));
				c.add(copyStmt);
			}
			
			// Get the top element
			Local topElem = localgen.generateLocal
				(util.threadStackCellType(elemType), "cflowStackTop");
			Stmt getTopElem = 
				Jimple.v().newAssignStmt(
						topElem,
						getStackFieldInstance(isSingleThreaded, useStaticField, cFlowLocal));
			c.add(getTopElem);
			// Make a new element
			Local newTopElem = localgen.generateLocal(
					util.threadStackCellType(elemType), "cflowStackNewTop");
			Stmt makeNewTopElem = 
				Jimple.v().newAssignStmt(
						newTopElem,
						Jimple.v().newNewExpr(
								util.threadStackCellType(elemType)));
			c.add(makeNewTopElem);
			List constrParams = new ArrayList(2);
			constrParams.add(topElem);
			constrParams.add(newElemValue);
			Stmt initNewTopElem = 
				Jimple.v().newInvokeStmt(
						Jimple.v().newSpecialInvokeExpr(
								newTopElem,
								util.threadStackCellConstructor(elemType),
								constrParams));
			c.add(initNewTopElem);
			// Set the new cell as top
			Stmt putNewTopElem = 
				Jimple.v().newAssignStmt(
						getStackFieldInstance(isSingleThreaded, useStaticField, cFlowLocal),
						newTopElem);
			c.add(putNewTopElem);
			
			return new ChainStmtBox(c, putNewTopElem);
		}
		
	}
	
	private static class CflowStackGlobalCodeGen extends CflowStackCommonCodeGen {
		
		private CflowStackGlobalCodeGen() {}
		public static CflowStackGlobalCodeGen v() { 
			return new CflowStackGlobalCodeGen(); }

		public SootClass getCflowInstanceClass() {
			if (!formalsSet) 
				throw new RuntimeException("getCflowInstanceClass() called before formals set");
			else
				return util.threadStackClass(elemType);
		}	
		
		/* CODEGEN */
		
		public Chain genInitCflowField(LocalGeneratorEx localgen, SootFieldRef field) {
			// override default to use a factory method
			
			Chain c = new HashChain();
			Local l = localgen.generateLocal(util.typedStackType(elemType), chooseName());
			
			Stmt init = 
				Jimple.v().newAssignStmt(l, 
						Jimple.v().newStaticInvokeExpr(
								util.makeMethod(elemType),
								new ArrayList()));
			Stmt set = 
				Jimple.v().newAssignStmt(
						Jimple.v().newStaticFieldRef(field), l);
			c.add(init);
			c.add(set);
			
			return c;
		}
		
		public Chain genInitLocal(LocalGeneratorEx localgen, Local cFlowLocal, Local cFlowInstance) {
			
			Stmt initStmt = 
				Jimple.v().newAssignStmt(
						cFlowLocal,
						Jimple.v().newInterfaceInvokeExpr(
								cFlowInstance,
								util.getMethod(elemType),
								new ArrayList()));
			Chain c = new HashChain();
			c.add(initStmt);
			
			return c;
		}

		public ChainStmtBox genIsValid(LocalGeneratorEx localgen, Local cFlowLocal, Local result, Stmt succeed, Stmt fail) {
			return protogenIsValid(localgen, cFlowLocal, result, succeed, fail, false, false);
		}

		public Chain genDepth(LocalGeneratorEx localgen, Local cFlowLocal, Local result) {
			return protogenDepth(localgen, cFlowLocal, result, false, false);
		}
	
		public Chain genPeek(LocalGeneratorEx localgen, Local cFlowLocal, List/*<Local>*/ targets) {
			return protogenPeek(localgen, cFlowLocal, targets, false, false);
		}

		public ChainStmtBox genPop(LocalGeneratorEx localgen, Local cFlowLocal) {
			return protogenPop(localgen, cFlowLocal, false, false);
		}

		public ChainStmtBox genPush(LocalGeneratorEx localgen, Local cFlowLocal, List/*<Value>*/ values) {
			return protogenPush(localgen, cFlowLocal, values, false, false);
		}
	}
	
	private static class CflowStackSingleThreadedCodeGen extends CflowStackCommonCodeGen {
		
		private CflowStackSingleThreadedCodeGen() {}
		public static CflowStackSingleThreadedCodeGen v() { 
			return new CflowStackSingleThreadedCodeGen(); }
		
		public Type getCflowType() { return util.threadStackType(elemType); }

		public SootClass getCflowInstanceClass() {
			if (!formalsSet) 
				throw new RuntimeException("getCflowInstanceClass() called before formals set");
			else
				return util.threadStackClass(elemType);
		}	
		
		/* CODEGEN */
		
		// Override the lazy init code in this case - not needed 
		// The dummy assignments cFlowInstance->cFlowLocal should have no effect on bytecode
		public Chain genInitLocalLazily(LocalGeneratorEx localgen, Local cFlowLocal, Local cFlowInstance) {
			return genInitLocal(localgen, cFlowLocal, cFlowInstance);
		}
		
		public Chain genInitLocal(LocalGeneratorEx localgen, Local cFlowLocal, Local cFlowInstance) {
			
			Chain c = new HashChain();
			c.addAll(cgu.genCopy(localgen, cFlowInstance, cFlowLocal));
			
			return c;
		}

		public ChainStmtBox genIsValid(LocalGeneratorEx localgen, Local cFlowLocal, Local result, Stmt succeed, Stmt fail) {
			return protogenIsValid(localgen, cFlowLocal, result, succeed, fail, true, false);
		}

		public Chain genDepth(LocalGeneratorEx localgen, Local cFlowLocal, Local result) {
			return protogenDepth(localgen, cFlowLocal, result, true, false);
		}
	
	
		public Chain genPeek(LocalGeneratorEx localgen, Local cFlowLocal, List/*<Local>*/ targets) {
			return protogenPeek(localgen, cFlowLocal, targets, true, false);
		}

		public ChainStmtBox genPop(LocalGeneratorEx localgen, Local cFlowLocal) {
			return protogenPop(localgen, cFlowLocal, true, false);
		}

		public ChainStmtBox genPush(LocalGeneratorEx localgen, Local cFlowLocal, List/*<Value>*/ values) {
			return protogenPush(localgen, cFlowLocal, values, true, false);
		}
	}
	
	private static class CflowStackSingleThreadedStaticFieldCodeGen extends CflowStackCommonCodeGen {
		
		private CflowStackSingleThreadedStaticFieldCodeGen() {}
		public static CflowStackSingleThreadedStaticFieldCodeGen v() { 
			return new CflowStackSingleThreadedStaticFieldCodeGen(); }
		
		public Type getCflowType() { return util.threadStackCellClass(elemType).getType(); }
		public SootClass getCflowInstanceClass() {
			if (!formalsSet) 
				throw new RuntimeException("getCflowInstanceClass() called before formals set");
			else
				return util.threadStackCellClass(elemType);
		}	
		
		/* CODEGEN */
		
		// Override the lazy init code in this case - not needed 
		public Chain genInitLocalLazily(LocalGeneratorEx localgen, Local cFlowLocal, Local cFlowInstance) {
			return genInitLocal(localgen, cFlowLocal, cFlowInstance);
		}
		
		public Chain genInitLocal(LocalGeneratorEx localgen, Local cFlowLocal, Local cFlowInstance) {
			// Nothing to initialise
			return new HashChain();
		}

		public ChainStmtBox genIsValid(LocalGeneratorEx localgen, Local cFlowLocal, Local result, Stmt succeed, Stmt fail) {
			return protogenIsValid(localgen, cFlowLocal, result, succeed, fail, true, true);
		}

		public Chain genDepth(LocalGeneratorEx localgen, Local cFlowLocal, Local result) {
			return protogenDepth(localgen, cFlowLocal, result, true, true);
		}
	
	
		public Chain genPeek(LocalGeneratorEx localgen, Local cFlowLocal, List/*<Local>*/ targets) {
			return protogenPeek(localgen, cFlowLocal, targets, true, true);
		}

		public ChainStmtBox genPop(LocalGeneratorEx localgen, Local cFlowLocal) {
			return protogenPop(localgen, cFlowLocal, true, true);
		}

		public ChainStmtBox genPush(LocalGeneratorEx localgen, Local cFlowLocal, List/*<Value>*/ values) {
			return protogenPush(localgen, cFlowLocal, values, true, true);
		}
		public Chain genInitCflowField(LocalGeneratorEx localgen, SootFieldRef field) {
			
			Chain c = new HashChain();
			Stmt s = Jimple.v().newAssignStmt(
					Jimple.v().newStaticFieldRef(field), 
					NullConstant.v());
			
			return c;
		}
	}
	
	private static class CflowOldCounterCodeGen extends CflowCodeGen {
		
		private CflowOldCounterCodeGen() {} 
		public static CflowOldCounterCodeGen v() { 
			return new CflowOldCounterCodeGen(); }
		
		private SootClass counterClass = null;
		private SootClass counterClass() { 
			if (counterClass == null) counterClass = 
				Scene.v().getSootClass("org.aspectbench.runtime.internal.CFlowCounter");
			return counterClass;
		}
		private Type counterType() { return counterClass().getType(); }
		
		public Type getCflowType() { return counterType(); }
		public SootClass getCflowInstanceClass() { return threadCounterClass(); }
		
		public String chooseName() { return "cflowCounter"; }
		
		private SootClass threadCounterClass = null;
		private SootClass threadCounterClass() { 
			if (threadCounterClass == null) threadCounterClass = 
				Scene.v().getSootClass("java.lang.Object");
			return threadCounterClass;
		}
		private Type threadCounterType() { return threadCounterClass().getType(); }
		
		private SootMethodRef getMethod() {
			return Scene.v().makeMethodRef(
					counterClass(),
					"getCounter",
					new ArrayList(),
					threadCounterType(),
					false);
		}	
		private SootMethodRef incMethod() {
			List types = new ArrayList(1);
			types.add(threadCounterType());
			return Scene.v().makeMethodRef(
					counterClass(),
					"incCounter",
					types,
					VoidType.v(),
					true);
		}
		private SootMethodRef decMethod() {
			List types = new ArrayList(1);
			types.add(threadCounterType());
			return Scene.v().makeMethodRef(
					counterClass(),
					"decCounter",
					types,
					VoidType.v(),
					true);
		}
		private SootMethodRef isValidMethod() {
			List types = new ArrayList(1);
			types.add(threadCounterType());
			return Scene.v().makeMethodRef(
					counterClass(),
					"isValidCounter",
					types,
					BooleanType.v(),
					true);
		}
		private SootMethodRef depthMethod() {
			List types = new ArrayList(1);
			types.add(threadCounterType());
			return Scene.v().makeMethodRef(
					counterClass(),
					"depthCounter",
					types,
					IntType.v(),
					true);
		}
		
		public Chain genInitLocal(LocalGeneratorEx localgen, Local cFlowLocal, Local cFlowInstance) {
			
			Chain  c = new HashChain();
			Stmt initLocal = 
				Jimple.v().newAssignStmt(
						cFlowLocal,
						Jimple.v().newVirtualInvokeExpr(
								cFlowInstance,
								getMethod(),
								new ArrayList()));
			c.add(initLocal);
			
			return c;
		}

		public ChainStmtBox genIsValid(LocalGeneratorEx localgen, Local cFlowLocal, Local result, Stmt succeed,
				Stmt fail) {
			
			Chain  c = new HashChain();
			List args = new ArrayList(1);
			args.add(cFlowLocal);
			Stmt testStmt = 
				Jimple.v().newAssignStmt(
						result,
						Jimple.v().newStaticInvokeExpr(
								isValidMethod(),
								args));
			c.add(testStmt);
			
			// Insert minimal number of jumps
			if (succeed == null && fail != null) 
				c.add(Jimple.v().newIfStmt(
						Jimple.v().newEqExpr(result, IntConstant.v(0)), 
						fail));
			else if (succeed != null && fail == null)
				c.add(Jimple.v().newIfStmt(
						Jimple.v().newNeExpr(result, IntConstant.v(0)), 
						succeed));
			else if (succeed != null && fail != null) {
				c.add(Jimple.v().newIfStmt(
						Jimple.v().newEqExpr(result, IntConstant.v(0)), 
						fail));
				c.add(Jimple.v().newGotoStmt(succeed));
			}
			
			
			return new ChainStmtBox(c, testStmt);
		}

		public Chain genDepth(LocalGeneratorEx localgen, Local cFlowLocal, Local result) {
			
			Chain  c = new HashChain();
			List args = new ArrayList(1);
			args.add(cFlowLocal);
			Stmt testStmt = 
				Jimple.v().newAssignStmt(
						result,
						Jimple.v().newStaticInvokeExpr(
								depthMethod(),
								args));
			c.add(testStmt);
			
			return c;
		}

		public Chain genPeek(LocalGeneratorEx localgen, Local cFlowLocal, List targets) {
			// Invalid operation
			throw new RuntimeException("Error: (old) counter codegen called to generate peek");
		}

		public ChainStmtBox genPop(LocalGeneratorEx localgen, Local cFlowLocal) {
			
			Chain  c = new HashChain();
			List args = new ArrayList(1);
			args.add(cFlowLocal);
			Stmt popStmt = 
				Jimple.v().newInvokeStmt(
						Jimple.v().newStaticInvokeExpr(
								decMethod(),
								args));
			c.add(popStmt);
			
			return new ChainStmtBox(c, popStmt);
		}

		public ChainStmtBox genPush(LocalGeneratorEx localgen, Local cFlowLocal, List values) {
			
			Chain  c = new HashChain();
			List args = new ArrayList(1);
			args.add(cFlowLocal);
			Stmt pushStmt = 
				Jimple.v().newInvokeStmt(
						Jimple.v().newStaticInvokeExpr(
								incMethod(),
								args));
			c.add(pushStmt);
			
			return new ChainStmtBox(c, pushStmt);
		}

		public void setFormals(List types) {
			if (types.size() > 0)
				throw new RuntimeException("Error: (old) counter codegen called with non-empty cflow formals.");
		}
	}
	
	private static class CflowOldStackCodeGen extends CflowCodeGen {

		private CflowOldStackCodeGen() {}
		public static CflowOldStackCodeGen v() 
		{ return new CflowOldStackCodeGen(); }
		
		private SootClass stackClass = null;
		private SootClass stackClass() { 
			if (stackClass == null) stackClass = 
				Scene.v().getSootClass("org.aspectbench.runtime.internal.CFlowStack");
			return stackClass;
		}
		private Type stackType() { return stackClass().getType(); }
		
		public Type getCflowType() { return stackType(); }
		public SootClass getCflowInstanceClass() { return threadStackClass(); }
		
		public String chooseName() { return "cflowStack"; }
		
		private SootClass threadStackClass = null;
		private SootClass threadStackClass() { 
			if (threadStackClass == null) threadStackClass = 
				Scene.v().getSootClass("java.lang.Object");
			return threadStackClass;
		}
		private Type threadStackType() { return threadStackClass().getType(); }
		
		private SootClass objectClass = null;
		private SootClass objectClass() { 
			if (objectClass == null) objectClass = 
				Scene.v().getSootClass("java.lang.Object");
			return objectClass;
		}
		private Type objectType() { return objectClass().getType(); }
		
		private SootMethodRef getMethod() {
			return Scene.v().makeMethodRef(
					stackClass(),
					"getStack",
					new ArrayList(),
					threadStackType(),
					false);
		}	
		private SootMethodRef pushMethod() {
			List types = new ArrayList(2);
			types.add(objectType().makeArrayType());
			types.add(threadStackType());
			return Scene.v().makeMethodRef(
					stackClass(),
					"pushStack",
					types,
					VoidType.v(),
					true);
		}
		private SootMethodRef popMethod() {
			List types = new ArrayList(1);
			types.add(threadStackType());
			return Scene.v().makeMethodRef(
					stackClass(),
					"popStack",
					types,
					VoidType.v(),
					true);
		}
		private SootMethodRef isValidMethod() {
			List types = new ArrayList(1);
			types.add(threadStackType());
			return Scene.v().makeMethodRef(
					stackClass(),
					"isValidStack",
					types,
					BooleanType.v(),
					true);
		}
		private SootMethodRef depthMethod() {
			List types = new ArrayList(1);
			types.add(threadStackType());
			return Scene.v().makeMethodRef(
					stackClass(),
					"depthStack",
					types,
					IntType.v(),
					true);
		}
		private SootMethodRef peekMethod() {
			List types = new ArrayList(2);
			types.add(IntType.v());
			types.add(threadStackType());
			return Scene.v().makeMethodRef(
					stackClass(),
					"getTopStack",
					types,
					objectType(),
					true);
		}
		
		public Chain genInitLocal(LocalGeneratorEx localgen, Local cFlowLocal, Local cFlowInstance) {
			
			Chain  c = new HashChain();
			Stmt initLocal = 
				Jimple.v().newAssignStmt(
						cFlowLocal,
						Jimple.v().newVirtualInvokeExpr(
								cFlowInstance,
								getMethod(),
								new ArrayList()));
			c.add(initLocal);
			
			return c;
		}
	
		
		public ChainStmtBox genIsValid(LocalGeneratorEx localgen, Local cFlowLocal, Local result, Stmt succeed,
				Stmt fail) {
			
			Chain  c = new HashChain();
			List args = new ArrayList(1);
			args.add(cFlowLocal);
			Stmt testStmt = 
				Jimple.v().newAssignStmt(
						result,
						Jimple.v().newStaticInvokeExpr(
								isValidMethod(),
								args));
			c.add(testStmt);
			
			// Insert minimal number of jumps
			if (succeed == null && fail != null) 
				c.add(Jimple.v().newIfStmt(
						Jimple.v().newEqExpr(result, IntConstant.v(0)), 
						fail));
			else if (succeed != null && fail == null)
				c.add(Jimple.v().newIfStmt(
						Jimple.v().newNeExpr(result, IntConstant.v(0)), 
						succeed));
			else if (succeed != null && fail != null) {
				c.add(Jimple.v().newIfStmt(
						Jimple.v().newEqExpr(result, IntConstant.v(0)), 
						fail));
				c.add(Jimple.v().newGotoStmt(succeed));
			}
			
			
			return new ChainStmtBox(c, testStmt);
		}

		public Chain genDepth(LocalGeneratorEx localgen, Local cFlowLocal, Local result) {
			
			Chain  c = new HashChain();
			List args = new ArrayList(1);
			args.add(cFlowLocal);
			Stmt testStmt = 
				Jimple.v().newAssignStmt(
						result,
						Jimple.v().newStaticInvokeExpr(
								depthMethod(),
								args));
			c.add(testStmt);
			
                        return c;
		}

		public Chain genPeek(LocalGeneratorEx localgen, Local cFlowLocal, List targets) {
			
			Chain c = new HashChain();
			
			Iterator it = targets.iterator(); int i = 0;
			while (it.hasNext()) {
				Local l = (Local)it.next();
				
				Local tempLocal = localgen.generateLocal(objectType(), "cflowBoundTemp");
				List args = new ArrayList(2);
				args.add(IntConstant.v(i));
				args.add(cFlowLocal);
				Stmt getStmt = 
					Jimple.v().newAssignStmt(
							tempLocal,
							Jimple.v().newStaticInvokeExpr(
									peekMethod(),
									args));
				c.add(getStmt);
				
				Chain copyChain = cgu.genCopy(localgen, tempLocal, l);
				c.addAll(copyChain);
				
				i++;
			}
			
			return c;
		}

		public ChainStmtBox genPop(LocalGeneratorEx localgen, Local cFlowLocal) {
			
			Chain  c = new HashChain();
			List args = new ArrayList(1);
			args.add(cFlowLocal);
			Stmt popStmt = 
				Jimple.v().newInvokeStmt(
						Jimple.v().newStaticInvokeExpr(
								popMethod(),
								args));
			c.add(popStmt);
			
			return new ChainStmtBox(c, popStmt);
		}

		public ChainStmtBox genPush(LocalGeneratorEx localgen, Local cFlowLocal, List values) {
			
			Chain c = new HashChain();
			// Create an array
			Local arrayLocal = localgen.generateLocal(
					objectType().makeArrayType(), "cflowBounds");
			Stmt createArrayStmt = 
				Jimple.v().newAssignStmt(
						arrayLocal,
						Jimple.v().newNewArrayExpr(
								objectType(),
								IntConstant.v(values.size())));
			c.add(createArrayStmt);
			// Copy each bound var into the array, boxing if necessary
			Iterator it = values.iterator(); int i = 0;
			while (it.hasNext()) {
				Value v = (Value)it.next();
				Local l = localgen.generateLocal(v.getType(), "arrayTemp");
				Stmt prepareValue = 
					Jimple.v().newAssignStmt(l, v);
				c.add(prepareValue);
				Chain arrayCopy = cgu.genCopyToArray(localgen, l, arrayLocal, i);
				c.addAll(arrayCopy);
				i++;
			}
			// Push the array onto the stack
			List params = new ArrayList(2);
			params.add(arrayLocal);
			params.add(cFlowLocal);
			Stmt pushArray = 
				Jimple.v().newInvokeStmt(
						Jimple.v().newStaticInvokeExpr(
								pushMethod(),
								params));
			c.add(pushArray);
			
			return new ChainStmtBox(c, pushArray);
		}

		public void setFormals(List types) {
			// Ignore it. Nothing to do
		}
	}
	
	/** A factory class for constructing Cflow CodeGen objects. This is the only
	 *  way to get a handle on an instance of CflowCodeGenUtils.CflowCodeGen */
	public static class CflowCodeGenFactory  {
		
		/** Return an instance of a Cflow codegen class appropriate for
		 * the given list of (types of) formals. Should always be used
		 * to get an instance of a Cflow codegen. Also sets the types
		 * of formals in the codegen instance.
		 * 
		 * @param types The list of types of formals of the cflow
		 * @return A subclass of CflowCodeGen to use to generate cflow code
		 */
		public static CflowCodeGen v(List/*<Type>*/ types) {
			boolean formalsEmpty = types.size() == 0;
			boolean useCounter = abc.main.options.OptionsParser.v().cflow_use_counters();
			boolean useOldRuntime = abc.main.options.OptionsParser.v().abc101runtime();
			boolean useSingleThreaded = Debug.v().forceSingleThreadedCflow;
			boolean useStaticField = Debug.v().forceStaticFieldCflow;
			
			CflowCodeGen g = null;
			
			if (!useOldRuntime) {
				if (useCounter && formalsEmpty) {
					if (useSingleThreaded || useStaticField) { 
						if (useStaticField)
							g = CflowCounterSingleThreadedStaticFieldCodeGen.v();
						else
							g = CflowCounterSingleThreadedCodeGen.v();
					}
					else
						g = CflowCounterGlobalCodeGen.v(); 
					}
				else {
					if (useSingleThreaded || useStaticField) {
						if (useStaticField)
							g = CflowStackSingleThreadedStaticFieldCodeGen.v();
						else
							g = CflowStackSingleThreadedCodeGen.v();
					}
					else
						g = CflowStackGlobalCodeGen.v();
				}
			} else {
				// FIXME: this is a quick hack to fix the thread-local problem
				// Cflow thread-local sharing is broken in the abc101 runtime (only)
				// so disable it in this case
				OptionsParser.v().set_cflow_share_thread_locals(false);
				if (useCounter && formalsEmpty)
					g = CflowOldCounterCodeGen.v();
				else
					g = CflowOldStackCodeGen.v();
			}
			
			g.setFormals(types);
			return g;
			}
	}

    /** Reset any static data used in the various cflow codegens
     */
    public static void reset() {
	// So far, nothing to reset
    }

}
