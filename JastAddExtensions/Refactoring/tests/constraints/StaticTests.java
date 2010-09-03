package tests.constraints;

import java.util.Collection;

import junit.framework.TestCase;
import AST.AccessibilityConstraint;
import AST.Program;

public class StaticTests extends TestCase {
	private void testSucc(Program p) {
		Collection<AccessibilityConstraint> accessibilityConstraints = p.accessibilityConstraints();
		for(AccessibilityConstraint constr : accessibilityConstraints)
			assertTrue(constr.isSolved());
	}
	
	public void test1() {
		testSucc(Program.fromClasses(
				"class A {" +
				"  A a;" +
				"}"));
	}
	
	public void test2() {
		Program p = Program.fromClasses(
				"class A {" +
				"  class Inner {" +
				"    int[] xs; " +
				"    private class B { int[] ys = xs; }" +
				"  }" +
				"  class Inner2 {" +
				"    Inner.B b;" +
				"  }" +
				"}");
		p.solve();
	}
}
