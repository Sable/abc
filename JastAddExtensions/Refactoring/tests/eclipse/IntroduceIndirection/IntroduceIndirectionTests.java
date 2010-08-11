/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Max Schaefer	   - adapted to work with JRRT
 *******************************************************************************/
package tests.eclipse.IntroduceIndirection;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.ASTNode;
import AST.MethodAccess;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class IntroduceIndirectionTests extends TestCase {
	public IntroduceIndirectionTests(String name) {
		super(name);
	}
	
	private ASTNode findNode(ASTNode p, int startLine, int startColumn, int endLine, int endColumn) {
		if(p == null)
			return null;
		for(int i=0;i<p.getNumChild();++i) {
			ASTNode child = p.getChild(i);
			if(child != null) {
				ASTNode res = findNode(child, startLine, startColumn, endLine, endColumn);
				if(res != null)
					return res;
			}
		}
		if(p instanceof MethodDecl || p instanceof MethodAccess) {
			int pstart = p.getStart(), pend = p.getEnd();
			int pstartLine = ASTNode.getLine(pstart), pstartColumn = ASTNode.getColumn(pstart),
				pendLine = ASTNode.getLine(pend), pendColumn = ASTNode.getColumn(pend);
			if((pstartLine < startLine || (pstartLine == startLine && pstartColumn <= startColumn))
					|| (endLine < pendLine || (endLine == pendLine && endColumn <= pendColumn)))
				return p;
		}
		return null;
	}
	
	private void helperPass(Object o1, String indname, String targetTypeName, int startLine, int startColumn, int endLine, int endColumn) {
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/IntroduceIndirection/"+getName()+"/in");
		Program out = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/IntroduceIndirection/"+getName()+"/out");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		ASTNode sel = findNode(in, startLine, startColumn, endLine, endColumn);
		assertNotNull(sel);
		int index = targetTypeName.indexOf('.');
		TypeDecl targetType = in.findType(targetTypeName.substring(0, index), targetTypeName.substring(index+1));
		assertNotNull(targetType);
		try {
			if(sel instanceof MethodAccess)
				sel = ((MethodAccess)sel).decl();
			((MethodDecl)sel).doIntroduceIndirection(indname, targetType);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helperWarn(Object o1, String indname, String targetTypeName, int startLine, int startColumn, int endLine, int endColumn) {
		helperPass(o1, indname, targetTypeName, startLine, startColumn, endLine, endColumn);
	}

	private void helperFail(Object o1, String indname, String targetTypeName, int startLine, int startColumn, int endLine, int endColumn) { 
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/IntroduceIndirection/"+getName()+"/in");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		ASTNode sel = findNode(in, startLine, startColumn, endLine, endColumn);
		assertNotNull(sel);
		int index = targetTypeName.indexOf('.');
		TypeDecl targetType = in.findType(targetTypeName.substring(0, index), targetTypeName.substring(index+1));
		assertNotNull(targetType);
		try {
			if(sel instanceof MethodAccess)
				sel = ((MethodAccess)sel).decl();
			((MethodDecl)sel).doIntroduceIndirection(indname, targetType);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) {
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}
	
	private void helperErr(Object o1, String indname, String targetTypeName, int startLine, int startColumn, int endLine, int endColumn) { 
		helperFail(o1, indname, targetTypeName, startLine, startColumn, endLine, endColumn);
	}
	
	private void helper(String[] strings, String string, String string2, int i,
			int j, int k, int l, boolean b, boolean c, boolean d, boolean e) {
		fail("not done yet");
	}

	public void test01() throws Exception {
		// very simple test
		helperPass(new String[] { "p.Foo" }, "bar", "p.Foo", 7, 10, 7, 13);
	}

	public void test02() throws Exception {
		// warning if a super call is found
		helperWarn(new String[] { "p.A", "p.B" }, "bar", "p.A", 8, 15, 8, 18);
	}

	public void test03() throws Exception {
		// add imports to target
		helperPass(new String[] { "p.Foo", "p.Bar" }, "bar", "p.Bar", 8, 17, 8, 20);
	}

	public void test04() throws Exception {
		// this qualification with outer type, method declaration is in outer type.
		helperPass(new String[] { "p.Foo" }, "bar", "p.Foo", 10, 17, 10, 20);
	}

	public void test05() throws Exception {
		// this qualification with outer type, method declaration is in
		// super type of outer type
		helperPass(new String[] { "p.Foo", "p.Bar" }, "bar", "p.Foo", 12, 17, 12, 27);
	}

	public void test06() throws Exception {
		// this qualification with the current type, method declaration is
		// in super type of current type
		helperPass(new String[] { "p.Foo", "p.Bar" }, "bar", "p.Foo", 10, 13, 10, 23);
	}

	public void test07() throws Exception {
		// test qualification with anonymous type (=> warning, don't update)
		helperWarn(new String[] { "p.E1" }, "bar", "p.E1", 30, 16, 30, 19);
	}

	public void test08() throws Exception {
		/* disabled: different interpretations
		// open hierarchy failure
		helperFail(new String[] { "p.SeaLevel", "p.Eiger", "p.Moench" }, "bar", "p.SeaLevel", 13, 11, 13, 14);*/
	}

	public void test09() throws Exception {
		// create static intermediary
		helperPass(new String[] { "p.Foo" }, "bar", "p.Foo", 7, 17, 7, 20);
	}

	public void test10() throws Exception {
		// error, method already exists
		helperErr(new String[] { "p.Foo", "p.Bar" }, "foo", "p.Foo", 10, 19, 10, 22);
	}

	public void test11() throws Exception {
		// test name clash with existing argument
		helperPass(new String[] { "p.Foo" }, "bar", "p.Foo", 12, 9, 12, 12);
	}

	public void test12() throws Exception {
		// cannot put the intermediary into an inner non-static type
		helperFail(new String[] { "p.Foo" }, "bar", "p.Foo.Inner", 9, 10, 9, 13);
	}

	public void test13() throws Exception {
		// create intermediary inside nested static types
		helperPass(new String[] { "p.Foo", "p.Bar" }, "bar", "p.Foo.Inner.MoreInner", 13, 10, 13, 13);
	}

	/* disabled: no support for adjusting visibility
	public void test14() throws Exception {
		// raise visibility of target so intermediary sees it.
		helperWarn(new String[] { "p0.Foo", "p1.Bar" }, "bar", "p1.Bar", 8, 18, 8, 23);
	}*/

	/* disabled: no support for adjusting visibility
	public void test15() throws Exception {
		// raise visibility of intermediary type so
		// existing references see it
		helperWarn(new String[] { "p0.Foo", "p0.Bar", "p1.Third" }, "bar", "p0.Bar", 8, 17, 8, 20);
	}*/

	/* disabled: no support for adjusting only a single invocation
	public void test16() throws Exception {
		// test non-reference mode with a method invocation selected
		helper(new String[] { "p.Bar", "p.Foo" }, "bar", "p.Bar", 6, 19, 6, 22, false, false, false, false);
	}*/

	public void test17() throws Exception {
		// generic target method
		helperPass(new String[] { "p.Foo" }, "bar", "p.Foo", 16, 9, 16, 12);
	}

	public void test18() throws Exception {
		// simple test with generic type, unused
		helperPass(new String[] { "p.Foo" }, "bar", "p.Foo", 19, 11, 19, 14);
	}

	public void test19() throws Exception {
		// simple test with generic type, used
		helperPass(new String[] { "p.Foo" }, "bar", "p.Foo", 19, 11, 19, 14);
	}

	public void test20() throws Exception {
		// complex case with generic type parameters and method parameters used
		helperPass(new String[] { "p.Foo" }, "bar", "p.Foo", 11, 11, 11, 17);
	}

	public void test21() throws Exception {
		// no call updating if type arguments are used
		helperWarn(new String[] { "p.Foo" }, "bar", "p.Foo", 9, 22, 9, 26);
	}

	public void test22() throws Exception {
		// method using type parameters from enclosing types as well
		helperPass(new String[] { "p.Foo" }, "bar", "p.Foo", 16, 24, 16, 27);
	}

	public void test23() throws Exception {
		/* disabled: different interpretation
		// warn about incorrect qualified static calls and don't update them.
		helperWarn(new String[] { "p.Foo" }, "bar", "p.Foo", 11, 25, 11, 28);*/
	}

	public void test24() throws Exception {
		/* disabled: different interpretation
		// assure common super type is used even if the hierarchy branches downwards
		helperPass(new String[] { "p.Foo" }, "bar", "p.Foo", 11, 11, 11, 14);*/
	}

	/* disabled: no support for adjusting visibility
	public void test25() throws Exception {
		// increase visibility of overridden methods as well
		helperWarn(new String[] { "p0.Foo", "p0.SubFoo", "p1.Bar" }, "bar", "p1.Bar", 8, 20, 8, 23);
	}*/

	public void test26() throws Exception {
		// ensure exceptions are copied
		helperPass(new String[] { "p.Foo" }, "bar", "p.Foo", 7, 24, 7, 27);
	}

	/* disabled: no support for adjusting visibility
	public void test27() throws Exception {
		// complex visibility adjustment case
		// target method is not inside target type, and is overridden
		// target type must be increased, and all overridden methods must be increased.
		helperWarn(new String[] { "p0.Foo", "p0.RealFoo", "p0.NonOriginalSubFoo", "p0.VerySuperFoo", "p1.Bar" }, "bar", "p1.Bar", 7, 13, 7, 16);
	}*/

	public void test28() throws Exception {
		/* disabled: different interpretation
		// the template for the intermediary must be the method inside the real
		// target (for parameter names and exceptions)
		helperWarn(new String[] { "p.Foo", "p.Bar",}, "bar", "p.Foo", 10, 9, 10, 12);*/
	}

	/* disabled: no support for adjusting visibility
	public void test29() throws Exception {
		// don't adjust visibility twice
		helperWarn(new String[] { "p0.Test", "p1.Other" }, "bar", "p1.Other", 5, 35, 5, 44);
	}*/

	public void test30() throws Exception {
		// multiple generic instantiations
		helperPass(new String[] { "p.MultiGenerics" }, "bar", "p.MultiGenerics", 7, 16, 7, 26);
	}

	public void test31() throws Exception {
		// test for bug 127665
		helperPass(new String[] { "p.Test" }, "foo", "p.TestO", 13, 20, 13, 23);
	}

}
