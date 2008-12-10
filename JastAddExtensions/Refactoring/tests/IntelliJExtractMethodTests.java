package tests;

import java.io.File;

import junit.framework.TestCase;
import AST.ASTNode;
import AST.AbstractDot;
import AST.Access;
import AST.Block;
import AST.CompilationUnit;
import AST.Expr;
import AST.FileRange;
import AST.Program;
import AST.RefactoringException;
import AST.Stmt;

public class IntelliJExtractMethodTests extends TestCase {
	
	public void runTest(String name) {
		String infile = "tests/IntelliJ/extractMethod/"+name+".java";
		String outfile = "tests/IntelliJ/extractMethod/"+name+"_after.java";
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
		ASTNode from = TestHelper.findFirstNodeInside(cu, rng);
		ASTNode to = TestHelper.findLastNodeInside(cu, rng);
		try {
			if(from instanceof Expr) {
				if(from != to) {
					if(to instanceof Access && isLeftChildOfDot(to) && isLeftChildOfDot(from) &&
							((AbstractDot)from.getParent()).getRight() == to.getParent()) {
						AbstractDot toParent = (AbstractDot)to.getParent();
						AbstractDot fromParent = (AbstractDot)from.getParent();
						Access rest = toParent.getRight();
						Expr newFrom = new AbstractDot((Expr)from, (Access)to);
						fromParent.setLeft(newFrom);
						fromParent.setRight(rest);
						from = newFrom;
					} else {
						fail("can only extract single expression");
					}
				}
				Expr expr = (Expr)from;
				expr.extractMethod("private", "newMethod");
			} else {
				assertTrue(from instanceof Stmt);
				Block blk = ((Stmt)from).hostBlock();
				assertNotNull(blk);
				int fromIdx = blk.getIndexOfStmt((Stmt)from);
				assertTrue(fromIdx != -1);
				int toIdx = blk.getIndexOfStmt((Stmt)to);
				assertTrue(toIdx != -1);
				blk.extractMethod("private", "newMethod", fromIdx, toIdx);
			}
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
	
	private static boolean isLeftChildOfDot(ASTNode n) {
		return n.getParent() instanceof AbstractDot &&
				((AbstractDot)n.getParent()).getLeft() == n;
	}
	
	public void runDuplicateTest(String name) {
		//fail("no support for duplicate elimination");
	}
	
//    public void testAnonInner() { runTest("AnonInner"); }
//    public void testBooleanExpression() { runTest("BooleanExpression"); }
//    public void testChainedConstructorDuplicates() { runDuplicateTest("ChainedConstructorDuplicates"); }
//    public void testChainedConstructorInvalidDuplicates() { runDuplicateTest("ChainedConstructorInvalidDuplicates"); }
//    public void testChainedConstructor() { runTest("ChainedConstructor"); }
//    public void testCodeDuplicates2() { runDuplicateTest("CodeDuplicates2"); }
//    public void testCodeDuplicates3() { runDuplicateTest("CodeDuplicates3"); }
//    public void testCodeDuplicates4() { runDuplicateTest("CodeDuplicates4"); }
//    public void testCodeDuplicates5() { runDuplicateTest("CodeDuplicates5"); }
//    public void testCodeDuplicates() { runDuplicateTest("CodeDuplicates"); }
//    public void testCodeDuplicatesWithComments() { runDuplicateTest("CodeDuplicatesWithComments"); }
//    public void testCodeDuplicatesWithContinue() { runDuplicateTest("CodeDuplicatesWithContinue"); }
//    public void testCodeDuplicatesWithContinueNoReturn() { runDuplicateTest("CodeDuplicatesWithContinueNoReturn"); }
//    public void testCodeDuplicatesWithMultExitPoints() { runDuplicateTest("CodeDuplicatesWithMultExitPoints"); }
//    public void testCodeDuplicatesWithOutputValue1() { runDuplicateTest("CodeDuplicatesWithOutputValue1"); }
//    public void testCodeDuplicatesWithOutputValue() { runDuplicateTest("CodeDuplicatesWithOutputValue"); }
//    public void testCodeDuplicatesWithReturn2() { runDuplicateTest("CodeDuplicatesWithReturn2"); }
//    public void testCodeDuplicatesWithReturn() { runDuplicateTest("CodeDuplicatesWithReturn"); }
//    public void testConstantConditionsAffectingControlFlow() { runTest("ConstantConditionsAffectingControlFlow"); }
//    public void testExitPoints1() { runTest("ExitPoints1"); }
//    public void testExitPoints2() { runTest("ExitPoints2"); }
//    public void testExitPoints3() { runTest("ExitPoints3"); }
    public void testExitPoints4() { runTest("ExitPoints4"); }
//    public void testExitPoints5() { runTest("ExitPoints5"); }
//    public void testExitPoints6() { runTest("ExitPoints6"); }
//    public void testExitPoints7() { runTest("ExitPoints7"); }
//    public void testExitPoints8() { runTest("ExitPoints8"); }
//    public void testExitPoints9() { runTest("ExitPoints9"); }
//    public void testExitPointsInsideLoop() { runTest("ExitPointsInsideLoop"); }
//    public void testExpressionDuplicates() { runDuplicateTest("ExpressionDuplicates"); }
//    public void testExtractFromAnonymous() { runTest("ExtractFromAnonymous"); }
//    public void testExtractFromCodeBlock() { runTest("ExtractFromCodeBlock"); }
//    public void testExtractFromFinally() { runTest("ExtractFromFinally"); }
//    public void testExtractFromTryFinally2() { runTest("ExtractFromTryFinally2"); }
//    public void testExtractFromTryFinally() { runTest("ExtractFromTryFinally"); }
//    public void testFinally() { runTest("Finally"); }
//    public void testFinalOutputVar() { runTest("FinalOutputVar"); }
//    public void testFinalParamUsedInsideAnon() { runTest("FinalParamUsedInsideAnon"); }
//    public void testForceBraces() { runTest("ForceBraces"); }
//    public void testForEach() { runTest("ForEach"); }
//    public void testGenericsParameters() { runTest("GenericsParameters"); }
//    public void testGuardMethodDuplicates1() { runDuplicateTest("GuardMethodDuplicates1"); }
//    public void testGuardMethodDuplicates() { runDuplicateTest("GuardMethodDuplicates"); }
    public void testIDEADEV11036() { runTest("IDEADEV11036"); }
//    public void testIDEADEV11748() { runTest("IDEADEV11748"); }
//    public void testIDEADEV11848() { runTest("IDEADEV11848"); }
//    public void testIdeaDev2291() { runTest("IdeaDev2291"); }
//    public void testInstanceMethodDuplicatesInStaticContext() { runDuplicateTest("InstanceMethodDuplicatesInStaticContext"); }
//    public void testLesyaBug() { runTest("LesyaBug"); }
//    public void testLocalClass() { runTest("LocalClass"); }
//    public void testLocalClassUsage() { runTest("LocalClassUsage"); }
//    public void testLValueNotDuplicate() { runTest("LValueNotDuplicate"); }
//    public void testNonFinalWritableParam() { runTest("NonFinalWritableParam"); }
//    public void testNoShortCircuit() { runTest("NoShortCircuit"); }
//    public void testNotInitializedInsideFinally() { runTest("NotInitializedInsideFinally"); }
//    public void testOneBranchAssignment() { runTest("OneBranchAssignment"); }
//    public void testOxfordBug() { runTest("OxfordBug"); }
//    public void testReassignedVarAfterCall() { runTest("ReassignedVarAfterCall"); }
//    public void testReturnFromTry() { runTest("ReturnFromTry"); }
//    public void testScr10464() { runTest("Scr10464"); }
//    public void testSCR12245() { runTest("SCR12245"); }
//    public void testSCR15815() { runTest("SCR15815"); }
    public void testSCR27887() { runTest("SCR27887"); }
//    public void testSCR28427() { runTest("SCR28427"); }
//    public void testSCR32924() { runTest("SCR32924"); }
//    public void testScr6241() { runTest("Scr6241"); }
//    public void testScr7091() { runTest("Scr7091"); }
//    public void testScr9852() { runTest("Scr9852"); }
//    public void testStaticImport() { runTest("StaticImport"); }
//    public void testThisCall() { runTest("ThisCall"); }
//    public void testTryFinallyInsideFor() { runTest("TryFinallyInsideFor"); }
//    public void testTryFinally() { runTest("TryFinally"); }
//    public void testUnusedInitializedVar() { runTest("UnusedInitializedVar"); }
//    public void testUseVarAfterTry() { runTest("UseVarAfterTry"); }
    
}
