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
package tests.eclipse.RenameVirtualMethodInClass;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class RenameVirtualMethodInClassTests extends TestCase {
	public RenameVirtualMethodInClassTests(String name) {
		super(name);
	}

	private void helper1_0(String methodName, String newMethodName, String[] signatures) throws Exception{
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameVirtualMethodInClass/"+getName()+"/in");
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

	private void helper2_0(String methodName, String newMethodName, String[] signatures, boolean shouldPass, boolean updateReferences, boolean createDelegate, String typeName) throws Exception{
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameVirtualMethodInClass/"+getName()+"/in");
		Program out = shouldPass ? CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameVirtualMethodInClass/"+getName()+"/out") : null;
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertTrue(!shouldPass || out!=null);
		TypeDecl td = in.findSimpleType(typeName);
		assertNotNull(td);
		MethodDecl md = td.findMethod(methodName);
		assertNotNull(md);
		try {
			md.rename(newMethodName);
			assertEquals(shouldPass ? out.toString() : "<failure>", in.toString());
		} catch(RefactoringException rfe) {
			if(shouldPass)
				assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper2_0(String methodName, String newMethodName, String[] signatures, boolean shouldPass, String typeName) throws Exception{
		helper2_0(methodName, newMethodName, signatures, shouldPass, true, false, typeName);
	}

	private void helper2_0(String methodName, String newMethodName, String[] signatures, String typeName) throws Exception{
		helper2_0(methodName, newMethodName, signatures, true, typeName);
	}

	private void helper2(boolean updateReferences) throws Exception{
		helper2_0("m", "k", new String[0], true, updateReferences, false, "A");
	}

	private void helper2() throws Exception{
		helper2(true);
	}

	private void helperDelegate() throws Exception{
		helper2_0("m", "k", new String[0], true, true, true, "A");
	}

	private void helper2_fail() throws Exception{
		helper2_0("m", "k", new String[0], false, "A");
	}

// ----------------------------------------------------------------

	public void testEnum1() throws Exception {
		helper2_0("getNameLength", "getNameSize", new String[0], "A");
	}

	public void testEnum2() throws Exception {
		helper2_0("getSquare", "get2ndPower", new String[0], "A");
	}

	public void testEnum3() throws Exception {
		helper2_0("getSquare", "get2ndPower", new String[0], "A");
	}

	public void testEnumFail1() throws Exception {
		helper1_0("value", "valueOf", new String[]{"QString;"});
	}

	public void testGenerics1() throws Exception {
		helper2_0("m", "k", new String[]{"QG;"}, "A");
	}

	public void testGenerics2() throws Exception {
		helper2_0("add", "addIfPositive", new String[]{"QE;"}, "A");
	}

	public void testGenerics3() throws Exception {
		helper2_0("add", "addIfPositive", new String[]{"QT;"}, "A");
	}

	public void testGenerics4() throws Exception {
		helper2_0("takeANumber", "doit", new String[]{"QNumber;"}, "A");
	}

	public void testGenerics5() throws Exception {
		helper2_0("covariant", "variant", new String[0], "A");
	}

	public void testVarargs1() throws Exception {
		helper2_0("runall", "runThese", new String[]{"[QRunnable;"}, "A");
	}

	public void testVarargs2() throws Exception {
		helper2_0("m", "k", new String[]{"[QString;"}, "A");
	}

	/* disabled: does not compile
	public void testFail0() throws Exception{
		helper1();
	}*/

	/* disabled: tests idiosyncratic feature
	public void testFail1() throws Exception{
		helper1_not_available("toString", new String[0]);
	}*/

	/* disabled: we can do this
	public void testFail2() throws Exception{
		helper1();
	}*/

	/* disabled: does not compile
	public void testFail3() throws Exception{
		helper1();
	}*/

	/* disabled: we can do this
	public void testFail4() throws Exception{
		helper1();
	}*/

	/* disabled: we can do this
	public void testFail5() throws Exception{
		helper1();
	}*/

	/* disabled: does not compile
	public void testFail6() throws Exception{
		helper1();
	}*/

	public void testFail7() throws Exception{
		helper1();
	}

	/* disabled: we can handle this
	public void testFail8() throws Exception{
		helper1();
	}*/

	/* disabled: does not compile
	public void testFail9() throws Exception{
		helper1_0("m", "k", null);
	}*/

	/* disabled: we can handle this
	public void testFail10() throws Exception{
		helper1();
	}*/

	public void testFail11() throws Exception{
		helper1();
	}

	/* disabled: we can handle this
	public void testFail12() throws Exception{
		helper1();
	}*/

	/* disabled: does not compile
	public void testFail13() throws Exception{
		helper1();
	}*/

	/* disabled: we can handle this
	public void testFail14() throws Exception{
		helper1_0("m", "k", null);
	}*/

	/* disabled: we can handle this
	public void testFail15() throws Exception{
		helper1();
	}*/

	public void testFail17() throws Exception{
		helper1();
	}

	public void testFail18() throws Exception{
		helper1();
	}

	/* disabled: we can handle this
	public void testFail19() throws Exception{
		helper1();
	}*/

	/* disabled: we can handle this
	public void testFail20() throws Exception{
		helper1();
	}*/

	public void testFail21() throws Exception{
		helper1();
	}

	public void testFail22() throws Exception{
		helper1();
	}

	/* disabled: we can handle this
	public void testFail23() throws Exception{
		helper1();
	}*/

	public void testFail24() throws Exception{
		helper1();
	}

	/* disabled: we can handle this
	public void testFail25() throws Exception{
		helper1();
	}*/

	public void testFail26() throws Exception{
		helper1();
	}

	public void testFail27() throws Exception{
		helper1();
	}

	public void testFail28() throws Exception{
		helper1();
	}

	/* disabled: we can do this
	public void testFail29() throws Exception{
		helper1();
	}*/

	public void testFail30() throws Exception{
		helper1();
	}

	/* disabled: we can do this
	public void testFail31() throws Exception{
		helper1_0("m", "k", new String[]{"QString;"});
	}*/

	/* disabled: we can do this
	public void testFail32() throws Exception{
		helper1_0("m", "k", new String[]{"QObject;"});
	}*/

	/* disabled: tests idiosyncratic feature
	public void testFail33() throws Exception{
		helper1_not_available("toString", new String[0]);
	}*/

	/* disabled: we can handle this
	public void testFail34() throws Exception{
		helper1_0("m", "k", new String[]{"QString;"});
	}*/

//	//test removed - was invalid
//	public void testFail35() throws Exception{
//	}

	public void testFail36() throws Exception{
		helper1();
	}

	public void testFail37() throws Exception{
		helper1();
	}

	/* disabled: we can do this
	public void testFail38() throws Exception{
		//printTestDisabledMessage("must fix - nested type");
		helper1();
	}*/

	/* disabled: we can do this
	public void testFail39() throws Exception{
		helper1();
	}*/

	/* disabled: we can do this
	public void testFail40() throws Exception{
		//Autoboxing -> calls to methods can be redirected due to overloading
		helper1_0("m", "k", null);
	}*/

	/* disabled: we can do this
	public void testFail41() throws Exception{
		helper1();
	}*/

	public void test1() throws Exception{
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
		helper2_0("m", "k", null, "A");
	}

	public void test16() throws Exception{
		helper2_0("m", "fred", null, "A");
	}

	public void test17() throws Exception{
		//printTestDisabledMessage("overloading");
		helper2_0("m", "kk", null, "A");
	}

	public void test18() throws Exception{
		helper2_0("m", "kk", null, "B");
	}

	public void test19() throws Exception{
		helper2_0("m", "fred", new String[0], "A");
	}

	public void test2() throws Exception{
		helper2_0("m", "fred", new String[0], "A");
	}

	public void test20() throws Exception{
		helper2_0("m", "fred", null, "A");
	}

	public void test21() throws Exception{
		helper2_0("m", "fred", null, "A");
	}

	public void test22() throws Exception{
		helper2();
	}

	/* disabled: wrong result
	public void test23() throws Exception{
		helper2();
	}*/

	public void test24() throws Exception{
		helper2_0("m", "k", new String[]{"QString;"}, "A");
	}

	public void test25() throws Exception{
		//printTestDisabledMessage("waiting for 1GIIBC3: ITPJCORE:WINNT - search for method references - missing matches");
		helper2();
	}

	public void test26() throws Exception{
		helper2();
	}

	public void test27() throws Exception{
		helper2();
	}

	public void test28() throws Exception{
		helper2();
	}

	public void test29() throws Exception{
		helper2();
	}

	public void test30() throws Exception{
		helper2();
	}

	public void test31() throws Exception{
		helper2();
	}

	/* disabled: does not compile
	public void test32() throws Exception{
		helper2(false);
	}*/

	public void test33() throws Exception{
		helper2();
	}

	/* disabled: by Eclipse
	public void test34() throws Exception{
		//printTestDisabledMessage("test for bug#18553");
		helper2_0("A", "foo", new String[0], true, true);
	}*/

	public void test35() throws Exception{
		helper2_0("foo", "bar", new String[] {"QObject;"}, true, "A");
	}

	public void test36() throws Exception{
		helper2_0("foo", "bar", new String[] {"QString;"}, true, "A");
	}

	public void test37() throws Exception{
		helper2_0("foo", "bar", new String[] {"QA;"}, true, "A");
	}

	/* disabled: by Eclipse
	public void test38() throws Exception {
		printTestDisabledMessage("difficult to set up test in current testing framework");
//		helper2();
	}*/

	public void test39() throws Exception {
		helper2();
	}

	public void test40() throws Exception { // test for bug 68592
		helper2_0("method", "method2", null, "LocalClass");
	}

	//anonymous inner class
	public void testAnon0() throws Exception{
		helper2();
	}

	public void testLocal0() throws Exception{
		helper2();
	}

	/* disabled: unimplemented feature
	public void testDelegate01() throws Exception {
		// simple delegate
		helperDelegate();
	}

	public void testDelegate02() throws Exception {
		// overridden delegates with abstract mix-in
		helperDelegate();
	}

	public void testDelegate03() throws Exception {
		// overridden delegates in local type
		helperDelegate();
	}*/
}
