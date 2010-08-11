/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package tests.eclipse.PushDown;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.FieldDeclaration;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class PushDownTests extends TestCase {

	public PushDownTests(String name) {
		super(name);
	}
	
	private void pushDownMethod(String name, boolean leaveAbstract, boolean succeed) {
		Program in = CompileHelper.compile("tests/eclipse/PushDown/"+getName()+"/in/A.java");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		
		TypeDecl td = in.findType("A");
		assertNotNull(td);
		
		MethodDecl md = td.findMethod(name);
		assertNotNull(md);
		
		try {
			md.doPushDown(leaveAbstract);
			
			if(!succeed)
				assertEquals("<failure>", in.toString());
			
			Program out = CompileHelper.compile("tests/eclipse/PushDown/"+getName()+"/out/A.java");
			assertNotNull(out);
			
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			if(succeed)
				fail(rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void pushDownField(String name, boolean succeed) {
		Program in = CompileHelper.compile("tests/eclipse/PushDown/"+getName()+"/in/A.java");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		
		TypeDecl td = in.findType("A");
		assertNotNull(td);
		
		FieldDeclaration fd = td.findField(name);
		assertNotNull(fd);
		
		try {
			fd.doPushDown();
			
			if(!succeed)
				assertEquals("<failure>", in.toString());
			
			Program out = CompileHelper.compile("tests/eclipse/PushDown/"+getName()+"/out/A.java");
			assertNotNull(out);
			
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			if(succeed)
				fail(rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper(String[] selectedMethodNames,
			String[][] selectedMethodSignatures, String[] selectedFieldNames,
			String[] namesOfMethodsToPushDown,
			String[][] signaturesOfMethodsToPushDown,
			String[] namesOfFieldsToPushDown,
			String[] namesOfMethodsToDeclareAbstract,
			String[][] signaturesOfMethodsToDeclareAbstract, Object object,
			Object object2) {
		if(selectedMethodNames.length > 0) {
			assertTrue("Cannot push more than one method.", selectedMethodNames.length == 1);
			pushDownMethod(selectedMethodNames[0], namesOfMethodsToDeclareAbstract == selectedMethodNames, true);
		} else {
			assertTrue("Cannot push more than one field.", selectedFieldNames.length == 1);
			pushDownField(selectedFieldNames[0], true);
		}
	}

	private void failActivationHelper(String[] selectedMethodNames,
			String[][] selectedMethodSignatures, String[] selectedFieldNames,
			String[] namesOfMethodsToPushDown,
			String[][] signaturesOfMethodsToPushDown,
			String[] namesOfFieldsToPushDown,
			String[] namesOfMethodsToDeclareAbstract,
			String[][] signaturesOfMethodsToDeclareAbstract, Object object) {
		if(selectedMethodNames.length > 0) {
			assertTrue("Cannot push more than one method.", selectedMethodNames.length == 1);
			pushDownMethod(selectedMethodNames[0], namesOfMethodsToDeclareAbstract == selectedMethodNames, false);
		} else {
			assertTrue("Cannot push more than one field.", selectedFieldNames.length == 1);
			pushDownField(selectedFieldNames[0], false);
		}
	}

	private void failInputHelper(String[] selectedMethodNames,
			String[][] selectedMethodSignatures, String[] selectedFieldNames,
			String[] namesOfMethodsToPushDown,
			String[][] signaturesOfMethodsToPushDown,
			String[] namesOfFieldsToPushDown,
			String[] namesOfMethodsToDeclareAbstract,
			String[][] signaturesOfMethodsToDeclareAbstract, Object object) {
		if(selectedMethodNames.length > 0) {
			assertTrue("Cannot push more than one method.", selectedMethodNames.length == 1);
			pushDownMethod(selectedMethodNames[0], namesOfMethodsToDeclareAbstract == selectedMethodNames, false);
		} else {
			assertTrue("Cannot push more than one field.", selectedFieldNames.length == 1);
			pushDownField(selectedFieldNames[0], false);
		}
	}

	//--------------------------------------------------------

	public void test0() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test1() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test2() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test3() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test4() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test5() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test6() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test7() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test8() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test9() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test10() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test11() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test12() throws Exception{
		String[] selectedMethodNames= {};
		String[][] selectedMethodSignatures= {};
		String[] selectedFieldNames= {"f"};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {"f"};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test13() throws Exception{
		String[] selectedMethodNames= {};
		String[][] selectedMethodSignatures= {};
		String[] selectedFieldNames= {"f"};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {"f"};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test14() throws Exception{
		String[] selectedMethodNames= {"f"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test15() throws Exception{
		String[] selectedMethodNames= {"f"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test16() throws Exception{
		String[] selectedMethodNames= {"f"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test17() throws Exception{
		String[] selectedMethodNames= {"f"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	/* disabled: multipush
	public void test18() throws Exception{
		String[] selectedMethodNames= {"f", "m"};
		String[][] selectedMethodSignatures= {new String[0], new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}*/

	/* disabled: multipush
	public void test19() throws Exception{
		String[] selectedMethodNames= {"f", "m"};
		String[][] selectedMethodSignatures= {new String[0], new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}*/

	/* disabled: multipush
	public void test20() throws Exception{
		String[] selectedMethodNames= {"f", "m"};
		String[][] selectedMethodSignatures= {new String[0], new String[0]};
		String[] selectedFieldNames= {"i"};
		String[] namesOfMethodsToPushDown= {"f"};
		String[][] signaturesOfMethodsToPushDown= {new String[0]};
		String[] namesOfFieldsToPushDown= {"i"};
		String[] namesOfMethodsToDeclareAbstract= {"m"};
		String[][] signaturesOfMethodsToDeclareAbstract= {new String[0]};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract,
			   new String[]{"B"}, new String[]{"p"});
	}*/

	/* disabled: multipush
	public void test21() throws Exception{
		String[] selectedMethodNames= {"f", "m"};
		String[][] selectedMethodSignatures= {new String[0], new String[0]};
		String[] selectedFieldNames= {"i"};
		String[] namesOfMethodsToPushDown= {"f", "m"};
		String[][] signaturesOfMethodsToPushDown= {new String[0], new String[0]};
		String[] namesOfFieldsToPushDown= {"i"};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract,
			   new String[]{"B", "C"}, new String[]{"p", "p"});
	}*/

	public void test22() throws Exception{
		String[] selectedMethodNames= {};
		String[][] selectedMethodSignatures= {};
		String[] selectedFieldNames= {"bar"};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= selectedFieldNames;
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test23() throws Exception{
		String[] selectedMethodNames= {};
		String[][] selectedMethodSignatures= {};
		String[] selectedFieldNames= {"bar"};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= selectedFieldNames;
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	/* disabled: multipush
	public void test24() throws Exception{
		String[] selectedMethodNames= {};
		String[][] selectedMethodSignatures= {};
		String[] selectedFieldNames= {"foo", "bar"};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= selectedFieldNames;
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}*/

	public void test25() throws Exception{
		String[] selectedMethodNames= {"foo"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test26() throws Exception{
		String[] selectedMethodNames= {"bar"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test27() throws Exception{
		String[] selectedMethodNames= {"bar"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	/* disabled: multipush
	public void test28() throws Exception{
//		if (true){
//			printTestDisabledMessage("37175");
//			return;
//		}
		String[] selectedMethodNames= {};
		String[][] selectedMethodSignatures= {};
		String[] selectedFieldNames= {"i", "j"};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {"i", "j"};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}*/

	public void test29() throws Exception{
		String[] selectedMethodNames= {"foo"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test30() throws Exception{
		String[] selectedMethodNames= {"foo"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test31() throws Exception{
		String[] selectedMethodNames= {"foo"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void test32() throws Exception{
		String[] selectedMethodNames= {"foo"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	/* disabled: multipush
	public void test33() throws Exception{
		String[] selectedMethodNames= {"f", "m"};
		String[][] selectedMethodSignatures= {new String[0], new String[0]};
		String[] selectedFieldNames= {"i"};
		String[] namesOfMethodsToPushDown= {"f", "m"};
		String[][] signaturesOfMethodsToPushDown= {new String[0], new String[0]};
		String[] namesOfFieldsToPushDown= {"i"};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract,
			   new String[]{"B", "C"}, new String[]{"p", "p"});
	}*/
	
	/* disabled: by Eclipse
	public void test34() throws Exception{
		printTestDisabledMessage("disabled due to missing support for statically imported methods");

//		String[] selectedMethodNames= {"f", "m"};
//		String[][] selectedMethodSignatures= {new String[0], new String[0]};
//		String[] selectedFieldNames= {"i"};
//		String[] namesOfMethodsToPushDown= {"f", "m"};
//		String[][] signaturesOfMethodsToPushDown= {new String[0], new String[0]};
//		String[] namesOfFieldsToPushDown= {"i"};
//		String[] namesOfMethodsToDeclareAbstract= {};
//		String[][] signaturesOfMethodsToDeclareAbstract= {};
//
//		helper(selectedMethodNames, selectedMethodSignatures,
//			   selectedFieldNames,
//			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
//			   namesOfFieldsToPushDown,
//			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract,
//			   new String[]{"B", "C"}, new String[]{"p", "p"});
	}*/

	public void testFail0() throws Exception {
		/* disabled: push into outer space
		String[] selectedMethodNames= {"f"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		failActivationHelper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract,
			   null);*/
	}

	public void testFail1() throws Exception {
		/* disabled: push into outer space
		String[] selectedMethodNames= {"f"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		failActivationHelper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract,
			   null);*/
	}

	/* disabled: we can do this
	public void testFail2() throws Exception {
		String[] selectedMethodNames= {"f"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		failInputHelper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract,
			   null);
	}*/

	public void testFail3() throws Exception {
		String[] selectedMethodNames= {};
		String[][] selectedMethodSignatures= {};
		String[] selectedFieldNames= {"i"};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= selectedFieldNames;
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		failInputHelper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract,
			   null);
	}

	/* disabled: visibility
	public void testVisibility1() throws Exception {
		String[] selectedMethodNames= {"f"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}*/

	/* disabled: visibility
	public void testVisibility2() throws Exception {
		String[] selectedMethodNames= {"f"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}*/

	/* disabled: visibility
	public void testVisibility3() throws Exception {
		String[] selectedMethodNames= {"f"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}*/

	public void testFail7() throws Exception {
		String[] selectedMethodNames= {"f"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		failInputHelper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract,
			   null);
	}

	public void testFail8() throws Exception {
		String[] selectedMethodNames= {"f"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		failInputHelper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract,
			   null);
	}

	public void testFail9() throws Exception {
		String[] selectedMethodNames= {};
		String[][] selectedMethodSignatures= {};
		String[] selectedFieldNames= {"f"};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= selectedFieldNames;
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		failInputHelper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract,
			   null);
	}

	public void testFail10() throws Exception {
		String[] selectedMethodNames= {"foo"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		failInputHelper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract,
			   null);
	}

	public void testFail11() throws Exception {
		String[] selectedMethodNames= {"foo"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		failInputHelper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract,
			   null);
	}

	public void testFail12() throws Exception {
		String[] selectedMethodNames= {};
		String[][] selectedMethodSignatures= {};
		String[] selectedFieldNames= {"bar"};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= selectedFieldNames;
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		failInputHelper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract,
			   null);
	}

	/* disabled: visibility
	public void testVisibility0() throws Exception {
		String[] selectedMethodNames= {"foo"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}*/

	/* disabled: tests idiosyncratic feature
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
		String[] expectedMethodNames= {"m", "f"};
		String[][] expectedMethodSignatures= {new String[0], new String[0]};
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}

	public void testAddingRequiredMembers4() throws Exception{
		String[] fieldNames= {};
		String[] methodNames= {"m", "f"};
		String[][] methodSignatures= {new String[0], new String[0]};

		String[] expectedFieldNames= fieldNames;
		String[] expectedMethodNames= methodNames;
		String[][] expectedMethodSignatures= methodSignatures;
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}

	public void testAddingRequiredMembers5() throws Exception{
		String[] fieldNames= {};
		String[] methodNames= {"m"};
		String[][] methodSignatures= {new String[0]};

		String[] expectedFieldNames= {"f"};
		String[] expectedMethodNames= methodNames;
		String[][] expectedMethodSignatures= methodSignatures;
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}

	public void testAddingRequiredMembers6() throws Exception{
		String[] fieldNames= {"f"};
		String[] methodNames= {"m"};
		String[][] methodSignatures= {new String[0]};

		String[] expectedFieldNames= fieldNames;
		String[] expectedMethodNames= methodNames;
		String[][] expectedMethodSignatures= methodSignatures;
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}

	public void testAddingRequiredMembers7() throws Exception{
		String[] fieldNames= {"f"};
		String[] methodNames= {};
		String[][] methodSignatures= {new String[0]};

		String[] expectedFieldNames= fieldNames;
		String[] expectedMethodNames= {"m"};
		String[][] expectedMethodSignatures= {new String[0]};
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}

	public void testAddingRequiredMembers8() throws Exception{
		String[] fieldNames= {"f"};
		String[] methodNames= {};
		String[][] methodSignatures= {new String[0]};

		String[] expectedFieldNames= {"f", "m"};
		String[] expectedMethodNames= {};
		String[][] expectedMethodSignatures= {new String[0]};
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}

	public void testAddingRequiredMembers9() throws Exception{
		String[] fieldNames= {"f"};
		String[] methodNames= {};
		String[][] methodSignatures= {new String[0]};

		String[] expectedFieldNames= {"f", "m"};
		String[] expectedMethodNames= {};
		String[][] expectedMethodSignatures= {new String[0]};
		addRequiredMembersHelper(fieldNames, methodNames, methodSignatures, expectedFieldNames, expectedMethodNames, expectedMethodSignatures);
	}*/

	/* disabled: tests idiosyncratic feature
	public void testEnablement0() throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeA= cu.getType("A");
		IMember[] members= {typeA};
		assertTrue("should be enabled", RefactoringAvailabilityTester.isPushDownAvailable(members));
	}

	public void testEnablement1() throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeA= cu.getType("A");
		IMember[] members= {typeA};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPushDownAvailable(members));
	}

	public void testEnablement2() throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("Outer").getType("B");
		IMember[] members= {typeB};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPushDownAvailable(members));
	}

	public void testEnablement3() throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeA= cu.getType("A");
		IType typeB= cu.getType("B");
		IMember[] members= {typeA, typeB};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPushDownAvailable(members));
	}

	public void testEnablement4() throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeA= cu.getType("A");
		IMember[] members= {typeA};
		assertTrue("should be enabled", RefactoringAvailabilityTester.isPushDownAvailable(members));
	}

	public void testEnablement5() throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeA= cu.getType("A");
		IMember[] members= {typeA};
		assertTrue("should be enabled", RefactoringAvailabilityTester.isPushDownAvailable(members));
	}

	public void testEnablement6() throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeA= cu.getType("A");
		IMember[] members= {typeA};
		assertTrue("should be enabled", RefactoringAvailabilityTester.isPushDownAvailable(members));
	}

	public void testEnablement7() throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeA= cu.getType("A");
		IMember[] members= {typeA};
		assertTrue("should be enabled", RefactoringAvailabilityTester.isPushDownAvailable(members));
	}

	public void testEnablement8() throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeA= cu.getType("A");
		IMember[] members= {typeA};
		assertTrue("should be enabled", RefactoringAvailabilityTester.isPushDownAvailable(members));
	}

	public void testEnablement9() throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeA= cu.getType("A");
		IMember[] members= {typeA};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPushDownAvailable(members));
	}

	public void testEnablement10() throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeA= cu.getType("A");
		IMember[] members= {typeA};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPushDownAvailable(members));
	}

	public void testEnablement11() throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeA= cu.getType("A");
		IMember[] members= {typeA};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPushDownAvailable(members));
	}

	public void testEnablement12() throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("Outer").getType("B");
		IMember[] members= {typeB};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPushDownAvailable(members));
	}

	public void testEnablement13() throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("Outer").getType("B");
		IMember[] members= {typeB};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPushDownAvailable(members));
	}

	public void testEnablement14() throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("Outer").getType("B");
		IMember[] members= {typeB};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPushDownAvailable(members));
	}

	public void testEnablement15() throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
		IType typeB= cu.getType("Outer").getType("B");
		IMember[] members= {typeB};
		assertTrue("should be disabled", ! RefactoringAvailabilityTester.isPushDownAvailable(members));
	}*/

	public void testGenerics0() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void testGenerics1() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void testGenerics2() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void testGenerics3() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void testGenerics4() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void testGenerics5() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void testGenerics6() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void testGenerics7() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void testGenerics8() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void testGenerics9() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void testGenerics10() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void testGenerics11() throws Exception{
		String[] selectedMethodNames= {"m"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void testGenerics12() throws Exception{
		String[] selectedMethodNames= {};
		String[][] selectedMethodSignatures= {};
		String[] selectedFieldNames= {"f"};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {"f"};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void testGenerics13() throws Exception{
		String[] selectedMethodNames= {};
		String[][] selectedMethodSignatures= {};
		String[] selectedFieldNames= {"f"};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {"f"};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void testGenerics14() throws Exception{
		String[] selectedMethodNames= {"f"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void testGenerics15() throws Exception{
		String[] selectedMethodNames= {"f"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void testGenerics16() throws Exception{
		String[] selectedMethodNames= {"f"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	public void testGenerics17() throws Exception{
		String[] selectedMethodNames= {"f"};
		String[][] selectedMethodSignatures= {new String[0]};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}

	/* disabled: multipush
	public void testGenerics18() throws Exception{
		String[] selectedMethodNames= {"f", "m"};
		String[][] selectedMethodSignatures= {new String[0], new String[] {"QT;"}};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= selectedMethodNames;
		String[][] signaturesOfMethodsToPushDown= selectedMethodSignatures;
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= {};
		String[][] signaturesOfMethodsToDeclareAbstract= {};

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}*/

	/* disabled: multipush
	public void testGenerics19() throws Exception{
		String[] selectedMethodNames= {"f", "m"};
		String[][] selectedMethodSignatures= {new String[0], new String[]{"QT;"}};
		String[] selectedFieldNames= {};
		String[] namesOfMethodsToPushDown= {};
		String[][] signaturesOfMethodsToPushDown= {};
		String[] namesOfFieldsToPushDown= {};
		String[] namesOfMethodsToDeclareAbstract= selectedMethodNames;
		String[][] signaturesOfMethodsToDeclareAbstract= selectedMethodSignatures;

		helper(selectedMethodNames, selectedMethodSignatures,
			   selectedFieldNames,
			   namesOfMethodsToPushDown, signaturesOfMethodsToPushDown,
			   namesOfFieldsToPushDown,
			   namesOfMethodsToDeclareAbstract, signaturesOfMethodsToDeclareAbstract, null, null);
	}*/
}
