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
package tests.eclipse.RenameTemp;

import java.util.Iterator;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import tests.eclipse.PromoteTempToField.PromoteTempToFieldTests;
import AST.CompilationUnit;
import AST.FileRange;
import AST.ParameterDeclaration;
import AST.Program;
import AST.RefactoringException;
import AST.Variable;
import AST.VariableArityParameterDeclaration;
import AST.VariableDeclaration;

public class RenameTempTests extends TestCase {
	public RenameTempTests(String name){
		super(name);
	}

	private String getSimpleTestFileName(boolean canRename, boolean input){
		String fileName = "A_" + getName();
		if (canRename)
			fileName += input ? "_in": "_out";
		return fileName + ".java";
	}

	private String getTestFileName(boolean canRename, boolean input){
		String fileName= "tests/eclipse/RenameTemp/";
		fileName += (canRename ? "canRename/": "cannotRename/");
		return fileName + getSimpleTestFileName(canRename, input);
	}

	static final String SELECTION_START_HERALD= "/*[*/";
	static final String SELECTION_END_HERALD= "/*]*/";

	private FileRange findSelectionInSource(CompilationUnit cu) {
		FileRange begin = cu.findComment(SELECTION_START_HERALD);
		FileRange end = cu.findComment(SELECTION_END_HERALD);
		assertNotNull(begin);
		assertNotNull(end);
		return new FileRange(begin.filename, begin.el, begin.ec+1, end.sl, end.sc);
	}
	
	//------------
	private void helper1(String newName, boolean updateReferences) throws Exception {
		Program in = CompileHelper.compile(getTestFileName(true, true));
		Program out = CompileHelper.compile(getTestFileName(true, false));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
	
		CompilationUnit cu = null;
		for(Iterator<CompilationUnit> iter=in.compilationUnitIterator();iter.hasNext();) {
			CompilationUnit next = iter.next();
			if(next.fromSource())
				cu = next;
		}
		assertNotNull(cu);
		
		FileRange selection = findSelectionInSource(cu);
		Variable v = PromoteTempToFieldTests.findNode(cu, VariableDeclaration.class, selection.sl, selection.sc, selection.el, selection.ec-1);
		if(v == null)
			v = PromoteTempToFieldTests.findNode(in, ParameterDeclaration.class, selection.sl, selection.sc, selection.el, selection.ec-1);
		if(v == null)
			v = PromoteTempToFieldTests.findNode(in, VariableArityParameterDeclaration.class, selection.sl, selection.sc, selection.el, selection.ec-1);
		assertNotNull(v);
		
		try {
			v.rename(newName);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper1(String newName, boolean updateReferences, int startLine, int startColumn, int endLine, int endColumn) throws Exception{
		Program in = CompileHelper.compile(getTestFileName(true, true));
		Program out = CompileHelper.compile(getTestFileName(true, false));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
	
		Variable v = PromoteTempToFieldTests.findNode(in, VariableDeclaration.class, startLine, startColumn, endLine, endColumn-1);
		if(v == null)
			v = PromoteTempToFieldTests.findNode(in, ParameterDeclaration.class, startLine, startColumn, endLine, endColumn-1);
		assertNotNull(v);
		
		try {
			v.rename(newName);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper1(String newName) throws Exception{
		helper1(newName, true);
	}

	private void helper2(String newName, boolean updateReferences) throws Exception{
		Program in = CompileHelper.compile(getTestFileName(false, true));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
	
		CompilationUnit cu = null;
		for(Iterator<CompilationUnit> iter=in.compilationUnitIterator();iter.hasNext();) {
			CompilationUnit next = iter.next();
			if(next.fromSource())
				cu = next;
		}
		assertNotNull(cu);
		
		FileRange selection = findSelectionInSource(cu);
		Variable v = PromoteTempToFieldTests.findNode(cu, VariableDeclaration.class, selection.sl, selection.sc, selection.el, selection.ec-1);
		if(v == null)
			v = PromoteTempToFieldTests.findNode(in, ParameterDeclaration.class, selection.sl, selection.sc, selection.el, selection.ec-1);
		assertNotNull(v);
		
		try {
			v.rename(newName);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) {
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper2(String newName) throws Exception{
		helper2(newName, true);
	}

	public void test0() throws Exception{
		helper1("j");
	}

	public void test1() throws Exception{
		helper1("j");
	}

//	public void test2() throws Exception{
//		Map renaming= new HashMap();
//		renaming.put("x", "j");
//		renaming.put("y", "k");
//		helper1(renaming, new String[0]);
//	}

	public void test3() throws Exception{
		helper1("j1");
	}

	public void test4() throws Exception{
		helper1("k");
	}

	public void test5() throws Exception{
		helper1("k");
	}

	public void test6() throws Exception{
		helper1("k");
	}

	public void test7() throws Exception{
		helper1("k");
	}
//
//	//8, 9, 10 removed
//
//
	public void test11() throws Exception{
		helper1("j");
	}

	public void test12() throws Exception{
		helper1("j");
	}

	public void test13() throws Exception{
		helper1("j");
	}

	public void test14() throws Exception{
		helper1("j");
	}

// disabled
//	public void test15() throws Exception{
//		Map renaming= new HashMap();
//		renaming.put("i", "j");
//		renaming.put("j", "i");
//		helper1(renaming, new String[0]);
//	}
//
	public void test16() throws Exception{
		helper1("j");
	}

// disabled
//	public void test17() throws Exception{
//		Map renaming= new HashMap();
//		renaming.put("i", "j");
//		renaming.put("j", "i");
//		helper1(renaming, new String[0]);
//	}
//
	public void test18() throws Exception{
		helper1("j");
	}

	public void test19() throws Exception{
		helper1("j");
	}

	/* disabled: does not compile
	public void test20() throws Exception{
		helper1("j");
	}*/

	public void test21() throws Exception{
		helper1("j");
	}

	public void test22() throws Exception{
		helper1("j");
	}

//	disabled
//	public void test23() throws Exception{
//		Map renaming= new HashMap();
//		renaming.put("i", "j");
//		renaming.put("j", "i");
//		helper1(renaming, new String[0]);
//	}

	public void test24() throws Exception{
		helper1("j");
	}

	public void test25() throws Exception{
		helper1("j");
	}

	public void test26() throws Exception{
		helper1("j");
	}

//  deleted - incorrect. see testFail26
//	public void test27() throws Exception{
//		helper1("j");
//	}

	/* disabled: does not compile
	public void test28() throws Exception{
		helper1("j");
	}*/

	public void test29() throws Exception{
		helper1("b");
	}

	public void test30() throws Exception{
		helper1("k");
	}

	public void test31() throws Exception{
		helper1("kk");
	}

	public void test32() throws Exception{
		helper1("j");
	}

	/* disabled: tests idiosyncratic feature
	public void test33() throws Exception{
		helper1("b", false);
	}*/

	public void test34() throws Exception{
		helper1("j");
	}

	public void test35() throws Exception{
//		printTestDisabledMessage("regression test for bug#9001");
		helper1("test2");
	}

	public void test36() throws Exception{
//		printTestDisabledMessage("regression test for bug#7630");
		helper1("j", true, 5, 13, 5, 14);
	}

	public void test37() throws Exception{
//		printTestDisabledMessage("regression test for bug#7630");
		helper1("j", true, 5, 16, 5, 17);
	}

	public void test38() throws Exception{
//		printTestDisabledMessage("regression test for Bug#11453");
		helper1("i", true, 7, 12, 7, 13);
	}

	public void test39() throws Exception{
//		printTestDisabledMessage("regression test for Bug#11440");
		helper1("j", true, 7, 16, 7, 18);
	}

	public void test40() throws Exception{
//		printTestDisabledMessage("regression test for Bug#10660");
		helper1("j", true, 4, 16, 4, 17);
	}

	public void test41() throws Exception{
//		printTestDisabledMessage("regression test for Bug#10660");
		helper1("j", true, 3, 17, 3, 18);
	}

	public void test42() throws Exception{
//		printTestDisabledMessage("regression test for Bug#10660");
		helper1("j", true, 3, 25, 3, 26);
	}

	public void test43() throws Exception{
//		printTestDisabledMessage("regression test for Bug#10660");
		helper1("j", true, 4, 23, 4, 24);
	}

	public void test44() throws Exception{
//		printTestDisabledMessage("regression test for Bug#12200");
		helper1("j", true, 6, 11, 6, 14);
	}

	public void test45() throws Exception{
//		printTestDisabledMessage("regression test for Bug#12210");
		helper1("j", true, 4, 14, 4, 14);
	}

	public void test46() throws Exception{
//		printTestDisabledMessage("regression test for Bug#12210");
		helper1("j", true, 4, 18, 4, 18);
	}

	public void test47() throws Exception{
//		printTestDisabledMessage("regression test for Bug#17922");
		helper1("newname", true, 7, 13, 7, 17);
	}

	public void test48() throws Exception{
//		printTestDisabledMessage("regression test for Bug#22938");
		helper1("newname", true, 4, 16, 4, 20);
	}

	public void test49() throws Exception{
//		printTestDisabledMessage("regression test for Bug#30923 ");
		helper1("newname", true, 4, 16, 4, 20);
	}

	public void test50() throws Exception{
//		printTestDisabledMessage("regression test for Bug#30923 ");
		helper1("newname", true, 4, 16, 4, 20);
	}

	/* disabled: does not compile
	public void test51() throws Exception {
//		printTestDisabledMessage("regression test for Bug#47822");
		helper1("qwerty", true, 5, 19, 5, 20);
	}*/

	public void test52() throws Exception{
		helper1("j");
	}

	/* disabled: does not compile
	public void test53() throws Exception{
//		printTestDisabledMessage("bug#19851");
		helper1("locker");
	}*/

	public void test54() throws Exception{
		helper1("obj");
	}

	public void test55() throws Exception{
		helper1("t");
	}

	public void test56() throws Exception{
		helper1("param");
	}

	/* disabled: does not compile
	public void test57() throws Exception{
		helper1("param");
	}*/

	public void test58() throws Exception{
		helper1("param");
	}

	public void test59() throws Exception{
		helper1("thing");
	}

	public void test60() throws Exception{
		helper1("param");
	}

	/* disabled: does not compile
	public void test61() throws Exception{
		helper1("x");
	}*/

	/* disabled: does not compile
	public void test62() throws Exception {
//		printTestDisabledMessage("bug#47822");
		helper1("xxx");
	}*/

	/* disabled: does not compile
	public void test63() throws Exception {
		// regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=144426
		helper1("xxx");
	}*/

	/* disabled: does not compile
	public void test64() throws Exception {
		// regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=144426
		helper1("xxx");
	}*/
	
// -----
	public void testFail0() throws Exception{
		helper2("j");
	}

	/* disabled: tests idiosyncratic feature
	public void testFail1() throws Exception{
		failHelperNoElement();
	}*/

	/* disabled: does not compile
	public void testFail2() throws Exception{
		helper2("i");
	}*/

	public void testFail3() throws Exception{
		helper2("9");
	}

	public void testFail4() throws Exception{
		helper2("j");
	}

	public void testFail5() throws Exception{
		helper2("j");
	}

	public void testFail6() throws Exception{
		helper2("j");
	}

	/* disabled: we can do this
	public void testFail7() throws Exception{
		helper2("j");
	}*/

	/* disabled: we can do this
	public void testFail8() throws Exception{
		helper2("j");
	}*/

	/* disabled: we can do this
	public void testFail9() throws Exception{
		helper2("j");
	}*/

	/* disabled: tests idiosyncratic feature
	public void testFail10() throws Exception{
		failHelperNoElement();
	}*/

// disabled - it's allowed now
//	public void testFail11() throws Exception{
//		helper2("uu");
//	}

	public void testFail12() throws Exception{
//		printTestDisabledMessage("http://dev.eclipse.org/bugs/show_bug.cgi?id=11638");
		helper2("j");
	}

	/* disabled: we can do this
	public void testFail13() throws Exception{
		helper2("j");
	}*/

	/* disabled: we can do this
	public void testFail14() throws Exception{
		helper2("j");
	}*/

	public void testFail15() throws Exception{
		helper2("j");
	}

	public void testFail16() throws Exception{
		helper2("j");
	}

	/* disabled: does not compile
	public void testFail17() throws Exception{
		helper2("j");
	}*/

	/* disabled: we can do this
	public void testFail18() throws Exception{
		helper2("j");
	}*/

	/* disabled: we can do this
	public void testFail19() throws Exception{
		helper2("j");
	}*/

	/* disabled: we can do this
	public void testFail20() throws Exception{
		helper2("j");
	}*/

// disabled - it's allowed now
//	public void testFail21() throws Exception{
//		helper2("j");
//	}

	/* disabled: tests idiosyncratic feature
	public void testFail22() throws Exception{
		failHelperNoElement();
	}*/

// disabled - it's allowed now
//	public void testFail23() throws Exception{
//		helper2("j");
//	}

	/* disabled: does not compile
	public void testFail24() throws Exception{
		//printTestDisabledMessage("compile errors are ok now");
		helper2("t"); //name collision
	}*/

	public void testFail25() throws Exception{
		helper2("j");
	}

	public void testFail26() throws Exception{
		helper2("j");
	}

	public void testFail27() throws Exception{
		helper2("j");
	}

	public void testFail28() throws Exception{
		helper2("j");
	}

}
