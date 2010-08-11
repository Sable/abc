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
package tests.eclipse.RenameType;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class RenameTypeTests extends TestCase {
	public RenameTypeTests(String name) {
		super(name);
	}

	private void helper1_0(String className, String newName) throws Exception {
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameType/"+getName()+"/in");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		
		TypeDecl td = in.findSimpleType(className);
		assertNotNull(td);
		try {
			td.rename(newName);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) { }
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper1() throws Exception{
		helper1_0("A", "B");
	}

	private void helper2_0(String oldName, String newName, String newCUName, boolean updateReferences) throws Exception{
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameType/"+getName()+"/in");
		Program out = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenameType/"+getName()+"/out");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		
		TypeDecl td = oldName.contains(".") ? in.findType(oldName) : in.findSimpleType(oldName);
		assertNotNull(td);
		try {
			td.rename(newName);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper2(String oldName, String newName) throws Exception{
		helper2_0(oldName, newName, newName, true);
	}

	public void testIllegalInnerClass() throws Exception {
		helper1();
	}

	public void testIllegalTypeName1() throws Exception {
		helper1_0("A", "X ");
	}

	public void testIllegalTypeName2() throws Exception {
		helper1_0("A", " X");
	}

	public void testIllegalTypeName3() throws Exception {
		helper1_0("A", "34");
	}

	public void testIllegalTypeName4() throws Exception {
		helper1_0("A", "this");
	}

	/* disabled: tests idiosyncratic feature
	public void testIllegalTypeName5() throws Exception {
		helper1_0("A", "fred");
	}*/

	public void testIllegalTypeName6() throws Exception {
		helper1_0("A", "class");
	}

	public void testIllegalTypeName7() throws Exception {
		helper1_0("A", "A.B");
	}

	/* disabled: tests idiosyncratic feature
	public void testIllegalTypeName8() throws Exception {
		helper1_0("A", "A$B");
	}*/

	/* disabled: tests idiosyncratic feature
	public void testIllegalTypeName9() throws Exception {
		if (Platform.getOS().equals(Platform.OS_WIN32))
			helper1_0("A", "aux");
	}*/

	/* disabled: we can do this
	public void testNoOp() throws Exception {
		helper1_0("A", "A");
	}*/

	public void testWrongArg1() throws Exception {
        helper1_0("A", "");
	}

	public void testFail0() throws Exception {
		helper1();
	}

	public void testFail1() throws Exception {
		helper1();
	}

	public void testFail2() throws Exception {
		helper1();
	}

	public void testFail3() throws Exception {
		helper1();
	}

	public void testFail4() throws Exception {
		helper1();
	}

	public void testFail5() throws Exception {
		helper1();
	}

	public void testFail6() throws Exception {
		helper1();
	}

	public void testFail7() throws Exception {
		helper1();
	}

	public void testFail8() throws Exception {
		helper1();
	}

	public void testFail9() throws Exception {
		helper1();
	}

	public void testFail10() throws Exception {
		helper1();
	}

	public void testFail11() throws Exception {
		helper1();
	}

	public void testFail12() throws Exception {
		helper1();
	}

	public void testFail16() throws Exception {
		helper1();
	}

	public void testFail17() throws Exception {
		helper1();
	}

	public void testFail18() throws Exception {
		helper1();
	}

	public void testFail19() throws Exception {
		helper1();
	}

	/* disabled: we can do this
	public void testFail20() throws Exception {
		helper1();
	}*/

	public void testFail22() throws Exception {
		helper1();
	}

	/* disabled: we can do this
	public void testFail23() throws Exception {
		helper1();
	}*/

	/* disabled: we can do this
	public void testFail24() throws Exception {
		helper1();
	}*/

	/* disabled: we can do this
	public void testFail25() throws Exception {
		helper1();
	}*/

	/* disabled: we can do this
	public void testFail26() throws Exception {
		helper1();
	}*/

	/* disabled: we can do this
	public void testFail27() throws Exception {
		helper1();
	}*/

	/* disabled: we can do this
	public void testFail28() throws Exception {
		//printTestDisabledMessage("obscuring");
		helper1();
	}

	public void testFail29() throws Exception {
		//printTestDisabledMessage("obscuring");
		helper1();
	}

	public void testFail30() throws Exception {
		//printTestDisabledMessage("obscuring");
		helper1();
	}*/

	/* disabled: we can do this
	public void testFail31() throws Exception {
		helper1();
	}*/

	/* disabled: we can do this
	public void testFail32() throws Exception {
		helper1();
	}

	public void testFail33() throws Exception {
		helper1();
	}

	public void testFail34() throws Exception {
		//printTestDisabledMessage("obscuring");
		helper1();
	}

	public void testFail35() throws Exception {
		//printTestDisabledMessage("obscuring");
		helper1();
	}

	public void testFail36() throws Exception {
		//printTestDisabledMessage("obscuring");
		helper1();
	}*/

	public void testFail37() throws Exception {
		helper1();
	}

	public void testFail38() throws Exception {
		helper1();
	}

	public void testFail39() throws Exception {
		helper1();
	}

	public void testFail40() throws Exception {
		helper1();
	}

	/* disabled: we can do this
	public void testFail41() throws Exception {
		//printTestDisabledMessage("obscuring");
		helper1();
	}

	public void testFail42() throws Exception {
		//printTestDisabledMessage("obscuring");
		helper1();
	}

	public void testFail43() throws Exception {
		//printTestDisabledMessage("obscuring");
		helper1();
	}

	public void testFail44() throws Exception {
		//printTestDisabledMessage("obscuring");
		helper1();
	}

	public void testFail45() throws Exception {
		//printTestDisabledMessage("obscuring");
		helper1();
	}

	public void testFail46() throws Exception {
		//printTestDisabledMessage("obscuring");
		helper1();
	}

	public void testFail47() throws Exception {
		//printTestDisabledMessage("obscuring");
		helper1();
	}

	public void testFail48() throws Exception {
		helper1();
	}

	public void testFail49() throws Exception {
		helper1();
	}

	public void testFail50() throws Exception {
		helper1();
	}

	public void testFail51() throws Exception {
		helper1();
	}

	public void testFail52() throws Exception {
		helper1();
	}

	public void testFail53() throws Exception {
		helper1();
	}

	public void testFail54() throws Exception {
		helper1();
	}

	public void testFail55() throws Exception {
		helper1();
	}

	public void testFail56() throws Exception {
		helper1();
	}

	public void testFail57() throws Exception {
		helper1();
	}

	public void testFail58() throws Exception {
		helper1();
	}

	public void testFail59() throws Exception {
		helper1();
	}

	public void testFail60() throws Exception {
		helper1();
	}

	public void testFail61() throws Exception {
		helper1();
	}

	public void testFail62() throws Exception {
		helper1();
	}

	public void testFail63() throws Exception {
		helper1();
	}

	public void testFail64() throws Exception {
		helper1();
	}

	public void testFail65() throws Exception {
		helper1();
	}

	public void testFail66() throws Exception {
		helper1();
	}

	public void testFail67() throws Exception {
		helper1();
	}

	public void testFail68() throws Exception {
		helper1();
	}

	public void testFail69() throws Exception {
		helper1();
	}

	public void testFail70() throws Exception {
		helper1();
	}

	public void testFail71() throws Exception {
		helper1();
	}

	public void testFail72() throws Exception {
		helper1();
	}

	public void testFail73() throws Exception {
		helper1();
	}

	public void testFail74() throws Exception {
		helper1();
	}

	public void testFail75() throws Exception {
		helper1();
	}

	public void testFail76() throws Exception {
		helper1();
	}

	public void testFail77() throws Exception {
		helper1();
	}

	public void testFail78() throws Exception {
		helper1();
	}

	public void testFail79() throws Exception {
		helper1();
	}*/

	public void testFail80() throws Exception {
		helper1();
	}

	public void testFail81() throws Exception {
		helper1();
	}

	/* disabled: we can do this
	public void testFail82() throws Exception {
		helper1();
	}

	public void testFail83() throws Exception {
		helper1_0("A", "Cloneable");
	}*/

	/* disabled: we can do this
	public void testFail84() throws Exception {
		helper1_0("A", "List");
	}*/

	public void testFail85() throws Exception {
		helper1();
	}

	public void testFail86() throws Exception {
		//printTestDisabledMessage("native method with A as parameter (same CU)");
		helper1();
	}

	public void testFail87() throws Exception {
		//printTestDisabledMessage("native method with A as parameter (same CU)");
		helper1();
	}

	public void testFail88() throws Exception {
		helper1();
	}

	public void testFail89() throws Exception {
		helper1();
	}

	/* disabled: we can do this
	public void testFail90() throws Exception {
		helper1();
	}

	public void testFail91() throws Exception {
		helper1();
	}*/

	public void testFail92() throws Exception {
//		printTestDisabledMessage("needs fixing - double nested local type named B");
		helper1();
	}

	public void testFail93() throws Exception {
//		printTestDisabledMessage("needs fixing - double nested local type named B");
		helper1();
	}

	public void testFail94() throws Exception {
		helper1();
	}

	public void testFail95() throws Exception {
		helper1();
	}

	public void testFail00() throws Exception {
		helper1();
	}

	public void testFail01() throws Exception {
		helper1_0("A", "B");
	}

	public void testFail02() throws Exception {
		helper1();
	}

	public void testFail03() throws Exception {
		helper1_0("A", "C");
	}

	/* disabled: we can do this
	public void testFail04() throws Exception {
		helper1_0("A", "A");
	}*/

	/* disabled: tests idiosyncratic feature
	public void testFailRegression1GCRKMQ() throws Exception {
		helper1_0("Blinky", "B");
	}*/

	public void test0() throws Exception {
		helper2("A", "B");
	}

	public void test1() throws Exception {
		helper2("A", "B");
	}

	public void test10() throws Exception {
		helper2("A", "B");
	}

	public void test12() throws Exception {
		helper2("A", "B");
	}

	public void test13() throws Exception {
		helper2("A", "B");
	}

	public void test14() throws Exception {
		helper2("A", "B");
	}

	public void test15() throws Exception {
		helper2("A", "B");
	}

	public void test16() throws Exception {
		helper2("A", "B");
	}

	public void test17() throws Exception {
		helper2("A", "B");
	}

	public void test18() throws Exception {
		helper2("A", "B");
	}

	public void test19() throws Exception {
		helper2("A", "B");
	}

	public void test2() throws Exception {
		helper2("A", "B");
	}

	public void test20() throws Exception {
		helper2("A", "B");
	}

	public void test21() throws Exception {
		helper2("A", "B");
	}

	public void test22() throws Exception {
		helper2("A", "B");
	}

	public void test23() throws Exception {
		helper2("A", "B");
	}

	public void test24() throws Exception {
		helper2("A", "B");
	}

	public void test25() throws Exception {
		helper2("A", "B");
	}

	public void test26() throws Exception {
		helper2("A", "B");
	}

	public void test27() throws Exception {
		helper2("A", "B");
	}

	public void test28() throws Exception {
		helper2("A", "B");
	}

	public void test29() throws Exception {
		helper2("A", "B");
	}

	public void test3() throws Exception {
		helper2("A", "B");
	}

	/* disabled: problem with pretty printing
	public void test30() throws Exception {
		helper2("A", "B");
	}*/
	/* disabled: problem with pretty printing
	public void test31() throws Exception {
		helper2("A", "B");
	}*/
	public void test32() throws Exception {
		helper2("A", "B");
	}

	public void test33() throws Exception {
		helper2("A", "B");
	}

	public void test34() throws Exception {
		helper2("A", "B");
	}

	public void test35() throws Exception {
		helper2("A", "B");
	}

	public void test36() throws Exception {
		helper2("A", "B");
	}

	public void test37() throws Exception {
		helper2("A", "B");
	}

	public void test38() throws Exception {
		helper2("A", "B");
	}

	public void test39() throws Exception {
		helper2("A", "B");
	}

	public void test4() throws Exception {
		helper2("A", "B");
	}

	public void test40() throws Exception {
		//printTestDisabledMessage("search engine bug");
		helper2("A", "B");
	}

	public void test41() throws Exception {
		helper2("A", "B");
	}

	public void test42() throws Exception {
		helper2("A", "B");
	}

	public void test43() throws Exception {
		helper2("A", "B");
	}

	public void test44() throws Exception {
		helper2("A", "B");
	}

	public void test45() throws Exception {
		helper2("A", "B");
	}

	public void test46() throws Exception {
		helper2("A", "B");
	}

	public void test47() throws Exception {
		helper2("A", "B");
	}

	public void test48() throws Exception {
		helper2("A", "B");
	}

	public void test49() throws Exception {
		helper2("A", "B");
	}

	public void test50() throws Exception {
		//printTestDisabledMessage("https://bugs.eclipse.org/bugs/show_bug.cgi?id=54948");
		helper2("A", "B");
	}

	public void test51() throws Exception {
		helper2("A", "B");
	}

	public void test52() throws Exception {
		//printTestDisabledMessage("1GJY2XN: ITPJUI:WIN2000 - rename type: error when with reference");
		helper2("A", "B");
	}

	/* disabled: does not compile
	public void test53() throws Exception {
		helper2("A", "B", false);
	}*/

	public void test54() throws Exception {
		//printTestDisabledMessage("waiting for: 1GKAQJS: ITPJCORE:WIN2000 - search: incorrect results for nested types");
		helper2("X", "XYZ");
	}

	/* disabled: does not compile
	public void test55() throws Exception {
		//printTestDisabledMessage("waiting for: 1GKAQJS: ITPJCORE:WIN2000 - search: incorrect results for nested types");
		helper2("A", "B");
	}*/

	/* disabled: tests idiosyncratic feature
	public void test57() throws Exception {
		helper2("A", "B");
	}*/

	public void test58() throws Exception {
		//printTestDisabledMessage("bug#16751");
		helper2("A", "B");
	}

	public void test59() throws Exception {
//		printTestDisabledMessage("bug#22938");
		helper2("A", "B");
	}

	/* disabled: tests idiosyncratic feature
	public void test60() throws Exception {
//		printTestDisabledMessage("test for bug 24740");
		helper2("A", "B");
	}*/

	public void test61() throws Exception {
		helper2("Inner", "InnerB");
	}

	/* disabled: does not compile
	public void test62() throws Exception {
//		printTestDisabledMessage("test for bug 66250");
		helper2("A", "B");
	}*/

	/* disabled: tests idiosyncratic feature
	public void test63() throws Exception {
//		printTestDisabledMessage("test for bug 79131");
		helper2("A", "B");
	}*/

	/* disabled: tests idiosyncratic feature
	public void test64() throws Exception {
//		printTestDisabledMessage("test for bug 79131");
		helper2("A", "B");
	}*/

	public void test5() throws Exception {
		helper2("A", "B");
	}

	public void test6() throws Exception {
		helper2("A", "B");
	}

	public void test7() throws Exception {
		helper2("A", "B");
	}

	public void test8() throws Exception {
		helper2("A", "B");
	}

	public void test9() throws Exception {
		helper2("A", "B");
	}

	public void testUnicode01() throws Exception {
		helper2("B", "C");
		//TODO: test undo!
	}

	public void testQualifiedName1() throws Exception {
		helperQualifiedName("A", "B", "build.xml", "*.xml");
	}

	public void testQualifiedName2() throws Exception {
		helperQualifiedName("Transient", "TransientEquipment", "mapping.hbm.xml", "*.xml");
	}

	private void helperQualifiedName(String oldName, String newName, String textFileName, String filePatterns) throws Exception {
		helper2(oldName, newName);
	}

	public void testGenerics1() throws Exception {
		helper2("A", "B");
	}

	public void testGenerics2() throws Exception {
		helper2("A", "B");
	}

	public void testGenerics3() throws Exception {
		helper2("A", "B");
	}

	public void testGenerics4() throws Exception {
		helper2("A", "B");
	}

	public void testEnum1() throws Exception {
		helper2("p.A", "B");
	}

	public void testEnum2() throws Exception {
		helper2("A", "B");
	}

	public void testEnum3() throws Exception {
		helper2("A", "B");
	}

	public void testEnum4() throws Exception {
		helper2("A", "B");
	}

	public void testEnum5() throws Exception {
		helper2("A", "B");
	}

	public void testAnnotation1() throws Exception {
		helper2("A", "B");
	}

	public void testAnnotation2() throws Exception {
		helper2("A", "B");
	}

	/* disabled: tests idiosyncratic feature
	public void testAnnotation3() throws Exception {
		helper2("A", "B");
	}*/

	/* disabled: unsupported feature
	// --------------- Similarly named elements -----------------

	public void testSimilarElements00() throws Exception {
		// Very basic test, one field, two methods
		helper3("SomeClass", "SomeClass2", true, false, true);
	}

	public void testSimilarElements01() throws Exception {
		// Already existing field with new name, shadow-error from field refac
		helper3_fail("SomeClass", "SomeClass2", true, false, true);
	}

	public void testSimilarElements02() throws Exception {
		// Already existing method
		helper3_fail("SomeClass", "SomeDifferentClass", true, false, true);
	}

	public void testSimilarElements03() throws Exception {
		// more methods
		helper3("SomeClass", "SomeClass2", true, false, true);
	}
	public void testSimilarElements04() throws Exception {
		//Additional field with exactly the same name and getters and setters in another class
		getClassFromTestFile(getPackageP(), "SomeOtherClass");
		helper3("SomeClass", "SomeClass2", true, false, true);
		checkResultInClass("SomeOtherClass");
	}

	public void testSimilarElements05() throws Exception {
		//qualified name updating
		//includes textual updating
		String content= getFileContents(getTestPath() + "testSimilarElements05/in/test.html");
		IProject project= getPackageP().getJavaProject().getProject();
		IFile file= project.getFile("test.html");
		file.create(new ByteArrayInputStream(content.getBytes()), true, null);

		helper3("SomeClass", "SomeDifferentClass", true, true, true, "test.html");

		InputStreamReader reader= new InputStreamReader(file.getContents(true));
		StringBuffer newContent= new StringBuffer();
		try {
			int ch;
			while((ch= reader.read()) != -1)
				newContent.append((char)ch);
		} finally {
			reader.close();
		}
		String definedContent= getFileContents(getTestPath() + "testSimilarElements05/out/test.html");
		assertEqualLines("invalid updating test.html", newContent.toString(), definedContent);

	}

	public void testSimilarElements06() throws Exception {
		//Additional field with exactly the same name and getters and setters in another class
		//includes textual updating
		// printTestDisabledMessage("potential matches in comments issue (bug 111891)");
		getClassFromTestFile(getPackageP(), "SomeNearlyIdenticalClass");
		helper3("SomeClass", "SomeOtherClass", true, true, true);
		checkResultInClass("SomeNearlyIdenticalClass");
	}

	public void testSimilarElements07() throws Exception {
		//Test 4 fields in one file, different suffixes/prefixes, incl. 2x setters/getters
		//includes textual updating
		helper3("SomeClass", "SomeDiffClass", true, true, true);
	}

	public void testSimilarElements08() throws Exception {
		//Interface renaming fun, this time without textual
		helper3("ISomeIf", "ISomeIf2", true, false, true);
	}

	public void testSimilarElements09() throws Exception {
		//Some inner types
		//includes textual updating
		getClassFromTestFile(getPackageP(), "SomeOtherClass");
		helper3_inner("SomeClass", "SomeInnerClass", "SomeClass", "SomeNewInnerClass", true, true, true, null);
		checkResultInClass("SomeOtherClass");
	}

	public void testSimilarElements10() throws Exception {
		//Two static fields
		getClassFromTestFile(getPackageP(), "SomeOtherClass");
		helper3("SomeClass", "SomeClass2", true, false, true, null);
		checkResultInClass("SomeOtherClass");
	}

	public void testSimilarElements11() throws Exception {
		//Assure participants get notified of normal stuff (type rename
		//and resource changes) AND similarly named elements.
		ParticipantTesting.reset();
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "SomeClass");
		IType someClass= getType(cu, "SomeClass");
		IType other= getClassFromTestFile(getPackageP(), "SomeOtherClass");

		List handleList= new ArrayList();
		List argumentList= new ArrayList();

		List similarOldHandleList= new ArrayList();
		List similarNewNameList= new ArrayList();
		List similarNewHandleList= new ArrayList();

		final String newName= "SomeNewClass";

		// f-Field + getters/setters
		IField f3= other.getField("fSomeClass");
		similarOldHandleList.add(f3.getHandleIdentifier());
		similarNewHandleList.add("Lp/SomeOtherClass;.fSomeNewClass");
		similarNewNameList.add("fSomeNewClass");

		IMethod m3= other.getMethod("getSomeClass", new String[0]);
		similarOldHandleList.add(m3.getHandleIdentifier());
		similarNewNameList.add("getSomeNewClass");
		similarNewHandleList.add("Lp/SomeOtherClass;.getSomeNewClass()V");
		IMethod m4= other.getMethod("setSomeClass", new String[] {"QSomeClass;"});
		similarOldHandleList.add(m4.getHandleIdentifier());
		similarNewNameList.add("setSomeNewClass");
		similarNewHandleList.add("Lp/SomeOtherClass;.setSomeNewClass(QSomeNewClass;)V");

		// non-f-field + getter/setters
		IField f1= someClass.getField("someClass");
		similarOldHandleList.add(f1.getHandleIdentifier());
		similarNewNameList.add("someNewClass");
		similarNewHandleList.add("Lp/SomeNewClass;.someNewClass");
		IMethod m1= someClass.getMethod("getSomeClass", new String[0]);
		similarOldHandleList.add(m1.getHandleIdentifier());
		similarNewNameList.add("getSomeNewClass");
		similarNewHandleList.add("Lp/SomeNewClass;.getSomeNewClass()V");
		IMethod m2= someClass.getMethod("setSomeClass", new String[] {"QSomeClass;"});
		similarOldHandleList.add(m2.getHandleIdentifier());
		similarNewNameList.add("setSomeNewClass");
		similarNewHandleList.add("Lp/SomeNewClass;.setSomeNewClass(QSomeNewClass;)V");

		// fs-field
		IField f2= someClass.getField("fsSomeClass");
		similarOldHandleList.add(f2.getHandleIdentifier());
		similarNewNameList.add("fsSomeNewClass");
		similarNewHandleList.add("Lp/SomeNewClass;.fsSomeNewClass");

		// Type Stuff
		handleList.add(someClass);
		argumentList.add(new RenameArguments(newName, true));
		handleList.add(cu);
		argumentList.add(new RenameArguments(newName + ".java", true));
		handleList.add(cu.getResource());
		argumentList.add(new RenameArguments(newName + ".java", true));

		String[] handles= ParticipantTesting.createHandles(handleList.toArray());
		RenameArguments[] arguments= (RenameArguments[])argumentList.toArray(new RenameArguments[0]);

		RenameJavaElementDescriptor descriptor= createRefactoringDescriptor(someClass, newName);
		setTheOptions(descriptor, true, false, true, null, RenamingNameSuggestor.STRATEGY_EMBEDDED);
		RefactoringStatus status= performRefactoring(descriptor);
		assertNull("was supposed to pass", status);

		checkResultInClass(newName);
		checkResultInClass("SomeOtherClass");

		ParticipantTesting.testRename(handles, arguments);
		ParticipantTesting.testSimilarElements(similarOldHandleList, similarNewNameList, similarNewHandleList);
	}

	public void testSimilarElements12() throws Exception {
		// Test updating of references
		helper3("SomeFieldClass", "SomeOtherFieldClass", true, false, true);
	}

	public void testSimilarElements13() throws Exception {
		// Test various locals and parameters with and without prefixes.
		// tests not renaming parameters with local prefixes and locals with parameter prefixes
		helper3("SomeClass", "SomeOtherClass", true, false, true);
	}

	public void testSimilarElements14() throws Exception {
		// Test for loop variables
		helper3("SomeClass2", "SomeOtherClass2", true, false, true);
	}

	public void testSimilarElements15() throws Exception {
		// Test catch block variables (exceptions)
		helper3("SomeClass3", "SomeOtherClass3", true, false, true);
	}

	public void testSimilarElements16() throws Exception {
		// Test updating of references
		helper3("SomeClass4", "SomeOtherClass4", true, false,  true);
	}

	public void testSimilarElements17() throws Exception {
		// Local with this name already exists - do not pass.
		helper3_fail("SomeClass6", "SomeOtherClass6", true, false, true);
	}

	public void testSimilarElements18() throws Exception {
		// factory method
		helper3("SomeClass", "SomeOtherClass", true, false, true);
	}

	public void testSimilarElements19() throws Exception {
		// Test detection of same target
		helper3_fail("ThreeHunkClass", "TwoHunk", true, false, true, RenamingNameSuggestor.STRATEGY_SUFFIX);
	}

	public void testSimilarElements20() throws Exception {
		// Overridden method, check both are renamed
		getClassFromTestFile(getPackageP(), "OtherClass");
		helper3("OverriddenMethodClass", "ThirdClass", true, false, true);
		checkResultInClass("OtherClass");
	}

	public void testSimilarElements21() throws Exception {
		// Constructors may not be renamed
		getClassFromTestFile(getPackageP(), "SomeClassSecond");
		helper3("SomeClass", "SomeNewClass", true, false, true);
		checkResultInClass("SomeClassSecond");
	}

	public void testSimilarElements22() throws Exception {
		// Test transplanter for fields in types inside of initializers

		ParticipantTesting.reset();
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "SomeClass");
		IType someClass= getType(cu, "SomeClass");

		List handleList= new ArrayList();
		List argumentList= new ArrayList();

		List similarOldHandleList= new ArrayList();
		List similarNewNameList= new ArrayList();
		List similarNewHandleList= new ArrayList();

		final String newName= "SomeNewClass";

		// field in class in initializer
		IField inInitializer= someClass.getInitializer(1).getType("InInitializer", 1).getField("someClassInInitializer");
		similarOldHandleList.add(inInitializer.getHandleIdentifier());
		similarNewNameList.add("someNewClassInInitializer");
		similarNewHandleList.add("Lp/SomeNewClass$InInitializer;.someNewClassInInitializer");

		// Type Stuff
		handleList.add(someClass);
		argumentList.add(new RenameArguments(newName, true));
		handleList.add(cu);
		argumentList.add(new RenameArguments(newName + ".java", true));
		handleList.add(cu.getResource());
		argumentList.add(new RenameArguments(newName + ".java", true));

		String[] handles= ParticipantTesting.createHandles(handleList.toArray());
		RenameArguments[] arguments= (RenameArguments[])argumentList.toArray(new RenameArguments[0]);

		RenameJavaElementDescriptor descriptor= createRefactoringDescriptor(someClass, newName);
		setTheOptions(descriptor, true, false, true, null, RenamingNameSuggestor.STRATEGY_EMBEDDED);
		RefactoringStatus status= performRefactoring(descriptor);
		assertNull("was supposed to pass", status);

		checkResultInClass(newName);

		ParticipantTesting.testRename(handles, arguments);
		ParticipantTesting.testSimilarElements(similarOldHandleList, similarNewNameList, similarNewHandleList);

	}

	public void testSimilarElements23() throws Exception {
		// Test transplanter for elements inside types inside fields

		ParticipantTesting.reset();
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "SomeClass");
		IType someClass= getType(cu, "SomeClass");

		List handleList= new ArrayList();
		List argumentList= new ArrayList();

		List similarOldHandleList= new ArrayList();
		List similarNewNameList= new ArrayList();
		List similarNewHandleList= new ArrayList();

		final String newName= "SomeNewClass";

		// some field
		IField anotherSomeClass= someClass.getField("anotherSomeClass");
		similarOldHandleList.add(anotherSomeClass.getHandleIdentifier());
		similarNewNameList.add("anotherSomeNewClass");
		similarNewHandleList.add("Lp/SomeNewClass;.anotherSomeNewClass");

		// field in class in method in field declaration ;)
		IField inInner= anotherSomeClass.getType("", 1).getMethod("foo", new String[0]).getType("X", 1).getField("someClassInInner");
		similarOldHandleList.add(inInner.getHandleIdentifier());
		similarNewNameList.add("someNewClassInInner");
		similarNewHandleList.add("Lp/SomeNewClass$1$X;.someNewClassInInner");

		// Type Stuff
		handleList.add(someClass);
		argumentList.add(new RenameArguments(newName, true));
		handleList.add(cu);
		argumentList.add(new RenameArguments(newName + ".java", true));
		handleList.add(cu.getResource());
		argumentList.add(new RenameArguments(newName + ".java", true));

		String[] handles= ParticipantTesting.createHandles(handleList.toArray());
		RenameArguments[] arguments= (RenameArguments[])argumentList.toArray(new RenameArguments[0]);

		RenameJavaElementDescriptor descriptor= createRefactoringDescriptor(someClass, newName);
		setTheOptions(descriptor, true, false, true, null, RenamingNameSuggestor.STRATEGY_EMBEDDED);
		RefactoringStatus status= performRefactoring(descriptor);
		assertNull("was supposed to pass", status);

		checkResultInClass(newName);

		ParticipantTesting.testRename(handles, arguments);
		ParticipantTesting.testSimilarElements(similarOldHandleList, similarNewNameList, similarNewHandleList);
	}

	public void testSimilarElements24() throws Exception {
		// Test transplanter for ICompilationUnit and IFile

		ParticipantTesting.reset();
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "SomeClass");
		IType someClass= getType(cu, "SomeClass");
		IJavaElement[] someClassMembers= someClass.getChildren();

		RenameJavaElementDescriptor descriptor= createRefactoringDescriptor(someClass, "SomeNewClass");
		setTheOptions(descriptor, true, false, true, null, RenamingNameSuggestor.STRATEGY_EMBEDDED);
		Refactoring ref= createRefactoring(descriptor);
		RefactoringStatus status= performRefactoring(ref);
		assertNull("was supposed to pass", status);

		checkResultInClass("SomeNewClass");

		checkMappers(ref, someClass, "SomeNewClass.java", someClassMembers);
	}

	private void checkMappers(Refactoring refactoring, IType type, String newCUName, IJavaElement[] someClassMembers) {
		RenameTypeProcessor rtp= (RenameTypeProcessor)((RenameRefactoring) refactoring).getProcessor();

		ICompilationUnit newUnit= (ICompilationUnit)rtp.getRefactoredJavaElement(type.getCompilationUnit());
		assertTrue(newUnit.exists());
		assertTrue(newUnit.getElementName().equals(newCUName));

		IFile newFile= (IFile)rtp.getRefactoredResource(type.getResource());
		assertTrue(newFile.exists());
		assertTrue(newFile.getName().equals(newCUName));

		if ((type.getParent().getElementType() == IJavaElement.COMPILATION_UNIT)
				&& type.getCompilationUnit().getElementName().equals(type.getElementName() + ".java")) {
			assertFalse(type.getCompilationUnit().exists());
			assertFalse(type.getResource().exists());
		}

		IPackageFragment oldPackage= (IPackageFragment)type.getCompilationUnit().getParent();
		IPackageFragment newPackage= (IPackageFragment)rtp.getRefactoredJavaElement(oldPackage);
		assertEquals(oldPackage, newPackage);

		for (int i= 0; i < someClassMembers.length; i++) {
			IMember member= (IMember) someClassMembers[i];
			IJavaElement refactoredMember= rtp.getRefactoredJavaElement(member);
			if (member instanceof IMethod && member.getElementName().equals(type.getElementName()))
				continue; // constructor
			assertTrue(refactoredMember.exists());
			assertEquals(member.getElementName(), refactoredMember.getElementName());
			assertFalse(refactoredMember.equals(member));
		}
	}

	public void testSimilarElements25() throws Exception {
		// Test renaming of several-in-one field declarations
		helper3("ScrewUp", "ScrewDown", true, false, true);
	}

	public void testSimilarElements26() throws Exception {
		// Test renaming of several-in-one local variable declarations
		helper3("ScrewUp", "ScrewDown", true, false, true);
	}

	public void testSimilarElements27() throws Exception {
		// Test methods are not renamed if the match is
		// not either a parameter or a return type
		helper3("ScrewUp", "ScrewDown", true, false, true);
	}

	public void testSimilarElements28() throws Exception {
		// Test local variables are not renamed if the match is
		// not the type of the local variable itself
		helper3("ScrewUp", "ScrewDown", true, false, true);
	}

	public void testSimilarElements29() throws Exception {
		// Test fields are not renamed if the match is
		// not the type of the field itself
		helper3("ScrewUp", "ScrewDown", true, false, true);
	}

	public void testSimilarElements30() throws Exception {
		// Test local variables in initializers
		helper3("SomeClass", "SomeNewClass", true, false, true);
	}

	public void testSimilarElements31() throws Exception {
		// Test references and textual references to local elements
		helper3("SomeClass", "SomeDiffClass", true, true, true);
	}

	public void testSimilarElements32() throws Exception {
		// Test whether local variable problem reporting still works
		helper3_fail("SomeClass", "SomeDifferentClass", true, false, true);
	}

	public void testSimilarElements33() throws Exception {
		// Test two local variables inside anonymous types do not generate warnings
		helper3("Why", "WhyNot", true, false, true);
	}

	public void testSimilarElements34() throws Exception {
		// Test references in annotations and type parameters
		helper3("Try", "Bla", true, false, true);
	}*/
}
