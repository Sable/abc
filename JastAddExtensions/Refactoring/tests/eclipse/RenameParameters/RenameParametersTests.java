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
package tests.eclipse.RenameParameters;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class RenameParametersTests extends TestCase {
	public RenameParametersTests(String name){
		super(name);
	}

	private String getSimpleTestFileName(boolean canRename, boolean input){
		String fileName = "A_" + getName();
		if (canRename)
			fileName += input ? "_in": "_out";
		return fileName + ".java";
	}

	private String getTestFileName(boolean canRename, boolean input){
		String fileName= "tests/eclipse/RenameParameters/";
		fileName += (canRename ? "canRename/": "cannotRename/");
		return fileName + getSimpleTestFileName(canRename, input);
	}

	//------------
	private void helper1(String[] newNames, String[] signature) throws Exception{
		Program in = CompileHelper.compile(getTestFileName(true, true));
		Program out = CompileHelper.compile(getTestFileName(true, false));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		
		TypeDecl A = in.findSimpleType("A");
		assertNotNull(A);
		MethodDecl m = A.findMethod("m");
		assertNotNull(m);
		
		try {
			for(int i=0;i<m.getNumParameter();++i)
				m.getParameter(i).rename(newNames[i]);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper2(String[] newNames, String[] signature) throws Exception{
		Program in = CompileHelper.compile(getTestFileName(false, true));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		
		TypeDecl A = in.findSimpleType("A");
		assertNotNull(A);
		MethodDecl m = A.findMethod("m");
		assertNotNull(m);
		
		try {
			for(int i=0;i<m.getNumParameter();++i)
				m.getParameter(i).rename(newNames[i]);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) {
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	public void test0() throws Exception{
		helper1(new String[]{"j"}, new String[]{"I"});
	}

	public void test1() throws Exception{
		helper1(new String[]{"j"}, new String[]{"I"});
	}

	public void test2() throws Exception{
		helper1(new String[]{"j", "k"}, new String[]{"I", "I"});
	}

	public void test3() throws Exception{
		helper1(new String[]{"j", "j1"}, new String[]{"I", "I"});
	}

	public void test4() throws Exception{
		helper1(new String[]{"k"}, new String[]{"QA;"});
	}

	public void test5() throws Exception{
		helper1(new String[]{"k"}, new String[]{"I"});
	}

	public void test6() throws Exception{
		helper1(new String[]{"k"}, new String[]{"I"});
	}

	public void test7() throws Exception{
		helper1(new String[]{"k"}, new String[]{"QA;"});
	}

	public void test8() throws Exception{
		helper1(new String[]{"j"}, new String[]{"I"});
	}

	public void test9() throws Exception{
		helper1(new String[]{"j"}, new String[]{"I"});
	}

	public void test10() throws Exception{
		helper1(new String[]{"j"}, new String[]{"I"});
	}

	public void test11() throws Exception{
		//printTestDisabledMessage("revisit in the context of anonymous types in type hierarchies");
		helper1(new String[]{"j"}, new String[]{"I"});
	}

	public void test12() throws Exception{
		helper1(new String[]{"j"}, new String[]{"I"});
	}

	public void test13() throws Exception{
		helper1(new String[]{"j"}, new String[]{"I"});
	}

	public void test14() throws Exception{
		helper1(new String[]{"j"}, new String[]{"QA;"});
	}

	/* disabled: tests idiosyncratic feature
	public void test15() throws Exception{
		helper1(new String[]{"j", "i"}, new String[]{"I", "I"});
	}*/

	public void test16() throws Exception{
		helper1(new String[]{"j"}, new String[]{"I"});
	}

	/* disabled: tests idiosyncratic feature
	public void test17() throws Exception{
		helper1(new String[]{"j", "i", "k"}, new String[]{"I", "I", "I"});
	}*/

	public void test18() throws Exception{
		helper1(new String[]{"j"}, new String[]{"QObject;"});
	}

	public void test19() throws Exception{
		helper1(new String[]{"j"}, new String[]{"QA;"});
	}

	public void test20() throws Exception{
		helper1(new String[]{"j"}, new String[]{"Qi;"});
	}

	public void test21() throws Exception{
		helper1(new String[]{"j"}, new String[]{"I"});
	}

	public void test22() throws Exception{
		helper1(new String[]{"j"}, new String[]{"I"});
	}

	/* disabled: test idiosyncratic feature
	public void test23() throws Exception{
		helper1(new String[]{"j", "i"}, new String[]{"I", "I"});
	}*/

	public void test24() throws Exception{
		helper1(new String[]{"j"}, new String[]{"I"});
	}

	public void test25() throws Exception{
		helper1(new String[]{"j"}, new String[]{"I"});
	}

	public void test26() throws Exception{
		//printTestDisabledMessage("revisit in the context of anonymous types in type hierarchies");
		helper1(new String[]{"j"}, new String[]{"I"});
	}

//removed - see testFail21
//	public void test27() throws Exception{
//		helper1(new String[]{"j"}, new String[]{"I"});
//	}

	public void test28() throws Exception{
		helper1(new String[]{"j"}, new String[]{"[I"});
	}

	public void test29() throws Exception{
		helper1(new String[]{"b"}, new String[]{"QA;"});
	}

	public void test30() throws Exception{
		helper1(new String[]{"i", "k"}, new String[]{"I", "I"});
	}

	public void test31() throws Exception{
		helper1(new String[]{"kk", "j"}, new String[]{"I", "I"});
	}

	/* disabled: by Eclipse
	public void test32() throws Exception{
		printTestDisabledMessage("must do - constructor params");
	}*/

	/* disabled: by Eclipse
	public void test33() throws Exception{
		printTestDisabledMessage("revisit - removed the 'no ref update' option");
//		helper1(new String[]{"b"}, new String[]{"QA;"}, false);
	}*/

	public void test34() throws Exception{
//		printTestDisabledMessage("regression test for bug#9001");
		helper1(new String[]{"test2"}, new String[]{"Z"});
	}

	public void test35() throws Exception{
		//printTestDisabledMessage("regression test for bug#6224");
		helper1(new String[]{"j"}, new String[]{"I"});
	}

	public void test36() throws Exception{
//		printTestDisabledMessage("regression test for bug#21163");
		helper1(new String[]{"j"}, new String[]{"I"});
	}

	// -----

	public void testFail0() throws Exception{
		//printTestDisabledMessage("must fix - name collision with an instance var");
		helper2(new String[]{"j"}, new String[]{"I"});
	}

	/* disabled: we can do this
	public void testFail1() throws Exception{
		helper2(new String[0], new String[0]);
	}*/

	public void testFail2() throws Exception{
		helper2(new String[]{"i", "i"}, new String[]{"I", "I"});
	}

	public void testFail3() throws Exception{
		helper2(new String[]{"i", "9"}, new String[]{"I", "I"});
	}

	public void testFail4() throws Exception{
		helper2(new String[]{"j"}, new String[]{"I"});
	}

	public void testFail5() throws Exception{
		helper2(new String[]{"j"}, new String[]{"I"});
	}

	public void testFail6() throws Exception{
		//printTestDisabledMessage("must fix - name collision with an instance var");
		helper2(new String[]{"j"}, new String[]{"I"});
	}

	/* disabled: we can do this
	public void testFail7() throws Exception{
		//printTestDisabledMessage("waiting for better conflict detection story from DB");
		helper2(new String[]{"j"}, new String[]{"I"});
	}*/

	/* disabled: we can do this
	public void testFail8() throws Exception{
		//printTestDisabledMessage("waiting for better conflict detection story from DB");
		helper2(new String[]{"j"}, new String[]{"I"});
	}*/

	/* disabled: we can do this
	public void testFail9() throws Exception{
		//printTestDisabledMessage("waiting for better conflict detection story from DB");
		helper2(new String[]{"j"}, new String[]{"I"});
	}*/

	public void testFail10() throws Exception{
		helper2(new String[]{"j", "j"}, new String[]{"I", "I"});
	}

	public void testFail11() throws Exception{
		helper2(new String[]{"j", "j"}, new String[]{"I", "I"});
	}

	/* disabled: by Eclipse
	public void testFail12() throws Exception{
		//printTestDisabledMessage("waiting for better conflict detection story from DB");
		helper2(new String[]{"j"}, new String[]{"I"});
	}*/

	/* disabled: we can do this
	public void testFail13() throws Exception{
		//printTestDisabledMessage("waiting for better conflict detection story from DB");
		helper2(new String[]{"j"}, new String[]{"I"});
	}*/

	/* disabled: we can do this
	public void testFail14() throws Exception{
		//printTestDisabledMessage("waiting for better conflict detection story from DB");
		helper2(new String[]{"j"}, new String[]{"QA;"});
	}*/

	public void testFail15() throws Exception{
		helper2(new String[]{"j"}, new String[]{"I"});
	}

	public void testFail16() throws Exception{
		helper2(new String[]{"j"}, new String[]{"I"});
	}

	public void testFail17() throws Exception{
		helper2(new String[]{"j"}, new String[]{"I"});
	}

	/* disabled: we can do this
	public void testFail18() throws Exception{
		helper2(new String[]{"j"}, new String[]{"I"});
	}*/

	/* disabled: we can do this
	public void testFail19() throws Exception{
		//printTestDisabledMessage("waiting for better conflict detection story from DB");
		helper2(new String[]{"j"}, new String[]{"I"});
	}*/

	/* disabled: we can do this
	public void testFail20() throws Exception{
		//printTestDisabledMessage("waiting for better conflict detection story from DB");
		helper2(new String[]{"j"}, new String[]{"I"});
	}*/

	/* disabled: by Eclipse
	public void testFail21() throws Exception{
		printTestDisabledMessage("Disabled since 1.4 compliance level doesn't produce error message");
		// helper2(new String[]{"j"}, new String[]{"I"});
	}*/

}
