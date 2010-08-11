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
package tests.eclipse.RenamePrivateMethod;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class RenamePrivateMethodTests extends TestCase {
	public RenamePrivateMethodTests(String name) {
		super(name);
	}

	private void helper1_0(String methodName, String newMethodName, String[] signatures) throws Exception{
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenamePrivateMethod/"+getName()+"/in");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		TypeDecl td = in.findSimpleType("A");
		assertNotNull(td);
		MethodDecl md = td.findMethod(methodName);
		assertNotNull(md);
		try {
			md.rename(newMethodName);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) { }
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper1() throws Exception{
		helper1_0("m", "k", new String[0]);
	}

	private void helper2_0(String methodName, String newMethodName, String[] signatures, boolean updateReferences, boolean createDelegate, String typeName) throws Exception{
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenamePrivateMethod/"+getName()+"/in");
		Program out = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenamePrivateMethod/"+getName()+"/out");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		TypeDecl td = in.findSimpleType(typeName);
		assertNotNull(td);
		MethodDecl md = td.findMethod(methodName);
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

	private void helper2_0(String methodName, String newMethodName, String[] signatures, String typeName) throws Exception{
		helper2_0(methodName, newMethodName, signatures, true, false, typeName);
	}

	private void helper2(boolean updateReferences) throws Exception{
		helper2_0("m", "k", new String[0], updateReferences, false, "A");
	}

	private void helper2() throws Exception{
		helper2(true);
	}

	private void helperDelegate() throws Exception{
		helper2_0("m", "k", new String[0], true, true, "A");
	}

	public void testFail0() throws Exception{
		helper1();
	}

	public void testFail1() throws Exception{
		helper1();
	}

	/* disabled: we can do this
	public void testFail2() throws Exception{
		helper1();
	}*/

	/* disabled: we can do this
	public void testFail5() throws Exception{
		helper1();
	}*/

	public void test0() throws Exception{
		helper2();
	}

	public void test10() throws Exception{
		helper2();
	}

	public void test11() throws Exception{
		helper2();
	}

	public void test12() throws Exception{
		helper2();
	}

	public void test13() throws Exception{
		helper2();
	}

	public void test14() throws Exception{
		helper2();
	}

	public void test15() throws Exception{
		helper2_0("m", "k", new String[]{"I"}, "A");
	}

	public void test16() throws Exception{
		helper2_0("m", "fred", new String[]{"I"}, "A");
	}

	public void test17() throws Exception{
		helper2_0("m", "kk", new String[]{"I"}, "A");
	}

	public void test18() throws Exception{
		helper2_0("m", "kk", null, "B");
	}

	public void test2() throws Exception{
		helper2_0("m", "fred", new String[0], "A");
	}

	public void test20() throws Exception{
		helper2_0("m", "fred", new String[]{"I"}, "A");
	}

	public void test23() throws Exception{
		helper2_0("m", "k", new String[0], "A");
	}

	public void test24() throws Exception{
		helper2_0("m", "k", new String[]{"QString;"}, "A");
	}

	public void test25() throws Exception{
		helper2_0("m", "k", new String[]{"[QString;"}, "A");
	}

	public void test26() throws Exception{
		helper2_0("m", "k", new String[0], "A");
	}

	/* disabled: does not compile
	public void test27() throws Exception{
		helper2_0("m", "k", new String[0], false, false);
	}*/

	public void testAnon0() throws Exception{
		helper2();
	}

	/* disabled: unsupported feature
	public void testDelegate01() throws Exception  {
		// simple static delegate
		helperDelegate();
	}*/
}
