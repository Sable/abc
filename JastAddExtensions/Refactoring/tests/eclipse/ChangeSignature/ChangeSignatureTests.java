/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package tests.eclipse.ChangeSignature;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.BooleanLiteral;
import AST.GenericTypeDecl;
import AST.IntegerLiteral;
import AST.Literal;
import AST.MethodDecl;
import AST.NullLiteral;
import AST.ParameterDeclaration;
import AST.Program;
import AST.RefactoringException;
import AST.SimpleSet;
import AST.TypeDecl;

/**
 * @see org.eclipse.jdt.core.Signature for encoding of signature strings.
 */
public class ChangeSignatureTests extends TestCase {
	private static final String REFACTORING_PATH= "ChangeSignature/";

	public ChangeSignatureTests(String name) {
		super(name);
	}

	protected String getRefactoringPath() {
		return REFACTORING_PATH;
	}

	private String getSimpleTestFileName(boolean canReorder, boolean input){
		String fileName = "A_" + getName();
		if (canReorder)
			fileName += input ? "_in": "_out";
		return fileName + ".java";
	}

	private String getTestFileName(boolean canReorder, boolean input){
		String fileName= getTestFolderPath(canReorder);
		return fileName + getSimpleTestFileName(canReorder, input);
	}

	private String getTestFolderPath(boolean canModify) {
		String fileName= "tests/eclipse/" + getRefactoringPath();
		fileName += (canModify ? "canModify/": "cannotModify/");
		return fileName;
	}
	
	//---helpers

	private void helperAdd(String[] signature, String[] newTypes, String[] newNames, Literal[] newDefaultValues, int[] newIndices) throws Exception {
		helperAdd(signature, newTypes, newNames, newDefaultValues, newIndices, false);
	}

	private void helperAdd(String[] signature, String[] newTypes, String[] newNames, Literal[] newDefaultValues, int[] newIndices, boolean createDelegate) throws Exception {
		Program in = CompileHelper.compile(getTestFileName(true, true));
		Program out = CompileHelper.compile(getTestFileName(true, false));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		
		TypeDecl td = in.findSimpleType("A");
		assertNotNull(td);
		MethodDecl md = td.findMethod("m");
		assertNotNull(md);
		
		try {
			for(int i=0;i<newTypes.length;++i) {
				TypeDecl parmtp = in.findType(newTypes[i]);
				assertNotNull(parmtp);
				if(parmtp.isGenericType())
					parmtp = ((GenericTypeDecl)parmtp).rawType();
				md.doAddParameter(new ParameterDeclaration(parmtp.createLockedAccess(), newNames[i]), newIndices[i], newDefaultValues[i], createDelegate);
			}
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helperPermuteFail(int[] perm) {
		Program in = CompileHelper.compile(getTestFileName(false, true));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		
		TypeDecl td = in.findSimpleType("A");
		assertNotNull(td);
		MethodDecl md = td.findMethod("m");
		assertNotNull(md);
		
		try {
			md.doPermuteParameters(perm, false);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) {
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	/*
	 * Rename method 'A.m(signature)' to 'A.newMethodName(signature)'
	 */
	private void helperRenameMethod(String[] signature, String newMethodName, boolean createDelegate) throws Exception {
		Program in = CompileHelper.compile(getTestFileName(true, true));
		Program out = CompileHelper.compile(getTestFileName(true, false));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		
		TypeDecl td = in.findSimpleType("A");
		assertNotNull(td);
		MethodDecl md = td.findMethod("m");
		assertNotNull(md);
		
		try {
			md.rename(newMethodName);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper1(String[] newOrder, String[] signature) throws Exception{
		helper1(newOrder, signature, null, null);
	}

	private void helper1(String[] newOrder, String[] signature, boolean createDelegate) throws Exception{
		helper1(newOrder, signature, null, null, createDelegate);
	}

	private void helper1(String[] newOrder, String[] signature, String[] oldNames, String[] newNames) throws Exception{
		helper1(newOrder, signature, oldNames, newNames, false);
	}

	private void helper1(String[] newOrder, String[] signature, String[] oldNames, String[] newNames, boolean createDelegate) throws Exception{
		Program in = CompileHelper.compile(getTestFileName(true, true));
		Program out = CompileHelper.compile(getTestFileName(true, false));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		
		TypeDecl td = in.findSimpleType("A");
		assertNotNull(td);
		MethodDecl md = td.findMethod("m");
		assertNotNull(md);
		
		try {
			int[] perm = new int[md.getNumParameter()];
			for(int i=0;i< md.getNumParameter();++i) {
				ParameterDeclaration pd = md.getParameter(i);
				for(int j=0;j<newOrder.length;++j) {
					if(newOrder[j].equals(pd.name())) {
						perm[i] = j;
						break;
					}
				}
			}
			md.doPermuteParameters(perm, createDelegate);
			if(oldNames != null) {
				for(int i=0;i<oldNames.length;++i) {
					SimpleSet s = md.parameterDeclaration(oldNames[i]);
					assertTrue(s instanceof ParameterDeclaration);
					((ParameterDeclaration)s).rename(newNames[i]);
				}
			}
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}
	
	private void helperDoAll(String typeName, String methodName, String[] newNames, String[] newTypes, Literal[] newDefaultValues,
			int[] newIndices, String[] newParamNames, int[] permutation, int[] deletedIndices, boolean createDelegate) {
		Program in = CompileHelper.compile(getTestFileName(true, true));
		Program out = CompileHelper.compile(getTestFileName(true, false));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		
		TypeDecl td = in.findSimpleType(typeName);
		assertNotNull(td);
		MethodDecl md = td.findMethod(methodName);
		assertNotNull(md);
		
		try {
			md.doPermuteParameters(permutation, createDelegate);
			for(int i=0; i<md.getNumParameter(); ++i)
				md.getParameter(i).rename(newParamNames[permutation[i]]);
			for(int i=0;newTypes!=null&&i<newTypes.length;++i) {
				TypeDecl parmtp = in.findType(newTypes[i]);
				assertNotNull(parmtp);
				if(parmtp.isGenericType())
					parmtp = ((GenericTypeDecl)parmtp).rawType();
				md.doAddParameter(new ParameterDeclaration(parmtp.createLockedAccess(), newNames[i]), newIndices[i], newDefaultValues[i], false);
			}
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	//------- tests

	public void testFail0() throws Exception{
		helperPermuteFail(new int[]{1, 0});
	}

	/* disabled: tests invalid arguments
	public void testFail1() throws Exception{
		helperFail(new String[0], new String[0]);
	}*/

	/* disabled: does not compile
	public void testFailAdd2() throws Exception{
		String[] signature= {"I"};
		String[] newNames= {"x"};
		String[] newTypes= {"int"};
		Literal[] newDefaultValues= {new IntegerLiteral(0)};
		int[] newIndices= {0};
		helperAddFail(signature, newNames, newTypes, newDefaultValues, newIndices);
	}*/

	/* disabled: these test invalid arguments
	public void testFailAdd3() throws Exception{
		String[] signature= {"I"};
		String[] newNames= {"x"};
		String[] newTypes= {"int"};
		String[] newDefaultValues= {"not good"};
		int[] newIndices= {0};
		helperAddFail(signature, newNames, newTypes, newIndices);
	}

	public void testFailAdd4() throws Exception{
		String[] signature= {"I"};
		String[] newNames= {"x"};
		String[] newTypes= {"not a type"};
		Literal[] newDefaultValues= {new IntegerLiteral(0)};
		int[] newIndices= {0};
		helperAddFail(signature, newNames, newTypes, newIndices);
	}

	public void testFailDoAll5()throws Exception{
		String[] signature= {"I"};
		String[] newNames= null;
		String[] newTypes= null;
		String[] newDefaultValues= null;
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= null;

		String[] oldParamNames= {"i", "j"};
		String[] newParamNames= {"i", "j"};
		int[] permutation= {0};
		int[] deletedIndices= {0};
		int newVisibility= Modifier.NONE;
		int expectedSeverity= RefactoringStatus.ERROR;
		helperDoAllFail("m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, expectedSeverity);
	}

	public void testFailDoAll6()throws Exception{
		String[] signature= {"I"};
		String[] newNames= {"a"};
		String[] newTypes= {"Certificate"};
		String[] newDefaultValues= {"null"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};

		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {0};
		int[] deletedIndices= {0};
		int newVisibility= Modifier.NONE;
		int expectedSeverity= RefactoringStatus.ERROR;
		helperDoAllFail("m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, expectedSeverity);
	}

	public void testFailDoAll7()throws Exception{
		String[] signature= {"I"};
		String[] newNames= {"a"};
		String[] newTypes= {"Fred"};
		String[] newDefaultValues= {"null"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};

		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {0};
		int[] deletedIndices= {0};
		int newVisibility= Modifier.NONE;
		int expectedSeverity= RefactoringStatus.ERROR;
		helperDoAllFail("m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, expectedSeverity);
	}

	public void testFailDoAll8()throws Exception{
		String[] signature= {"I"};
		ParameterInfo[] newParamInfo= null;
		int[] newIndices= {0};

		String[] oldParamNames= {"I"};
		String[] newParamNames= {};
		int[] permutation= {};
		int[] deletedIndices= {0};
		int newVisibility= Modifier.NONE;
		int expectedSeverity= RefactoringStatus.ERROR;
		helperDoAllFail("run", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, expectedSeverity);
	}

	public void testFailAnnotation1() throws Exception{
		IType classA= getType(createCUfromTestFile(getPackageP(), false, false), "A");
		IMethod method= classA.getMethod("name", new String[0]);
		assertNotNull(method);
		assertFalse(RefactoringAvailabilityTester.isChangeSignatureAvailable(method));
	}*/

	public void testFailVararg01() throws Exception {
		helperPermuteFail(new int[]{1, 0});
	}

	/* disabled: change type
	public void testFailVararg02() throws Exception {
		//cannot introduce vararg in non-last position
		String[] signature= {"I", "[QString;"};
		String[] newNames= null;
		String[] newTypes= null;
		String[] newDefaultValues= null;
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= null;

		String[] oldParamNames= {"i", "names"};
		String[] newParamNames= {"i", "names"};
		String[] newParamTypeNames= {"int...", "String[]"};
		int[] permutation= {0, 1};
		int[] deletedIndices= {};
		int newVisibility= Modifier.PUBLIC;
		int expectedSeverity= RefactoringStatus.FATAL;
		helperDoAllFail("m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, newParamTypeNames, permutation, newVisibility, deletedIndices, expectedSeverity);
	}

	public void testFailVararg03() throws Exception {
		//cannot change parameter type which is vararg in overriding method
		String[] signature= {"I", "[QString;"};
		String[] newNames= null;
		String[] newTypes= null;
		String[] newDefaultValues= null;
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= null;

		String[] oldParamNames= {"i", "names"};
		String[] newParamNames= {"i", "names"};
		String[] newParamTypeNames= {"int", "Object[]"};
		int[] permutation= {1, 0};
		int[] deletedIndices= {};
		int newVisibility= Modifier.PUBLIC;
		int expectedSeverity= RefactoringStatus.FATAL;
		helperDoAllFail("m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, newParamTypeNames, permutation, newVisibility, deletedIndices, expectedSeverity);
	}

	public void testFailVararg04() throws Exception {
		//cannot change vararg to non-vararg
		String[] signature= {"I", "[QString;"};
		String[] newNames= null;
		String[] newTypes= null;
		String[] newDefaultValues= null;
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= null;

		String[] oldParamNames= {"i", "names"};
		String[] newParamNames= {"i", "names"};
		String[] newParamTypeNames= {"int", "String[]"};
		int[] permutation= {0, 1};
		int[] deletedIndices= {};
		int newVisibility= Modifier.PUBLIC;
		int expectedSeverity= RefactoringStatus.FATAL;
		helperDoAllFail("m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, newParamTypeNames, permutation, newVisibility, deletedIndices, expectedSeverity);
	}*/

	public void testFailVararg05() throws Exception {
		helperPermuteFail(new int[]{1, 0});
	}

	/* disabled: test would fail as expected, but test harness hard to set up
	public void testFailGenerics01() throws Exception {
		//type variable name may not be available in related methods
		String[] signature= {"QE;"};
		String[] newNames= {"e2"};
		String[] newTypes= {"E"};
		String[] newDefaultValues= {"null"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {1};

		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {};
		int[] deletedIndices= {};
		int newVisibility= Modifier.NONE;
		int expectedSeverity= RefactoringStatus.ERROR;
		helperDoAllFail("m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, expectedSeverity);
	}*/

	//---------
	public void test0() throws Exception{
		helper1(new String[]{"j", "i"}, new String[]{"I", "I"});
	}

	public void test1() throws Exception{
		helper1(new String[]{"j", "i"}, new String[]{"I", "I"});
	}

	public void test2() throws Exception{
		helper1(new String[]{"j", "i"}, new String[]{"I", "I"});
	}

	public void test3() throws Exception{
		helper1(new String[]{"j", "i"}, new String[]{"I", "I"});
	}

	public void test4() throws Exception{
		helper1(new String[]{"j", "i"}, new String[]{"I", "I"});
	}

	public void test5() throws Exception{
		helper1(new String[]{"j", "i"}, new String[]{"I", "I"});
	}

	public void test6() throws Exception{
		helper1(new String[]{"k", "i", "j"}, new String[]{"I", "I", "I"});
	}

	public void test7() throws Exception{
		helper1(new String[]{"i", "k", "j"}, new String[]{"I", "I", "I"});
	}

	public void test8() throws Exception{
		helper1(new String[]{"k", "j", "i"}, new String[]{"I", "I", "I"});
	}

	public void test9() throws Exception{
		helper1(new String[]{"j", "i", "k"}, new String[]{"I", "I", "I"});
	}

	public void test10() throws Exception{
		helper1(new String[]{"j", "k", "i"}, new String[]{"I", "I", "I"});
	}

	public void test11() throws Exception{
		helper1(new String[]{"j", "k", "i"}, new String[]{"I", "I", "I"});
	}

	public void test12() throws Exception{
		helper1(new String[]{"j", "k", "i"}, new String[]{"I", "I", "I"});
	}

	public void test13() throws Exception{
		helper1(new String[]{"j", "k", "i"}, new String[]{"I", "I", "I"});
	}

	public void test14() throws Exception{
		helper1(new String[]{"j", "i"}, new String[]{"I", "I"});
	}

	public void test15() throws Exception{
		helper1(new String[]{"b", "i"}, new String[]{"I", "Z"}, true);
	}

	public void test16() throws Exception{
		helper1(new String[]{"b", "i"}, new String[]{"I", "Z"}, true);
	}

	public void test17() throws Exception{
		//exception because of bug 11151
		helper1(new String[]{"b", "i"}, new String[]{"I", "Z"}, true);
	}

	public void test18() throws Exception{
		//exception because of bug 11151
		helper1(new String[]{"b", "i"}, new String[]{"I", "Z"}, true);
	}

	public void test19() throws Exception{
//		printTestDisabledMessage("bug 7274 - reorder parameters: incorrect when parameters have more than 1 modifiers");
		helper1(new String[]{"b", "i"}, new String[]{"I", "Z"}, true);
	}
	public void test20() throws Exception{
//		printTestDisabledMessage("bug 18147");
		helper1(new String[]{"b", "a"}, new String[]{"I", "[I"}, true);
	}

//constructor tests
	/* disabled
	public void test21() throws Exception{
		helperPermute(1, 0);
	}
	public void test22() throws Exception{
		helperPermute(1, 0);
	}
	public void test23() throws Exception{
		helperPermute(1, 0);
	}
	public void test24() throws Exception{
		helperPermute(1, 0);
	}
	public void test25() throws Exception{
		helperPermute(1, 0);
	}
	public void test26() throws Exception{
		helperPermute(1, 0);
	}

	public void test27() throws Exception{
		String[] signature= {"QString;", "QObject;", "I"};
		String[] newNames = {"newParam"};
		String[] newTypes = {"Object"};
		Literal[] newDefaultValues = {new NullLiteral("null")};
		int[] newIndices = { 3 };
	}*/

	public void testRenameReorder26() throws Exception{
		helper1(new String[]{"a", "y"}, new String[]{"Z", "I"}, new String[]{"y", "a"}, new String[]{"zzz", "bb"}, true);
	}

	public void testRenameReorder27() throws Exception{
		helper1(new String[]{"a", "y"}, new String[]{"Z", "I"}, new String[]{"y", "a"}, new String[]{"yyy", "a"}, true);
	}

	public void testAdd28()throws Exception{
		String[] signature= {"I"};
		String[] newNames= {"x"};
		String[] newTypes= {"int"};
		Literal[] newDefaultValues= { new IntegerLiteral(0) };
		int[] newIndices= {1};
		helperAdd(signature, newTypes, newNames, newDefaultValues, newIndices, true);
	}

	public void testAdd29()throws Exception{
		String[] signature= {"I"};
		String[] newNames= {"x"};
		String[] newTypes= {"int"};
		Literal[] newDefaultValues= { new IntegerLiteral(0) };
		int[] newIndices= {0};
		helperAdd(signature, newTypes, newNames, newDefaultValues, newIndices, true);
	}

	public void testAdd30()throws Exception{
		String[] signature= {"I"};
		String[] newNames= {"x"};
		String[] newTypes= {"int"};
		Literal[] newDefaultValues= { new IntegerLiteral(0) };
		int[] newIndices= {1};
		helperAdd(signature, newTypes, newNames, newDefaultValues, newIndices, true);
	}

	public void testAdd31()throws Exception{
		String[] signature= {"I"};
		String[] newNames= {"x"};
		String[] newTypes= {"int"};
		Literal[] newDefaultValues= { new IntegerLiteral(0) };
		int[] newIndices= {1};
		helperAdd(signature, newTypes, newNames, newDefaultValues, newIndices, true);
	}

	public void testAdd32()throws Exception{
		String[] signature= {"I"};
		String[] newNames= {"x"};
		String[] newTypes= {"int"};
		Literal[] newDefaultValues= { new IntegerLiteral(0) };
		int[] newIndices= {0};
		helperAdd(signature, newTypes, newNames, newDefaultValues, newIndices, true);
	}

	public void testAdd33()throws Exception{
		String[] signature= {};
		String[] newNames= {"x"};
		String[] newTypes= {"int"};
		Literal[] newDefaultValues= { new IntegerLiteral(0) };
		int[] newIndices= {0};
		helperAdd(signature, newTypes, newNames, newDefaultValues, newIndices, true);
	}

	public void testAddReorderRename34()throws Exception{
		String[] newNames= {"x"};
		String[] newTypes= {"Object"};
		Literal[] newDefaultValues= {new NullLiteral("null")};
		int[] newIndices= {1};

		String[] newParamNames= {"i", "jj"};
		int[] permutation= {1, 0};
		int[] deletedIndices= null;
		helperDoAll("A", "m", newNames, newTypes, newDefaultValues, newIndices, newParamNames, permutation, deletedIndices, true);
	}

	/* disabled: no support for adjusting visibility
	public void testAll35()throws Exception{
		String[] newNames= null;
		String[] newTypes= null;
		Literal[] newDefaultValues= null;
		int[] newIndices= null;

		String[] oldParamNames= {"iii", "j"};
		String[] newParamNames= oldParamNames;
		int[] permutation= {0, 1};
		int[] deletedIndices= null;
		int newVisibility= VIS_PUBLIC;
		helperDoAll("A", "m", newNames, newTypes, newDefaultValues, newIndices, newParamNames, permutation, deletedIndices, true);
	}*/

	/* disabled: no support for adjusting visibility
	public void testAll36()throws Exception{
		String[] signature= {"I", "Z"};
		String[] newNames= null;
		String[] newTypes= null;
		Literal[] newDefaultValues= null;
		int[] newIndices= null;

		String[] oldParamNames= {"iii", "j"};
		String[] newParamNames= oldParamNames;
		int[] permutation= {0, 1};
		int[] deletedIndices= null;
		//int newVisibility= Modifier.PRIVATE;
		String newReturnTypeName= null;
		helperDoAll("A", "m", newNames, newTypes, newDefaultValues, newIndices, newParamNames, permutation, deletedIndices, true);
	}*/

	/* disabled: no support for adjusting visibility
	public void testAll37()throws Exception{
		String[] signature= {"I", "Z"};
		String[] newNames= null;
		String[] newTypes= null;
		Literal[] newDefaultValues= null;
		int[] newIndices= null;

		String[] oldParamNames= {"iii", "j"};
		String[] newParamNames= oldParamNames;
		int[] permutation= {0, 1};
		int[] deletedIndices= null;
		//int newVisibility= Modifier.PROTECTED;
		String newReturnTypeName= null;
		helperDoAll("A", "m", newNames, newTypes, newDefaultValues, newIndices, newParamNames, permutation, deletedIndices, true);
	}*/

	/* disabled: no support for adjusting visibility
	public void testAll38()throws Exception{
		String[] signature= {"I", "Z"};
		String[] newNames= null;
		String[] newTypes= null;
		Literal[] newDefaultValues= null;
		int[] newIndices= null;

		String[] oldParamNames= {"iii", "j"};
		String[] newParamNames= oldParamNames;
		int[] permutation= {0, 1};
		int[] deletedIndices= null;
		//int newVisibility= Modifier.PROTECTED;
		String newReturnTypeName= null;
		helperDoAll("A", "m", newNames, newTypes, newDefaultValues, newIndices, newParamNames, permutation, deletedIndices, true);
	}*/

	/* disabled: no support for adjusting visibility
	public void testAll39()throws Exception{
		String[] signature= {"I", "Z"};
		String[] newNames= {"x"};
		String[] newTypes= {"Object"};
		Literal[] newDefaultValues= {new NullLiteral("null")};
		int[] newIndices= {1};

		String[] oldParamNames= {"iii", "j"};
		String[] newParamNames= {"i", "jj"};
		int[] permutation= {1, 0};
		int[] deletedIndices= null;
		//int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= null;
		helperDoAll("A", "m", newNames, newTypes, newDefaultValues, newIndices, newParamNames, permutation, deletedIndices, false);
	}*/

	/* disabled: cannot parse "int[]"
	public void testAll40()throws Exception{
		String[] signature= {"I", "Z"};
		String[] newNames= {"x"};
		String[] newTypes= {"int[]"};
		String[] newDefaultValues= {"null"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {1};

		String[] oldParamNames= {"iii", "j"};
		String[] newParamNames= {"i", "jj"};
		int[] permutation= {2, -1, 0};
		int[] deletedIndices= null;
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}*/

	/*public void testAll41()throws Exception{
		String[] signature= {"I"};
		String[] newNames= null;
		String[] newTypes= null;
		String[] newDefaultValues= null;
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= null;

		String[] oldParamNames= {"i"};
		String[] newParamNames= {"i"};
		int[] permutation= {0};
		int[] deletedIndices= {0};
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName, true);
	}

	public void testAll42()throws Exception{
		String[] signature= {"I"};
		String[] newNames= {"i"};
		String[] newTypes= {"int"};
		String[] newDefaultValues= {"0"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {1};

		String[] oldParamNames= {"i"};
		String[] newParamNames= {"i"};
		int[] permutation= {0, -1};
		int[] deletedIndices= {0};
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testAll43()throws Exception{
		String[] signature= {"I", "I"};
		String[] newNames= null;
		String[] newTypes= null;
		String[] newDefaultValues= null;
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= null;

		String[] oldParamNames= {"i", "j"};
		String[] newParamNames= {"i", "j"};
		int[] permutation= {1, 0};
		int[] deletedIndices= {0};
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName, true);
	}

	public void testAll44()throws Exception{
		if (true){
			printTestDisabledMessage("need to decide how to treat compile errors");
			return;
		}
		String[] signature= {"I", "I"};
		String[] newNames= null;
		String[] newTypes= null;
		String[] newDefaultValues= null;
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= null;

		String[] oldParamNames= {"i", "j"};
		String[] newParamNames= {"i", "j"};
		int[] permutation= {0, 1};
		int[] deletedIndices= {0};
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= "boolean";
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testAll45()throws Exception{
		if (true){
			printTestDisabledMessage("need to decide how to treat compile errors");
			return;
		}

		String[] signature= {"I", "I"};
		String[] newNames= null;
		String[] newTypes= null;
		String[] newDefaultValues= null;
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= null;

		String[] oldParamNames= {"i", "j"};
		String[] newParamNames= {"i", "j"};
		String[] newParamTypeNames= {"int", "boolean"};
		int[] permutation= {0, 1};
		int[] deletedIndices= {0};
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, newParamTypeNames, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testAll46()throws Exception{
		if (! RUN_CONSTRUCTOR_TEST){
			printTestDisabledMessage("disabled for constructors for now");
			return;
		}

		String[] signature= {};
		String[] newNames= {"i"};
		String[] newTypes= {"int"};
		String[] newDefaultValues= {"1"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};

		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {0};
		int[] deletedIndices= null;
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= null;
		helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testAll47()throws Exception{
		if (! RUN_CONSTRUCTOR_TEST){
			printTestDisabledMessage("disabled for constructors for now");
			return;
		}

		String[] signature= {};
		String[] newNames= {"i"};
		String[] newTypes= {"int"};
		String[] newDefaultValues= {"1"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};

		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {0};
		int[] deletedIndices= null;
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= null;
		helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testAll48()throws Exception{
		if (! RUN_CONSTRUCTOR_TEST){
			printTestDisabledMessage("disabled for constructors for now");
			return;
		}

		String[] signature= {};
		String[] newNames= {"i"};
		String[] newTypes= {"int"};
		String[] newDefaultValues= {"1"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};

		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {0};
		int[] deletedIndices= null;
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= null;
		helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testAll49()throws Exception{
		if (! RUN_CONSTRUCTOR_TEST){
			printTestDisabledMessage("disabled for constructors for now");
			return;
		}

		String[] signature= {};
		String[] newNames= {"i"};
		String[] newTypes= {"int"};
		String[] newDefaultValues= {"1"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};

		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {0};
		int[] deletedIndices= null;
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= null;
		helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testAll50()throws Exception{
		if (! RUN_CONSTRUCTOR_TEST){
			printTestDisabledMessage("disabled for constructors for now");
			return;
		}

		String[] signature= {};
		String[] newNames= {"i"};
		String[] newTypes= {"int"};
		String[] newDefaultValues= {"1"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};

		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {0};
		int[] deletedIndices= null;
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= null;
		helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testAll51()throws Exception{
		if (! RUN_CONSTRUCTOR_TEST){
			printTestDisabledMessage("disabled for constructors for now");
			return;
		}

		String[] signature= {};
		String[] newNames= {"i"};
		String[] newTypes= {"int"};
		String[] newDefaultValues= {"1"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};

		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {0};
		int[] deletedIndices= null;
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= null;
		helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testAll52()throws Exception{
		if (! RUN_CONSTRUCTOR_TEST){
			printTestDisabledMessage("disabled for constructors for now");
			return;
		}

		String[] signature= {};
		String[] newNames= {"i"};
		String[] newTypes= {"int"};
		String[] newDefaultValues= {"1"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};

		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {0};
		int[] deletedIndices= null;
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= null;
		helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testAll53()throws Exception{
		String[] signature= {"I"};
		String[] newNames= {"a"};
		String[] newTypes= {"HashSet"};
		String[] newDefaultValues= {"null"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};

		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {0};
		int[] deletedIndices= {0};
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testAll54()throws Exception{
		String[] signature= {"I"};
		String[] newNames= {"a"};
		String[] newTypes= {"List"};
		String[] newDefaultValues= {"null"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};

		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {0};
		int[] deletedIndices= {0};
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}*/

	public void testAll55()throws Exception{
//		printTestDisabledMessage("test for bug 32654 [Refactoring] Change method signature with problems");
		String[] signature= {"[QObject;", "I", "Z"};
		String[] newNames= {"e"};
		String[] newTypes= {"boolean"};
		Literal[] newDefaultValues= { new BooleanLiteral(true) };
		int[] newIndices= {2};
		helperAdd(signature, newTypes, newNames, newDefaultValues, newIndices, false);
	}

	/*public void testAll56()throws Exception{
		if (! RUN_CONSTRUCTOR_TEST){
			printTestDisabledMessage("disabled for constructors for now");
			return;
		}

//		printTestDisabledMessage("test for 38366 ArrayIndexOutOfBoundsException in change signeture [refactoring] ");
		String[] signature= {"QEvaViewPart;", "I"};
		String[] newNames= null;
		String[] newTypes= null;
		String[] newDefaultValues= null;
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {};

		String[] oldParamNames= {"part", "title"};
		String[] newParamNames= {"part", "title"};
		int[] permutation= {0, 1};
		int[] deletedIndices= {0};
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= null;
		helperDoAll("HistoryFrame", "HistoryFrame", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testAll57()throws Exception{
//		printTestDisabledMessage("test for 39633 classcast exception when refactoring change method signature [refactoring]");
//		if (true)
//			return;
		String[] signature= {"I", "QString;", "QString;"};
		String[] newNames= null;
		String[] newTypes= null;
		String[] newDefaultValues= null;
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= null;

		String[] oldParamNames= {"i", "hello", "goodbye"};
		String[] newParamNames= oldParamNames;
		int[] permutation= {0, 2, 1};
		int[] deletedIndices= {};
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= null;
		helperDoAll("TEST.X", "method", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testAll58()throws Exception{
		String[] signature= {"I", "[[[QString;"};
		String[] newNames= null;
		String[] newTypes= null;
		String[] newDefaultValues= null;
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= null;

		String[] oldParamNames= {"a", "b"};
		String[] newParamNames= {"abb", "bbb"};
		int[] permutation= {1, 0};
		int[] deletedIndices= {};
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testAll59() throws Exception{
		String[] signature= {"I", "J"};
		String[] newNames= {"really"};
		String[] newTypes= {"boolean"};
		String[] newDefaultValues= {"true"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};

		String[] oldParamNames= {"from", "to"};
		String[] newParamNames= {"f", "t"};
		String[] newParameterTypeNames= {"int", "char"};
		int[] permutation= {0, 1, 2};
		int[] deletedIndices= {};
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= "java.util.List";
		helperDoAll("A", "getList", signature, newParamInfo, newIndices, oldParamNames, newParamNames, newParameterTypeNames, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testAll60() throws Exception{
		String[] signature= {"I", "J"};
		String[] newNames= {"l"};
		String[] newTypes= {"java.util.List"};
		String[] newDefaultValues= {"null"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {1};

		String[] oldParamNames= {"from", "to"};
		String[] newParamNames= {"to", "tho"};
		String[] newParameterTypeNames= {"int", "long"};
		int[] permutation= {2, 1, 0};
		int[] deletedIndices= {};
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= "java.util.List";
		String[] removeExceptions= {"java.io.IOException"};
		String[] addExceptions= {"java.lang.Exception"};
		helperDoAllWithExceptions("I", "getList", signature, newParamInfo, newIndices,
				oldParamNames, newParamNames, newParameterTypeNames, permutation, newVisibility,
				deletedIndices, newReturnTypeName, removeExceptions, addExceptions);
	}

	public void testAll61()throws Exception{ //bug 51634
		String[] signature= {};
		ParameterInfo[] newParamInfo= null;
		int[] newIndices= null;

		String[] oldParamNames= {};
		String[] newParamNames= oldParamNames;
		int[] permutation= {};
		int[] deletedIndices= null;
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= "Object";
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testAll62()throws Exception{ //bug
		String[] signature= {"QBigInteger;", "QBigInteger;", "QBigInteger;"};
		ParameterInfo[] newParamInfo= null;
		int[] newIndices= null;
		String[] newParamTypeNames= {"long", "long", "long"};
		String[] oldParamNames= {"a", "b", "c"};
		String[] newParamNames= {"x", "y", "z"};
		int[] permutation= {0, 1, 2};
		int[] deletedIndices= null;
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= "void";
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, newParamTypeNames, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testAll63()throws Exception{ //bug
		String[] signature= {};
		ParameterInfo[] newParamInfo= null;
		int[] newIndices= null;
		String[] newParamTypeNames= {};
		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {};
		int[] deletedIndices= null;
		int newVisibility= Modifier.PROTECTED;
		String newReturnTypeName= "void";
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, newParamTypeNames, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testAll64() throws Exception{ // https://bugs.eclipse.org/bugs/show_bug.cgi?id=158008 and https://bugs.eclipse.org/bugs/show_bug.cgi?id=201929
		ParameterInfo[] newParamInfo= { ParameterInfo.createInfoForAddedParameter("java.util.List<Local>", "list", "null") };
		int[] newIndices= { 2 };
		String[] newParamTypeNames= {};
		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {};
		int[] deletedIndices= {0};
		int newVisibility= Modifier.PRIVATE;
		String newReturnTypeName= "void";

		ICompilationUnit cu= createCUfromTestFile(getPackageP(), true, true);
		int lastNasty= cu.getSource().lastIndexOf("nasty");
		IMethod method= (IMethod) cu.getElementAt(lastNasty);
		assertTrue(method.exists());
		helperDoAll(method, newParamInfo, newIndices, oldParamNames, newParamNames, newParamTypeNames, permutation, newVisibility, deletedIndices, newReturnTypeName, false);
	}

	public void testAddSyntaxError01()throws Exception{ // https://bugs.eclipse.org/bugs/show_bug.cgi?id=191349
		String refNameIn= "A_testAddSyntaxError01_Ref_in.java";
		String refNameOut= "A_testAddSyntaxError01_Ref_out.java";
		ICompilationUnit refCu= createCU(getPackageP(), refNameIn, getFileContents(getTestFolderPath(true) + refNameIn));

		String[] signature= {"QString;"};
		String[] newNames= {"newParam"};
		String[] newTypes= {"Object"};
		String[] newDefaultValues= {"null"};
		ParameterInfo[] newParamInfos= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= { 1 };
		helperAdd(signature, newParamInfos, newIndices);

		String expectedRefContents= getFileContents(getTestFolderPath(true) + refNameOut);
		assertEqualLines(expectedRefContents, refCu.getSource());
	}*/

	/* disabled: different interpretation
	public void testAddRecursive1()throws Exception{ //bug 42100
		String[] signature= {"I"};
		String[] newNames= {"bool"};
		String[] newTypes= {"boolean"};
		Literal[] newDefaultValues= { new BooleanLiteral(true) };
		int[] newIndices= {1};
		helperAdd(signature, newTypes, newNames, newDefaultValues, newIndices, true);
	}*/

	/*public void testException01() throws Exception {
		String[] signature= {"J"};
		String[] remove= {};
		String[] add= {"java.util.zip.ZipException"};
		helperException(signature, remove, add);
	}

	public void testException02() throws Exception {
		String[] add= new String[] {"java.lang.RuntimeException"};
		helperException(new String[0], new String[0], add);
	}

	public void testException03() throws Exception { //bug 52091
		String[] remove= new String[] {"java.lang.RuntimeException"};
		helperException(new String[0], remove, new String[0]);
	}

	public void testException04() throws Exception { //bug 52058
		String[] add= new String[] {"java.io.IOException", "java.lang.ClassNotFoundException"};
		helperException(new String[0], new String[0], add);
	}

	public void testException05() throws Exception { //bug 56132
		String[] remove= new String[] {"java.lang.IllegalArgumentException", "java.io.IOException"};
		helperException(new String[0], remove, new String[0]);
	}

	public void testInStatic01() throws Exception { //bug 47062
		String[] signature= {"QString;", "QString;"};
		ParameterInfo[] newParamInfo= null;
		int[] newIndices= null;

		String[] oldParamNames= {"arg1", "arg2"};
		String[] newParamNames= {"a", "b"};
		int[] permutation= {1, 0};
		int newVisibility= JdtFlags.VISIBILITY_CODE_INVALID;//retain
		int[] deleted= null;
		String newReturnTypeName= null;
		helperDoAll("Example", "Example", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deleted, newReturnTypeName);
	}

	public void testInStatic02() throws Exception { //bug 47062
		String[] signature= {"QString;", "QString;"};
		ParameterInfo[] newParamInfo= null;
		int[] newIndices= null;

		String[] oldParamNames= {"arg1", "arg2"};
		String[] newParamNames= {"a", "b"};
		int[] permutation= {1, 0};
		int newVisibility= JdtFlags.VISIBILITY_CODE_INVALID;//retain
		int[] deleted= null;
		String newReturnTypeName= null;
		helperDoAll("Example", "getExample", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deleted, newReturnTypeName);
	}*/

	public void testName01() throws Exception {
		String[] signature= {"QString;"};
		helperRenameMethod(signature, "newName", false);
	}

	/* disabled: does not compile
	public void testName02() throws Exception {
		String[] signature= {"QString;"};
		helperRenameMethod(signature, "newName", false);
	}*/

	/*public void testFailImport01() throws Exception {
		String[] signature= {};
		String[] newTypes= {"Permission"};
		String[] newNames= {"p"};
		String[] newDefaultValues= {"null"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};
		helperAddFail(signature, newParamInfo, newIndices, RefactoringStatus.ERROR);
	}*/
	
	/* disabled:default value
	public void testImport01() throws Exception {
		String[] signature= {};
		String[] newTypes= {"java.security.acl.Permission", "Permission"};
		String[] newNames= {"acl", "p"};
		Literal[] newDefaultValues= {new NullLiteral("null"), "perm"};
		int[] newIndices= {0, 0};
		helperAdd(signature, newTypes, newNames, newDefaultValues, newIndices, true);
	} */

	public void testImport02() throws Exception {
		String[] signature= {};
		String[] newTypes= {"Permission", "java.security.acl.Permission"};
		String[] newNames= {"p", "acl"};
		Literal[] newDefaultValues= {new NullLiteral("null"), new NullLiteral("null")};
		int[] newIndices= {0, 1};
		helperAdd(signature, newTypes, newNames, newDefaultValues, newIndices, false);
	}

	public void testImport03() throws Exception {
		String[] signature= {};
		String[] newTypes= {"java.security.acl.Permission", "java.security.Permission"};
		String[] newNames= {"p", "pp"};
		Literal[] newDefaultValues= { new NullLiteral("null"), new NullLiteral("null") };
		int[] newIndices= {0, 1};
		helperAdd(signature, newTypes, newNames, newDefaultValues, newIndices, false);
	}

	public void testImport04() throws Exception {
		String[] signature= {};
		String[] newTypes= {"Object"};
		String[] newNames= {"o"};
		Literal[] newDefaultValues= {new NullLiteral("null")};
		int[] newIndices= {0};
		helperAdd(signature, newTypes, newNames, newDefaultValues, newIndices, false);
	}

	/*public void testImport05() throws Exception {
		// printTestDisabledMessage("49772: Change method signature: remove unused imports [refactoring]");
		String[] signature= {};
		String[] newNames= null;
		String[] newTypes= null;
		String[] newDefaultValues= null;
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {};

		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {};
		int[] deletedIndices= null;
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= "Object";
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testImport06() throws Exception {
		String[] signature= {"QPermission;", "Qjava.security.acl.Permission;"};
		String[] newNames= null;
		String[] newTypes= null;
		String[] newDefaultValues= null;
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {};

		String[] oldParamNames= {"perm", "acl"};
		String[] newParamNames= {"xacl", "xperm"};
		String[] newParamTypeNames= {"java.security.acl.Permission [] []", "java.security.Permission"};
		int[] permutation= {1, 0};
		int[] deletedIndices= null;
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= "java.security.acl.Permission";
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, newParamTypeNames, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testImport07() throws Exception {
		// printTestDisabledMessage("49772: Change method signature: remove unused imports [refactoring]");
		String[] signature= {"QList;"};
		String[] newNames= null;
		String[] newTypes= null;
		String[] newDefaultValues= null;
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {};

		String[] oldParamNames= {"list"};
		String[] newParamNames= oldParamNames;
		String[] newParamTypeNames= null;
		int[] permutation= {0};
		int[] deletedIndices= {0};
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, newParamTypeNames, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testImport08() throws Exception {
		// printTestDisabledMessage("68504: Refactor -> Change Method Signature removes import [refactoring]");
		String[] signature= {"QString;", "QVector;"};
		String[] newNames= null;
		String[] newTypes= null;
		String[] newDefaultValues= null;
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {};

		String[] oldParamNames= {"text", "v"};
		String[] newParamNames= oldParamNames;
		String[] newParamTypeNames= null;
		int[] permutation= {1, 0};
		int[] deletedIndices= {};
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= null;
		helperDoAll("A", "textContains", signature, newParamInfo, newIndices, oldParamNames, newParamNames, newParamTypeNames, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testEnum01() throws Exception {
		if (BUG_83691_CORE_JAVADOC_REF) {
			printTestDisabledMessage("BUG_83691_CORE_JAVADOC_REF");
			return;
		}
		String[] signature= {"I"};
		String[] newNames= {"a"};
		String[] newTypes= {"int"};
		String[] newDefaultValues= {"17"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {1};

		String[] oldParamNames= {"i"};
		String[] newParamNames= {"i"};
		int[] permutation= {0};
		int[] deletedIndices= {};
		int newVisibility= Modifier.PRIVATE;
		String newReturnTypeName= null;
		helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testEnum02() throws Exception {
		String[] signature= {"I"};
		String[] newNames= {"a"};
		String[] newTypes= {"int"};
		String[] newDefaultValues= {"17"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {1};

		String[] oldParamNames= {"i"};
		String[] newParamNames= {"i"};
		int[] permutation= {0};
		int[] deletedIndices= {};
		int newVisibility= Modifier.PRIVATE;
		String newReturnTypeName= null;
		helperDoAll("A_testEnum02_in", "A_testEnum02_in", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testEnum03() throws Exception {
		if (BUG_83691_CORE_JAVADOC_REF) {
			printTestDisabledMessage("BUG_83691_CORE_JAVADOC_REF");
			return;
		}
		String[] signature= {};
		String[] newNames= {"obj"};
		String[] newTypes= {"Object"};
		String[] newDefaultValues= {"null"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};

		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {};
		int[] deletedIndices= {};
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= null;
		helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testEnum04() throws Exception {
		String[] signature= {};
		String[] newNames= {"forward"};
		String[] newTypes= {"boolean"};
		String[] newDefaultValues= {"true"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};

		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {};
		int[] deletedIndices= {};
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= null;
		helperDoAll("A", "getNext", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}*/

	public void testStaticImport01() throws Exception {
		helperRenameMethod(new String[0], "abc", false);
	}

	public void testStaticImport02() throws Exception {
		String[] signature= {"QInteger;"};
		String[] newTypes= {"Object"};
		String[] newNames= {"o"};
		Literal[] newDefaultValues= {new NullLiteral("null")};
		int[] newIndices= {1};
		helperAdd(signature, newTypes, newNames, newDefaultValues, newIndices);
	}

	/*public void testVararg01() throws Exception {
		String[] signature= {"I", "[QString;"};
		String[] newNames= {};
		String[] newTypes= {};
		String[] newDefaultValues= {};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {};

		String[] oldParamNames= {"i", "names"};
		String[] newParamNames= {"i", "strings"};
		int[] permutation= {0, 1};
		int[] deletedIndices= {};
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testVararg02() throws Exception {
		String[] signature= {"I", "[QString;"};
		String[] newNames= {"o"};
		String[] newTypes= {"Object"};
		String[] newDefaultValues= {"new Object()"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};

		String[] oldParamNames= {"i", "names"};
		String[] newParamNames= oldParamNames;
		int[] permutation= {0, 1, 2};
		int[] deletedIndices= {};
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName, true);
	}

	public void testVararg03() throws Exception {
		String[] signature= {"[QString;"};
		String[] newNames= {};
		String[] newTypes= {};
		String[] newDefaultValues= {};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {};

		String[] oldParamNames= {"args"};
		String[] newParamNames= oldParamNames;
		String[] newParameterTypeNames= {"Object..."};
		int[] permutation= {0};
		int[] deletedIndices= {};
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= null;
		helperDoAll("A", "use", signature, newParamInfo, newIndices, oldParamNames, newParamNames, newParameterTypeNames, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testVararg04() throws Exception {
		String[] signature= {"[QString;"};
		String[] newNames= {"i"};
		String[] newTypes= {"int"};
		String[] newDefaultValues= {"1"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};

		String[] oldParamNames= {"args"};
		String[] newParamNames= {"args"};
		int[] permutation= {};
		int[] deletedIndices= {};
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= null;
		helperDoAll("A", "use", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testVararg05() throws Exception {
		String[] signature= {"QObject;", "[QString;"};
		String[] newNames= {};
		String[] newTypes= {};
		String[] newDefaultValues= {};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {};

		String[] oldParamNames= {"first", "args"};
		String[] newParamNames= {"arg", "invalid name"};
		int[] permutation= {0, 1};
		int[] deletedIndices= {1};
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= null;
		helperDoAll("A", "use", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName, true);
	}

	public void testVararg06() throws Exception {
		String[] signature= {"I", "[QString;"};
		String[] newNames= {};
		String[] newTypes= {};
		String[] newDefaultValues= {};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {};

		String[] oldParamNames= {"i", "names"};
		String[] newParamNames= {"i", "names"};
		String[] newParameterTypeNames= {"int", "String..."};
		int[] permutation= {0, 1};
		int[] deletedIndices= { };
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, newParameterTypeNames, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testVararg07() throws Exception {
		//can remove parameter which is vararg in ripple method
		String[] signature= {"I", "[QString;"};
		String[] newNames= {"j", "k"};
		String[] newTypes= {"String", "Integer"};
		String[] newDefaultValues= {"\"none\"", "17"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {1, 2};

		String[] oldParamNames= {"i", "names"};
		String[] newParamNames= {"i", "names"};
		int[] permutation= {0, 1, 2, 3};
		int[] deletedIndices= { 1 };
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName, true);
	}

	public void testVararg08() throws Exception {
		//can add vararg parameter with empty default value
		String[] signature= {};
		String[] newNames= {"args"};
		String[] newTypes= {"String ..."};
		String[] newDefaultValues= {""};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};

		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {0};
		int[] deletedIndices= { };
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testVararg09() throws Exception {
		//can add vararg parameter with one-expression default value
		String[] signature= {};
		String[] newNames= {"args"};
		String[] newTypes= {"String ..."};
		String[] newDefaultValues= {"\"Hello\""};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};

		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {0};
		int[] deletedIndices= { };
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testVararg10() throws Exception {
		//can add vararg parameter with multiple-expressions default value
		String[] signature= {};
		String[] newNames= {"args"};
		String[] newTypes= {"String ..."};
		String[] newDefaultValues= {"\"Hello\", new String()"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {0};

		String[] oldParamNames= {};
		String[] newParamNames= {};
		int[] permutation= {0};
		int[] deletedIndices= { };
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testGenerics01() throws Exception {
		String[] signature= {"QInteger;", "QE;"};
		String[] newNames= {};
		String[] newTypes= {};
		String[] newDefaultValues= {};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {};

		String[] oldParamNames= {"i", "e"};
		String[] newParamNames= {"integer", "e"};
		String[] newParameterTypeNames= null;
		int[] permutation= {1, 0};
		int[] deletedIndices= { };
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, newParameterTypeNames, permutation, newVisibility, deletedIndices, newReturnTypeName, true);
	}

	public void testGenerics02() throws Exception {
		String[] signature= {"QT;", "QE;"};
		String[] newNames= {"maps"};
		String[] newTypes= {"java.util.List<HashMap>"};
		String[] newDefaultValues= {"null"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {2};

		String[] oldParamNames= {"e", "t"};
		String[] newParamNames= {"e", "t"};
		String[] newParameterTypeNames= null;
		int[] permutation= {1, 0, 2};
		int[] deletedIndices= { };
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, newParameterTypeNames, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testGenerics03() throws Exception {
		String[] signature= {"QT;", "QE;"};
		String[] newNames= {"maps"};
		String[] newTypes= {"java.util.List<HashMap>"};
		String[] newDefaultValues= {"null"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {2};

		String[] oldParamNames= {"e", "t"};
		String[] newParamNames= {"e", "t"};
		String[] newParameterTypeNames= null;
		int[] permutation= {1, 0, 2};
		int[] deletedIndices= { };
		int newVisibility= Modifier.NONE;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, newParameterTypeNames, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testGenerics04() throws Exception {
		String[] signature= {"QList<QInteger;>;", "QA<QString;>;"};
		String[] newNames= {"li"};
		String[] newTypes= {"List<Integer>"};
		String[] newDefaultValues= {"null"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {2};

		String[] oldParamNames= {"li", "as"};
		String[] newParamNames= {"li", "as"};
		String[] newParameterTypeNames= null;
		int[] permutation= {1, 2, 0};
		int[] deletedIndices= {0};
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, newParameterTypeNames, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}

	public void testGenerics05() throws Exception {
		String[] signature= { "QClass;" };
		String[] newNames= {};
		String[] newTypes= {};
		String[] newDefaultValues= {};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {};

		String[] oldParamNames= { "arg" };
		String[] newParamNames= { "arg" };
		String[] newParameterTypeNames= { "Class<?>" };
		int[] permutation= { 0 };
		int[] deletedIndices= {};
		int newVisibility= Modifier.PUBLIC;
		String newReturnTypeName= null;
		helperDoAll("I", "test", signature, newParamInfo, newIndices, oldParamNames, newParamNames, newParameterTypeNames, permutation,
				newVisibility, deletedIndices, newReturnTypeName);
	}
	
	public void testGenerics06() throws Exception {
		String[] signature= { "QString;" };
		String[] newNames= {};
		String[] newTypes= {};
		String[] newDefaultValues= {};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {};

		String[] oldParamNames= { "string" };
		String[] newParamNames= {};
		String[] newParameterTypeNames= {};
		int[] permutation= {};
		int[] deletedIndices= {0};
		int newVisibility= Modifier.PRIVATE;
		String newReturnTypeName= null;
		helperDoAll("C", "foo", signature, newParamInfo, newIndices, oldParamNames, newParamNames, newParameterTypeNames, permutation, newVisibility, deletedIndices, newReturnTypeName);
	}*/

	public void testDelegate01() throws Exception {
		// simple reordering with delegate
		helper1(new String[]{"j", "i"}, new String[]{"I", "QString;"}, null, null, true);
	}

	public void testDelegate02() throws Exception {
		// add a parameter -> import it
		String[] signature= {};
		String[] newTypes= {"java.util.List" };
		String[] newNames= {"list" };
		Literal[] newDefaultValues= {new NullLiteral("null")};
		int[] newIndices= { 0 };
		helperAdd(signature, newTypes, newNames, newDefaultValues, newIndices, true);
	}

	public void testDelegate03() throws Exception {
		// reordering with imported type in body => don't remove import
		helper1(new String[]{"j", "i"}, new String[]{"I", "QString;"}, null, null, true);
	}

	/*public void testDelegate04() throws Exception {
		// delete a parameter => import stays
		String[] signature= {"QList;"};
		String[] newNames= null;
		String[] newTypes= null;
		String[] newDefaultValues= null;
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= null;

		String[] oldParamNames= {"l"};
		String[] newParamNames= {"l"};
		int[] permutation= {};
		int[] deletedIndices= {0};
		int newVisibility= Modifier.PRIVATE;
		String newReturnTypeName= null;
		helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames, newParamNames, null, permutation, newVisibility, deletedIndices, newReturnTypeName, true);
	}*/

	public void testDelegate05() throws Exception {
		// bug 138320
		String[] signature= {};
		helperRenameMethod(signature, "renamed", true);
	}

}

