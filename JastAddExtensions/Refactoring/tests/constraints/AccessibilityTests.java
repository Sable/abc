package tests.constraints;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;
import AST.ASTNode;
import AST.AccessibilityConstraint;
import AST.Modifier;
import AST.Program;
import AST.RawCU;
import AST.RefactoringException;
import AST.TypeDecl;
import AST.Visible;

public class AccessibilityTests extends TestCase {
	private void testPossibleVisibilities(Visible element, String should) {
		assertNotNull(element);
		Program prog = ((ASTNode)element).programRoot();
		char[] is = { '-', '-', '-', '-' };
		int orig = element.getVisibility();
		
		// Generate all constraints
		for(Map<Visible, Integer> vismap : prog.allPossibleSolutions()) {
			Integer vis = vismap.get(element);
			is[vis == null ? orig : vis] = '+';
		}
		assertEquals(should, new String(is));

		// Generate necessary constraints, only
		for(Map<Visible, Integer> vismap : allPossibleSolutions(prog, Collections.singleton(element))) {
			Integer vis = vismap.get(element);
			is[vis == null ? orig : vis] = '+';
		}
		assertEquals(should, new String(is));
}
	
	private Collection<Map<Visible, Integer>> allPossibleSolutions(Program prog, Collection<Visible> startValues) {
		Collection<AccessibilityConstraint> constraints = prog.accessibilityConstraints();
		return prog.allPossibleSolutions(prog.generateNetwork(constraints, false), constraints);
	}
	
	public void testSucc(String tp, int vis, Program in, Program out) {
		assertTrue(ASTNode.VIS_PRIVATE <= vis && vis <= ASTNode.VIS_PUBLIC);
		assertNotNull(in);
		assertNotNull(out);
		TypeDecl td = in.findType(tp);
		assertNotNull(tp);
		try {
			td.changeAccessibility(vis);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException e) {
			assertEquals(out.toString(), e.getMessage());
		}		
	}
	
	public void testFail(String tp, int vis, Program in) {
		assertTrue(ASTNode.VIS_PRIVATE <= vis && vis <= ASTNode.VIS_PUBLIC);
		assertNotNull(in);
		TypeDecl td = in.findType(tp);
		assertNotNull(tp);
		try {
			td.changeAccessibility(vis);
			assertEquals("<FAILURE>", in.toString());
		} catch(RefactoringException e) {
		}		
		
	}
	
	public void test1() {
		Program prog = Program.fromCompilationUnits(
				new RawCU("A.java", "package p; public class A { }"),
				new RawCU("B.java", "package q; public class B extends p.A { }"));
		testPossibleVisibilities(prog.findType("p.A"), "---+");
	}
	
	public void test2() {
		Program prog = Program.fromBodyDecls(
				"private void m() { }",
				"void n() { m(); }");
		testPossibleVisibilities(prog.findMethod("m"), "++++");
	}
	
	public void test3() {
		Program prog = Program.fromClasses(
				"class A { void m() { } }",
				"class B { { new A().m(); } }");
		testPossibleVisibilities(prog.findMethod("m"), "-+++");
	}
	
	public void test4() {
		Program prog = Program.fromCompilationUnits(
				new RawCU("A.java", "package p; public class A { void m() { } }"),
				new RawCU("B.java", "package p; class B extends A { { m(); } }"));
		testPossibleVisibilities(prog.findMethod("m"), "-+++");
	}
	
	public void test5() {
		Program prog = Program.fromCompilationUnits(
				new RawCU("A.java", "package p; public class A { public void m() { } }"),
				new RawCU("B.java", "package q; class B extends p.A { { m(); } }"));
		testPossibleVisibilities(prog.findMethod("m"), "--++");
	}
	
	public void test6() {
		Program prog = Program.fromCompilationUnits(
				new RawCU("A.java", "package p; public class A { public void m() { } }"),
				new RawCU("B.java", "package q; import p.A; class B extends A { { new A().m(); } }"));
		testPossibleVisibilities(prog.findMethod("m"), "---+");
	}
	
	public void test7() {
		Program prog = Program.fromCompilationUnits(
				new RawCU("A.java", "package p; public class A { public A() { } }"),
				new RawCU("B.java", "package p; public class B extends A { }"));
		testPossibleVisibilities(prog.findConstructor("A"), "-+++");
	}
	
	public void test8() {
		Program prog = Program.fromCompilationUnits(
				new RawCU("A.java", "package p; public class A { public A(int i) { } }"),
				new RawCU("B.java", "package q; public class B extends p.A { public B() { super(1); } }"));
		testPossibleVisibilities(prog.findConstructor("A"), "--++");
	}
	
	public void test9() {
		Program prog = Program.fromCompilationUnits(
				new RawCU("A.java", "package p; public class A { public A(int i) { } }"),
				new RawCU("B.java", "package q; public class B { p.A a = new p.A(1) { }; }"));
		testPossibleVisibilities(prog.findConstructor("A"), "--++");
	}
	
	public void test10() {
		Program prog = Program.fromCompilationUnits(
				new RawCU("A.java", "package p; public class A { public A(int i) { } }"),
				new RawCU("B.java", "package q; public class B { p.A a = new p.A(1); }"));
		testPossibleVisibilities(prog.findConstructor("A"), "---+");
	}
	
	public void test11() {
		Program prog = Program.fromCompilationUnits(
				new RawCU("A.java", "package p; public class A { protected int i; { new q.B().i = 1; } }"),
				new RawCU("B.java", "package q; public class B extends p.A { }"));
		testPossibleVisibilities(prog.findField("i"), "--++");
	}
	
	public void test12() {
		Program prog = Program.fromClasses(
				"class A { void m() { } }" +
				"class B extends A { void m() { } }");
		testPossibleVisibilities(prog.findType("B").findMethod("m"), "-+++");
		testPossibleVisibilities(prog.findType("A").findMethod("m"), "-+++");
	}
	
	public void test13() {
		Program prog = Program.fromClasses(
				"class A { private void m() { } }" +
				"class B extends A { private void m() { m(); } }");
		testPossibleVisibilities(prog.findType("B").findMethod("m"), "++++");
		testPossibleVisibilities(prog.findType("A").findMethod("m"), "+---");
	}
	
	public void test14() {
		Program prog = Program.fromCompilationUnits(
				new RawCU("A.java", "package p; public class A { protected void m() { } }"),
				new RawCU("B.java", "package q; public class B extends p.A { protected void m() { } }"));
		testPossibleVisibilities(prog.findType("B").findMethod("m"), "--++");
		testPossibleVisibilities(prog.findType("A").findMethod("m"), "--++");
	}
	
	public void test15() {
		Program prog = Program.fromCompilationUnits(
				new RawCU("A.java", "package p; public class A { void m() { } }"),
				new RawCU("B.java", "package q; public class B extends p.A { protected void m() { m(); } }"));
		testPossibleVisibilities(prog.findType("B").findMethod("m"), "++++");
		testPossibleVisibilities(prog.findType("A").findMethod("m"), "++--");
	}
	
	public void test16() {
		Program prog = Program.fromClasses(
				"interface I { void m(); }" +
				"abstract class A implements I { }" +
				"class B extends A { public void m() { } }");
		testPossibleVisibilities(prog.findType("B").findMethod("m"), "---+");
	}
	
	public void test17() {
		Program prog = Program.fromClasses(
				"class A { private class Inner { Inner inner; } }",
				"interface I { class Inner { } }",
				"class B extends A implements I { Inner x; }");
		testPossibleVisibilities(prog.findType("A").findSimpleType("Inner"), "++++");
	}
	
	public void test18() {
		testFail("Anonymous0", Modifier.VIS_PROTECTED, Program.fromClasses(
				"class A { void n() { A a = new A(){}; } } "));
	}
	
	public void test19() {
		Program prog = Program.fromCompilationUnits(
				new RawCU("A.java", "package p; public class A { public void m(){}}"),
				new RawCU("B.java", "package p; public class B { public A getA(){return null;}}"),
				new RawCU("C.jvav", "package q; public class C { void n(){new p.B().getA().m();}}"));
		testFail("A", Modifier.VIS_PACKAGE, prog);
	}
}
