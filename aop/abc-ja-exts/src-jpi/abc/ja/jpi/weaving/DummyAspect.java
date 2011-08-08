package abc.ja.jpi.weaving;

import java.util.ArrayList;
import java.util.Stack;

import polyglot.util.Position;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Unit;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceFileTag;
import abc.ja.jpi.jrag.BooleanType;
import abc.ja.jpi.jrag.Expr;
import abc.ja.jpi.jrag.ExprStmt;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.AbcFactory;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.MethodCategory;

public class DummyAspect {

	private static SootClass dummyAspectSootClass = null;
	private static AbcClass dummyAspectAbcClass = null;
	private static Aspect dummyAspect = null;

	/***
	 * Creates an aspect in the front end. The user must add this aspect in
	 * globalinfo structure.
	 * 
	 * @param aspectName
	 */
	public static boolean createDummyAspect(String aspectName) {
		if (dummyAspect == null) {
			dummyAspectSootClass = new SootClass(aspectName, 1);
			dummyAspectAbcClass = AbcFactory.AbcClass(dummyAspectSootClass);

			Scene.v().addClass(dummyAspectSootClass);
			dummyAspectSootClass.setApplicationClass();
			dummyAspectSootClass.setSuperclass(Scene.v().getObjectType()
					.getSootClass());
			dummyAspect = new Aspect(AbcFactory.AbcClass(dummyAspectSootClass),
					null, new Position(""));
			// jimplify1phase2
			dummyAspectSootClass.setResolvingLevel(SootClass.DANGLING);
			dummyAspectSootClass.setModifiers(1); // 1 -->sootTypeModifiers()
			dummyAspectSootClass.setApplicationClass();
			SourceFileTag st = new soot.tagkit.SourceFileTag(""); // ""-->
																	// sourceNameWithoutPath()
			st.setAbsolutePath(""); // "" -->new
									// File(sourceFile()).getAbsolutePath()
			dummyAspectSootClass.addTag(st);
			// if(hasSuperclass()) {
			// dummyAspectSootClass.setSuperclass(superclass().getSootClassDecl());
			// }
			// for(Iterator iter = interfacesIterator(); iter.hasNext(); ) {
			// TypeDecl typeDecl = (TypeDecl)iter.next();
			// if(!dummyAspectSootClass.implementsInterface(typeDecl.getSootClassDecl().getName()))
			// dummyAspectSootClass.addInterface(typeDecl.getSootClassDecl());
			// }
			// if(isNestedType())
			// dummyAspectSootClass.setOuterClass(enclosingType().getSootClassDecl());
			dummyAspectSootClass.setResolvingLevel(SootClass.HIERARCHY);
			/*
			 * jimplify1phase2 over its children
			 */
			// super.jimplify1phase2();
			ConstructorDecl_jimplify("<init>", dummyAspectSootClass.getType());
			MethodDecl_jimplify("aspectOf", 9, dummyAspectSootClass.getType(),
					soot.jimple.NullConstant.v(),
					dummyAspectSootClass.getType());
			MethodDecl_jimplify("hasAspect", 9, soot.BooleanType.v(),
					BooleanType.emitConstant(true),
					dummyAspectSootClass.getType());
			dummyAspectSootClass.setResolvingLevel(SootClass.SIGNATURES);
			// jimplify2
			dummyAspectSootClass.setResolvingLevel(SootClass.BODIES);
			return true;
		}
		return false;
	}

	public static void ConstructorDecl_jimplify(String name,
			RefType aspectSootType) {
		ArrayList parameters = new ArrayList();
		ArrayList paramnames = new ArrayList();
		// this$0
		// TypeDecl typeDecl = hostType();
		// if(typeDecl.needsEnclosing())
		// parameters.add(typeDecl.enclosingType().getSootType());
		// if(typeDecl.needsSuperEnclosing()) {
		// TypeDecl superClass = ((ClassDecl)typeDecl).superclass();
		// parameters.add(superClass.enclosingType().getSootType());
		// }
		// args
		// for(int i = 0; i < getNumParameter(); i++) {
		// parameters.add(getParameter(i).type().getSootType());
		// paramnames.add(getParameter(i).name());
		// }
		soot.Type returnType = soot.VoidType.v();
		int modifiers = 0; // 0--> sootTypeModifiers();
		ArrayList throwtypes = new ArrayList();
		// for(int i = 0; i < getNumException(); i++)
		// throwtypes.add(getException(i).type().getSootClassDecl());
		String signature = SootMethod.getSubSignature(name, parameters,
				returnType);
		// if(!hostType().getSootClassDecl().declaresMethod(signature)) { we
		// always need the constructor
		SootMethod sootMethod = new SootMethod(name, parameters, returnType,
				modifiers, throwtypes);
		dummyAspectSootClass.addMethod(sootMethod); // dummyAspectSootClass -->
													// hostType().getSootClassDecl()
		sootMethod.addTag(new soot.tagkit.ParamNamesTag(paramnames));
		// }
		// addAttributes()

		// jimplify2
		// if(!generate() || sootMethod().hasActiveBody() ||
		// sootMethod().getSource() != null) return;
		JimpleBody body = Jimple.v().newBody(sootMethod);// Jimple.v().newBody(sootMethod());
		sootMethod.setActiveBody(body);

		// !!Body b = new Body(hostType(), body, this);//new Body(hostType(),
		// body, this);
		Stack chains = new java.util.Stack();
		chains.push(body.getUnits());
		LineNumberTag lineTag = new soot.tagkit.LineNumberTag(-1); // -1
																	// -->node.lineNumber()
		// false! --> if(!body.getMethod().isStatic())
		// emitThis(typeDecl);
		// emiting this
		soot.Local thisName = newLocal("this", aspectSootType, body);
		addStmt(Jimple.v().newIdentityStmt(thisName,
				Jimple.v().newThisRef(aspectSootType)), lineTag, chains);
		// !!b.setLine(this);

		// for(int i = 0; i < getNumParameter(); i++)
		// getParameter(i).jimplify2(b);

		boolean needsInit = true;

		// if(hasConstructorInvocation()) {
		// getConstructorInvocation().jimplify2(b);
		SootMethodRef methodRef = Scene.v().makeConstructorRef(
				dummyAspectSootClass.getSuperclass(), parameters);
		soot.jimple.SpecialInvokeExpr specialInovkeExpr = Jimple.v()
				.newSpecialInvokeExpr(thisName, methodRef, parameters);
		java.util.HashMap<soot.Value, soot.tagkit.Tag> tagMap = new java.util.HashMap<soot.Value, soot.tagkit.Tag>();
		tagMap.put(specialInovkeExpr, new soot.tagkit.LineNumberTag(-1)); // -1
																			// -->node.lineNumber()
		// if(value instanceof soot.jimple.InvokeExpr) {
		// b.add(b.newInvokeStmt((soot.jimple.InvokeExpr)value, this));
		soot.jimple.InvokeStmt stmt = Jimple.v().newInvokeStmt(
				specialInovkeExpr);
		soot.tagkit.Tag tag = tagMap.get(specialInovkeExpr);
		stmt.getInvokeExprBox().addTag(tag);
		addStmt(stmt, lineTag, chains);
		if (stmt instanceof ExprStmt) {
			ExprStmt exprStmt = (ExprStmt) stmt;
			Expr expr = exprStmt.getExpr();
			if (!expr.isSuperConstructorAccess())
				needsInit = false;
		}
		addStmt(Jimple.v().newReturnVoidStmt(), lineTag, chains);// b.add(b.newReturnVoidStmt(null));
		// }
		MethodCategory.register(sootMethod, 0);// 0--> category
	}

	/***
	 * Add the stmt in the stack chain.
	 * 
	 * @param stmt
	 * @param lineTag
	 * @param chains
	 */
	private static void addStmt(soot.jimple.Stmt stmt, LineNumberTag lineTag,
			Stack chains) {
		// if(list != null) {
		// list.add(stmt);
		// list = null;
		// }
		stmt.addTag(lineTag);
		soot.PatchingChain<Unit> chain = (soot.PatchingChain<Unit>) chains
				.peek();
		// if(stmt instanceof IdentityStmt && chain.size() != 0) {
		// IdentityStmt idstmt = (IdentityStmt) stmt;
		// if(!(idstmt.getRightOp() instanceof CaughtExceptionRef)) {
		// soot.Unit s = chain.getFirst();
		// while(s instanceof IdentityStmt)
		// s = chain.getSuccOf((soot.jimple.Stmt)s);
		// if(s != null) {
		// chain.insertBefore(stmt, (soot.jimple.Stmt)s);
		// return this;
		// }
		// }
		// }
		chain.add(stmt);
	}

	/***
	 * Creates and adds a new jimple local var
	 * 
	 * @param name
	 * @param type
	 * @param body
	 * @return Local
	 */
	private static Local newLocal(String name, soot.Type type, JimpleBody body) {
		Local local = Jimple.v().newLocal(name, type);
		body.getLocals().add(local);
		// if(name.equals("this") && thisName == null)
		// thisName = local;
		return local;
	}

	/***
	 * Creates and registers a SootMethod and then It does the jimplify process
	 * properly (1_phase2, 2)
	 * 
	 * @param name
	 * @param typeModifier
	 * @param returnType
	 * @param returnValue
	 * @param aspectSootType
	 */
	public static void MethodDecl_jimplify(String name, int typeModifier,
			soot.Type returnType, Value returnValue, RefType aspectSootType) {
		ArrayList parameters = new ArrayList();
		ArrayList paramnames = new ArrayList();
		// for(int i = 0; i < getNumParameter(); i++) {
		// parameters.add(getParameter(i).type().getSootType());
		// paramnames.add(getParameter(i).name());
		// }
		// provided by args -->soot.Type returnType = type().getSootType();
		int modifiers = typeModifier;// sootTypeModifiers();
		ArrayList throwtypes = new ArrayList();
		// for(int i = 0; i < getNumException(); i++)
		// throwtypes.add(getException(i).type().getSootClassDecl());
		String signature = SootMethod.getSubSignature(name, parameters,
				returnType);
		SootMethod sootMethod = new SootMethod(name, parameters, returnType,
				modifiers, throwtypes);
		dummyAspectSootClass.addMethod(sootMethod);// hostType().getSootClassDecl().addMethod(m);
		sootMethod.addTag(new soot.tagkit.ParamNamesTag(paramnames));
		// sootMethod = m;
		// jimplify2
		// if(!generate() || sootMethod().hasActiveBody() ||
		// sootMethod().getSource() != null) return;
		// try {
		// if(hasBlock() && sootMethod().isConcrete() &&
		// !(hostType().isInterfaceDecl())) {
		JimpleBody body = Jimple.v().newBody(sootMethod);
		sootMethod.setActiveBody(body);

		// entering to the Body constructor
		Stack chains = new java.util.Stack();
		chains.push(body.getUnits());
		LineNumberTag lineTag = new soot.tagkit.LineNumberTag(-1); // -1
																	// -->node.lineNumber()
		java.util.HashMap<soot.Value, soot.tagkit.Tag> tagMap = new java.util.HashMap<soot.Value, soot.tagkit.Tag>();
		// false! --> if(!body.getMethod().isStatic())
		// emitThis(typeDecl);
		// emiting this
		if (!sootMethod.isStatic()) {
			soot.Local thisName = newLocal("this", aspectSootType, body);
			addStmt(Jimple.v().newIdentityStmt(thisName,
					Jimple.v().newThisRef(aspectSootType)), lineTag, chains);
		}
		// }
		// }
		// getBlock().jimplify2(b);
		int nextTempIndex = 0;
		Local local = newLocal("temp$" + nextTempIndex++, returnType, body);
		soot.jimple.AssignStmt assignStmt = Jimple.v().newAssignStmt(local,
				returnValue);
		soot.tagkit.Tag left = tagMap.get(local);
		if (left != null)
			assignStmt.getLeftOpBox().addTag(left);
		soot.tagkit.Tag right = tagMap.get(returnValue);
		if (right != null)
			assignStmt.getRightOpBox().addTag(right);
		soot.tagkit.Tag tag = tagMap.get(returnValue);
		if (tag != null)
			tagMap.put(local, tag);
		addStmt(assignStmt, lineTag, chains);
		ArrayList list = null; // exceptionRanges();
		soot.jimple.ReturnStmt returnStmt = Jimple.v().newReturnStmt(local);
		tag = tagMap.get(local);
		if (tag != null)
			returnStmt.getOpBox().addTag(tag);
		addStmt(returnStmt, lineTag, chains);
		MethodCategory.register(sootMethod, 1); // 1-->category
	}

	public static SootClass getDummyAspectSootClass() {
		return dummyAspectSootClass;
	}

	public static AbcClass getDummyAspectAbcClass() {
		return dummyAspectAbcClass;
	}

	public static Aspect getDummyAspect() {
		return dummyAspect;
	}

}
