package tests;

import java.io.File;

import junit.framework.TestCase;
import AST.ASTNode;
import AST.CompilationUnit;
import AST.FileRange;
import AST.MethodAccess;
import AST.Program;
import AST.RefactoringException;
import AST.VariableDeclaration;

public class IntelliJInlineVariableTests extends TestCase {

	public void runTest(String name) {
		String infile = "tests/IntelliJ/inlineLocal/"+name+".java";
		String outfile = "tests/IntelliJ/inlineLocal/"+name+".java.after";
		Program prog = TestHelper.compile(infile);
		assertNotNull(prog);
		CompilationUnit cu = null;
		for(CompilationUnit unit: prog.getCompilationUnits()) {
			if (unit.fromSource()) {
				cu = unit;
				break;
			}
		}
		assertNotNull(cu);
		FileRange start = cu.findComment("/*[*/");
		assertNotNull(start);
		FileRange end = cu.findComment("/*]*/");
		assertNotNull(end);
		FileRange rng = new FileRange("", start.el, start.ec, end.sl, end.sc);
		ASTNode nd = TestHelper.findFirstNodeInside(cu, rng);
		try {
			assertTrue(nd instanceof VariableDeclaration);
			VariableDeclaration var = (VariableDeclaration)nd;
			var.inline();
		} catch(RefactoringException rfe) {
			if(new File(outfile).exists())
				fail(rfe.getMessage());
			else
				return;
		}
		Program outprog = TestHelper.compile(outfile);
		assertNotNull(outprog);
		assertEquals(outprog.toString(), prog.toString());
	}

	public void testAugmentedAssignment() { runTest("AugmentedAssignment"); }
	public void testIDEADEV10376() { runTest("IDEADEV10376"); }
	public void testIDEADEV12244() { runTest("IDEADEV12244"); }
	public void testIDEADEV13151() { runTest("IDEADEV13151"); }
	public void testIdeaDEV9404() { runTest("IdeaDEV9404"); }
	public void testIDEADEV950() { runTest("IDEADEV950"); }
	public void testInference() { runTest("Inference"); }
	public void testNoRedundantCasts() { runTest("NoRedundantCasts"); }
	public void testQualifier() { runTest("Qualifier"); }
	public void testUsedInInnerClass2() { runTest("UsedInInnerClass2"); }
	public void testUsedInInnerClass3() { runTest("UsedInInnerClass3"); }
	public void testUsedInInnerClass4() { runTest("UsedInInnerClass4"); }
	public void testUsedInInnerClass() { runTest("UsedInInnerClass"); }

}