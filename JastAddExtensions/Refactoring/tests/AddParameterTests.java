package tests;

import junit.framework.TestCase;
import AST.IntegerLiteral;
import AST.Literal;
import AST.MethodDecl;
import AST.ParameterDeclaration;
import AST.Program;
import AST.RawCU;
import AST.RefactoringException;
import AST.TypeAccess;
import AST.TypeDecl;

public class AddParameterTests extends TestCase {
	private void testSucc(String hosttp, String sig, int i, String parmtp,
			String parmname, Literal val, Program in, Program out) {
		assertNotNull(in);
		TypeDecl td = in.findType(hosttp);
		assertNotNull(td);
		MethodDecl md = in.findMethodBySig(sig);
		assertNotNull(md);
		try {
			md.doAddParameter(new ParameterDeclaration(new TypeAccess(parmtp), parmname), i, val, false);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) { 
			assertEquals(out.toString(), rfe.getMessage());
		}
	}

	public void test1() {
		testSucc("p.A", "k()", 0, "int", "i", new IntegerLiteral(0),
 				Program.fromCompilationUnits(
				new RawCU("A.java",
					"package p;" +
					"public class A {" +
					"  protected long k() { return 0; }" +
					"  public long m() { return k(); }" +
					"}"),
				new RawCU("B.java",
					"package q;" +
					"import p.*;" +
					"public class B extends A {" +
					"  protected long k(int a) { return 2; }" +
					"  public long test() {" +
					"    return m();" +
					"  }" +
					"}")),
 				Program.fromCompilationUnits(
				new RawCU("A.java",
					"package p;" +
					"public class A {" +
					"  long k(int i) { return 0; }" +
					"  public long m() { return k(0); }" +
					"}"),
				new RawCU("B.java",
					"package q;" +
					"import p.*;" +
					"public class B extends A {" +
					"  protected long k(int a) { return 2; }" +
					"  public long test() {" +
					"    return m();" +
					"  }" +
					"}")));
	}
	
	public void test2() {
		testSucc("A", "k()", 0, "int", "i", new IntegerLiteral(0),
				Program.fromClasses(
				"class A {" +
				"  long k() { return 0; }" +
				"  public long m() {" +
				"    return A.this.k();" +
				"  }" +
				"}",
				"class B extends A {" +
				"  protected long k() { return 2; }" +
				"  public long test() { return m(); }" +
				"}"),
				Program.fromClasses(
				"class A {" +
				"  long k(int i) { return 0; }" +
				"  public long m() {" +
				"    return A.this.k(0);" +
				"  }" +
				"}",
				"class B extends A {" +
				"  protected long k(int i) { return 2; }" +
				"  public long test() { return m(); }" +
				"}"));
	}
}
