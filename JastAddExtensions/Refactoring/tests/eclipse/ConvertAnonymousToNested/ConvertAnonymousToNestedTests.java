/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     N.Metchev@teamphone.com - contributed fixes for
 *     - convert anonymous to nested should sometimes declare class as static [refactoring]
 *       (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=43360)
 *******************************************************************************/
package tests.eclipse.ConvertAnonymousToNested;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.ASTNode;
import AST.AnonymousDecl;
import AST.Program;
import AST.RefactoringException;

public class ConvertAnonymousToNestedTests extends TestCase {
	public ConvertAnonymousToNestedTests(String name) {
		super(name);
	}

	private String getSimpleTestFileName(boolean canInline, boolean input){
		String fileName = "A_" + getName();
		if (canInline)
			fileName += input ? "_in": "_out";
		return fileName + ".java";
	}

	private String getTestFileName(boolean canConvert, boolean input){
		String fileName= "tests/eclipse/ConvertAnonymousToNested/";
		fileName += (canConvert ? "canConvert/": "cannotConvert/");
		return fileName + getSimpleTestFileName(canConvert, input);
	}
	
	private AnonymousDecl findAnonymous(ASTNode p, int startLine, int startColumn, int endLine, int endColumn) {
		if(p == null)
			return null;
		for(int i=0;i<p.getNumChild();++i) {
			AnonymousDecl res = findAnonymous(p.getChild(i), startLine, startColumn, endLine, endColumn);
			if(res != null)
				return res;
		}
		if(p instanceof AnonymousDecl) {
			int start = p.getParent().getStart(), end = p.getParent().getEnd();
			int pstartLine = ASTNode.getLine(start), pstartColumn = ASTNode.getColumn(start),
				pendLine = ASTNode.getLine(end), pendColumn = ASTNode.getColumn(end);
			if((pstartLine < startLine ||
				pstartLine == startLine && pstartColumn <= startColumn) &&
			   (pendLine > endLine ||
				pendLine == endLine && pendColumn >= endColumn))
				return (AnonymousDecl)p;
		}
		return null;
	}

	private void helper1(int startLine, int startColumn, int endLine, int endColumn, boolean makeFinal, String className, boolean makePublic) throws Exception {
		Program in = CompileHelper.compile(getTestFileName(true, true));
		Program out = CompileHelper.compile(getTestFileName(true, false));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		AnonymousDecl anon = findAnonymous(in, startLine, startColumn, endLine, endColumn);
		assertNotNull(anon);
		try {
			anon.doPromoteToMemberClass(className, makeFinal, makePublic);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.toString());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper1(int startLine, int startColumn, int endLine, int endColumn, boolean makeFinal, boolean makeStatic, String className, boolean makePublic) throws Exception{
		Program in = CompileHelper.compile(getTestFileName(true, true));
		Program out = CompileHelper.compile(getTestFileName(true, false));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		AnonymousDecl anon = findAnonymous(in, startLine, startColumn, endLine, endColumn);
		assertNotNull(anon);
		try {
			anon.doPromoteToMemberClass(className, makeFinal, makePublic);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.toString());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void failHelper1(int startLine, int startColumn, int endLine, int endColumn, boolean makeFinal, String className, boolean makePublic) throws Exception{
		Program in = CompileHelper.compile(getTestFileName(false, true));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		AnonymousDecl anon = findAnonymous(in, startLine, startColumn, endLine, endColumn);
		assertNotNull(anon);
		try {
			anon.doPromoteToMemberClass(className, makeFinal, makePublic);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) {
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void failActivationHelper(int startLine, int startColumn, int endLine, int endColumn) throws Exception {
		Program in = CompileHelper.compile(getTestFileName(false, true));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		AnonymousDecl anon = findAnonymous(in, startLine, startColumn, endLine, endColumn);
		assertNotNull(anon);
		try {
			anon.doPromoteToMemberClass("Inner", true, false);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) {
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	//--- TESTS

	/* disabled: by Eclipse
	public void testFail0() throws Exception{
		printTestDisabledMessage("corner case - local types");
//		failHelper1(6, 14, 6, 16, true, "Inner", Modifier.PRIVATE, RefactoringStatus.FATAL);
	}*/

	public void testFail1() throws Exception{
		failHelper1(5, 17, 5, 17, true, "Inner", false);
	}

	// adjusted position -- MS
	public void testFail2() throws Exception{
		failHelper1(5, 22, 5, 22, true, "Inner", false);
	}

	/* disabled: does not compile
	public void testFail3() throws Exception{
		failActivationHelper(13, 27, 13, 27);
	} */

	/* disabled: we can do this
	public void testFail4() throws Exception{
	    failHelper1(8, 31, 8, 31, true, "Inner", false);
	}*/

	// adjusted position -- MS
	public void test0() throws Exception{
		helper1(5, 10, 5, 11, true, "Inner", false);
	}

	// adjusted position -- MS
	public void test1() throws Exception{
		helper1(5, 10, 5, 11, true, "Inner", true);
	}

	// adjusted position -- MS
	public void test2() throws Exception{
		helper1(5, 10, 5, 11, true, "Inner", true);
	}

	// adjusted position -- MS
	public void test3() throws Exception{
		helper1(5, 10, 5, 11, false, "Inner", true);
	}

	// adjusted position -- MS
	public void test4() throws Exception{
		helper1(7, 10, 7, 11, true, "Inner", false);
	}

	// adjusted position -- MS
	public void test5() throws Exception{
		helper1(7, 11, 7, 12, true, "Inner", false);
	}

	public void test6() throws Exception{
		helper1(8, 13, 9, 14, true, "Inner", false);
	}

	public void test7() throws Exception{
		helper1(7, 18, 7, 18, true, "Inner", false);
	}

	public void test8() throws Exception{
		helper1(8, 14, 8, 15, true, "Inner", false);
	}

	public void test9() throws Exception{
		helper1(8, 13, 8, 14, true, "Inner", false);
	}

	public void test10() throws Exception{
		helper1(7, 13, 7, 14, true, "Inner", false);
	}

	public void test11() throws Exception{
		helper1(5, 15, 5, 17, true, "Inner", false);
	}

	// adjusted position -- MS
	public void test12() throws Exception{
		helper1(8, 11, 10, 3, true, "Inner", false);
	}

	public void test13() throws Exception{
		helper1(6, 28, 6, 28, true, "Inner", false);
	}

	// adjusted position -- MS
	public void test14() throws Exception{
		helper1(5, 15, 5, 16, true, "Inner", false);
	}

	public void test15() throws Exception{
		helper1(7, 26, 7, 26, true, "Inner", false);
	}

	// adjusted position -- MS
	public void test16() throws Exception{
		helper1(4, 19, 8, 3, true, "Inner", false);
	}

	public void test17() throws Exception{
		helper1(6, 14, 6, 15, true, "Inner", false);
	}

	public void test18() throws Exception{
		helper1(5, 15, 5, 17, true, "Inner", false);
	}

	public void test19() throws Exception{
		helper1(5, 12, 6, 21, true, "Inner", false);
	}

	public void test20() throws Exception{
		helper1(4, 25, 4, 25, true, "Inner", false);
	}

    public void test21() throws Exception{
        helper1(4, 25, 4, 25, true, "Inner", false);
    }

    public void test22() throws Exception{
        /* disabled: differing interpretation
    	helper1(9, 34, 9, 34, true, "Inner", false);*/
    }

    // adjusted position -- MS
    public void test23() throws Exception{
        /* disabled: differing interpretation
    	helper1(6, 20, 6, 21, true, "Inner", false);*/
    }

    public void test24() throws Exception{
    	helper1(3, 26, 3, 26, true, "Inner", false);
    }

    public void test25() throws Exception{
    	/* disabled: differing interpretation
    	helper1(8, 28, 8, 28, true, "Inner", false);*/
    }

    public void test26() throws Exception{
        /* disabled: differing interpretation
    	helper1(8, 28, 8, 28, true, "Inner", false);*/
    }

    public void test27() throws Exception{
        /* disabled: differing interpretation
    	helper1(11, 39, 11, 39, true, "Inner", false);*/
    }

    public void test28() throws Exception{
//        printTestDisabledMessage("disabled: bug 43360");
        /* disabled: differing interpretation
    	helper1(10, 17, 10, 17, true, "Inner", false);*/
    }

	// adjusted position -- MS
    public void test29() throws Exception{
    		helper1(6, 18, 6, 18, true, "Inner", false);
    }

    /* disabled: does not compile
    public void test30() throws Exception{ // 2 syntax errors
    	helper1(5, 32, 5, 32, true, true, "Greeter", false);
    }*/

	// adjusted position -- MS
	public void testGenerics0() throws Exception{
		helper1(5, 13, 5, 13, true, "Inner", false);
	}

	// adjusted position -- MS
	public void testGenerics1() throws Exception{
		helper1(5, 13, 5, 13, true, "Inner", true);
	}

	// adjusted position -- MS
	public void testGenerics2() throws Exception{
		helper1(5, 13, 5, 13, true, "Inner", true);
	}

	// adjusted position -- MS
	public void testGenerics3() throws Exception{
		helper1(5, 13, 5, 13, false, "Inner", true);
	}

	// adjusted position -- MS
	public void testGenerics4() throws Exception{
		helper1(7, 13, 7, 13, true, "Inner", false);
	}

	// adjusted position -- MS
	public void testGenerics5() throws Exception{
		helper1(7, 13, 7, 13, true, "Inner", false);
	}

	/* disabled: no support for making new type static
	public void testGenerics6() throws Exception{
		helper1(7, 20, 7, 20, true, true, "Inner", false);
	}*/

	// adjusted position -- MS
    public void test31() throws Exception{ // for bug 181054
    	helper1(10, 28, 10, 28, true, false, "Inner1Extension", false);
    }

    /* disabled: JastAddJ bug
    public void test32() throws Exception{ // for bug 158028
    	helper1(10, 30, 10, 36, true, false, "Inner1Extension", false);
    }*/
}
