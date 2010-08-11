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
package tests.eclipse.MoveInnerToTopLevel;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.BodyDecl;
import AST.MemberTypeDecl;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class MoveInnerToTopLevelTests extends TestCase {

	public MoveInnerToTopLevelTests(String name) {
		super(name);
	}

	MemberTypeDecl findMemberType(Program in, String outer, String inner) {
		TypeDecl td = in.findSimpleType(outer);
		assertNotNull(td);

		for(BodyDecl bd : td.getBodyDecls())
			if(bd.declaresType(inner))
				return (MemberTypeDecl)bd;
		fail("member type not found");
		return null;
	}
	
	public void validatePassingTest(String outer, String inner, Object o3, Object o2, String instanceName, boolean b1, boolean b2) {
		validatePassingTest(outer, inner, instanceName, false);
	}
	
	public void validatePassingTest(String outer, String inner, String instanceName, boolean makeFinal) {
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/MoveInnerToTopLevel/"+getName()+"/in");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		Program out = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/MoveInnerToTopLevel/"+getName()+"/out");
		assertNotNull(out);
		
		try {
			findMemberType(in, outer, inner).moveToToplevel(true, instanceName, makeFinal);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.toString());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	public void validateFailingTest(String outer, String inner, Object o3, Object o2, String instanceName) {
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/MoveInnerToTopLevel/"+getName()+"/in");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		
		try {
			findMemberType(in, outer, inner).moveToToplevel(true, instanceName, false);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) {
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	public void validatePassingTest(String outer, String inner, Object o1, Object o2, String instanceName, boolean b1, boolean b2, boolean b3, boolean b4) {
		validatePassingTest(outer, inner, instanceName, b1);
	}
	
	public void validatePassingTest(String outer, String inner, Object o1, Object o2, Object o3, String instanceName, boolean b1, boolean b2, boolean b3, boolean b4) {
		validatePassingTest(outer, inner, instanceName, b1);
	}
	
	public void validatePassingTest(String outer, String inner, String moreInner, Object o2, Object o3, Object o4, String instanceName, boolean b1, boolean b2, boolean b3, boolean b4) {
		if(moreInner.equals(""))
			validatePassingTest(outer, inner, instanceName, b1);
		else
			validatePassingTest(inner, moreInner, instanceName, b1);
	}
	
	//-- tests

	public void test0() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}

	public void test1() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}

	public void test2() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}

	public void test3() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}

	public void test4() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}

	public void test5() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}

	public void test6() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}
	public void test7() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}

	public void test8() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}
	public void test9() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}

	public void test10() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}
	public void test11() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}

	public void test12() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}
	public void test13() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}

	public void test14() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}

	public void test15() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A", "A1"}, new String[]{"p", "p1"}, null, false, false);
	}

	public void test16() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A", "A1"}, new String[]{"p", "p1"}, null, false, false);
	}

	public void test17() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}

	public void test18() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A", "A1"}, new String[]{"p", "p1"}, null, false, false);
	}

	/* disabled: by Eclipse
	public void test19() throws Exception{
		printTestDisabledMessage("bug 23078");
//		validatePassingTest("A", "Inner", new String[]{"A", "A1"}, new String[]{"p", "p1"}, null, false, false);
	}*/

	public void test20() throws Exception{
//		printTestDisabledMessage("bug 23077 ");
		validatePassingTest("A", "Inner", new String[]{"A", "A1"}, new String[]{"p", "p1"}, null, false, false);
	}

	public void test21() throws Exception{
//		printTestDisabledMessage("bug 23627");
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}
	public void test22() throws Exception{
//		printTestDisabledMessage("bug 23627");
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}

	public void test23() throws Exception{
//		printTestDisabledMessage("bug 24576 ");
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}

	public void test24() throws Exception{
//		printTestDisabledMessage("bug 28816 ");
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, null, false, false);
	}

	public void test25() throws Exception{
//		printTestDisabledMessage("bug 39716");
		validatePassingTest("A", "Inner", "", new String[]{"A"}, new String[]{""}, null, false, false, false, true);
	}

	/* disabled: visibility
	public void test26() throws Exception{
		validatePassingTest("A", "Inner", "", new String[]{"A"}, new String[]{""}, null, false, true, true, true);
	}*/

	/* disabled: visibility
	public void test30() throws Exception{
		validatePassingTest("A", "Inner", "", new String[]{"A"}, new String[]{""}, null, false, true, true, true);
	}*/

	/* disabled: by Eclipse
	public void test31() throws Exception{
		printTestDisabledMessage("disabled due to missing support for statically imported methods");
		// validatePassingTest("A", "Inner", "", new String[]{"A"}, new String[]{""}, null, false, true, true, true);
	}*/

	// ---- Visibility issues with the moved member itself and its parents

	// Move inner class; enclosing class must remain private if not  used
	public void test32() throws Exception{
		validatePassingTest("A", "Inner", "MoreInner", "p1", new String[]{"A"}, new String[]{"p1"}, null, false, false, false, false);
	}

	/* disabled: visibility
	// Move inner class which has access to enclosing private class, enclosing class must be increased in visibility
	public void test33() throws Exception{
		validatePassingTest("A", "Inner", "MoreInner", "p2", new String[]{"A"}, new String[]{"p2"}, null, false, false, false, false);
	}*/

	// --- Visibility issues with members of moved members

	// Move inner class which has private members, which are accessed from enclosing types.
	/* disabled: visibility
	public void test34() throws Exception {
		validatePassingTest("A", "SomeClass", "p", new String[] { "A"}, new String[] { "p"}, null, false, true, false, false);
	}*/

	// Move inner class which has private members, but they are unused (and must remain private)
	public void test35() throws Exception {
		validatePassingTest("A", "Inner", "p", new String[] { "A"}, new String[] { "p"}, null, false, true, false, false);
	}

	/* disabled: visibility
	// Move inner class which has access private members, and accessing private members of
	// enclosing class (4 visibility increments)
	public void test36() throws Exception {
		validatePassingTest("A", "SomeInner", "Inner", "p", new String[] { "A"}, new String[] { "p"}, null, false, false, false, false);
	}*/

	/* disabled: visibility
	// Move inner class with some private used and some private non-used members.
	// used members go default, non-used stay private
	// bug 97411 + 117465 (comment #1)
	public void test37() throws Exception {
		validatePassingTest("A", "SomeInner", "p", new String[] { "A"}, new String[] { "p"}, null, false, false, false, false);
	}*/

	/* disabled: visibility
	public void test38() throws Exception {
		validatePassingTest("A", "B", "p", new String[] { "A"}, new String[] { "p"}, null, false, false, false, false);
	}*/

	// --- Non static

	public void test_nonstatic_0() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}

	public void test_nonstatic_1() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_2() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_3() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_4() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_5() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_6() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_7() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_8() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_9() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_10() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_11() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_12() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, true);
	}
	public void test_nonstatic_13() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, true);
	}
	public void test_nonstatic_14() throws Exception{
//		printTestDisabledMessage("bug 23488");
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_15() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_16() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_17() throws Exception{
//		printTestDisabledMessage("bug 23488");
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_18() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_19() throws Exception{
//		printTestDisabledMessage("bug 23464 ");
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_20() throws Exception{
//		printTestDisabledMessage("bug 23464 ");
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_21() throws Exception{
//		printTestDisabledMessage("must fix - consequence of fix for 23464");
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_22() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_23() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_24() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_25() throws Exception{
//		printTestDisabledMessage("bug 23464 ");
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_26() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_27() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	public void test_nonstatic_28() throws Exception{
//		printTestDisabledMessage("test for bug 23725");
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}
	
	/* disabled: by Eclipse
	public void test_nonstatic_29() throws Exception{
		printTestDisabledMessage("test for bug 23724");
//		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}*/
	
	public void test_nonstatic_30() throws Exception{
//		printTestDisabledMessage("test for bug 23715");
		validatePassingTest("A", "Inner", "p", new String[]{"A"}, new String[]{"p"}, "a", true, true, false, true);
	}

	public void test_nonstatic_31() throws Exception{
//		printTestDisabledMessage("test for bug 25537");
		validatePassingTest("A", "Inner", "p", new String[]{"A"}, new String[]{"p"}, "a", true, true, true, true);
	}

	public void test_nonstatic_32() throws Exception{
//		printTestDisabledMessage("test for bug 25537");
		validatePassingTest("A", "Inner", "p", new String[]{"A"}, new String[]{"p"}, "a", true, true, true, true);
	}

	/* disabled: visibility
    public void test_nonstatic_33() throws Exception{
//		printTestDisabledMessage("test for bug 26252");
        validatePassingTest("A", "I", "p", new String[]{"A"}, new String[]{"p"}, "a", true, true, false, true);
    }*/

	public void test_nonstatic_34() throws Exception{
//		printTestDisabledMessage("test for bug 31861");
		validatePassingTest("A", "Inner", "p", new String[]{"A"}, new String[]{"p"}, "a", true, true, true, true);
	}

	public void test_nonstatic_35() throws Exception{
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, false);
	}

	public void test_nonstatic_36() throws Exception{
//		printTestDisabledMessage("test for bug 34591");
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, true);
	}

	public void test_nonstatic_37() throws Exception{
//		printTestDisabledMessage("test for bug 38114");
		validatePassingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a", true, true);
	}

	public void test_nonstatic_38() throws Exception{
//		printTestDisabledMessage("test for bug 37540");
		validatePassingTest("A", "Inner", "p", new String[]{"A"}, new String[]{"p"}, "a", false, true, false, false);
	}

	public void test_nonstatic_39() throws Exception{
//		printTestDisabledMessage("test for bug 37540");
		validatePassingTest("A", "Inner", "p", new String[]{"A"}, new String[]{"p"}, "a", false, true, false, false);
	}

	/* disabled: input does not compile
	public void test_nonstatic_40() throws Exception{
//		printTestDisabledMessage("test for bug 77083");
		validatePassingTest("A", "Inner", "p", new String[]{"A"}, new String[]{"p"}, "a", false, true, false, false);
	}*/

	/* disabled:input does not compile
	public void test_nonstatic_41() throws Exception{
		validatePassingTest("A", "Inner", "p", new String[]{"A"}, new String[]{"p"}, "a", false, true, false, false);
	}*/

	/* disabled: by Eclipse
	public void test_nonstatic_42() throws Exception{
		printTestDisabledMessage("disabled due to missing support for statically imported methods");
//		validatePassingTest("A", "Inner", "p", new String[]{"A"}, new String[]{"p"}, "a", false, true, false, false);
	}*/

	// Using member of enclosing type, non-static edition
	/* disabled: visibility
	public void test_nonstatic_43() throws Exception{
		validatePassingTest("A", "Inner", "MoreInner", "p5", new String[]{"A"}, new String[]{"p5"}, "inner", true, true, true, true);
	}*/

	/* disabled: visibility
	// Move inner class and create field; enclosing class must be changed to use default visibility.
	public void test_nonstatic_44() throws Exception{
		validatePassingTest("A", "Inner", "MoreInner", "p2", new String[]{"A"}, new String[]{"p2"}, "p", true, true, false, true);
	}*/

	public void testFail_nonstatic_0() throws Exception{
		validateFailingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a");
	}
	
	/* disabled: we can do this
	public void testFail_nonstatic_1() throws Exception{
		validateFailingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a");
	}*/
	
	public void testFail_nonstatic_2() throws Exception{
		validateFailingTest("A", "Inner", new String[]{"A"}, new String[]{"p"}, "a");
	}

	public void testFail_nonstatic_3() throws Exception{
		validateFailingTest("Local", "NestedLocal", new String[]{"A"}, new String[]{"p"}, "a");
	}
	
	/* disabled: these tests are arguably for a different refactoring
	public void test_secondary_0() throws Exception {
		validatePassingTestSecondaryType("A", "Secondary", "p", new String[] { "A" }, new String[] { "p" }, null, false, false, false, false);
	}

	public void test_secondary_1() throws Exception {
		validatePassingTestSecondaryType("A", "Secondary", "p", new String[] { "A" }, new String[] { "p" }, null, false, false, false, false);
	}

	public void test_secondary_2() throws Exception {
		if (BUG_304827)
			return;
		validatePassingTestSecondaryType("A", "Secondary", "p", new String[] { "A" }, new String[] { "p" }, null, false, false, false, false);
	}

	public void test_secondary_3() throws Exception {
		validatePassingTestSecondaryType("A", "Secondary", "p", new String[] { "A", "S" }, new String[] { "p", "q" }, null, false, false, false, false);
	}

	public void test_secondary_4() throws Exception {
		if (BUG_304827)
			return;
		validatePassingTestSecondaryType("A", "Secondary", "p", new String[] { "A" }, new String[] { "p" }, null, false, false, false, false);
	}

	public void test_secondary_5() throws Exception {
		if (BUG_304827)
			return;
		validatePassingTestSecondaryType("A", "Secondary", "p", new String[] { "A" }, new String[] { "p" }, null, false, false, false, false);
	}

	public void test_secondary_6() throws Exception {
		if (BUG_304827)
			return;
		validatePassingTestSecondaryType("A", "Secondary", "p", new String[] { "A" }, new String[] { "p" }, null, false, false, false, false);
	}

	public void test_secondary_7() throws Exception {
		validatePassingTestSecondaryType("A", "Secondary", "p", new String[] { "A", "S", "T" }, new String[] { "p", "q", "q" }, null, false, false, false, false);
	}

	public void test_secondary_8() throws Exception {
		validatePassingTestSecondaryType("A", "Secondary", "p", new String[] { "A", "S", "T" }, new String[] { "p", "q", "q" }, null, false, false, false, false);
	}*/
}
