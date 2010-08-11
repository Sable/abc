/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package tests.eclipse.MoveMembers;

import java.util.Collection;
import java.util.LinkedList;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.FieldDeclaration;
import AST.MemberDecl;
import AST.MemberTypeDecl;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class MoveMembersTests extends TestCase {

	public MoveMembersTests(String name) {
		super(name);
	}

	/* Move members from A to B */
	private void fieldMethodTypeHelper(String[] fieldNames, String[] methodNames, String[][] signatures, String[] typeNames, boolean succeed, String AName, String BName) throws Exception {
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/MoveMembers/"+getName()+"/in");
		Program out = succeed ? CompileHelper.compileAllJavaFilesUnder("tests/eclipse/MoveMembers/"+getName()+"/out") : null;
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertTrue(!succeed || out != null);
		TypeDecl A = in.findSimpleType(AName);
		TypeDecl B = in.findSimpleType(BName);
		assertNotNull(A);
		assertNotNull(B);
		Collection<MemberDecl> members = new LinkedList<MemberDecl>();
		for(String fn : fieldNames) {
			FieldDeclaration fd = A.findField(fn);
			assertNotNull(fd);
			members.add(fd);
		}
		for(String mn : methodNames) {
			MethodDecl md = A.findMethod(mn);
			assertNotNull(md);
			members.add(md);
		}
		for(String tn : typeNames) {
			TypeDecl td = A.findSimpleType(tn);
			assertNotNull(td);
			assertTrue(td.getParent() instanceof MemberTypeDecl);
			members.add((MemberTypeDecl)td.getParent());
		}
		try {
			A.doMoveMembers(members, B);
			if(succeed)
				assertEquals(out.toString(), in.toString());
			else
				assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) {
			if(succeed)
				assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void fieldMethodTypeHelper_passing(String[] fieldNames, String[] methodNames, String[][] signatures, String[] typeNames, boolean addDelegates) throws Exception{
		fieldMethodTypeHelper(fieldNames, methodNames, signatures, typeNames, true, "A", "B");
	}

	private void fieldMethodTypeHelper_failing(String[] fieldNames, String[] methodNames, String[][] signatures, String[] typeNames, String BName) throws Exception{
		fieldMethodTypeHelper(fieldNames, methodNames, signatures, typeNames, false, "A", BName);
	}

	private void fieldHelper_passing(String[] fieldNames) throws Exception {
		fieldMethodTypeHelper_passing(fieldNames, new String[0], new String[0][0], new String[0], false);
	}

	private void fieldHelperDelegate_passing(String[] fieldNames) throws Exception {
		fieldMethodTypeHelper_passing(fieldNames, new String[0], new String[0][0], new String[0], true);
	}

	private void methodHelper_passing(String[] methodNames, String[][] signatures) throws Exception {
		fieldMethodTypeHelper_passing(new String[0], methodNames, signatures, new String[0], false);
	}

	private void methodHelperDelegate_passing(String[] methodNames, String[][] signatures) throws Exception {
		fieldMethodTypeHelper_passing(new String[0], methodNames, signatures, new String[0], true);
	}

	private void typeHelper_passing(String[] typeNames) throws Exception {
		fieldMethodTypeHelper_passing(new String[0], new String[0], new String[0][0], typeNames, false);
	}

	public void test0() throws Exception{
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	public void test1() throws Exception{
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	public void test2() throws Exception{
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	public void test3() throws Exception{
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	public void test4() throws Exception{
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	public void test5() throws Exception{
		fieldHelper_passing(new String[]{"f"});
	}

	public void test6() throws Exception{
		fieldHelper_passing(new String[]{"f"});
	}

	public void test7() throws Exception{
		fieldHelper_passing(new String[]{"f"});
	}

	public void test8() throws Exception{
		fieldHelper_passing(new String[]{"f"});
	}

	public void test9() throws Exception{
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	public void test10() throws Exception{
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	public void test11() throws Exception{
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	public void test12() throws Exception{
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	public void test13() throws Exception{
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	public void test14() throws Exception{
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	public void test15() throws Exception{
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	public void test16() throws Exception{
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	public void test17() throws Exception{
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	public void test18() throws Exception{
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	public void test19() throws Exception{
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	public void test20() throws Exception{
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	/* disabled: by Eclipse
	public void test21() throws Exception{
		printTestDisabledMessage("not currently handling visibility issues induced by moving more than one static member");
		//fieldHelper_passing(new String[]{"F", "i"});
	}*/

	public void test22() throws Exception{
		fieldHelper_passing(new String[]{"i"});
	}

	public void test23() throws Exception{
		fieldHelper_passing(new String[]{"FRED"});
	}

	public void test24() throws Exception{
		fieldHelper_passing(new String[]{"FRED"});
	}

	public void test25() throws Exception{
		//printTestDisabledMessage("test for 27098");
		fieldHelper_passing(new String[]{"FRED"});
	}

	public void test26() throws Exception{
		fieldMethodTypeHelper_passing(new String[0], new String[]{"n"}, new String[][]{new String[0]}, new String[0], false);
	}

	public void test27() throws Exception{
		fieldMethodTypeHelper_passing(new String[0], new String[]{"n"}, new String[][]{new String[0]}, new String[0], false);
	}

	public void test28() throws Exception{
		methodHelper_passing(new String[]{"m", "n"}, new String[][]{new String[0], new String[0]});
	}

	public void test29() throws Exception{ //test for bug 41691
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	public void test30() throws Exception{ //test for bug 41691
		fieldHelper_passing(new String[]{"id"});
	}

	public void test31() throws Exception{ //test for bug 41691
		fieldHelper_passing(new String[]{"odd"});
	}

	/* disabled: by Eclipse
	public void test32() throws Exception{ //test for bug 41734, 41691
		printTestDisabledMessage("test for 41734");
		//methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}*/

	public void test33() throws Exception{ //test for bug 28022
		fieldHelper_passing(new String[]{"i"});
	}

	public void test34() throws Exception{ //test for bug 28022
		fieldHelper_passing(new String[]{"i"});
	}

	public void test35() throws Exception{ //test for bug 28022
		fieldHelper_passing(new String[]{"i"});
	}

	//-- move types:

	public void test36() throws Exception {
		typeHelper_passing(new String[]{"I"});
	}

	/* disabled: by Eclipse
	public void test37() throws Exception {
		//printTestDisabledMessage("qualified access to source");
		typeHelper_passing(new String[] {"Inner"});
	}*/

	public void test38() throws Exception {
		fieldMethodTypeHelper(new String[0], new String[0], new String[0][0], new String[]{"Inner"}, true, "A", "B");
	}

	/* disabled: by Eclipse
	public void test39() throws Exception {
		printTestDisabledMessage("complex imports - need more work");
//		fieldMethodType3CUsHelper_passing(new String[0], new String[0], new String[0][0],
//							new String[]{"Inner"});
	}*/

	public void test40() throws Exception{
		fieldMethodTypeHelper_passing(new String[] {"f"}, new String[]{"m"}, new String[][]{new String[0]}, new String[0], false);
	}

	public void test41() throws Exception{
		methodHelper_passing(new String[] {"m"}, new String[][]{new String[0]});
	}

	//-- Visibility issues in the moved member:

	/* disabled: no support for adjusting visibility
	public void test42() throws Exception{
		//former testFail9
		//Tests move of public static method m, which references private method f, into same package.
		methodHelper_passing(new String[] {"m"}, new String[][]{new String[0]});
	}

	public void test43() throws Exception{
		//former testFail10
		//Tests move of public static method m, which references private field F, into same package
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	public void test44() throws Exception{
		//former testFail11
		//Tests move of public static field i, which references private field F, into same package
		fieldHelper_passing(new String[]{"i"});
	}

	public void test45() throws Exception{
		//former testFail12
		//Tests move of public static field i, which references private method m, into same package
		fieldHelper_passing(new String[]{"i"});
	}

	public void test46() throws Exception{
		//former testFail13
		//Tests move of public static method m, which gets referenced by a field, into same package
		methodHelper_passing(new String[]{"m"}, new String[][]{new String[0]});
	}

	public void test47() throws Exception{
		//former testFail14
		//Tests move of public static field i, which gets referenced by a field, into same package
		fieldHelper_passing(new String[]{"i"});
	}

	public void test48() throws Exception{
		//Move private unused method which calls another private method into another package
		IPackageFragment packageForB= null;
		try{
			packageForB= getRoot().createPackageFragment("r", false, null);
			fieldMethodTypePackageHelper_passing(new String[0], new String[]{"bar"}, new String[][]{new String[0]}, new String[0], getPackageP(), packageForB, false);
		} finally{
			performDummySearch();
			if (packageForB != null)
				packageForB.delete(true, null);
		}
	}

	// --- Visibility issues of the moved member itself

	public void test49() throws Exception{
		//Move protected used field into another package
		IPackageFragment packageForB= null;
		try{
			packageForB= getRoot().createPackageFragment("r", false, null);
			fieldMethodTypePackageHelper_passing(new String[]{"someVar"}, new String[0], new String[][]{new String[0]}, new String[0], getPackageP(), packageForB, false);
		} finally{
			performDummySearch();
			if (packageForB != null)
				packageForB.delete(true, null);
		}
	}

	public void test50() throws Exception{
		//Move private used method into subtype.
		methodHelper_passing(new String[]{"foo"}, new String[][]{new String[0]});
	}

	public void test51() throws Exception {
		//Move private static inner class with private field (but used in outer class)
		//assure both class and field get their visibility increased
		typeHelper_passing(new String[] { "Inner" });
	}

	public void test52() throws Exception {
		// assure moved unused field keeps its visibility
		fieldHelper_passing(new String[] { "a" });
	}

	public void test53() throws Exception {
		// assure moved unusued class keeps its visibility
		typeHelper_passing(new String[] { "C" });
	}

	public void test54() throws Exception {
		// moved used method is changed in visibility
		methodHelper_passing(new String[] { "b" }, new String[][]{new String[0]});
	}

	public void test55() throws Exception {
		// moved used method is changed in visibility
		typeHelper_passing(new String[] { "C" });
	}

	// --- Visibility of members of the moved type

	public void test56() throws Exception {
		// Move an inner class with two USED members
		typeHelper_passing(new String[] { "Inner" });
	}

	public void test57() throws Exception {
		// Move an inner class with two UNUSED members
		typeHelper_passing(new String[] { "Inner" });
	}

	// --- Visibility of used outer members

	public void test58() throws Exception {
		// Move a type which references a field in an enclosing type
		// and a field in a sibling
		typeHelper_passing(new String[] { "Inner" });
	}

	public void test59() throws Exception {
		// Move a type which references a field in an enclosing type,
		// and the enclosing type is private
		typeHelper_passing(new String[] { "SomeInner.Inner" });
	}

	public void test60() throws Exception{
		// Move a static private "getter" of a static field into another class
		// only the field should be changed to public (bug 122490)
		IPackageFragment packageForB= null;
		try{
			packageForB= getRoot().createPackageFragment("e", false, null);
			fieldMethodTypePackageHelper_passing(new String[0], new String[] { "getNAME" }, new String[][]{new String[0]}, new String[0], getPackageP(), packageForB, false);
		} finally{
			performDummySearch();
			if (packageForB != null)
				packageForB.delete(true, null);
		}
	}

	public void test61() throws Exception{
		// Move some method which references a field with a getter and a setter
		// only the field should be changed to public (bug 122490)
		IPackageFragment packageForB= null;
		try{
			packageForB= getRoot().createPackageFragment("e", false, null);
			fieldMethodTypePackageHelper_passing(new String[0], new String[] { "foo" }, new String[][]{new String[0]}, new String[0], getPackageP(), packageForB, false);
		} finally{
			performDummySearch();
			if (packageForB != null)
				packageForB.delete(true, null);
		}
	}*/

	// parameterized type references


	public void test62() throws Exception {
		// Move a type which references a field in an enclosing type
		// and a field in a sibling
		typeHelper_passing(new String[] { "SomeInner" });
	}

	//---
	/* disabled: we can do this
	public void testFail0() throws Exception{
		fieldMethodTypeHelper_failing(new String[0],
									  new String[]{"m"}, new String[][]{new String[0]},
									  new String[0], "X");
	}*/


	public void testFail1() throws Exception{
		fieldMethodTypeHelper_failing(new String[0],
									  new String[]{"m"}, new String[][]{new String[0]},
									  new String[0], "X");
	}

	public void testFail2() throws Exception{
		fieldMethodTypeHelper_failing(new String[0],
									  new String[]{"m"}, new String[][]{new String[0]},
									  new String[0], "B");
	}

	public void testFail3() throws Exception{
		fieldMethodTypeHelper_failing(new String[0],
									  new String[]{"m"}, new String[][]{new String[]{"I", "I"}},
									  new String[0], "B");
	}

	/* disabled: we can do this
	public void testFail4() throws Exception{
		fieldMethodTypeHelper_failing(new String[0],
									  new String[]{"m"}, new String[][]{new String[]{"I", "I"}},
									  new String[0], "B");
	}*/

	/* disabled: we can do this
	public void testFail5() throws Exception{
		fieldMethodTypeHelper_failing(new String[0],
									  new String[]{"m"}, new String[][]{new String[]{"I", "I"}},
									  new String[0], "B");
	}*/

	public void testFail6() throws Exception{
		fieldMethodTypeHelper_failing(new String[]{"i"}, new String[0], new String[0][0], new String[0], "B");
	}

	public void testFail7() throws Exception{
		fieldMethodTypeHelper_failing(new String[]{"i"}, new String[0], new String[0][0], new String[0], "B");
	}

	public void testFail8() throws Exception{
		fieldMethodTypeHelper_failing(new String[]{"i"}, new String[0], new String[0][0], new String[0], "B");
	}

	public void testFail15() throws Exception{
		fieldMethodTypeHelper_failing(new String[0],
									  new String[]{"m"}, new String[][]{new String[0]}, new String[0], "B");
	}

	public void testFail16() throws Exception{
		fieldMethodTypeHelper_failing(new String[]{"f"}, new String[0], new String[0][0], new String[0], "B");
	}

	/* disabled: we can do this
	public void testFail17() throws Exception{
		fieldMethodTypeHelper_failing(new String[0],
									  new String[]{"m"}, new String[][]{new String[0]}, new String[0], "X");
	}*/

	/* disabled: we can do this
	public void testFail18() throws Exception{
		fieldMethodTypeHelper_failing(new String[0],
									  new String[]{"m"}, new String[][]{new String[0]}, new String[0], "X");
	}*/

	public void testFail19() throws Exception{
		fieldMethodTypeHelper_failing(new String[0],
									  new String[]{"m"}, new String[][]{new String[0]}, new String[0], "B");
	}

	public void testFail20() throws Exception{
		// was same as test19
	}

	/* disabled: does not compile
	public void testFail21() throws Exception{
		fieldMethodTypeHelper_failing(new String[0],
									  new String[]{"m"}, new String[][]{new String[0]}, new String[0], "X");
	}*/

	public void testFail22() throws Exception{
		//free slot
	}

	public void testFail23() throws Exception{
		//free slot
	}

	public void testFail24() throws Exception{
		fieldMethodTypeHelper_failing(new String[0],
									  new String[]{"m"}, new String[][]{new String[0]}, new String[0], "B");
	}

	// Delegate creation

	/* disabled: no support for delegates
	public void testDelegate01() throws Exception {
		// simple delegate method
		methodHelperDelegate_passing(new String[] { "foo" }, new String[][]{new String[0]});
	}

	public void testDelegate02() throws Exception {
		// increase visibility
		methodHelperDelegate_passing(new String[] { "foo" }, new String[][]{new String[0]});
	}

	public void testDelegate03() throws Exception {
		// ensure imports are removed correctly
		methodHelperDelegate_passing(new String[] { "foo" }, new String[][]{new String[0]});
	}

	public void testDelegate04() throws Exception{
		// add import when moving to another package
		IPackageFragment packageForB= null;
		try{
			packageForB= getRoot().createPackageFragment("r", false, null);
			fieldMethodTypePackageHelper_passing(new String[0], new String[] { "foo" }, new String[][]{new String[0]}, new String[0], getPackageP(), packageForB, true);
		} finally{
			performDummySearch();
			if (packageForB != null)
				packageForB.delete(true, null);
		}
	}

	public void testDelegate05() throws Exception {
		// simple delegate field
		fieldHelperDelegate_passing(new String[] { "FOO" });
	}

	public void testDelegate06() throws Exception {
		// increase visibility
		fieldHelperDelegate_passing(new String[] { "FOO" });
	}

	public void testDelegate07() throws Exception {
		// remove imports correctly
		fieldHelperDelegate_passing(new String[] { "FOO" });
	}

	public void testDelegate08() throws Exception{
		// add import when moving to another package
		IPackageFragment packageForB= null;
		try{
			packageForB= getRoot().createPackageFragment("r", false, null);
			fieldMethodTypePackageHelper_passing(new String[] { "FOO" }, new String[0], new String[][]{new String[0]}, new String[0], getPackageP(), packageForB, true);
		} finally{
			performDummySearch();
			if (packageForB != null)
				packageForB.delete(true, null);
		}
	}*/
}
