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
package tests.eclipse.RenameMethodInInterface;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class RenameMethodInInterfaceTests extends TestCase {
	private static final String[] NO_ARGUMENTS= new String[0];

	public RenameMethodInInterfaceTests(String name) {
		super(name);
	}

	private void helper1_0(String methodName, String newMethodName, String[] signatures) {
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameMethodInInterface/"+getName()+"/in");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		
		TypeDecl I = in.findSimpleType("I");
		assertNotNull(I);
		MethodDecl m = I.findMethod(methodName);
		assertNotNull(m);
		
		try {
			m.rename(newMethodName);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) {
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper1() throws Exception{
		helper1_0("m", "k", NO_ARGUMENTS);
	}

	private void helper2_0(String methodName, String newMethodName, String[] signatures, boolean shouldPass, boolean updateReferences, boolean createDelegate) throws Exception{
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameMethodInInterface/"+getName()+"/in");
		Program out = shouldPass ? CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameMethodInInterface/"+getName()+"/out") : null;
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertTrue(!shouldPass || out!=null);
		
		TypeDecl I = in.findSimpleType("I");
		assertNotNull(I);
		MethodDecl m = I.findMethod(methodName);
		assertNotNull(m);
		
		try {
			m.rename(newMethodName);
			assertEquals(shouldPass ? out.toString() : "<failure>", in.toString());
		} catch(RefactoringException rfe) {
			if(shouldPass)
				assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper2(boolean updateReferences) throws Exception{
		helper2_0("m", "k", NO_ARGUMENTS, true, updateReferences, false);
	}

	private void helper2() throws Exception{
		helper2(true);
	}

	private void helperDelegate() throws Exception{
		helper2_0("m", "k", NO_ARGUMENTS, true, true, true);
	}

// --------------------------------------------------------------------------

	public void testAnnotation0() throws Exception{
		helper2_0("name", "ident", NO_ARGUMENTS, true, true, false);
	}

	public void testAnnotation1() throws Exception{
		helper2_0("value", "number", NO_ARGUMENTS, true, true, false);
	}

	public void testAnnotation2() throws Exception{
		helper2_0("thing", "value", NO_ARGUMENTS, true, true, false);
	}

	public void testAnnotation3() throws Exception{
		helper2_0("value", "num", NO_ARGUMENTS, true, true, false);
	}

	public void testAnnotation4() throws Exception{
		// see also bug 83064
		helper2_0("value", "num", NO_ARGUMENTS, true, true, false);
	}

	public void testGenerics01() throws Exception {
		helper2_0("getXYZ", "zYXteg", new String[] {"QList<QSet<QRunnable;>;>;"}, true, true, false);
	}

	public void testFail0() throws Exception{
		helper1();
	}
	public void testFail1() throws Exception{
		helper1();
	}
	/* disabled: does not compile
	public void testFail3() throws Exception{
		helper1();
	}*/
	public void testFail4() throws Exception{
		helper1();
	}
	public void testFail5() throws Exception{
		helper1();
	}
	public void testFail6() throws Exception{
		helper1();
	}
	/* disabled: does not compile
	public void testFail7() throws Exception{
		helper1();
	}*/
	public void testFail8() throws Exception{
		helper1_0("m", "k", NO_ARGUMENTS);
	}
	public void testFail9() throws Exception{
		helper1();
	}
	public void testFail10() throws Exception{
		helper1();
	}
	/* disabled: does not compile
	public void testFail11() throws Exception{
		helper1();
	}*/
	public void testFail12() throws Exception{
		helper1();
	}
	public void testFail13() throws Exception{
		helper1();
	}
	public void testFail14() throws Exception{
		helper1();
	}
	public void testFail15() throws Exception{
		helper1();
	}
	public void testFail16() throws Exception{
		helper1();
	}
	public void testFail17() throws Exception{
		helper1();
	}
	/* disabled: does not compile
	public void testFail18() throws Exception{
		helper1();
	}*/
	public void testFail19() throws Exception{
		helper1();
	}
	public void testFail20() throws Exception{
		helper1();
	}
	/* disabled: we can do this
	public void testFail21() throws Exception{
		helper1_0("m", "k", new String[]{"QString;"});
	}*/
	/* disabled: we can do this
	public void testFail22() throws Exception{
		helper1_0("m", "k", new String[]{"QObject;"});
	}*/
	/* disabled: tests idionyncratic feature
	public void testFail23() throws Exception{
		helper1_not_available("toString", NO_ARGUMENTS);
	}*/
	public void testFail24() throws Exception{
		helper1();
	}
	public void testFail25() throws Exception{
		helper1();
	}
	public void testFail26() throws Exception{
		helper1();
	}
	public void testFail27() throws Exception{
		helper1();
	}
	public void testFail28() throws Exception{
		helper1();
	}
	public void testFail29() throws Exception{
		helper1();
	}

	/* disabled: tests idiosyncratic feature
	public void testFail30() throws Exception{
		helper1_not_available("toString", NO_ARGUMENTS);
	}*/

	/* disabled: tests idiosyncratic feature
	public void testFail31() throws Exception{
		helper1_not_available("toString", NO_ARGUMENTS);
	}*/

	public void testFail32() throws Exception{
		helper1_0("m", "toString", NO_ARGUMENTS);
	}

	public void testFail33() throws Exception{
		helper1_0("m", "toString", NO_ARGUMENTS);
	}

	public void testFail34() throws Exception{
		helper1_0("m", "equals", new String[]{"QObject;"});
	}

	public void testFail35() throws Exception{
		helper1_0("m", "equals", new String[]{"Qjava.lang.Object;"});
	}

	public void testFail36() throws Exception{
		helper1_0("m", "getClass", NO_ARGUMENTS);
	}

	public void testFail37() throws Exception{
		helper1_0("m", "hashCode", NO_ARGUMENTS);
	}

	public void testFail38() throws Exception{
		helper1_0("m", "notify", NO_ARGUMENTS);
	}

	public void testFail39() throws Exception{
		helper1_0("m", "notifyAll", NO_ARGUMENTS);
	}

	public void testFail40() throws Exception{
		helper1_0("m", "wait", NO_ARGUMENTS);
	}

	public void testFail41() throws Exception{
		helper1_0("m", "wait", NO_ARGUMENTS);
	}

	public void testFail42() throws Exception{
		helper1_0("m", "wait", NO_ARGUMENTS);
	}

	public void testFail43() throws Exception{
		helper1_0("m", "wait", NO_ARGUMENTS);
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
		helper2();
	}
	public void test10() throws Exception{
		helper2();
	}
	/* disabled: differing interpretation
	public void test11() throws Exception{
		helper2();
	}*/
	/* disabled: differing interpretation
	public void test12() throws Exception{
		helper2();
	}*/

	//test13 became testFail45
	//public void test13() throws Exception{
	//	helper2();
	//}
	public void test14() throws Exception{
		helper2();
	}
	/* disabled: differing interpretation
	public void test15() throws Exception{
		helper2();
	}*/
	public void test16() throws Exception{
		helper2();
	}
	public void test17() throws Exception{
		helper2();
	}
	/* disabled: differing interpretation
	public void test18() throws Exception{
		helper2();
	}*/
	/* disabled: differing interpretation
	public void test19() throws Exception{
		helper2();
	}*/
	/* disabled: differing interpretation
	public void test20() throws Exception{
		helper2();
	}*/
	
	/* disabled: by Eclipse
	//anonymous inner class
	public void test21() throws Exception{
		printTestDisabledMessage("must fix - incorrect warnings");
		//helper2_fail();
	}*/
	public void test22() throws Exception{
		helper2();
	}

	//test23 became testFail45
	//public void test23() throws Exception{
	//	helper2();
	//}

	/* disabled: differing interpretation
	public void test24() throws Exception{
		helper2();
	}*/
	public void test25() throws Exception{
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
	/* disabled: differing interpretation
	public void test29() throws Exception{
		helper2();
	}*/
	/* disabled: differing interpretation
	public void test30() throws Exception{
		helper2();
	}*/
	//anonymous inner class
	public void test31() throws Exception{
		helper2();
	}
	//anonymous inner class
	public void test32() throws Exception{
		helper2();
	}
	/* disabled: differing interpretation
	public void test33() throws Exception{
		helper2();
	}*/
	public void test34() throws Exception{
		helper2();
	}
	public void test35() throws Exception{
		helper2();
	}
	public void test36() throws Exception{
		helper2();
	}
	/* disabled: differing interpretation
	public void test37() throws Exception{
		helper2();
	}*/
	public void test38() throws Exception{
		helper2();
	}
	/* disabled: differing interpretation
	public void test39() throws Exception{
		helper2();
	}*/
	public void test40() throws Exception{
		helper2();
	}
	public void test41() throws Exception{
		helper2();
	}
	public void test42() throws Exception{
		helper2();
	}
	public void test43() throws Exception{
		helper2();
	}
	/* disabled: differing interpretation
	public void test44() throws Exception{
		helper2();
	}*/
	public void test45() throws Exception{
		helper2();
	}
	/* disabled: does not compile
	public void test46() throws Exception{
		helper2(false);
	}*/
	public void test47() throws Exception{
		helper2();
	}

	/* disabled: tests idiosyncratic feature
	public void testDelegate01() throws Exception {
		// simple delegate
		helperDelegate();
	}
	public void testDelegate02() throws Exception {
		// "overridden" delegate
		helperDelegate();
	}*/
}
