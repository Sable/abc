package tests;

import java.io.File;

import junit.framework.TestCase;
import AST.ASTNode;
import AST.CompilationUnit;
import AST.FileRange;
import AST.MethodAccess;
import AST.Program;
import AST.RefactoringException;

public class IntelliJInlineMethodTests extends TestCase {

	public void runTest(String name) {
		String infile = "tests/IntelliJ/inlineMethod/"+name+".java";
		String outfile = "tests/IntelliJ/inlineMethod/"+name+".java.after";
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
			assertTrue(nd instanceof MethodAccess);
			MethodAccess ma = (MethodAccess)nd;
			ma.inline();
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

	public void testArrayAccess() { runTest("ArrayAccess"); }
	public void testCallInFor() { runTest("CallInFor"); }
	public void testCallUnderIf() { runTest("CallUnderIf"); }
	public void testChainingConstructor1() { runTest("ChainingConstructor1"); }
	public void testChainingConstructor() { runTest("ChainingConstructor"); }
	public void testConflictingField() { runTest("ConflictingField"); }
	public void testConstantInChainingConstructor() { runTest("ConstantInChainingConstructor"); }
	public void testEnumConstantConstructorParameterComplex2() { runTest("EnumConstantConstructorParameterComplex2"); }
	public void testEnumConstantConstructorParameterComplex() { runTest("EnumConstantConstructorParameterComplex"); }
	public void testEnumConstantConstructorParameter() { runTest("EnumConstantConstructorParameter"); }
	public void testEnumConstructor() { runTest("EnumConstructor"); }
	public void testFieldInitializer() { runTest("FieldInitializer"); }
	public void testFinalParameters1() { runTest("FinalParameters1"); }
	public void testFinalParameters() { runTest("FinalParameters"); }
	public void testIDEADEV12616() { runTest("IDEADEV12616"); }
	public void testIDEADEV3672() { runTest("IDEADEV3672"); }
	public void testIDEADEV5806() { runTest("IDEADEV5806"); }
	public void testIDEADEV6807() { runTest("IDEADEV6807"); }
	public void testInlineParms() { runTest("InlineParms"); }
	public void testInlineWithQualifierFromSuper() { runTest("InlineWithQualifierFromSuper"); }
	public void testInlineWithQualifier() { runTest("InlineWithQualifier"); }
	public void testInlineWithTry() { runTest("InlineWithTry"); }
	public void testLocalVariableResult() { runTest("LocalVariableResult"); }
	public void testNameClash() { runTest("NameClash"); }
	public void testNestedCall() { runTest("NestedCall"); }
	public void testReplaceParameterWithArgumentForConstructor() { runTest("ReplaceParameterWithArgumentForConstructor"); }
	public void testScr10884() { runTest("Scr10884"); }
	public void testScr13831() { runTest("Scr13831"); }
	public void testSCR20655() { runTest("SCR20655"); }
	public void testSCR22644() { runTest("SCR22644"); }
	public void testSCR31093() { runTest("SCR31093"); }
	public void testSCR37742() { runTest("SCR37742"); }
	public void testSideEffect() { runTest("SideEffect"); }
	public void testStaticFieldInitializer() { runTest("StaticFieldInitializer"); }
	public void testTailCallReturn() { runTest("TailCallReturn"); }
	public void testTailCallSimple() { runTest("TailCallSimple"); }
	public void testTry() { runTest("Try"); }
	public void testTrySynchronized() { runTest("TrySynchronized"); }
	public void testVarargs1() { runTest("Varargs1"); }
	public void testVarargs() { runTest("Varargs"); }
	public void testVoidWithReturn1() { runTest("VoidWithReturn1"); }
	public void testVoidWithReturn() { runTest("VoidWithReturn"); }
}