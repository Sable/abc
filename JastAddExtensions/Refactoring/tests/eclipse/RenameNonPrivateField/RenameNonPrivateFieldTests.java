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
package tests.eclipse.RenameNonPrivateField;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.FieldDeclaration;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class RenameNonPrivateFieldTests extends TestCase {
	public RenameNonPrivateFieldTests(String name) {
		super(name);
	}

	private void helper1_0(String fieldName, String newFieldName) throws Exception{
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameNonPrivateField/"+getName()+"/in");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		
		TypeDecl A = in.findSimpleType("A");
		assertNotNull(A);
		FieldDeclaration f = A.findField(fieldName);
		assertNotNull(f);
		
		try {
			f.rename(newFieldName);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) { }
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper1() throws Exception{
		helper1_0("f", "g");
	}

	private void helper2(String fieldName, String newFieldName) throws Exception{
		helper2(fieldName, newFieldName, false);
	}

	private void helper2(String fieldName, String newFieldName, boolean createDelegates) throws Exception{
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameNonPrivateField/"+getName()+"/in");
		Program out = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameNonPrivateField/"+getName()+"/out");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		
		TypeDecl A = in.findSimpleType("A");
		assertNotNull(A);
		FieldDeclaration f = A.findField(fieldName);
		assertNotNull(f);
		
		try {
			f.rename(newFieldName);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper2() throws Exception{
		helper2("f", "g");
	}

	//--------- tests ----------
	public void testFail0() throws Exception{
		helper1();
	}

	public void testFail1() throws Exception{
		helper1();
	}

	public void testFail2() throws Exception{
		helper1();
	}

	public void testFail3() throws Exception{
		helper1();
	}

	public void testFail4() throws Exception{
		helper1();
	}

	/* disabled: we can handle these
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

	public void testFail9() throws Exception{
		helper1();
	}

	public void testFail10() throws Exception{
		helper1();
	}

	public void testFail11() throws Exception{
		helper1();
	}

	public void testFail12() throws Exception{
		helper1();
	}

	public void testFail13() throws Exception{
		//printTestDisabledMessage("1GKZ8J6: ITPJCORE:WIN2000 - search: missing field occurrecnces");
		helper1();
	}

	public void testFail14() throws Exception{
		//printTestDisabledMessage("1GKZ8J6: ITPJCORE:WIN2000 - search: missing field occurrecnces");
		helper1();
	}*/

	// ------
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
		//printTestDisabledMessage("1GKZ8J6: ITPJCORE:WIN2000 - search: missing field occurrecnces");
	}

	public void test5() throws Exception{
		helper2();
	}

	public void test6() throws Exception{
		//printTestDisabledMessage("1GKB9YH: ITPJCORE:WIN2000 - search for field refs - incorrect results");
		helper2();
	}

	public void test7() throws Exception{
		helper2();
	}

	public void test8() throws Exception{
		//printTestDisabledMessage("1GD79XM: ITPJCORE:WINNT - Search - search for field references - not all found");
		helper2();
	}

	public void test9() throws Exception{
		helper2();
	}

	public void test10() throws Exception{
		helper2();
	}

	public void test11() throws Exception{
		helper2();
	}

	public void test12() throws Exception{
		//System.out.println("\nRenameNonPrivateField::" + name() + " disabled (1GIHUQP: ITPJCORE:WINNT - search for static field should be more accurate)");
		helper2();
	}

	public void test13() throws Exception{
		//System.out.println("\nRenameNonPrivateField::" + name() + " disabled (1GIHUQP: ITPJCORE:WINNT - search for static field should be more accurate)");
		helper2();
	}

	/* disabled: tests idiosyncratic features
	public void test14() throws Exception{
		fUpdateReferences= false;
		fUpdateTextualMatches= false;
		helper2();
	}

	public void test15() throws Exception{
		fUpdateReferences= false;
		fUpdateTextualMatches= false;
		helper2();
	}

	public void test16() throws Exception{
//		printTestDisabledMessage("text for bug 20693");
		helper2();
	}

	public void test17() throws Exception{
//		printTestDisabledMessage("test for bug 66250, 79131 (corner case: reference "A.f" to p.A#f)");
		fUpdateReferences= false;
		fUpdateTextualMatches= true;
		helper2("f", "g");
	}

	public void test18() throws Exception{
//		printTestDisabledMessage("test for 79131 (corner case: reference "A.f" to p.A#f)");
		fUpdateReferences= false;
		fUpdateTextualMatches= true;
		helper2("field", "member");
	}

//--- test 1.5 features: ---
	public void test19() throws Exception{
		fRenameGetter= true;
		fRenameSetter= true;
		helper2("list", "items");
	}*/

	public void test20() throws Exception{
		helper2("list", "items");
	}

	public void test21() throws Exception{
		helper2("fValue", "fOrdinal");
	}

	/* disabled: tests idiosyncratic features
	public void test22() throws Exception{
		fRenameGetter= true;
		fRenameSetter= true;
		helper2("tee", "thing");
	}

	public void test23() throws Exception{
		fRenameGetter= true;
		fRenameSetter= true;
		helper2();
	}*/

//--- end test 1.5 features. ---

	public void testBug5821() throws Exception{
		helper2("test", "test1");
	}

	/*
	public void testStaticImport() throws Exception{
		//bug 77622
		IPackageFragment test1= getRoot().createPackageFragment("test1", true, null);
		ICompilationUnit cuC= null;
		try {
			ICompilationUnit cuB= createCUfromTestFile(test1, "B");
			cuC= createCUfromTestFile(getRoot().getPackageFragment(""), "C");

			helper2("PI", "e");

			assertEqualLines("invalid renaming", getFileContents(getOutputTestFileName("B")), cuB.getSource());
			assertEqualLines("invalid renaming", getFileContents(getOutputTestFileName("C")), cuC.getSource());
		} finally {
			if (test1.exists())
				test1.delete(true, null);
			if (cuC != null && cuC.exists())
				cuC.delete(true, null);
		}
	}

	public void testEnumConst() throws Exception {
		//bug 77619
		IPackageFragment test1= getRoot().createPackageFragment("test1", true, null);
		ICompilationUnit cuC= null;
		try {
			ICompilationUnit cuB= createCUfromTestFile(test1, "B");
			cuC= createCUfromTestFile(getRoot().getPackageFragment(""), "C");

			helper2("RED", "REDDISH");

			assertEqualLines("invalid renaming", getFileContents(getOutputTestFileName("B")), cuB.getSource());
			assertEqualLines("invalid renaming", getFileContents(getOutputTestFileName("C")), cuC.getSource());
		} finally {
			if (test1.exists())
				test1.delete(true, null);
			if (cuC != null && cuC.exists())
				cuC.delete(true, null);
		}

	}*/

	public void testGenerics1() throws Exception {
		helper2();
	}

	/* disabled: tests idiosyncratic features
	public void testGenerics2() throws Exception {
		fRenameSetter= true;
		fRenameGetter= true;
		helper2();
	}

	public void testGenerics3() throws Exception {
		if (BUG_79990_CORE_SEARCH_METHOD_DECL) {
			printTestDisabledMessage("BUG_79990_CORE_SEARCH_METHOD_DECL");
			return;
		}
		fRenameSetter= true;
		fRenameGetter= true;
		helper2();
	}

	public void testGenerics4() throws Exception {
		fRenameSetter= true;
		fRenameGetter= true;
		helper2("count", "number");
	}

	public void testEnumField() throws Exception {
		fRenameSetter= true;
		fRenameGetter= true;
		helper2("buddy", "other");
	}*/

	public void testAnnotation1() throws Exception {
		helper2("ZERO", "ZORRO");
	}

	public void testAnnotation2() throws Exception {
		helper2("ZERO", "ZORRO");
	}

	/* disabled: tests unimplemented features
	public void testDelegate01() throws Exception {
		// a simple delegate
		helper2("f", "g", true);
	}

	public void testDelegate02() throws Exception {
		// nonstatic field, getter and setter
		fRenameSetter= true;
		fRenameGetter= true;
		helper2("f", "g", true);
	}

	public void testDelegate03() throws Exception {
		// create delegates for the field and a getter
		fRenameGetter= true;
		helper2("f", "g", true);
	}

	public void testRenameNLSAccessor01() throws Exception {
		IFile file= createPropertiesFromTestFile("messages");

		helper2("f", "g");

		assertEqualLines(getExpectedFileConent("messages"), getContents(file));
	}

	private String getExpectedFileConent(String propertyName) throws IOException {
		String fileName= getOutputTestFileName(propertyName);
		fileName= fileName.substring(0, fileName.length() - ".java".length()) + ".properties";
		return getContents(getFileInputStream(fileName));
	}

	private IFile createPropertiesFromTestFile(String propertyName) throws IOException, CoreException {
		IFolder pack= (IFolder) getPackageP().getResource();
		IFile file= pack.getFile(propertyName + ".properties");

		String fileName= getInputTestFileName(propertyName);
		fileName= fileName.substring(0, fileName.length() - ".java".length()) + ".properties";
		InputStream inputStream= getFileInputStream(fileName);
		file.create(inputStream, true, null);

		return file;
	}*/
}
