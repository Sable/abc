package tests;

import junit.framework.TestCase;
import AST.ClassDecl;
import AST.InterfaceDecl;
import AST.Program;
import AST.RawCU;
import AST.RefactoringException;

public class IntroduceInheritanceTests extends TestCase {
	private void testSucc(String className, String ifaceName, Program in, Program out) {
		assertNotNull(in);
		assertNotNull(out);
		ClassDecl cd = (ClassDecl)in.findType(className);
		InterfaceDecl id = (InterfaceDecl)in.findType(ifaceName);
		try {
			cd.doImplement(id);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.toString());
		}
	}
	
	private void testFail(String className, String ifaceName, Program in) {
		assertNotNull(in);
		ClassDecl cd = (ClassDecl)in.findType(className);
		InterfaceDecl id = (InterfaceDecl)in.findType(ifaceName);
		try {
			cd.doImplement(id);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) {
		}
	}
	
	public void test1() {
		testSucc("A", "I",
				Program.fromClasses(
				"interface I { }",
				"class A implements I { }"),
				Program.fromClasses(
				"interface I { }",
				"class A implements I { }"));
	}
	
	public void test2() {
		testSucc("A", "I",
				Program.fromClasses(
				"interface I { }",
				"class A { }"),
				Program.fromClasses(
				"interface I { }",
				"class A implements I { }"));
	}
	
	public void test3() {
		testSucc("A", "I",
				Program.fromCompilationUnits(
				new RawCU("I.java", "package p; interface I { }"),
				new RawCU("A.java", "package q; class A { }")),
				Program.fromCompilationUnits(
				new RawCU("I.java", "package p; public interface I { }"),
				new RawCU("A.java", "package q; class A implements p.I { }")));
	}
	
	public void test4() {
		testSucc("A", "I",
				Program.fromClasses(
				"interface I { void m(); }",
				"class A { public void m() { } }"),
				Program.fromClasses(
				"interface I { void m(); }",
				"class A implements I { public void m() { } }"));
	}
	
	public void test5() {
		testSucc("A", "I",
				Program.fromClasses(
				"interface I { void m(); }",
				"class A { void m() { } }"),
				Program.fromClasses(
				"interface I { void m(); }",
				"class A implements I { public void m() { } }"));
	}
	
	public void test6() {
		testSucc("A", "I",
				Program.fromClasses(
				"interface I { int hashCode(); }",
				"class A { }"),
				Program.fromClasses(
				"interface I { int hashCode(); }",
				"class A implements I { }"));
	}
	
	public void test7() {
		testFail("A", "I",
				Program.fromClasses(
				"interface I { int hash(); }",
				"class A { }"));
	}
}
