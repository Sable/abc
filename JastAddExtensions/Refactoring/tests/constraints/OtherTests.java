package tests.constraints;

import junit.framework.TestCase;
import AST.ASTNode;
import AST.FieldDeclaration;
import AST.Program;
import AST.RawCU;

public class OtherTests extends TestCase {
	public void test1() {
		Program in = Program.fromCompilationUnits(
			new RawCU("A.java",
				"package p;" +
				"class A {" +
				"  private static int i;" +
				"}" +
				"interface I {" +
				"  static int i = 42;" +
				"}" +
				"class B extends A implements I {" +
				"  Object I;" +
				"  int x = i;" +
				"}"));
		assertNotNull(in);
		String orig = in.toString();
		FieldDeclaration fd = in.findField("i");
		assertNotNull(fd);
		fd.changeAccessibility(ASTNode.VIS_PROTECTED);
		assertEquals(orig, in.toString());
	}
}
