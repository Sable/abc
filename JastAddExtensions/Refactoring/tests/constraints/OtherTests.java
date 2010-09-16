package tests.constraints;

import junit.framework.TestCase;
import AST.ASTNode;
import AST.FieldDeclaration;
import AST.MethodDecl;
import AST.Program;
import AST.RawCU;
import AST.SimpleSet;
import AST.TypeDecl;

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
	
	public void test2() {
		Program in = Program.fromCompilationUnits(
			new RawCU("A.java",
				"package p;" +
				"class Super {" +
				"  private void f() { }" +
				"}" +
				"class Outer {" +
				"  void f() { }" +
				"  class Inner extends Super {" +
				"    { f(); }" +
				"  }" +
				"}"));
		assertNotNull(in);
		String orig = in.toString();
		TypeDecl td = in.findType("Super");
		assertNotNull(td);
		SimpleSet meths = td.localMethods("f");
		assertFalse(meths.isEmpty());
		MethodDecl md = (MethodDecl)meths.iterator().next();
		md.changeAccessibility(ASTNode.VIS_PACKAGE);
		assertEquals(orig, in.toString());
	}
	
	public void test3() {
		Program in = Program.fromClasses(
				"class A { private class Inner { } }",
				"interface I { class Inner { } }",
				"class B extends A implements I { Inner x; }");
		assertNotNull(in);
		String orig = in.toString();
		in.findType("A").findSimpleType("Inner").changeAccessibility(ASTNode.VIS_PUBLIC);
		assertEquals(orig, in.toString());
	}
}
