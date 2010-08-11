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
package tests.eclipse.InlineTemp;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import tests.eclipse.PromoteTempToField.PromoteTempToFieldTests;
import AST.Program;
import AST.RefactoringException;
import AST.VariableDeclaration;

public class InlineTempTests extends TestCase {

	public InlineTempTests(String name) {
		super(name);
	}

	private String getSimpleTestFileName(boolean canInline, boolean input){
		String fileName = "A_" + getName();
		if (canInline)
			fileName += input ? "_in": "_out";
		return fileName + ".java";
	}

	private String getTestFileName(boolean canInline, boolean input){
		String fileName= "tests/eclipse/InlineTemp/";
		fileName += (canInline ? "canInline/": "cannotInline/");
		return fileName + getSimpleTestFileName(canInline, input);
	}

	private void helper1(int startLine, int startColumn, int endLine, int endColumn) throws Exception{
		Program in = CompileHelper.compile(getTestFileName(true, true));
		Program out = CompileHelper.compile(getTestFileName(true, false));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		
		VariableDeclaration decl = PromoteTempToFieldTests.findNode(in, VariableDeclaration.class, startLine, startColumn, endLine, endColumn);
		assertNotNull(decl);
		
		try {
			decl.doInline();
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper2(int startLine, int startColumn, int endLine, int endColumn) throws Exception{
		Program in = CompileHelper.compile(getTestFileName(false, true));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		
		VariableDeclaration decl = PromoteTempToFieldTests.findNode(in, VariableDeclaration.class, startLine, startColumn, endLine, endColumn);
		assertNotNull(decl);
		
		try {
			decl.doInline();
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) {
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}


	//--- tests

	public void test0() throws Exception{
		helper1(4, 9, 4, 17);
	}

	public void test1() throws Exception{
		helper1(4, 9, 4, 17);
	}

	public void test2() throws Exception{
		helper1(4, 9, 4, 17);
	}

	public void test3() throws Exception{
		helper1(4, 9, 4, 21);
	}

	public void test4() throws Exception{
		helper1(4, 9, 4, 21);
	}

	public void test5() throws Exception{
		helper1(4, 9, 4, 21);
	}

	/* disabled: does not compile
	public void test6() throws Exception{
		//printTestDisabledMessage("bug#6429 declaration source start incorrect on local variable");
		helper1(9, 13, 9, 14);
	}*/

	/* disabled: does not compile
	public void test7() throws Exception{
		helper1(9, 9, 9, 18);
	}*/

	public void test8() throws Exception{
		//printTestDisabledMessage("bug#6429 declaration source start incorrect on local variable");
		helper1(5, 13, 5, 14);
	}

	public void test9() throws Exception{
		helper1(5, 9, 5, 20);
	}

	public void test10() throws Exception{
//		printTestDisabledMessage("regression test for bug#9001");
		helper1(4, 21, 4, 25);
	}

	public void test11() throws Exception{
		helper1(5, 21, 5, 25);
	}

	public void test12() throws Exception{
		helper1(5, 15, 5, 19);
	}

	/* disabled: unsupported feature (inlining expression with side effect)
	public void test13() throws Exception{
		helper1(5, 17, 5, 18);
	}*/

	public void test14() throws Exception{
//		printTestDisabledMessage("regression for bug 11664");
		helper1(4, 13, 4, 14);
	}

	public void test15() throws Exception{
//		printTestDisabledMessage("regression for bug 11664");
		helper1(4, 19, 4, 20);
	}

	public void test16() throws Exception{
//		printTestDisabledMessage("regression test for 10751");
		helper1(5, 17, 5, 24);
	}

	public void test17() throws Exception{
//		printTestDisabledMessage("regression test for 12200");
		helper1(7, 7, 7, 18);
	}

	public void test18() throws Exception{
//		printTestDisabledMessage("regression test for 12200");
		helper1(5, 7, 5, 18);
	}

	public void test19() throws Exception{
//		printTestDisabledMessage("regression test for 12212");
		helper1(5, 7, 5, 18);
	}

	public void test20() throws Exception{
//		printTestDisabledMessage("regression test for 16054");
		helper1(4, 17, 4, 18);
	}

	public void test21() throws Exception{
//		printTestDisabledMessage("regression test for 17479");
		helper1(5, 9, 5, 42);
	}

	public void test22() throws Exception{
//		printTestDisabledMessage("regression test for 18284");
		helper1(5, 13, 5, 17);
	}

	public void test23() throws Exception{
//		printTestDisabledMessage("regression test for 22938");
		helper1(5, 16, 5, 20);
	}

	public void test24() throws Exception{
//		printTestDisabledMessage("regression test for 26242");
		helper1(5, 19, 5, 24);
	}

	public void test25() throws Exception{
//		printTestDisabledMessage("regression test for 26242");
		helper1(5, 19, 5, 24);
	}

	public void test26() throws Exception{
		helper1(5, 17, 5, 24);
	}

	public void test27() throws Exception{
		helper1(5, 22, 5, 29);
	}

	public void test28() throws Exception{
		helper1(11, 14, 11, 21);
	}

	/* disabled: does not compile
	public void test29() throws Exception{
		helper1(4, 8, 4, 11);
	}*/

	/* disabled: does not compile
	public void test30() throws Exception{
		helper1(4, 8, 4, 11);
	}*/

	/* disabled: conservative data flow
	public void test31() throws Exception{
		helper1(8, 30, 8, 30);
	}*/

	/* disabled: conservative data flow
	public void test32() throws Exception{
		helper1(10, 27, 10, 27);
	}*/

	public void test33() throws Exception{
		// add explicit cast for primitive types: https://bugs.eclipse.org/bugs/show_bug.cgi?id=46216
		helper1(5, 22, 5, 22);
	}
	
	public void test34() throws Exception{
		// add explicit cast for boxing: https://bugs.eclipse.org/bugs/show_bug.cgi?id=201434#c4
		helper1(5, 11, 5, 11);
	}
	
	public void test35() throws Exception{
		// add explicit cast for unchecked conversion: https://bugs.eclipse.org/bugs/show_bug.cgi?id=201434#c0
		helper1(7, 32, 7, 36);
	}
	
	public void test36() throws Exception{
		// parenthesize complex cast expression
		helper1(7, 8, 7, 10);
	}
	
	/* disabled: unsupported feature (inlining impure expressions)
	public void test37() throws Exception{
		// parameterized method invocation needs class expression: https://bugs.eclipse.org/bugs/show_bug.cgi?id=277968
		helper1(5, 16, 5, 17);
	}
	
	public void test38() throws Exception{
		// parameterized method invocation needs this expression: https://bugs.eclipse.org/bugs/show_bug.cgi?id=277968
		helper1(5, 16, 5, 17);
	}
	
	public void test39() throws Exception{
		// parameterized method invocation needs to keep super expression: https://bugs.eclipse.org/bugs/show_bug.cgi?id=277968
		helper1(5, 16, 5, 17);
	}
	
	public void test40() throws Exception{
		// better cast for unboxing: https://bugs.eclipse.org/bugs/show_bug.cgi?id=297868
		helper1(5, 43, 5, 46);
	}*/
	
	//------

	/* disabled: by Eclipse
	public void testFail0() throws Exception{
		printTestDisabledMessage("compile errors are ok now");
//		helper2();
	}

	public void testFail1() throws Exception{
		printTestDisabledMessage("compile errors are ok now");
//		helper2();
	}*/

	/* disabled: tests idiosyncratic feature
	public void testFail2() throws Exception{
		helper2();
	}*/

	/* disabled: we can do this
	public void testFail3() throws Exception{
		helper2(5, 14, 5, 19);
	}

	public void testFail4() throws Exception{
		helper2(5, 14, 5, 22);
	}*/

	public void testFail5() throws Exception{
		helper2(5, 14, 5, 22);
	}

	public void testFail6() throws Exception{
		helper2(5, 14, 5, 22);
	}

	/* disabled: tests idiosyncratic feature
	public void testFail7() throws Exception{
		helper2();
	}*/

	/* disabled: tests idiosyncratic feature
	public void testFail8() throws Exception{
		helper2();
	}*/

	/* disabled: tests idiosyncratic feature
	public void testFail9() throws Exception{
		//test for 16737
		helper2(3, 9, 3, 13);
	}*/

	/* disabled: tests idiosyncratic feature
	public void testFail10() throws Exception{
		//test for 16737
		helper2(3, 5, 3, 17);
	}*/

	/* disabled: we can do this
	public void testFail11() throws Exception{
		//test for 17253
		helper2(8, 9, 8, 24);
	}*/

	/* disabled: does not compile
	public void testFail12() throws Exception{
		//test for 19851
		helper2(10, 16, 10, 19);
	}*/

	public void testFail13() throws Exception{
//		printTestDisabledMessage("12106");
		helper2(4, 18, 4, 19);
	}

}
