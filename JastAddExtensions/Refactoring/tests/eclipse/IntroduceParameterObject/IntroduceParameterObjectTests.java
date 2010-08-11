/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Max Schaefer    - adapted to work with JRRT
 *******************************************************************************/
package tests.eclipse.IntroduceParameterObject;

import java.util.Arrays;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;

/* Things we don't support yet:
 * 	- reusing wrapped parameter objects for recursive calls
 * 	- using fields of parameter object directly (we always extract into a local first)
 *  - reordering/renaming parameters
 *  - creating delegates */

public class IntroduceParameterObjectTests extends TestCase {
	public IntroduceParameterObjectTests(String name) {
		super(name);
	}

	private void runRefactoring(boolean expectError, boolean toplevel) throws Exception {
		runRefactoring(null, expectError, toplevel);
	}
	
	private void runRefactoring(String[] parms, boolean expectError, boolean toplevel) throws Exception {
		runRefactoring(parms, "FooParameter", "parameterObject", expectError, toplevel);
	}

	private void runRefactoring(String className, String parmName, boolean expectError, boolean toplevel) throws Exception {
		runRefactoring(null, className, parmName, expectError, toplevel);
	}
	
	private void runRefactoring(String[] parms, String className, String parmName, boolean expectError, boolean toplevel) throws Exception {
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/IntroduceParameterObject/"+getName()+"/in");
		Program out = expectError ? null : CompileHelper.compileAllJavaFilesUnder("tests/eclipse/IntroduceParameterObject/"+getName()+"/out");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertTrue(expectError || out != null);
		try {
			MethodDecl foo = in.findMethod("foo");
			foo.doIntroduceParameterObject(parms == null ? null : Arrays.asList(parms), className, parmName, toplevel);
			assertEquals(expectError ? "<failure>" : out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			if(!expectError)
				assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	public void testBodyUpdate() throws Exception {
		runRefactoring(false, false);
	}

	public void testDefaultPackagePoint() throws Exception {
		runRefactoring("ArrayList", "parameterObject", false, false);
	}

	public void testDefaultPackagePointTopLevel() throws Exception {
		runRefactoring("ArrayList", "parameterObject", false, true);
	}

	/* disabled: delegation creation not supported yet
	public void testDelegateCreation() throws Exception {
		runRefactoring(false, false);
	}

	public void testDelegateCreationCodeStyle() throws Exception {
		runRefactoring(false, false);
	}*/

	public void testImportAddEnclosing() throws Exception {
		runRefactoring(false, false);
	}

	public void testImportAddTopLevel() throws Exception {
		runRefactoring("TestImportAddTopLevelParameter", "parameterObject", false, true);
	}

	public void testImportNameSimple() throws Exception {
		runRefactoring("ArrayList", "p", false, true);
	}

	public void testInlineRename() throws Exception {
		runRefactoring(new String[]{"xg", "yg"}, false, false);
	}
	
	public void testSubclassInCU() throws Exception {
		runRefactoring("FooParameter", "parameterObject", false, true);
	}

	public void testInterfaceMethod() throws Exception {
		runRefactoring(false, true);
	}

	/* disabled: reordering not supported yet
	public void testRecursiveReordered() throws Exception {
		runRefactoring(false, false);
	}*/

	public void testRecursiveSimple() throws Exception {
		runRefactoring(false, false);
	}

	/* disabled: reordering not supported yet
	public void testRecursiveSimpleReordered() throws Exception {
		runRefactoring(false, false);
	}*/

	/* disabled: delegation creation not supported yet
	public void testReorderGetter() throws Exception{
		runRefactoring(false, false);
	}*/

	public void testSimpleEnclosing() throws Exception{
		runRefactoring(false, false);
	}

	public void testSimpleEnclosingCodeStyle() throws Exception {
		runRefactoring(false, false);
	}

	public void testVarArgsNotReordered() throws Exception{
		runRefactoring(false, false);
	}

	/* disabled: reordering not supported yet
	public void testVarArgsReordered() throws Exception{
		runRefactoring(false, false);
	}*/
}
