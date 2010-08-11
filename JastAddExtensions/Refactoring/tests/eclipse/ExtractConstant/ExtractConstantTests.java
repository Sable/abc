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
package tests.eclipse.ExtractConstant;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import tests.eclipse.ExtractTemp.ExtractTempTests;
import AST.Expr;
import AST.Program;
import AST.RefactoringException;

public class ExtractConstantTests extends TestCase {
	public ExtractConstantTests(String name) {
		super(name);
	}

	private String getSimpleTestFileName(boolean canInline, boolean input){
		String fileName = "A_" + getName();
		if (canInline)
			fileName += input ? "_in": "_out";
		return fileName + ".java";
	}

	private String getTestFileName(boolean canExtract, boolean input){
		String fileName= "tests/eclipse/ExtractConstant/";
		fileName += (canExtract ? "canExtract/": "cannotExtract/");
		return fileName + getSimpleTestFileName(canExtract, input);
	}

	protected Program getProgram(boolean canExtract, boolean input) {
		Program p = CompileHelper.compile(getTestFileName(canExtract, input));
		assertNotNull(p);
		return p;
	}

	private void helper1(int startLine, int startColumn, int endLine, int endColumn, boolean replaceAll, boolean allowLoadtime, boolean qualifyReferencesWithConstantName, String constantName, String guessedConstantName) throws Exception{
		//assertFalse(replaceAll);
		Program in = getProgram(true, true);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		Program out = getProgram(true, false);
		
		Expr e = ExtractTempTests.findExpr(in, startLine, startColumn, endLine, endColumn);
		assertNotNull(e);
		
		try {
			e.doExtractConstant(constantName);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void failHelper1(int startLine, int startColumn, int endLine, int endColumn, boolean replaceAll, boolean allowLoadtime, String constantName, int errorCode, boolean checkCode) throws Exception{
		//assertFalse(replaceAll);
		Program in = getProgram(false, true);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		
		Expr e = ExtractTempTests.findExpr(in, startLine, startColumn, endLine, endColumn);
		assertNotNull(e);
		
		try {
			e.doExtractConstant(constantName);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) {
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper1(int startLine, int startColumn, int endLine, int endColumn, boolean replaceAll, boolean allowLoadtime, String constantName, String guessedConstantName) throws Exception{
		helper1(startLine, startColumn, endLine, endColumn, replaceAll, allowLoadtime, false, constantName, guessedConstantName);
	}

	private void helper1(int startLine, int startColumn, int endLine, int endColumn, boolean replaceAll, boolean allowLoadtime, boolean qualifyReferencesWithConstantName, String constantName) throws Exception{
		helper1(startLine, startColumn, endLine, endColumn, replaceAll, allowLoadtime, qualifyReferencesWithConstantName, constantName, constantName);
	}

	private void helper1(int startLine, int startColumn, int endLine, int endColumn, boolean replaceAll, boolean allowLoadtime, String constantName) throws Exception{
		helper1(startLine, startColumn, endLine, endColumn, replaceAll, allowLoadtime, false, constantName);
	}

	private void failHelper1(int startLine, int startColumn, int endLine, int endColumn, boolean replaceAll, boolean allowLoadtime, String constantName) throws Exception {
		failHelper1(startLine, startColumn, endLine, endColumn, replaceAll, allowLoadtime, constantName, 0, false);
	}
	
	//--- TESTS

	public void test0() throws Exception {
		helper1(5, 10, 5, 11, true, false, "CONSTANT", "_0");
	}

	public void test1() throws Exception {
		helper1(5, 10, 5, 15, false, false, "CONSTANT", "INT");
	}

	public void test2() throws Exception {
		helper1(8, 10, 8, 21, false, false, "CONSTANT", "INT");
	}

	/* disabled: clone detection
	public void test3() throws Exception {
		helper1(8, 10, 8, 21, true, false, "CONSTANT", "INT");
	}
	
	public void test4() throws Exception {
		helper1(5, 17, 5, 28, true, false, "CONSTANT", "INT");
	}*/

	public void test5() throws Exception {
		helper1(11, 11, 11, 17, true, true, "CONSTANT", "RG");
	}

	/* disabled: conservative data flow
	public void test6() throws Exception {
		helper1(13, 11, 13, 26, true, true, "CONSTANT", "RF");
	}*/

	public void test7() throws Exception {
		helper1(12, 11, 12, 19, true, true, "CONSTANT", "RG");
	}

	/* disabled: conservative data flow
	public void test8() throws Exception {
		helper1(8, 10, 8, 16, true, true, "CONSTANT", "INT");
	}*/

	/* disabled: clone detection
	public void test9() throws Exception {
		helper1(6, 10, 6, 23, true, true, "CONSTANT", "INT");
	}

	public void test10() throws Exception {
		helper1(8, 11, 8, 16, true, true, "CONSTANT", "INT");
	}

	public void test11() throws Exception {
		helper1(10, 31, 10, 37, true, true, "CONSTANT", "INT");
	}

	public void test12() throws Exception {
		helper1(9, 13, 9, 18, true, true, "CONSTANT", "INT");
	}*/

	/* disabled: conservative data flow
	public void test13() throws Exception{
		helper1(9, 10, 9, 22, true, true, "CONSTANT", "F");
	}*/

	/* disabled: conservative data flow
	public void test14() throws Exception{
		helper1(10, 22, 10, 38, true, true, "CONSTANT", "INT");
	}*/

	public void test15() throws Exception{
		helper1(5, 10, 5, 19, true, false, "CONSTANT", "FRED");
	}

	public void test16() throws Exception{
		helper1(5, 14, 5, 21, true, false, "CONSTANT", "RED");
	}

	public void test17() throws Exception{
		helper1(5, 10, 5, 29, true, false, "CONSTANT", "YET_ANOTHER_FRED");
	}

	/* disabled: clone detection
	public void test18() throws Exception {
		helper1(5, 10, 5, 11, true, false, true, "CONSTANT", "_0");
	}*/

	public void test19() throws Exception {
		helper1(5, 17, 5, 32, false, false, "CONSTANT", "STRING");
	}

	public void test20() throws Exception {
		helper1(7, 13, 7, 22, false, false, "CONSTANT", "STRING");
	}

	public void test21() throws Exception {
		helper1(4, 22, 4, 31, false, false, "CONSTANT", "STRING");
	}

	/* disabled: conservative data flow
	public void test22() throws Exception {
		helper1(9, 29, 9, 53, false, false, "ITEMS", "ARRAY_LIST");
	}*/

	/* disabled: JastAddJ bug
	public void test23() throws Exception {
		helper1(14, 6, 14, 9, true, false, "COLOR");
	}*/

	/* disabled: JastAddJ bug
	public void test24() throws Exception {
		helper1(9, 22, 9, 30, true, false, "NUM", "ENUM");
	}*/

	public void test25() throws Exception {
		helper1(5, 24, 5, 37, false, false, "DEFAULT_NAME", "JEAN_PIERRE");
	}

	/* disabled: conservative data flow
	public void test26() throws Exception {
		helper1(6, 10, 6, 26, true, false, true, "INT", "A");
	}*/

	public void test27() throws Exception {
		helper1(13, 14, 13, 19, true, false, false, "FOO", "FOO");
	}

	public void test28() throws Exception {
		helper1(13, 14, 13, 19, true, false, false, "FOO", "FOO");
	}

	/* disabled: conservative data flow
	public void test29() throws Exception {
		helper1(12, 13, 12, 22, false, true, "NUMBER", "NUMBER");
	}

	public void test30() throws Exception {
		helper1(12, 13, 12, 22, false, true, "INTEGER", "INTEGER");
	}

	public void test31() throws Exception { //bug 104293
		helper1(9, 23, 9, 35, true, false, "AS_LIST", "AS_LIST");
	}

	public void test32() throws Exception { //bug 104293
		helper1(7, 14, 7, 31, true, false, "STRING", "STRING");
	}*/

	/* disabled: clone detection
	public void test33() throws Exception { //bug 108354
		helper1(7, 14, 7, 31, true, false, "STRING", "STRING");
	}

	public void test34() throws Exception { // syntax error
		helper1(7, 14, 7, 31, true, false, "STRING", "STRING");
	}*/

	public void test35() throws Exception { // bug 218108
		/* disabled: enums pretty-print in strange ways
		helper1(7, 11, 7, 16, true, false, "BUG", "BUG");*/
	}

	public void test36() throws Exception { // bug 218108
		/* disabled: enums pretty-print in strange ways
		helper1(6, 11, 6, 16, true, false, "BUG", "BUG");*/
	}

	public void testZeroLengthSelection0() throws Exception {
		helper1(5, 10, 5, 13, false, false, "CONSTANT", "_100");
	}

	// -- testing failing preconditions
	public void testFail0() throws Exception{
		failHelper1(8, 10, 8, 15, true, true, "CONSTANT");
	}

	public void testFail1() throws Exception{
		failHelper1(8, 10, 8, 20 , true, true, "CONSTANT");
	}

	public void testFail2() throws Exception{
		failHelper1(9, 11, 9, 12 , true, true, "CONSTANT");
	}

	public void testFail3() throws Exception{
		failHelper1(9, 9, 9, 16, true, true, "CONSTANT");
	}

	public void testFail4() throws Exception{
		failHelper1(6, 10, 6, 14, true, true, "CONSTANT");
	}

	public void testFail5() throws Exception{
		failHelper1(9, 10, 9, 19, true, true, "CONSTANT");
	}

	public void testFail6() throws Exception{
		failHelper1(11, 11, 11, 15, true, true, "CONSTANT");
	}

	public void testFail7() throws Exception{
		failHelper1(11, 11, 11, 25, true, true, "CONSTANT");
	}

	public void testFail10() throws Exception{
		failHelper1(15, 11, 15, 28, true, false, "CONSTANT");
	}

	public void testFail11() throws Exception{
		failHelper1(8, 10, 8, 16, true, false, "CONSTANT");
	}

	/* disabled: no such expression
	public void testFail12() throws Exception{
		failHelper1(4, 7, 4, 8, true, true, "CONSTANT");
	}

	public void testFail13() throws Exception {
		failHelper1(2, 9, 2, 10, true, true, "CONSTANT");
	}*/

	/* disabled: why should these fail?
	public void testFail14() throws Exception {
		failHelper1(5, 9, 5, 10, true, true, "CONSTANT");
	}

	public void testFail15() throws Exception {
		failHelper1(5, 9, 5, 10, true, true, "CONSTANT");
	}*/

	public void testFail16() throws Exception {
		failHelper1(9, 14, 9, 32, true, false, "CONSTANT");
	}

	/* disabled: JastAddJ bug
	public void testFail17() throws Exception {
		failHelper1(16, 12, 16, 15, true, true, "COLOR");
	}*/

	/* disabled: no support for name guessing
	public void testGuessStringLiteral0() throws Exception {
		//test for bug 37377
		guessHelper(4, 19, 4, 32, "FOO_HASH_MAP");
	}

	public void testGuessStringLiteral1() throws Exception {
		//test for bug 37377
		guessHelper(4, 19, 4, 33, "FOO_HASH_MAP");
	}

	public void testGuessStringLiteral2() throws Exception {
		//test for bug 37377
		guessHelper(4, 19, 4, 56, "HANS_IM_GLUECK123_34_BLA_BLA");
	}

	public void testGuessStringLiteral3() throws Exception {
		guessHelper(5, 16, 5, 16, "ASSUME_CAMEL_CASE");
	}

	public void testGuessFromGetterName0() throws Exception {
		guessHelper(4, 19, 4, 30, "FOO_BAR");
	}

	public void testGuessFromGetterName1() throws Exception {
		guessHelper(4, 23, 4, 33, "FOO_BAR");
	}*/
}

