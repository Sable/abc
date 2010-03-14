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
package tests.eclipse.InlineConstant;

import junit.framework.TestCase;
import tests.CompileHelper;
import AST.ASTNode;
import AST.CompilationUnit;
import AST.FieldDeclaration;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class InlineConstantTests extends TestCase {
	public InlineConstantTests(String name) {
		super(name);
	}
	
	private static FieldDeclaration findField(ASTNode p, int startLine, int startColumn, int endLine, int endColumn) {
		for(int i=0;i<p.getNumChild();++i) {
			ASTNode child = p.getChild(i);
			if(child != null) {
				FieldDeclaration fd = findField(child, startLine, startColumn, endLine, endColumn);
				if(fd != null)
					return fd;
			}
		}
		if(p instanceof FieldDeclaration) {
			int start = p.getStart(), end = p.getEnd();
			int pstartLine = ASTNode.getLine(start),
				pstartColumn = ASTNode.getColumn(start),
				pendLine = ASTNode.getLine(end),
				pendColumn = ASTNode.getColumn(end);
			if((pstartLine < startLine || pstartLine == startLine && pstartColumn <= startColumn) &&
					(endLine < pendLine || endLine == pendLine && endColumn <= pendColumn))
				return (FieldDeclaration)p;
		}
		return null;
	}
	
	private void helper1(Program in, Program out, CompilationUnit cu, int startLine, int startColumn, int endLine, int endColumn, boolean removeDeclaration) {
		FieldDeclaration fd = findField(cu, startLine, startColumn, endLine, endColumn);
		assertNotNull(fd);
		try {
			fd.doInlineConstant(removeDeclaration);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
	}
	
	public void helper1(String className, int startLine, int startColumn, int endLine, int endColumn, boolean replaceAll, boolean removeDeclaration) {
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/InlineConstant/canInline/"+getName()+"/in");
		Program out = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/InlineConstant/canInline/"+getName()+"/out");
		assertNotNull(in);
		assertNotNull(out);
		int idx = className.lastIndexOf('.');
		TypeDecl td = in.findType(className.substring(0, idx), className.substring(idx+1));
		assertNotNull(td);
		helper1(in, out, td.compilationUnit(), startLine, startColumn, endLine, endColumn, removeDeclaration);
	}

	public void helper1(Object o, String className, int startLine, int startColumn, int endLine, int endColumn, boolean replaceAll, boolean removeDeclaration) {
		helper1(className, startLine, startColumn, endLine, endColumn, replaceAll, removeDeclaration);
	}
	
	public void failHelper1(String className, int startLine, int startColumn, int endLine, int endColumn, boolean replaceAll, boolean removeDeclaration) {
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/InlineConstant/cannotInline/"+getName()+"/in");
		assertNotNull(in);
		int idx = className.lastIndexOf('.');
		TypeDecl td = in.findType(className.substring(0, idx), className.substring(idx+1));
		assertNotNull(td);
		FieldDeclaration fd = findField(td.compilationUnit(), startLine, startColumn, endLine, endColumn);
		assertNotNull(fd);
		try {
			fd.doInlineConstant(removeDeclaration);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) {
		}		
	}

	//--- TESTS

	public void test0() throws Exception {
		helper1("p.C", 5, 30, 5, 36, true, false);
	}

	/* disabled: does not compile
	public void test1() throws Exception {
		helper1("C", 3, 33, 3, 40, true, false);
	}*/

	public void test2() throws Exception {
		helper1("p.Klass", 10, 3, 10, 25, false, false);
	}

	public void test3() throws Exception {
		helper1("p.LeVinSuperieure", 5, 32, 5, 43, true, true);
	}

	public void test4() throws Exception {
		helper1("p.Klus", 5, 36, 5, 36, true, false);
	}

	/* disabled: does not compile
	public void test5() throws Exception {
		helper1("p.PartOfDeclNameSelected", 5, 32, 5, 34, true, true);
	}*/

	/* disabled: does not compile
	public void test6() throws Exception {
		helper1("p.CursorPositionedInReference", 8, 57, 8, 57, false, false);
	}*/

	/* disabled: does not compile
	public void test7() throws Exception {
		helper1("p.PartOfReferenceSelected", 8, 52, 8, 62, false, false);
	}*/

	public void test8() throws Exception {
		helper1(new String[] {"p1.C", "p2.D"}, "p1.C", 5, 29, 5, 37, true, false);
	}

	public void test9() throws Exception {
		helper1(new String[] {"p1.C", "p2.D", "p3.E"}, "p1.C", 4, 4, 4, 4, true, true);
	}

	public void test10() throws Exception {
		helper1(new String[] {"p1.A", "p2.B"}, "p2.B", 9, 22, 9, 31, false, false);
	}

	/* disabled: does not compile
	public void test11() throws Exception {
		helper1(new String[] {"p1.A", "p2.B", "p3.C"}, "p1.A", 8, 25, 8, 25, false, false);
	}*/

	/* disabled: does not compile
	public void test12() throws Exception {
		helper1(new String[] {"p1.Declarer", "p2.InlineSite"}, "p2.InlineSite", 7, 37, 7, 43, true, false);
	}*/

	/* disabled: does not compile
	public void test13() throws Exception {
		helper1(new String[] {"p1.A", "p2.InlineSite"}, "p2.InlineSite", 8, 19, 8, 29, false, false);
	}*/

	/* disabled: conservative data flow
	public void test14() throws Exception {
		helper1("cantonzuerich.GrueziWohl", 7, 35, 7, 35, true, false);
	}*/

	/* disabled: conservative data flow
	public void test15() throws Exception {
		helper1("schweiz.zuerich.zuerich.Froehlichkeit", 9, 3, 9, 3, true, false);
	}*/

	public void test16() throws Exception {
		helper1("p.IntegerMath", 8, 23, 8, 23, true, true);
	}

	/* disabled: conservative data flow
	public void test17() throws Exception {
		helper1("p.EnumRef", 4, 59, 4, 59, true, true);
	}*/

	public void test18() throws Exception {
		helper1("p.Annot", 5, 18, 5, 18, true, true);
	}

	public void test19() throws Exception {
		helper1("p.Test", 7, 36, 7, 36, true, false);
	}

	public void test20() throws Exception {
		helper1("p.Test", 10, 21, 10, 21, true, true);
	}

	public void test21() throws Exception {
		helper1(new String[] {"p.A", "q.Consts"}, "q.Consts", 5, 5, 5, 5, true, false);
	}

	/* disabled: does not compile
	public void test22() throws Exception {
		helper1(new String[] {"p.A", "q.Consts", "r.Third"}, "p.A", 11, 16, 11, 19, true, true);
	}*/

	public void test23() throws Exception {
		helper1("p.Test", 6, 10, 6, 30, false, false);
	}

	/* disabled: conservative data flow
	public void test24() throws Exception {
		helper1(new String[] {"p.A", "q.Consts"}, "q.Consts", 8, 4, 8, 4, true, true);
	}*/

	public void test25() throws Exception {
		helper1("p.A", 5, 32, 5, 32, true, true);
	}

	/* disabled: cannot remove if initialiser is impure
	public void test26() throws Exception { // test for bug 93689
		helper1("p.A", 5, 42, 5, 42, true, true);
	}*/

	/* disabled: does not compile
	public void test27() throws Exception { // test for bug 109071
		helper1("p.A", 4, 24, 4, 29, true, true);
	}*/

	/* disabled: does not compile
	public void test28() throws Exception {
		helper1(new String[] {"p.Const", "p.AnotherClass", "q.UsedClass"}, "p.Const", 6, 35, 6, 43, true, true);
	}*/

	/* disabled: conservative data flow
	public void test29() throws Exception { // test for bug 174327
		helper1("p.A", 7, 44, 7, 44, true, true);
	}*/

	public void test30() throws Exception { //test for bug 237547 (inline unused constant)
		helper1(new String[] {"p.A", "p.B", "p.C", "p.D", "q.Consts"}, "q.Consts", 5, 32, 5, 40, true, true);
	}

	// -- testing failing preconditions

	public void testFail0() throws Exception {
		failHelper1("foo.NeueZuercherZeitung", 5, 5, 5, 5, true, false);
	}

	public void testFail1() throws Exception {
		failHelper1("fun.Fun", 8, 35, 8, 35, false, false);
	}

	public void testFail2() throws Exception {
		failHelper1("p.EnumRef", 7, 22, 7, 22, true, true);
	}
}
