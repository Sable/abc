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
package tests.eclipse.PromoteTempToField;

import static AST.ASTNode.VIS_PRIVATE;
import static AST.ASTNode.VIS_PUBLIC;
import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.ASTNode;
import AST.Program;
import AST.RefactoringException;
import AST.VariableDeclaration;

public class PromoteTempToFieldTests extends TestCase {
	
	static class PromoteTempToFieldRefactoring {
		public static final int INITIALIZE_IN_FIELD = 0;
		public static final int INITIALIZE_IN_CONSTRUCTOR = 1;
		public static final int INITIALIZE_IN_METHOD = 2;
	}

	public PromoteTempToFieldTests(String name){
		super(name);
	}

	private String getSimpleTestFileName(boolean canRename, boolean input){
		String fileName = "A_" + getName();
		if (canRename)
			fileName += input ? "_in": "_out";
		return fileName + ".java";
	}

	private String getTestFileName(boolean canRename, boolean input){
		String fileName= "tests/eclipse/PromoteTempToField/";
		fileName += (canRename ? "canPromote/": "cannotPromote/");
		return fileName + getSimpleTestFileName(canRename, input);
	}

	public static <T extends ASTNode> T findNode(ASTNode p, Class<T> clazz, int startLine, int startColumn, int endLine, int endColumn) {
		if(p == null)
			return null;
		for(int i=0;i<p.getNumChild();++i) {
			T res = findNode(p.getChild(i), clazz, startLine, startColumn, endLine, endColumn);
			if(res != null)
				return res;
		}
		if(p.getClass().equals(clazz)) {
			int pstart = p.getStart(),
				pend = p.getEnd();
			int pstartLine = ASTNode.getLine(pstart),
				pstartColumn = ASTNode.getColumn(pstart),
				pendLine = ASTNode.getLine(pend),
				pendColumn = ASTNode.getColumn(pend);
			if((pstartLine < startLine || pstartLine == startLine && pstartColumn <= startColumn)
					&&
				(endLine < pendLine || endLine == pendLine && endColumn <= pendColumn))
				return (T)p;
		}
		return null;
	}


	//------------
	private void passHelper(int startLine, int startColumn, int endLine, int endColumn,
						  String newName,
						  boolean declareStatic,
						  boolean declareFinal,
						  int initializeIn,
						  int accessModifier) throws Exception{
		Program in = CompileHelper.compile(getTestFileName(true, true));
		Program out = CompileHelper.compile(getTestFileName(true, false));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		
		VariableDeclaration decl = findNode(in, VariableDeclaration.class, startLine, startColumn, endLine, endColumn);
		assertNotNull(decl);
		
		assertTrue(initializeIn == PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD);
		
		try {
			decl.rename(newName);
			decl.doPromoteToField(accessModifier);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void failHelper(int startLine, int startColumn, int endLine, int endColumn,
						  String newName,
						  boolean declareStatic,
						  boolean declareFinal,
						  int initializeIn,
						  int accessModifier) throws Exception{
		Program in = CompileHelper.compile(getTestFileName(false, true));
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		
		VariableDeclaration decl = findNode(in, VariableDeclaration.class, startLine, startColumn, endLine, endColumn);
		if(decl == null)
			return;
		
		assertTrue(initializeIn == PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD);
		
		try {
			decl.rename(newName);
			decl.doPromoteToField(accessModifier);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) {
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	///---------------------- tests -------------------------//

	/* disabled: tests implementation detail
	public void testEnablement0() throws Exception{
        boolean expectedCanEnableInitInConstructors	= true;
        boolean expectedCanEnableInitInMethod			= true;
        boolean expectedCanEnableInitInField			= true;
        boolean expectedCanEnableSettingStatic			= true;
        boolean expectedCanEnableSettingFinal			= true;

        String newName= "i";
		boolean declareStatic = false;
	  	boolean declareFinal= false;
	  	int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_FIELD;
	  	int accessModifier= VIS_PRIVATE;

		enablementHelper(5, 13, 5, 14, newName, declareStatic, declareFinal, initializeIn, accessModifier,
					expectedCanEnableSettingFinal, expectedCanEnableSettingStatic, expectedCanEnableInitInField, expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
	}

	public void testEnablement1() throws Exception{
        boolean expectedCanEnableInitInConstructors	= false;
        boolean expectedCanEnableInitInMethod			= false;
        boolean expectedCanEnableInitInField			= false;
        boolean expectedCanEnableSettingStatic			= true;
        boolean expectedCanEnableSettingFinal			= false;
		enablementHelper1(5, 13, 5, 14, expectedCanEnableSettingFinal, expectedCanEnableSettingStatic, expectedCanEnableInitInField, expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
	}

	public void testEnablement2() throws Exception{
        boolean expectedCanEnableInitInConstructors	= false;
        boolean expectedCanEnableInitInMethod			= false;
        boolean expectedCanEnableInitInField			= false;
        boolean expectedCanEnableSettingStatic			= true;
        boolean expectedCanEnableSettingFinal			= false;
		enablementHelper1(5, 13, 5, 14, expectedCanEnableSettingFinal, expectedCanEnableSettingStatic, expectedCanEnableInitInField, expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
	}

	public void testEnablement3() throws Exception{
        boolean expectedCanEnableInitInConstructors	= true;
        boolean expectedCanEnableInitInMethod			= true;
        boolean expectedCanEnableInitInField			= true;
        boolean expectedCanEnableSettingStatic			= true;
        boolean expectedCanEnableSettingFinal			= false;
		enablementHelper1(5, 13, 5, 14, expectedCanEnableSettingFinal, expectedCanEnableSettingStatic, expectedCanEnableInitInField, expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
	}

	public void testEnablement4() throws Exception{
        boolean expectedCanEnableInitInConstructors	= false;
        boolean expectedCanEnableInitInMethod			= true;
        boolean expectedCanEnableInitInField			= true;
        boolean expectedCanEnableSettingStatic			= true;
        boolean expectedCanEnableSettingFinal			= true;

        String newName= "i";
		boolean declareStatic = false;
	  	boolean declareFinal= false;
	  	int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_FIELD;
	  	int accessModifier= VIS_PRIVATE;

		enablementHelper(5, 13, 5, 14, newName, declareStatic, declareFinal, initializeIn, accessModifier,
					expectedCanEnableSettingFinal, expectedCanEnableSettingStatic, expectedCanEnableInitInField, expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
	}

	public void testEnablement5() throws Exception{
        boolean expectedCanEnableInitInConstructors	= false;
        boolean expectedCanEnableInitInMethod			= true;
        boolean expectedCanEnableInitInField			= true;
        boolean expectedCanEnableSettingStatic			= true;
        boolean expectedCanEnableSettingFinal			= true;

        String newName= "i";
		boolean declareStatic = false;
	  	boolean declareFinal= false;
	  	int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_FIELD;
	  	int accessModifier= VIS_PRIVATE;

		enablementHelper(7, 21, 7, 22, newName, declareStatic, declareFinal, initializeIn, accessModifier,
					expectedCanEnableSettingFinal, expectedCanEnableSettingStatic, expectedCanEnableInitInField, expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
	}

	public void testEnablement6() throws Exception{
        boolean expectedCanEnableInitInConstructors	= false;
        boolean expectedCanEnableInitInMethod			= false;
        boolean expectedCanEnableInitInField			= false;
        boolean expectedCanEnableSettingStatic			= true;
        boolean expectedCanEnableSettingFinal			= false;
		enablementHelper1(7, 21, 7, 22, expectedCanEnableSettingFinal, expectedCanEnableSettingStatic, expectedCanEnableInitInField, expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
	}

	public void testEnablement7() throws Exception{
        boolean expectedCanEnableInitInConstructors	= false;
        boolean expectedCanEnableInitInMethod			= true;
        boolean expectedCanEnableInitInField			= true;
        boolean expectedCanEnableSettingStatic			= false;
        boolean expectedCanEnableSettingFinal			= false;
		enablementHelper1(5, 13, 5, 14, expectedCanEnableSettingFinal, expectedCanEnableSettingStatic, expectedCanEnableInitInField, expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
	}

	public void testEnablement8() throws Exception{
        boolean expectedCanEnableInitInConstructors	= true;
        boolean expectedCanEnableInitInMethod			= true;
        boolean expectedCanEnableInitInField			= true;
        boolean expectedCanEnableSettingStatic			= false;
        boolean expectedCanEnableSettingFinal			= true;

        String newName= "i";
		boolean declareStatic = false;
	  	boolean declareFinal= false;
	  	int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_CONSTRUCTOR;
	  	int accessModifier= VIS_PRIVATE;

		enablementHelper(4, 13, 4, 14, newName, declareStatic, declareFinal, initializeIn, accessModifier,
					expectedCanEnableSettingFinal, expectedCanEnableSettingStatic, expectedCanEnableInitInField, expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
	}

	public void testEnablement9() throws Exception{
        boolean expectedCanEnableInitInConstructors	= false;
        boolean expectedCanEnableInitInMethod			= true;
        boolean expectedCanEnableInitInField			= true;
        boolean expectedCanEnableSettingStatic			= true;
        boolean expectedCanEnableSettingFinal			= false;

        String newName= "i";
		boolean declareStatic = true;
	  	boolean declareFinal= false;
	  	int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
	  	int accessModifier= VIS_PRIVATE;

		enablementHelper(4, 13, 4, 14, newName, declareStatic, declareFinal, initializeIn, accessModifier,
					expectedCanEnableSettingFinal, expectedCanEnableSettingStatic, expectedCanEnableInitInField, expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
	}

	public void testEnablement10() throws Exception{
        boolean expectedCanEnableInitInConstructors	= false;
        boolean expectedCanEnableInitInMethod			= true;
        boolean expectedCanEnableInitInField			= false;
        boolean expectedCanEnableSettingStatic			= false;
        boolean expectedCanEnableSettingFinal			= false;

        String newName= "fMyT";
		boolean declareStatic = false;
	  	boolean declareFinal= false;
	  	int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
	  	int accessModifier= VIS_PRIVATE;

		enablementHelper(6, 12, 6, 12, newName, declareStatic, declareFinal, initializeIn, accessModifier,
					expectedCanEnableSettingFinal, expectedCanEnableSettingStatic, expectedCanEnableInitInField, expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
	}

	public void testEnablement11() throws Exception{
        boolean expectedCanEnableInitInConstructors	= false;
        boolean expectedCanEnableInitInMethod			= true;
        boolean expectedCanEnableInitInField			= false;
        boolean expectedCanEnableSettingStatic			= true;
        boolean expectedCanEnableSettingFinal			= false;

        String newName= "fTarget";
		boolean declareStatic = false;
	  	boolean declareFinal= false;
	  	int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
	  	int accessModifier= VIS_PRIVATE;

		enablementHelper(6, 21, 6, 27, newName, declareStatic, declareFinal, initializeIn, accessModifier,
					expectedCanEnableSettingFinal, expectedCanEnableSettingStatic, expectedCanEnableInitInField, expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
	}

	public void testEnablement12() throws Exception{
        boolean expectedCanEnableInitInConstructors	= false;
        boolean expectedCanEnableInitInMethod			= true;
        boolean expectedCanEnableInitInField			= false;
        boolean expectedCanEnableSettingStatic			= true;
        boolean expectedCanEnableSettingFinal			= false;

        String newName= "i";
		boolean declareStatic = false;
	  	boolean declareFinal= false;
	  	int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
	  	int accessModifier= VIS_PRIVATE;

		enablementHelper(5, 16, 5, 17, newName, declareStatic, declareFinal, initializeIn, accessModifier,
					expectedCanEnableSettingFinal, expectedCanEnableSettingStatic, expectedCanEnableInitInField, expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
	}

	public void testEnablement13() throws Exception{
        boolean expectedCanEnableInitInConstructors	= false;
        boolean expectedCanEnableInitInMethod			= true;
        boolean expectedCanEnableInitInField			= false;
        boolean expectedCanEnableSettingStatic			= true;
        boolean expectedCanEnableSettingFinal			= false;

        String newName= "i";
		boolean declareStatic = false;
	  	boolean declareFinal= false;
	  	int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
	  	int accessModifier= VIS_PRIVATE;

		enablementHelper(4, 18, 4, 19, newName, declareStatic, declareFinal, initializeIn, accessModifier,
					expectedCanEnableSettingFinal, expectedCanEnableSettingStatic, expectedCanEnableInitInField, expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
	}*/


	///---- test failing preconditions --------------

	public void testFail0() throws Exception{
		failHelper(3, 16, 3, 17, "i", false, false, PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD, VIS_PRIVATE);
	}

	public void testFail1() throws Exception{
		failHelper(5, 28, 5, 29, "i", false, false, PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD, VIS_PRIVATE);
	}

	public void testFail2() throws Exception{
		failHelper(5, 15, 5, 16, "i", false, false, PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD, VIS_PRIVATE);
	}

	/* disabled: initialization in constructor
	public void testFail4() throws Exception{
		failHelper(7, 13, 7, 14, "i", false, false, PromoteTempToFieldRefactoring.INITIALIZE_IN_CONSTRUCTOR, VIS_PRIVATE);
	}*/

	public void testFail5() throws Exception{
		failHelper(6, 13, 6, 14, "i", false, false, PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD, VIS_PRIVATE);
	}

	/* disabled: initialization in constructor
	public void testFailGenerics1() throws Exception{
		failHelper(6, 12, 6, 12, "fYou", false, false, PromoteTempToFieldRefactoring.INITIALIZE_IN_CONSTRUCTOR, VIS_PRIVATE);
	}*/

	///----------- tests of transformation ------------

	public void test0() throws Exception{
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(5, 7, 5, 8, "i", declareStatic, declareFinal, initializeIn, accessModifier);
	}

	/* disabled: initialization in field
	public void test1() throws Exception{
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_FIELD;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(5, 13, 5, 14, "i", declareStatic, declareFinal, initializeIn, accessModifier);
	}*/

	/* disabled: initialization in constructor
	public void test2() throws Exception{
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_CONSTRUCTOR;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(7, 13, 7, 14, "i", declareStatic, declareFinal, initializeIn, accessModifier);
	}*/

	/* disabled: initialization in constructor
	public void test3() throws Exception{
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_CONSTRUCTOR;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(9, 13, 9, 14, "i", declareStatic, declareFinal, initializeIn, accessModifier);
	}*

	/* disabled: initialization in constructor
	public void test4() throws Exception{
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_CONSTRUCTOR;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(5, 13, 5, 14, "i", declareStatic, declareFinal, initializeIn, accessModifier);
	}*/

	public void test5() throws Exception{
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(6, 9, 6, 10, "i", declareStatic, declareFinal, initializeIn, accessModifier);
	}

	/* disabled: initialization in field
	public void test6() throws Exception{
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_FIELD;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(6, 21, 6, 22, "i", declareStatic, declareFinal, initializeIn, accessModifier);
	}*/

	/* disabled: initialization in field
	public void test7() throws Exception{
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_FIELD;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(4, 13, 4, 14, "i", declareStatic, declareFinal, initializeIn, accessModifier);
	}*/

	public void test8() throws Exception{
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(4, 7, 4, 8, "i", declareStatic, declareFinal, initializeIn, accessModifier);
	}

	public void test9() throws Exception{
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(5, 7, 5, 8, "field", declareStatic, declareFinal, initializeIn, accessModifier);
	}

	/* disabled: initialization in constructor
	public void test10() throws Exception{
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_CONSTRUCTOR;
        boolean declareFinal= true;
        boolean declareStatic= false;
		passHelper(7, 13, 7, 14, "i", declareStatic, declareFinal, initializeIn, accessModifier);
	}*/

	public void test11() throws Exception{
        int accessModifier= VIS_PUBLIC;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(7, 7, 7, 8, "i", declareStatic, declareFinal, initializeIn, accessModifier);
	}

	public void test12() throws Exception{
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
        boolean declareFinal= false;
        boolean declareStatic= true;
		passHelper(5, 7, 5, 8, "i", declareStatic, declareFinal, initializeIn, accessModifier);
	}

	public void test13() throws Exception{
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
        boolean declareFinal= false;
        boolean declareStatic= true;
		passHelper(5, 7, 5, 8, "i", declareStatic, declareFinal, initializeIn, accessModifier);
	}

	public void test14() throws Exception{
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
        boolean declareFinal= false;
        boolean declareStatic= true;
		passHelper(5, 13, 5, 14, "i", declareStatic, declareFinal, initializeIn, accessModifier);
	}

	public void test15() throws Exception{
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
        boolean declareFinal= false;
        boolean declareStatic= true;
		passHelper(5, 13, 5, 14, "i", declareStatic, declareFinal, initializeIn, accessModifier);
	}

	/* disabled: initialization in constructor
	public void test16() throws Exception{
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_CONSTRUCTOR;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(10, 13, 10, 14, "i", declareStatic, declareFinal, initializeIn, accessModifier);
	}*/

	public void test17() throws Exception{
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(4, 7, 4, 8, "i", declareStatic, declareFinal, initializeIn, accessModifier);
	}

	/* disabled: initialization in constructor
	public void test18() throws Exception{
		//printTestDisabledMessage("regression test for bug 39363");
		if (true) return;
		int accessModifier= VIS_PRIVATE;
		int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_CONSTRUCTOR;
		boolean declareFinal= false;
		boolean declareStatic= false;
		passHelper(5, 13, 5, 14, "i", declareStatic, declareFinal, initializeIn, accessModifier);
	}*/

	public void test19() throws Exception{ //test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=49840
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(4, 7, 4, 16, "fSomeArray", declareStatic, declareFinal, initializeIn, accessModifier);
	}

	public void test20() throws Exception{ //test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=49840
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(4, 18, 4, 18, "fDoubleDim", declareStatic, declareFinal, initializeIn, accessModifier);
	}

	/* disabled: initialization in field
	public void test21() throws Exception{ //test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=47798
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_FIELD;
        boolean declareFinal= true;
        boolean declareStatic= true;
		passHelper(4, 17, 4, 18, "fgX", declareStatic, declareFinal, initializeIn, accessModifier);
	}*/

	/* disabled: tests implementation detail
	public void test22() throws Exception{ //test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=54444
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), true, true);
		ISourceRange selection= TextRangeUtil.getSelection(cu, 4, 34, 4, 37);
        PromoteTempToFieldRefactoring ref= new PromoteTempToFieldRefactoring(cu, selection.getOffset(), selection.getLength());
		ref.checkInitialConditions(new NullProgressMonitor());
        assertEquals("sortByDefiningTypeAction", ref.guessFieldNames()[0]);
	}*/

	/* disabled: does not compile
	public void test23() throws Exception{ //syntax error
		int accessModifier= VIS_PRIVATE;
		int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_FIELD;
		boolean declareFinal= false;
		boolean declareStatic= false;
		passHelper(5, 31, 5, 31, "fCount", declareStatic, declareFinal, initializeIn, accessModifier);
	}*/

	public void test24() throws Exception{ //syntax error
		int accessModifier= VIS_PRIVATE;
		int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
		boolean declareFinal= false;
		boolean declareStatic= false;
		passHelper(4, 27, 4, 27, "fFinisheds", declareStatic, declareFinal, initializeIn, accessModifier);
	}

	public void test25() throws Exception{ //test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=136911
		int accessModifier= VIS_PRIVATE;
		int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
		boolean declareFinal= false;
		boolean declareStatic= false;
		passHelper(7, 9, 7, 10, "i", declareStatic, declareFinal, initializeIn, accessModifier);
	}

	/* disabled: initialization in field
	public void testGenerics01() throws Exception {
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_FIELD;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(9, 9, 9, 11, "fVt", declareStatic, declareFinal, initializeIn, accessModifier);
	}*/

	public void testGenerics02() throws Exception {
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(6, 6, 6, 6, "fMyT", declareStatic, declareFinal, initializeIn, accessModifier);
	}

	/* disabled: does not compile
	public void testEnum1() throws Exception {
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_CONSTRUCTOR;
        boolean declareFinal= true;
        boolean declareStatic= false;
		passHelper(6, 13, 6, 16, "fVar", declareStatic, declareFinal, initializeIn, accessModifier);
	}*/

	/* disabled: initialization in constructor
	public void testEnum2() throws Exception {
        int accessModifier= VIS_PUBLIC;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_FIELD;
        boolean declareFinal= true;
        boolean declareStatic= true;
		passHelper(10, 21, 10, 21, "fM", declareStatic, declareFinal, initializeIn, accessModifier);
	}*/

	public void testMultiVariableDeclFragment01() throws Exception {
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(6, 23, 6, 23, "fA", declareStatic, declareFinal, initializeIn, accessModifier);
	}

	public void testMultiVariableDeclFragment02() throws Exception {
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(5, 22, 5, 23, "fB", declareStatic, declareFinal, initializeIn, accessModifier);
	}

	/* disabled: does not compile
	public void testMultiVariableDeclFragment03() throws Exception {
        int accessModifier= VIS_PRIVATE;
        int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
        boolean declareFinal= false;
        boolean declareStatic= false;
		passHelper(5, 72, 5, 72, "fC", declareStatic, declareFinal, initializeIn, accessModifier);
	}*/

	public void testMultiVariableDeclFragment04() throws Exception {
		int accessModifier= VIS_PRIVATE;
		int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
		boolean declareFinal= false;
		boolean declareStatic= false;
		passHelper(5, 34, 5, 35, "fD", declareStatic, declareFinal, initializeIn, accessModifier);
	}

	/* disabled: does not compile
	public void testDeclaringMethodBindingUnavailable01() throws Exception {
		int accessModifier= VIS_PRIVATE;
		int initializeIn= PromoteTempToFieldRefactoring.INITIALIZE_IN_METHOD;
		boolean declareFinal= false;
		boolean declareStatic= false;
		passHelper(9, 8, 9, 12, "fDate", declareStatic, declareFinal, initializeIn, accessModifier);
	}*/

}
