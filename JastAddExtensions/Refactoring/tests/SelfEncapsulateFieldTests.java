package tests;

import junit.framework.TestCase;
import AST.FieldDeclaration;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class SelfEncapsulateFieldTests extends TestCase {
	private void testFail(String hosttp, String name, Program in) {
		assertNotNull(in);
		TypeDecl td = in.findType(hosttp);
		assertNotNull(td);
		FieldDeclaration fd = td.findField(name);
		assertNotNull(fd);
		try {
			fd.doSelfEncapsulate();
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) { 
		}
	}

	public void test1() {
		testFail("B", "f",
 				Program.fromClasses(
 				"class A {" +
 				"  protected long getF() {" +
 				"    return 0;" +
 				"  }" +
 				"  public long m() {" +
 				"    return getF();" +
 				"  }" +
 				"}",
 				"class B extends A {" +
 				"  int f = 71;" +
 				"  public long test() {" +
 				"    return m();" +
 				"  }" +
 				"}"));
	}
}