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
package tests.eclipse.RenameStaticMethod;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class RenameStaticMethodTests extends TestCase {
	public RenameStaticMethodTests(String name) {
		super(name);
	}

	private void helper1_0(String methodName, String newMethodName, String[] signatures) throws Exception{
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameStaticMethod/"+getName()+"/in");
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
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameStaticMethod/"+getName()+"/in");
		Program out = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameStaticMethod/"+getName()+"/out");
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

	public void testFail0() throws Exception {
		helper1();
	}

	public void testFail1() throws Exception{
		helper1();
	}

	public void testFail2() throws Exception{
		helper1();
	}

	//testFail3 deleted

	public void testFail4() throws Exception{
		helper1();
	}

	public void testFail5() throws Exception{
		helper1();
	}

	public void testFail6() throws Exception{
		helper1();
	}

	public void testFail7() throws Exception{
		helper1();
	}

	public void testFail8() throws Exception{
		helper1();
	}

	public void test0() throws Exception{
		helper2();
	}

	public void test1() throws Exception{
		helper2();
	}

	public void test2() throws Exception{
		helper2();
	}

	public void test3() throws Exception{
		helper2();
	}

	public void test4() throws Exception{
		helper2();
	}

	public void test5() throws Exception{
		helper2();
	}

	public void test6() throws Exception{
		helper2();
	}

	public void test7() throws Exception{
		helper2_0("m", "k", null, "A");
	}

	public void test8() throws Exception{
		helper2_0("m", "k", null, "A");
	}

	/* disabled: tests idiosyncratic feature
	public void test9() throws Exception{
		helper2_0("m", "k", new String[]{Signature.SIG_INT}, false, false);
	}*/

	public void test10() throws Exception{
		helper2_0("method", "newmethod", null, "B");
	}

	public void test11() throws Exception{
		helper2_0("method2", "fred", null, "A");
	}

	public void testUnicode01() throws Exception{
		helper2_0("e", "f", new String[]{}, "A");
	}

	/* disabled: we can do this
	public void testStaticImportFail0() throws Exception {
		helper1();
	}*/

	public void testStaticImport1() throws Exception {
		helper2();
	}

	public void testStaticImport2() throws Exception {
		helper2();
	}

	/* disabled: JastAddJ bug
	public void testStaticImport3() throws Exception {
		helper2();
	}*/

	public void testStaticImport4() throws Exception {
		helper2();
	}

	/* disabled: JastAddJ bug
	public void testStaticImport5() throws Exception {
		helper2();
	}*/

	/* disabled: unsupported feature
	public void testDelegate01() throws Exception  {
		// simple static delegate
		helperDelegate();
	}*/

}
