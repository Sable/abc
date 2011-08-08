package abc.ja.jpi.weaving;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

import polyglot.util.Position;

import soot.Local;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.IdentityStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.ParameterRef;
import soot.tagkit.LineNumberTag;
import abc.ja.jpi.jrag.ASTNode;
import abc.ja.jpi.jrag.AdviceDecl;
import abc.ja.jpi.jrag.AdviceSpec;
import abc.ja.jpi.jrag.Body;
import abc.ja.jpi.jrag.List;
import abc.ja.jpi.jrag.ParameterDeclaration;
import abc.ja.jpi.jrag.TypeAccess;
import abc.ja.jpi.jrag.TypeDecl;
import abc.weaving.aspectinfo.AbcFactory;
import abc.weaving.aspectinfo.AbcType;
import abc.weaving.aspectinfo.AroundAdvice;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.aspectinfo.MethodSig;
import abc.weaving.aspectinfo.Pointcut;

public class DummyAdvice {

	private static String aroundAdviceName = "";
	private static MethodSig proceedMethodSig;
	private static SootMethod aroundAdvice;
	private static int aroundAdviceCounter = 0;
	private static java.util.HashMap<soot.Value, soot.tagkit.Tag> tagMap;
	private static SootMethod proceedMethod;
	
	public static void reset(){
		aroundAdviceName = "";
		proceedMethodSig = null;
		aroundAdvice = null;
		aroundAdviceCounter = 0;
		tagMap = null;
		proceedMethod = null;		
	}

	public static void createDummyAroundAdvice(int sootTypeModifiers,
			List<ParameterDeclaration> parameterList, Type returnType) {
		aroundAdviceName = "";
		proceedMethod = null;
		aroundAdvice = null;
		tagMap = new java.util.HashMap<soot.Value, soot.tagkit.Tag>();
		proceedMethod = null;

		ArrayList parameters = new ArrayList();
		ArrayList paramnames = new ArrayList();
		for (int i = 0; i < parameterList.getNumChild(); i++) {
			parameters.add(parameterList.getChild(i).type().getSootType());
			paramnames.add(parameterList.getChild(i).name());
		}
		// provided by args -->soot.Type returnType = type().getSootType();
		// int modifiers = typeModifier;//sootTypeModifiers();
		ArrayList throwtypes = new ArrayList();
		// for(int i = 0; i < getNumException(); i++)
		// throwtypes.add(getException(i).type().getSootClassDecl());
		String name = getAroundAdviceName();
		String signature = SootMethod.getSubSignature(name, parameters,
				returnType);
		aroundAdvice = new SootMethod(name, parameters, returnType,
				sootTypeModifiers, throwtypes);
		aroundAdvice.addTag(new soot.tagkit.ParamNamesTag(paramnames));
		DummyAspect.getDummyAspectSootClass().addMethod(aroundAdvice);
		// createAspectInfo();
		// getAdviceSpec().jimplify1phase2();

	}

	public static String getAroundAdviceName() {
		aroundAdviceName = "around" + "$" + aroundAdviceCounter++;
		return aroundAdviceName;
	}

	public static int[] implicitParameters(
			List<ParameterDeclaration> parameterList) {
		int[] params = new int[3];
		for (int i = 0; i < 3; i++) {
			params[i] = -1;

			for (int j = 0; j < parameterList.getNumChild(); j++)
				if (parameterList.getChild(j).name()
						.equals(AdviceDecl.implicitVarName(i)))
					params[i] = j;
		}
		return params;
	}

	public static MethodSig proceedSig(Type returnType,
			List<ParameterDeclaration> parameterDeclaration) {
		return new MethodSig(soot.Modifier.PUBLIC | soot.Modifier.FINAL
				| soot.Modifier.STATIC, AbcFactory.AbcClass(DummyAspect
				.getDummyAspectSootClass()), AbcFactory.AbcType(returnType),
				proceedName(), proceedFormals(parameterDeclaration),
				new ArrayList(), new Position(""));
	}

	public static MethodSig methodSig(Type returnType,
			List<ParameterDeclaration> parameterDeclaration) {
		return new MethodSig(1, AbcFactory.AbcClass(DummyAspect
				.getDummyAspectSootClass()), AbcFactory.AbcType(returnType),
				aroundAdviceName, proceedFormals(parameterDeclaration),
				new ArrayList(),// abcExceptionList(),
				new Position(""));
	}

	public static String proceedName() {
		return "proceed" + aroundAdviceName.substring(6);
	}

	public static java.util.List proceedFormals(
			List<ParameterDeclaration> parameterDeclaration) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < parameterDeclaration.getNumChild(); i++)
			list.add(parameterDeclaration.getChild(i).formal());
		return list;
	}

	public static abc.weaving.aspectinfo.AdviceSpec adviceSpec(Type returnType,
			List<ParameterDeclaration> parameterDeclaration, TypeAccess jpi) {
		proceedMethodSig = proceedSig(returnType, parameterDeclaration);
		MethodCategory.register(proceedMethodSig, MethodCategory.PROCEED);
		AbcType return_type = AbcFactory.AbcType(returnType);
		return new CJPAroundAdvice(return_type, proceedMethodSig,
				proceedMethodSig.getPosition(), jpi);
	}

	public static SootMethod createProceedMethod(
			List<ParameterDeclaration> parameterDeclaration) {
		proceedMethod = new SootMethod(proceedMethodSig.getName(),
				proceedParamList(parameterDeclaration), proceedMethodSig
						.getReturnType().getSootType(),
				proceedMethodSig.getModifiers(), new ArrayList());
		return proceedMethod;

	}

	public static java.util.List proceedParamList(
			List<ParameterDeclaration> parameterDeclaration) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < parameterDeclaration.getNumChild(); i++)
			list.add(parameterDeclaration.getChild(i).type().getSootType());
		return list;
	}

	public static void jimplify2(
			List<ParameterDeclaration> parameterDeclaration,
			TypeDecl returnType, Value returnValue) {
		JimpleBody body = Jimple.v().newBody(aroundAdvice);
		aroundAdvice.setActiveBody(body);
		// Body b = new Body(hostType(), body, this);
		Stack chains = new java.util.Stack();
		chains.push(body.getUnits());
		LineNumberTag lineTag = new soot.tagkit.LineNumberTag(-1); // -1
																	// -->node.lineNumber()
		// false! --> if(!body.getMethod().isStatic())
		// emitThis(typeDecl);
		// emiting this
		soot.RefType aspectSootType = DummyAspect.getDummyAspectSootClass()
				.getType();
		soot.Local thisName = newLocal("this", aspectSootType, body);
		addStmt(Jimple.v().newIdentityStmt(thisName,
				Jimple.v().newThisRef(aspectSootType)), lineTag, chains);
		// jimplify2 -->parameters
		int localNum = 0;
		for (ParameterDeclaration pd : parameterDeclaration) {
			// b.setLine(this);
			Local local = newLocal(pd.name(), pd.type().getSootType(), body);
			addStmt(newIdentityStmt(
					local,
					newParameterRef(pd.type().getSootType(), localNum++/*
																		 * pd.
																		 * localNum
																		 * ()
																		 */, pd),
					pd), lineTag, chains);
		}
		if (!returnType.isVoid()) {
			int nextTempIndex = 0;
			Local local = newLocal("temp$" + nextTempIndex++,
					returnType.getSootType(), body);
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
		} else {
			addStmt(Jimple.v().newReturnVoidStmt(), lineTag, chains);
		}
		// jimplify2 AroundSpec
		body = Jimple.v().newBody(proceedMethod);
		proceedMethod.setActiveBody(body);
		// Body b = new Body(hostType(), body, this);
		chains = new java.util.Stack();
		chains.push(body.getUnits());
		lineTag = new soot.tagkit.LineNumberTag(-1); // -1 -->node.lineNumber()
		tagMap = new java.util.HashMap<soot.Value, soot.tagkit.Tag>();
		// jimplify2 -->parameters
		localNum = 0;
		for (ParameterDeclaration pd : parameterDeclaration) {
			// b.setLine(this);
			Local tempLocal = newLocal(pd.name(), pd.type().getSootType(), body);
			addStmt(newIdentityStmt(
					tempLocal,
					newParameterRef(pd.type().getSootType(), localNum++/*
																		 * pd.
																		 * localNum
																		 * ()
																		 */, pd),
					pd), lineTag, chains);
		}
		// jimplify2 --> body --> returnStmt
		addStmt(Jimple.v().newReturnVoidStmt(), lineTag, chains);
		MethodCategory.register(aroundAdvice, MethodCategory.ADVICE_BODY);
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

	private static void addStmt(soot.jimple.Stmt stmt, LineNumberTag lineTag,
			Stack chains) {
		stmt.addTag(lineTag);
		soot.PatchingChain<Unit> chain = (soot.PatchingChain<Unit>) chains
				.peek();
		if (stmt instanceof IdentityStmt && chain.size() != 0) {
			IdentityStmt idstmt = (IdentityStmt) stmt;
			if (!(idstmt.getRightOp() instanceof CaughtExceptionRef)) {
				soot.Unit s = chain.getFirst();
				while (s instanceof IdentityStmt)
					s = chain.getSuccOf((soot.jimple.Stmt) s);
				if (s != null) {
					chain.insertBefore(stmt, (soot.jimple.Stmt) s);
					// return this;
				}
			}
		}
		chain.add(stmt);
	}

	public static soot.jimple.ParameterRef newParameterRef(Type paramType,
			int number, ASTNode location) {
		soot.jimple.ParameterRef ref = Jimple.v().newParameterRef(paramType,
				number);
		createTag(ref, location);
		return ref;
	}

	public static void createTag(soot.Value value, ASTNode node) {
		if (node == null || tagMap.containsKey(value))
			return;
		if (node.getStart() != 0 && node.getEnd() != 0) {
			int line = node.getLine(node.getStart());
			int column = node.getColumn(node.getStart());
			int endLine = node.getLine(node.getEnd());
			int endColumn = node.getColumn(node.getEnd());
			String s = "";// node.sourceFile();
			s = s != null ? s.substring(s
					.lastIndexOf(java.io.File.separatorChar) + 1) : "Unknown";
			tagMap.put(value, new soot.tagkit.SourceLnNamePosTag(s, line,
					endLine, column, endColumn));
		} else {
			tagMap.put(value, new soot.tagkit.LineNumberTag(node.lineNumber()));
		}
	}

	public static soot.jimple.IdentityStmt newIdentityStmt(Value local,
			Value identityRef, ASTNode location) {
		soot.jimple.IdentityStmt stmt = Jimple.v().newIdentityStmt(local,
				identityRef);
		soot.tagkit.Tag left = tagMap.get(local);
		if (left != null)
			stmt.getLeftOpBox().addTag(left);
		soot.tagkit.Tag right = tagMap.get(identityRef);
		if (right != null)
			stmt.getRightOpBox().addTag(right);
		return stmt;
	}
}
