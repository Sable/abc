/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla - 228950: [pull up] exception if target calls super with multiple parameters
 *******************************************************************************/
package tests.eclipse.PullUp;

import java.io.File;
import java.util.HashSet;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.ClassDecl;
import AST.FieldDeclaration;
import AST.MemberTypeDecl;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class PullUpTests extends TestCase {

	public PullUpTests(String name) {
		super(name);
	}

	private String getInFileName(String cu) {
		return "tests/eclipse/PullUp/" + getName() + "/in/" + cu + ".java";
	}

	private String getOutFileName(String cu) {
		return "tests/eclipse/PullUp/" + getName() + "/out/" + cu + ".java";
	}

	private void pullUpMembers(String[] methodNames, boolean[] makeAbstract, String[] fieldNames, String[] memberTypeNames, boolean succeed, int targetClassIndex) {
		Program in;
		if(new File(getInFileName("B")).exists())
			in = CompileHelper.compile(getInFileName("A"), getInFileName("B"));
		else
			in = CompileHelper.compile(getInFileName("A"));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		
		TypeDecl td = in.findType("B");
		assertNotNull(td);
		
		MethodDecl[] meths = new MethodDecl[methodNames.length];
		for(int i=0;i<methodNames.length;++i) {
			MethodDecl md = td.findMethod(methodNames[i]);
			assertNotNull(md);
			meths[i] = md;
		}
		
		FieldDeclaration[] fields = new FieldDeclaration[fieldNames.length];
		for(int i=0;i<fieldNames.length;++i) {
			FieldDeclaration fd = td.findField(fieldNames[i]);
			assertNotNull(fd);
			fields[i] = fd;
		}
		
		MemberTypeDecl[] memberTypes = new MemberTypeDecl[memberTypeNames.length];
		for(int i=0;i<memberTypeNames.length;++i) {
			TypeDecl m = td.findSimpleType(memberTypeNames[i]);
			assertNotNull(m);
			assertTrue(m.getParent() instanceof MemberTypeDecl);
			memberTypes[i] = (MemberTypeDecl)m.getParent();
		}
		
		Program out = null;
		try {
			if(succeed) {
				if(new File(getInFileName("B")).exists())
					out = CompileHelper.compile(getOutFileName("A"), getOutFileName("B"));
				else
					out = CompileHelper.compile(getOutFileName("A"));
				assertNotNull(out);
			}

			do {
				in.flushCaches();
				td.doPullUpMembers(meths, 
								   makeAbstract, 
								   fields, 
								   memberTypes);
				td = ((ClassDecl)td).superclass();
			} while(targetClassIndex-- > 0);
			
			if(!succeed)
				assertEquals("<failure>", in.toString());
			else
				assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			if(succeed)
				assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void declareAbstractFailHelper(String[] selectedMethodNames, String[][] selectedMethodSignatures,
											String[] selectedFieldNames,
											String[] selectedTypeNames, String[] namesOfMethodsToPullUp,
											String[][] signaturesOfMethodsToPullUp, String[] namesOfFieldsToPullUp,
											String[] namesOfMethodsToDeclareAbstract,
											String[][] signaturesOfMethodsToDeclareAbstract, String[] namesOfTypesToPullUp,
											boolean deleteAllPulledUpMethods, boolean deleteAllMatchingMethods, int targetClassIndex) throws Exception{
		boolean[] makeAbstract = new boolean[selectedMethodNames.length];
		for(String methodName : namesOfMethodsToDeclareAbstract)
			for(int i=0;i<selectedMethodNames.length;++i)
				if(selectedMethodNames[i].equals(methodName))
					makeAbstract[i] = true;
		pullUpMembers(selectedMethodNames, makeAbstract, selectedFieldNames, namesOfTypesToPullUp, false, targetClassIndex);
	}

	private void declareAbstractHelper(String[] selectedMethodNames, String[][] selectedMethodSignatures,
											String[] selectedFieldNames,
											String[] selectedTypeNames, String[] namesOfMethodsToPullUp,
											String[][] signaturesOfMethodsToPullUp, String[] namesOfFieldsToPullUp,
											String[] namesOfMethodsToDeclareAbstract,
											String[][] signaturesOfMethodsToDeclareAbstract, String[] namesOfTypesToPullUp,
											boolean deleteAllPulledUpMethods, boolean deleteAllMatchingMethods, int targetClassIndex) throws Exception{
		boolean[] makeAbstract = new boolean[selectedMethodNames.length];
		for(String methodName : namesOfMethodsToDeclareAbstract)
			for(int i=0;i<selectedMethodNames.length;++i)
				if(selectedMethodNames[i].equals(methodName))
					makeAbstract[i] = true;
		pullUpMembers(selectedMethodNames, makeAbstract, selectedFieldNames, namesOfTypesToPullUp, true, targetClassIndex);
	}

	private void helper1(String[] methodNames, String[][] signatures, boolean deleteAllInSourceType, boolean deleteAllMatchingMethods, int targetClassIndex) throws Exception{
		pullUpMembers(methodNames, new boolean[methodNames.length], new String[0], new String[0], true, targetClassIndex);
	}

	private void helper1(String[] fieldNames,  boolean deleteAllInSourceType, boolean deleteAllMatchingMethods, int targetClassIndex) throws Exception{
		pullUpMembers(new String[0], new boolean[fieldNames.length], fieldNames, new String[0], true, targetClassIndex);
	}
	
	private void fieldHelper1(String[] fieldNames, int targetClassIndex) throws Exception{
		helper1(fieldNames, true, true, targetClassIndex);
	}

	private void helper2(String[] methodNames, String[][] signatures, boolean deleteAllInSourceType, boolean deleteAllMatchingMethods, int targetClassIndex) throws Exception{
		pullUpMembers(methodNames, new boolean[methodNames.length], new String[0], new String[0], false, targetClassIndex);
	}

	private void helper2(String[] fieldNames,  boolean deleteAllInSourceType, boolean deleteAllMatchingMethods, int targetClassIndex) throws Exception{
		pullUpMembers(new String[0], new boolean[fieldNames.length], fieldNames, new String[0], false, targetClassIndex);
	}
	
	private void fieldHelper2(String[] fieldNames, int targetClassIndex) throws Exception{
		helper2(fieldNames, true, true, targetClassIndex);
	}

	private void helper3(String[] methodNames, String[][] signatures, boolean deleteAllInSourceType, boolean deleteAllMatchingMethods, int targetClassIndex, boolean shouldActivationCheckPass) throws Exception {
		pullUpMembers(methodNames, new boolean[methodNames.length], new String[0], new String[0], false, targetClassIndex);
	}

	private void addRequiredMembersHelper(String[] fieldNames, String[] methodNames, String[][] methodSignatures, String[] expectedFieldNames, 
			String[] expectedMethodNames, String[][] expectedMethodSignatures) {
		Program in;
		if(new File(getInFileName("B")).exists())
			in = CompileHelper.compile(getInFileName("A"), getInFileName("B"));
		else
			in = CompileHelper.compile(getInFileName("A"));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		
		TypeDecl td = in.findType("B");
		assertNotNull(td);
		
		HashSet<MethodDecl> meths = new HashSet<MethodDecl>();
		for(int i=0;i<methodNames.length;++i) {
			MethodDecl md = td.findMethod(methodNames[i]);
			assertNotNull(md);
			meths.add(md);
		}
		
		HashSet<FieldDeclaration> fields = new HashSet<FieldDeclaration>();
		for(int i=0;i<fieldNames.length;++i) {
			FieldDeclaration fd = td.findField(fieldNames[i]);
			assertNotNull(fd);
			fields.add(fd);
		}

		td.addRequiredMembers(meths, fields, new HashSet<MemberTypeDecl>());
		
		f_outer:
		for(String efn : expectedFieldNames) {
			for(FieldDeclaration f : fields)
				if(f.name().equals(efn))
					continue f_outer;
			fail(efn+" not added");
		}
		
		m_outer:
		for(String emn : expectedMethodNames) {
			for(MethodDecl m : meths)
				if(m.name().equals(emn))
					continue m_outer;
			fail(emn+" not added");
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	//------------------ tests -------------

	public void test0() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void test1() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void test2() throws Exception{
		helper1(new String[]{"mmm", "n"}, new String[][]{new String[0], new String[0]}, true, false, 0);
	}

	/* disabled: unifying multiple pulled-up methods not supported
	public void test3() throws Exception{
		helper1(new String[]{"mmm", "n"}, new String[][]{new String[0], new String[0]}, true, true, 0);
	}*/

	public void test4() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void test5() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void test6() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void test7() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void test8() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	/* disabled: test case changes super call into normal call; we don't do that
	public void test9() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}*/

	public void test10() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void test11() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	// NB: excellent example of the advantages of general naming framework
	public void test12() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void test13() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void test14() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	/* disabled: by Eclipse
	public void test15() throws Exception{
		printTestDisabledMessage("must fix - incorrect error");
//		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false);
	}

	public void test16() throws Exception{
		printTestDisabledMessage("must fix - incorrect error");
//		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false);
	}

	public void test17() throws Exception{
		printTestDisabledMessage("must fix - incorrect error with static method access");
//		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false);
	}

	public void test18() throws Exception{
		printTestDisabledMessage("must fix - incorrect error with static field access");
//		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false);
	}*/

	public void test19() throws Exception{
//		printTestDisabledMessage("bug 18438");
//		printTestDisabledMessage("bug 23324 ");
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void test20() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 1);
	}

	public void test21() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 1);
	}

	/* disabled: result looks off
	public void test22() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}*/

	public void test23() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void test24() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void test25() throws Exception{
//		printTestDisabledMessage("bug in ASTRewrite - extra dimensions 29553");
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void test26() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		declareAbstractHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								new String[0], namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, new String[0], true, true, 0);
	}

	public void test27() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		declareAbstractHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								new String[0], namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, new String[0], true, true, 0);
	}

	public void test28() throws Exception{
//		printTestDisabledMessage("unimplemented (increase method visibility if declare abstract in superclass)");
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		declareAbstractHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								new String[0], namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, new String[0], true, true, 0);
	}

	public void test29() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[]{"[I"}};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		declareAbstractHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								new String[0], namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, new String[0], true, true, 0);
	}

	public void test30() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[]{"[I"}};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		declareAbstractHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								new String[0], namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, new String[0], true, true, 0);
	}

	public void test31() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[]{"[I"}};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		declareAbstractHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								new String[0], namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, new String[0], true, true, 0);
	}

	public void test32() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		declareAbstractHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								new String[0], namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, new String[0], true, true, 0);
	}

	public void test33() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPullUp= selectedMethodNames;
		String[][] signaturesOfMethodsToPullUp= selectedMethodSignatures;
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {new String[0]};

		declareAbstractHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								new String[0], namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, new String[0], true, true, 0);
	}

	public void test34() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		declareAbstractHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								new String[0], namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, new String[0], true, true, 0);
	}

	public void test35() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		declareAbstractHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								new String[0], namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, new String[0], true, true, 0);
	}

	public void test36() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		declareAbstractHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								new String[0], namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, new String[0], true, true, 0);
	}

	public void test37() throws Exception{
		String[] selectedMethodNames= {"m", "f"};
		String[][] selectedMethodSignatures= {new String[0], new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPullUp= {"m"};
		String[][] signaturesOfMethodsToPullUp= {new String[0]};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= {"f"};
		String[][] signaturesOfMethodsToDeclareAbstract= {new String[0]};

		declareAbstractHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								new String[0], namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, new String[0], true, true, 0);
	}

	public void test38() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {"A"};
		String[] namesOfMethodsToPullUp= {"m"};
		String[][] signaturesOfMethodsToPullUp= {new String[0]};
		String[] namesOfFieldsToPullUp= {"A"};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		declareAbstractHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								new String[0], namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, new String[0], true, true, 0);
	}

	public void test39() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {"A"};
		String[] selectedTypeNames= {"X", "Y"};
		String[] namesOfMethodsToPullUp= {"m"};
		String[][] signaturesOfMethodsToPullUp= {new String[0]};
		String[] namesOfFieldsToPullUp= {"A"};
		String[] namesOfTypesToPullUp= {"X", "Y"};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		declareAbstractHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								selectedTypeNames, namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, namesOfTypesToPullUp, true, true, 0);
	}

	/* disabled: dubious result
	public void test40() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] selectedTypeNames= {};
		String[] namesOfMethodsToPullUp= {"m"};
		String[][] signaturesOfMethodsToPullUp= {new String[0]};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfTypesToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper1(selectedMethodNames, selectedMethodSignatures, true, true, 0);
	}*/

	public void test41() throws Exception{
		String[] selectedMethodNames= {};
		String[][] selectedMethodSignatures= {};
		String[] selectedFieldNames= {"i"};
		String[] selectedTypeNames= {};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {"i"};
		String[] namesOfTypesToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper1(selectedFieldNames, true, true, 0);
	}

	public void test42() throws Exception{
		String[] selectedMethodNames= {};
		String[][] selectedMethodSignatures= {};
		String[] selectedFieldNames= {"i", "j"};
		String[] selectedTypeNames= {};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {"i", "j"};
		String[] namesOfTypesToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		declareAbstractHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								selectedTypeNames, namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, namesOfTypesToPullUp, true, true, 0);
	}

	public void test43() throws Exception{
//		printTestDisabledMessage("bug 35562 Method pull up wrongly indents javadoc comment [refactoring]");

		String[] selectedMethodNames= {"f"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] selectedTypeNames= {};
		String[] namesOfMethodsToPullUp= selectedMethodNames;
		String[][] signaturesOfMethodsToPullUp= selectedMethodSignatures;
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfTypesToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper1(selectedMethodNames, selectedMethodSignatures, true, true, 0);
	}

	public void test44() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {"A"};
		String[] selectedTypeNames= {"X", "Y"};
		String[] namesOfMethodsToPullUp= {"m"};
		String[][] signaturesOfMethodsToPullUp= {new String[0]};
		String[] namesOfFieldsToPullUp= {"A"};
		String[] namesOfTypesToPullUp= {"X", "Y"};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		declareAbstractHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								selectedTypeNames, namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, namesOfTypesToPullUp, true, true, 0);
	}

	public void test45() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {"A"};
		String[] selectedTypeNames= {"X", "Y"};
		String[] namesOfMethodsToPullUp= {"m"};
		String[][] signaturesOfMethodsToPullUp= {new String[0]};
		String[] namesOfFieldsToPullUp= {"A"};
		String[] namesOfTypesToPullUp= {"X", "Y"};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		declareAbstractHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								selectedTypeNames, namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, namesOfTypesToPullUp, true, true, 0);
	}

	/* disabled: pulling up into interfaces not implemented
	public void test46() throws Exception{
		// for bug 196635

		String[] selectedMethodNames= {"getConst"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {"CONST"};
		String[] selectedTypeNames= {};
		String[] namesOfMethodsToPullUp= {"getConst"};
		String[][] signaturesOfMethodsToPullUp= {new String[0]};
		String[] namesOfFieldsToPullUp= {"CONST"};
		String[] namesOfTypesToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		declareAbstractHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								selectedTypeNames, namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, namesOfTypesToPullUp, true, true, 0);
	}*/

	/* disabled: pulling up into interfaces not implemented
	public void test47() throws Exception{
		// for bug 211491

		String[] selectedMethodNames= {"method"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] selectedTypeNames= {};
		String[] namesOfMethodsToPullUp= {"method"};
		String[][] signaturesOfMethodsToPullUp= {new String[0]};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfTypesToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper1(selectedMethodNames, selectedMethodSignatures, true, true, 0);
	}*/

	/* disabled: retaining pulled method not implemented
	public void test48() throws Exception{
		// for bug 211491, but with a super class

		String[] selectedMethodNames= {"method"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] selectedTypeNames= {};
		String[] namesOfMethodsToPullUp= {"method"};
		String[][] signaturesOfMethodsToPullUp= {new String[0]};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfTypesToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper1(selectedMethodNames, selectedMethodSignatures, false, false, 0);
	}*/

	/* disabled: test case changes super call into normal call; we don't do that
	public void test49() throws Exception{
		// for bug 228950

		String[] selectedMethodNames= {"g"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] selectedTypeNames= {};
		String[] namesOfMethodsToPullUp= {"g"};
		String[][] signaturesOfMethodsToPullUp= {new String[0]};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfTypesToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};


		helper1(selectedMethodNames, selectedMethodSignatures, true, true, 0);
	}*/

	public void testFail0() throws Exception{
//		printTestDisabledMessage("6538: searchDeclarationsOf* incorrect");
		helper2(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	/* disabled: by Eclipse
	public void testFail1() throws Exception{
		printTestDisabledMessage("overloading - current limitation");
//		helper2(new String[]{"m"}, new String[][]{new String[0]}, true, false);
	}*/

	public void testFail2() throws Exception{
//		printTestDisabledMessage("6538: searchDeclarationsOf* incorrect");
		helper2(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void testFail3() throws Exception{
//		printTestDisabledMessage("6538: searchDeclarationsOf* incorrect");
		helper2(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void testFail4() throws Exception{
//		printTestDisabledMessage("6538: searchDeclarationsOf* incorrect");
		helper2(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void testFail6() throws Exception{
		String[] methodNames= new String[]{"m"};
		String[][] signatures= new String[][]{new String[0]};
		boolean deleteAllInSourceType= true;
		boolean deleteAllMatchingMethods= false;

		helper3(methodNames, signatures, deleteAllInSourceType, deleteAllMatchingMethods, 0, true);
	}

	public void testFail7() throws Exception{
		String[] methodNames= new String[]{"m"};
		String[][] signatures= new String[][]{new String[0]};
		boolean deleteAllInSourceType= true;
		boolean deleteAllMatchingMethods= false;
		helper3(methodNames, signatures, deleteAllInSourceType, deleteAllMatchingMethods, 0, false);
	}

	public void testFail8() throws Exception{
		String[] methodNames= new String[]{"m"};
		String[][] signatures= new String[][]{new String[0]};
		boolean deleteAllInSourceType= true;
		boolean deleteAllMatchingMethods= false;
		helper3(methodNames, signatures, deleteAllInSourceType, deleteAllMatchingMethods, 0, true);
	}

	public void testFail9() throws Exception{
		String[] methodNames= new String[]{"m"};
		String[][] signatures= new String[][]{new String[0]};
		boolean deleteAllInSourceType= true;
		boolean deleteAllMatchingMethods= false;
		helper3(methodNames, signatures, deleteAllInSourceType, deleteAllMatchingMethods, 0, true);
	}

	public void testFail10() throws Exception{
		String[] methodNames= new String[]{"m"};
		String[][] signatures= new String[][]{new String[0]};
		boolean deleteAllInSourceType= true;
		boolean deleteAllMatchingMethods= false;
		helper3(methodNames, signatures, deleteAllInSourceType, deleteAllMatchingMethods, 0, false);
	}

	public void testFail11() throws Exception{
		String[] methodNames= new String[]{"m"};
		String[][] signatures= new String[][]{new String[0]};
		boolean deleteAllInSourceType= true;
		boolean deleteAllMatchingMethods= false;
		helper3(methodNames, signatures, deleteAllInSourceType, deleteAllMatchingMethods, 0, true);
	}

	/* disabled: by Eclipse
	public void testFail12() throws Exception{
		printTestDisabledMessage("overloading - current limitation");
//		String[] methodNames= new String[]{"m"};
//		String[][] signatures= new String[][]{new String[0]};
//		boolean deleteAllInSourceType= true;
//		boolean deleteAllMatchingMethods= false;
//		helper3(methodNames, signatures, deleteAllInSourceType, deleteAllMatchingMethods);
	}*/

	public void testFail13() throws Exception{
		String[] methodNames= new String[]{"m"};
		String[][] signatures= new String[][]{new String[0]};
		boolean deleteAllInSourceType= true;
		boolean deleteAllMatchingMethods= false;
		helper2(methodNames, signatures, deleteAllInSourceType, deleteAllMatchingMethods, 0);
	}

	public void testFail14() throws Exception{
		//removed - this (pulling up classes) is allowed now
	}

	public void testFail15() throws Exception{
		String[] methodNames= new String[]{"m"};
		String[][] signatures= new String[][]{new String[0]};
		boolean deleteAllInSourceType= true;
		boolean deleteAllMatchingMethods= false;
		helper2(methodNames, signatures, deleteAllInSourceType, deleteAllMatchingMethods, 1);
	}

	public void testFail16() throws Exception{
		String[] methodNames= new String[]{"m"};
		String[][] signatures= new String[][]{new String[0]};
		boolean deleteAllInSourceType= true;
		boolean deleteAllMatchingMethods= false;
		helper2(methodNames, signatures, deleteAllInSourceType, deleteAllMatchingMethods, 1);
	}

	/* disabled: by Eclipse
	public void testFail17() throws Exception{
		printTestDisabledMessage("unimplemented test - see bug 29522");
//		String[] methodNames= new String[]{"m"};
//		String[][] signatures= new String[][]{new String[0]};
//		boolean deleteAllInSourceType= true;
//		boolean deleteAllMatchingMethods= false;
//		helper2(methodNames, signatures, deleteAllInSourceType, deleteAllMatchingMethods, 1);
	}

	public void testFail18() throws Exception{
		printTestDisabledMessage("unimplemented test - see bug 29522");
//		String[] methodNames= new String[]{"m"};
//		String[][] signatures= new String[][]{new String[0]};
//		boolean deleteAllInSourceType= true;
//		boolean deleteAllMatchingMethods= false;
//		helper2(methodNames, signatures, deleteAllInSourceType, deleteAllMatchingMethods, 0);
	}*/

	public void testFail19() throws Exception{
		String[] methodNames= new String[]{"m"};
		String[][] signatures= new String[][]{new String[0]};
		boolean deleteAllInSourceType= true;
		boolean deleteAllMatchingMethods= false;
		helper2(methodNames, signatures, deleteAllInSourceType, deleteAllMatchingMethods, 1);
	}

	public void testFail20() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		declareAbstractFailHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								new String[0], namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, new String[0], true, true, 0);
	}

	public void testFail21() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		declareAbstractFailHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								new String[0], namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, new String[0], true, true, 0);
	}

	public void testFail22() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		declareAbstractFailHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								new String[0], namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, new String[0], true, true, 0);
	}

	public void testFail23() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		declareAbstractFailHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								new String[0], namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, new String[0], true, true, 0);
	}

	public void testFail24() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		declareAbstractFailHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								new String[0], namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, new String[0], true, true, 0);
	}

	public void testFail25() throws Exception{
		String[] selectedMethodNames= {};
		String[][] selectedMethodSignatures= {};
		String[] selectedFieldNames= {};
		String[] selectedTypeNames= {"Test"};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfTypesToPullUp= {"Test"};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		declareAbstractFailHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								selectedTypeNames, namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, namesOfTypesToPullUp, true, true, 0);
	}

	public void testFail26() throws Exception{
		String[] selectedMethodNames= {};
		String[][] selectedMethodSignatures= {};
		String[] selectedFieldNames= {};
		String[] selectedTypeNames= {"Test"};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfTypesToPullUp= {"Test"};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		declareAbstractFailHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								selectedTypeNames, namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, namesOfTypesToPullUp, true, true, 0);
	}

	public void testFail27() throws Exception{
		String[] selectedMethodNames= {};
		String[][] selectedMethodSignatures= {};
		String[] selectedFieldNames= {};
		String[] selectedTypeNames= {"A"};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfTypesToPullUp= {"A"};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		declareAbstractFailHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								selectedTypeNames, namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, namesOfTypesToPullUp, true, true, 0);
	}

	public void testFail28() throws Exception{
		String[] selectedMethodNames= {};
		String[][] selectedMethodSignatures= {};
		String[] selectedFieldNames= {};
		String[] selectedTypeNames= {"Test"};
		String[] namesOfMethodsToPullUp= {};
		String[][] signaturesOfMethodsToPullUp= {};
		String[] namesOfFieldsToPullUp= {};
		String[] namesOfTypesToPullUp= {"Test"};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		declareAbstractFailHelper(selectedMethodNames, selectedMethodSignatures,
								selectedFieldNames,
								selectedTypeNames, namesOfMethodsToPullUp,
								signaturesOfMethodsToPullUp,
								namesOfFieldsToPullUp, namesOfMethodsToDeclareAbstract,
								signaturesOfMethodsToDeclareAbstract, namesOfTypesToPullUp, true, true, 0);
	}

	public void testFail29() throws Exception {
		helper2(new String[] {"stop"}, new String[][]{new String[0]}, true, false, 0);
	}

	//----------------------------------------------------------
	public void testField0() throws Exception{
		fieldHelper1(new String[]{"i"}, 0);
	}

	public void testFieldFail0() throws Exception{
		fieldHelper2(new String[]{"x"}, 0);
	}

	/* disabled: we can do this
	public void testFieldFail1() throws Exception{
		fieldHelper2(new String[]{"x"}, 0);
	}*/

	public void testFieldFail2() throws Exception{
		fieldHelper2(new String[]{"f"}, 1);
	}

	//---------------------------------------------------------
	public void testFieldMethod0() throws Exception{
		declareAbstractHelper(new String[]{"m"}, null, new String[]{"f"}, new String[0], new String[]{"m"},
							  null, new String[]{"f"}, new String[0], null, new String[0], true, true, 0);
	}

	//----
	public void testAddingRequiredMembers0() throws Exception{
		String[] fieldNames= {};
		String[] methodNames= {"m"};
		String[][] methodSignatures= {new String[0]};

		String[] expectedFieldNames= fieldNames;
		String[] expectedMethodNames= methodNames;
		String[][] expectedMethodSignatures= methodSignatures;
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}

	public void testAddingRequiredMembers1() throws Exception{
		String[] fieldNames= {};
		String[] methodNames= {"m"};
		String[][] methodSignatures= {new String[0]};

		String[] expectedFieldNames= fieldNames;
		String[] expectedMethodNames= methodNames;
		String[][] expectedMethodSignatures= methodSignatures;
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}

	public void testAddingRequiredMembers2() throws Exception{
		String[] fieldNames= {};
		String[] methodNames= {"m"};
		String[][] methodSignatures= {new String[0]};

		String[] expectedFieldNames= fieldNames;
		String[] expectedMethodNames= methodNames;
		String[][] expectedMethodSignatures= methodSignatures;
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}

	public void testAddingRequiredMembers3() throws Exception{
		String[] fieldNames= {};
		String[] methodNames= {"m"};
		String[][] methodSignatures= {new String[0]};

		String[] expectedFieldNames= fieldNames;
		String[] expectedMethodNames= {"m", "y"};
		String[][] expectedMethodSignatures= {new String[0], new String[0]};
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}

	public void testAddingRequiredMembers4() throws Exception{
		String[] fieldNames= {};
		String[] methodNames= {"m"};
		String[][] methodSignatures= {new String[0]};

		String[] expectedFieldNames= fieldNames;
		String[] expectedMethodNames= {"m", "y"};
		String[][] expectedMethodSignatures= {new String[0], new String[0]};
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}

	public void testAddingRequiredMembers5() throws Exception{
		String[] fieldNames= {"y"};
		String[] methodNames= {};
		String[][] methodSignatures= {};

		String[] expectedFieldNames= fieldNames;
		String[] expectedMethodNames= {"m"};
		String[][] expectedMethodSignatures= {new String[0]};
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}

	public void testAddingRequiredMembers6() throws Exception{
		String[] fieldNames= {};
		String[] methodNames= {"m"};
		String[][] methodSignatures= {new String[0]};

		String[] expectedFieldNames= fieldNames;
		String[] expectedMethodNames= methodNames;
		String[][] expectedMethodSignatures= methodSignatures;
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}

	public void testAddingRequiredMembers7() throws Exception{
		String[] fieldNames= {};
		String[] methodNames= {"m"};
		String[][] methodSignatures= {new String[0]};

		String[] expectedFieldNames= fieldNames;
		String[] expectedMethodNames= methodNames;
		String[][] expectedMethodSignatures= methodSignatures;
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}

	public void testAddingRequiredMembers8() throws Exception{
		String[] fieldNames= {};
		String[] methodNames= {"m"};
		String[][] methodSignatures= {new String[0]};

		String[] expectedFieldNames= fieldNames;
		String[] expectedMethodNames= {"m", "foo"};
		String[][] expectedMethodSignatures= {new String[0], new String[0]};
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}

	public void testAddingRequiredMembers9() throws Exception{
		String[] fieldNames= {"m"};
		String[] methodNames= {};
		String[][] methodSignatures= {};

		String[] expectedFieldNames= fieldNames;
		String[] expectedMethodNames= methodNames;
		String[][] expectedMethodSignatures= methodSignatures;
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}

	public void testAddingRequiredMembers10() throws Exception{
		String[] fieldNames= {"m"};
		String[] methodNames= {};
		String[][] methodSignatures= {};

		String[] expectedFieldNames= fieldNames;
		String[] expectedMethodNames= {"foo"};
		String[][] expectedMethodSignatures= {new String[0]};
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}

	public void testAddingRequiredMembers11() throws Exception{
		String[] fieldNames= {"m"};
		String[] methodNames= {};
		String[][] methodSignatures= {};

		String[] expectedFieldNames= fieldNames;
		String[] expectedMethodNames= {"foo"};
		String[][] expectedMethodSignatures= {new String[0]};
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}

	public void testAddingRequiredMembers12() throws Exception{
		String[] fieldNames= {};
		String[] methodNames= {"m"};
		String[][] methodSignatures= {new String[0]};

		String[] expectedFieldNames= fieldNames;
		String[] expectedMethodNames= {"foo", "m"};
		String[][] expectedMethodSignatures= {new String[0], new String[0]};
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}

	/* disabled: tests idiosyncratic features
	public void testEnablement0() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("B");
		IMember[] members= {typeB};
		assertTrue("should be enabled", RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement1() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("B");
		IType typeD= cu.getType("D");
		IMember[] members= {typeB, typeD};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement2() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("Outer").getType("B");
		IMember[] members= {typeB};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement3() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("B");
		IMember[] members= {typeB};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement4() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("B");
		IMember[] members= {typeB};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement5() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("B");
		IMember[] members= {typeB};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement6() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("B");
		IMember[] members= {typeB};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement7() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("B");
		IMember[] members= {typeB};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement8() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("Outer").getType("B");
		IMember[] members= {typeB};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement9() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("Outer").getType("B");
		IMember[] members= {typeB};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement10() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("Outer").getType("B");
		IMember[] members= {typeB};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement11() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("Outer").getType("B");
		IMember[] members= {typeB};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement12() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("Outer").getType("B");
		IMember[] members= {typeB};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement13() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("B");
		IType typeD= cu.getType("D");
		IMember[] members= {typeB, typeD};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement14() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("B");
		IType typeD= cu.getType("D");
		IMember[] members= {typeB, typeD};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement15() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("B");
		IType typeD= cu.getType("D");
		IMember[] members= {typeB, typeD};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement16() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("B");
		IType typeD= cu.getType("D");
		IMember[] members= {typeB, typeD};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement17() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("B");
		IMember[] members= {typeB};
		assertTrue("should be enabled", RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement18() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("B");
		IMember[] members= {typeB};
		assertTrue("should be enabled", RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement19() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("B");
		IMember[] members= {typeB};
		assertTrue("should be enabled", RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement20() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("B");
		IMember[] members= {typeB};
		assertTrue("should be enabled", RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement21() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("B");
		IMember[] members= {typeB};
		assertTrue("should be enabled", RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement22() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("B");
		IMember[] members= {typeB};
		assertTrue("should be enabled", RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement23() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("B");
		IMember[] members= {typeB};
		assertTrue("should be enabled", RefactoringAvailabilityTester.isPullUpAvailable(members));
	}

	public void testEnablement24() throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("B");
		IMember[] members= {typeB};
		assertTrue("should be enabled", RefactoringAvailabilityTester.isPullUpAvailable(members));
	}*/

	//------------------ tests -------------

	public void testStaticImports0() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void testStaticImports1() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void testGenerics0() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void testGenerics1() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void testGenerics2() throws Exception{
		helper1(new String[]{"mmm", "n"}, new String[][]{new String[] {"QT;"}, new String[0]}, true, false, 0);
	}

	/* disabled: unifying multiple pulled-up methods not supported
	public void testGenerics3() throws Exception{
		helper1(new String[]{"mmm", "n"}, new String[][]{new String[] {"QT;"}, new String[0]}, true, true, 0);
	}*/

	/* disabled: by Eclipse
	public void testGenerics4() throws Exception{
		printTestDisabledMessage("see bug 75642");

//		ICompilationUnit cuA= createCUfromTestFile(getPackageP(), "A");
//		ICompilationUnit cuB= createCUfromTestFile(getPackageP(), "B");
//
//		try{
//			String[] methodNames= new String[]{"m"};
//			String[][] signatures= new String[][]{new String[]{"QList<QT;>;"}};
//
//			IType type= getType(cuB, "B");
//			IMethod[] methods= getMethods(type, methodNames, signatures);
//			PullUpRefactoring ref= createRefactoring(methods);
//			assertTrue("activation", ref.checkInitialConditions(new NullProgressMonitor()).isOK());
//			setSuperclassAsTargetClass(ref);
//
//			ref.setMethodsToDelete(getMethods(ref.getMatchingElements(new NullProgressMonitor(), false)));
//
//			RefactoringStatus result= performRefactoring(ref);
//			assertEquals("precondition was supposed to pass", null, result);
//
//			assertEqualLines("A", cuA.getSource(), getFileContents(getOutputTestFileName("A")));
//			assertEqualLines("B", cuB.getSource(), getFileContents(getOutputTestFileName("B")));
//		} finally{
//			performDummySearch();
//			cuA.delete(false, null);
//			cuB.delete(false, null);
//		}
	}*/

	public void testGenerics5() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void testGenerics6() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void testGenerics7() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void testGenerics8() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	/* disabled: test case changes super call into normal call; we don't do that
	public void testGenerics9() throws Exception{
			helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}*/

	public void testGenerics10() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void testGenerics11() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}

	public void testGenerics12() throws Exception{
		helper1(new String[]{"m"}, new String[][]{new String[0]}, true, false, 0);
	}
	
	public void testGenerics13() throws Exception {
		helper1(new String[] { "m"}, new String[][] { new String[0]}, true, false, 0);
	}

	public void testGenerics14() throws Exception {
		helper1(new String[] { "m"}, new String[][] { new String[0]}, true, false, 0);
	}

	public void testGenerics15() throws Exception {
		helper1(new String[] { "m"}, new String[][] { new String[0]}, true, false, 0);
	}

	public void testGenericsFail0() throws Exception {
		helper2(new String[] { "m"}, new String[][] { new String[] {"QT;"}}, true, false, 0);
	}

	public void testGenericsFail1() throws Exception {
		helper2(new String[] { "m"}, new String[][] { new String[]{"QS;"}}, true, false, 0);
	}

	public void testGenericsFail2() throws Exception {
		helper2(new String[] { "m"}, new String[][] { new String[]{"QT;"}}, true, false, 0);
	}
}
