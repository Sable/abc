/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package tests.eclipse.RenameTypeParameter;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.GenericMethodDecl;
import AST.GenericTypeDecl;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;
import AST.TypeVariable;

public class RenameTypeParameterTests extends TestCase {
	public RenameTypeParameterTests(String name) {
		super(name);
	}

	private void helper1(String parameterName, String newParameterName, String typeName, boolean references) throws Exception {
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameTypeParameter/"+getName()+"/in");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		
		TypeDecl td = in.findSimpleType("A");
		assertTrue(td instanceof GenericTypeDecl);
		GenericTypeDecl gtd = (GenericTypeDecl)td;
		
		TypeVariable tv = null;
		for(int i=0;i<gtd.getNumTypeParameter();++i)
			if(gtd.getTypeParameter(i).name().equals(parameterName))
				tv = gtd.getTypeParameter(i);
		assertNotNull(tv);
		
		try {
			tv.rename(newParameterName);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) { }
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper1(String parameterName, String newParameterName, String typeName, String methodName, String[] methodSignature, boolean references) throws Exception {
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameTypeParameter/"+getName()+"/in");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		
		TypeDecl td = in.findSimpleType("A");
		assertNotNull(td);
		MethodDecl md = td.findMethod(methodName);
		assertTrue(md instanceof GenericMethodDecl);
		GenericMethodDecl gmd = (GenericMethodDecl)md;
		
		TypeVariable tv = null;
		for(int i=0;i<gmd.getNumTypeParameter();++i)
			if(gmd.getTypeParameter(i).name().equals(parameterName))
				tv = gmd.getTypeParameter(i);
		assertNotNull(tv);
		
		try {
			tv.rename(newParameterName);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) { }
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper2(String parameterName, String newParameterName, String typeName, boolean references) throws Exception {
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameTypeParameter/"+getName()+"/in");
		Program out = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameTypeParameter/"+getName()+"/out");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		
		TypeDecl td = in.findSimpleType("A");
		assertTrue(td instanceof GenericTypeDecl);
		GenericTypeDecl gtd = (GenericTypeDecl)td;
		
		TypeVariable tv = null;
		for(int i=0;i<gtd.getNumTypeParameter();++i)
			if(gtd.getTypeParameter(i).name().equals(parameterName))
				tv = gtd.getTypeParameter(i);
		assertNotNull(tv);
		
		try {
			tv.rename(newParameterName);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper2(String parameterName, String newParameterName, String typeName, String methodName, String[] methodSignature, boolean references) throws Exception {
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameTypeParameter/"+getName()+"/in");
		Program out = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameTypeParameter/"+getName()+"/out");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		
		TypeDecl td = in.findSimpleType("A");
		assertNotNull(td);
		MethodDecl md = td.findMethod(methodName);
		assertTrue(md instanceof GenericMethodDecl);
		GenericMethodDecl gmd = (GenericMethodDecl)md;
		
		TypeVariable tv = null;
		for(int i=0;i<gmd.getNumTypeParameter();++i)
			if(gmd.getTypeParameter(i).name().equals(parameterName))
				tv = gmd.getTypeParameter(i);
		assertNotNull(tv);
		
		try {
			tv.rename(newParameterName);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	public void test0() throws Exception {
		helper2("T", "S", "A", true);
	}

	public void test1() throws Exception {
		helper2("T", "S", "A", true);
	}

	/* disabled: does not compile
	public void test2() throws Exception {
		helper2("T", "S", "A", false);
	}*/

	public void test3() throws Exception {
		helper2("T", "S", "A", true);
	}

	/* disabled: does not compile
	public void test4() throws Exception {
		helper2("T", "S", "A", false);
	}*/

	public void test5() throws Exception {
		helper2("T", "S", "A", true);
	}

	public void test6() throws Exception {
		helper2("S", "T", "A", true);
	}

	public void test7() throws Exception {
		helper2("T", "S", "A", false);
	}

	public void test8() throws Exception {
		helper2("S", "T", "A", false);
	}

	public void test9() throws Exception {
		helper2("T", "S", "A", "f", new String[] { "QT;"}, true);
	}

	public void test10() throws Exception {
		helper2("T", "S", "B", "f", new String[] { "QT;"}, true);
	}

	/* disabled: does not compile
	public void test11() throws Exception {
		helper2("T", "S", "A", "f", new String[] { "QT;"}, false);
	}*/

	/* disabled: does not compile
	public void test12() throws Exception {
		helper2("T", "S", "B", "f", new String[] { "QT;"}, false);
	}*/

	public void test13() throws Exception {
		helper2("T", "S", "A", true);
	}

	public void test14() throws Exception {
		helper2("ELEMENT", "E", "A", true);
	}

	public void test15() throws Exception {
		helper2("T", "S", "A", true);
	}
	
// ------------------------------------------------

	/* disabled: we can do this
	public void testFail0() throws Exception {
		helper1("T", "S", "A", true);
	}*/

	/* disabled: we can do this
	public void testFail1() throws Exception {
		helper1("T", "S", "A", true);
	}*/

	/* disabled: we can do this
	public void testFail2() throws Exception {
		helper1("T", "S", "A", true);
	}*/

	public void testFail3() throws Exception {
		helper1("T", "S", "A", true);
	}

	/* disabled: we can do this
	public void testFail4() throws Exception {
		helper1("T", "S", "A", true);
	}*/

	/* disabled: we can do this
	public void testFail5() throws Exception {
		helper1("T", "S", "B", "f", new String[] { "QT;"}, true);
	}*/
}
