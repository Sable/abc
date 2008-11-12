import AST.ASTNode;
import AST.Block;
import AST.BytecodeParser;
import AST.ClosureInvocation;
import AST.CompilationUnit;
import AST.Frontend;
import AST.JavaParser;
import AST.LTExpr;
import AST.MethodDecl;
import AST.RefactoringException;
import AST.TypeDecl;
import AST.VarAccess;

class JavaChecker extends Frontend {

	public static void main(String args[]) throws RefactoringException {
		JavaChecker checker = new JavaChecker();
		checker.compile(new String[]{"/home/maxs/JastAddEclipse/Refactoring/tests/data/ExtractMethod/test8/in/A.java"});
		TypeDecl A = checker.program.lookupType("", "A");
		MethodDecl m = (MethodDecl)A.memberMethods("m").iterator().next();
		Block body = m.getBlock();
		Block newblock = body.extractBlock(2, 2);
		ClosureInvocation cl = newblock.wrapIntoClosure();
		cl.convert();
	}

	public boolean compile(String args[]) {
		return process(
				args,
				new BytecodeParser(),
				new JavaParser() {
					public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
						return new parser.JavaParser().parse(is, fileName);
					}
				}
		);
	}
	
	private static LTExpr findLTExpr(ASTNode n) {
		if(n == null)
			return null;
		if(n instanceof LTExpr)
			return (LTExpr)n;
		for(int i=0;i<n.getNumChild();++i) {
			LTExpr res = findLTExpr(n.getChild(i));
			if(res != null)
				return res;
		}
		return null;
	}
	
}
