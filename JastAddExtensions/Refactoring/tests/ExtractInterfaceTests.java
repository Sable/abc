package tests;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;
import AST.ClassDecl;
import AST.MethodDecl;
import AST.Program;
import AST.RawCU;
import AST.RefactoringException;
import AST.SimpleSet;
import AST.TypeDecl;

public class ExtractInterfaceTests extends TestCase {
	private void testSucc(String className, String[] signatures, String pkg, String iface, Program in, Program out) {
		assertNotNull(in);
		assertNotNull(out);
		
		TypeDecl td = in.findType(className);
		assertTrue(td instanceof ClassDecl);
		
		Collection<MethodDecl> mds = new ArrayList<MethodDecl>();
		for(String sig : signatures) {
			SimpleSet s = td.localMethodsSignature(sig);
			assertTrue(s instanceof MethodDecl);
			mds.add((MethodDecl)s);
		}
		
		assertNotNull(iface);
		
		try {
			((ClassDecl)td).doExtractInterface(pkg, iface, mds);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
	}

	private void testFail(String className, String[] signatures, String pkg, String iface, Program in) {
		assertNotNull(in);
		
		TypeDecl td = in.findType(className);
		assertTrue(td instanceof ClassDecl);
		
		Collection<MethodDecl> mds = new ArrayList<MethodDecl>();
		for(String sig : signatures) {
			SimpleSet s = td.localMethodsSignature(sig);
			assertTrue(s instanceof MethodDecl);
			mds.add((MethodDecl)s);
		}
		
		assertNotNull(iface);
		
		try {
			((ClassDecl)td).doExtractInterface(pkg, iface, mds);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) {
		}
	}

	public void test1() {
		testSucc("X.C", new String[]{"foo(X.B)"}, null, "I",
				Program.fromClasses(
				"class X {" +
				"  private class B { }" +
				"  static class C {" +
				"    public void foo(B b){}" +
				"    public void bar(){}" +
				"  }" +
				"}" +
				"interface Y {" +
				"  public class B { }" +
				"}" +
				"class Z extends X implements Y {" +
				"  Z(X.C p, X.C q){ q.bar(); }" +
				"  Z(X.C r, Object s){" +
				"    r.bar();" +
				"    Z z = new Z(r,r);" +
				"  }" +
				"  B f;" +
				"}"),
				Program.fromClasses(
				"interface I {" +
				"  abstract public void foo(X.B b);" +
				"}" +
				"class X {" +
				"  class B { }" +
				"  static class C implements I {" +
				"    public void foo(B b){}" +
				"    public void bar(){}" +
				"  }" +
				"}" +
				"interface Y {" +
				"  public class B { }" +
				"}" +
				"class Z extends X implements Y {" +
				"  Z(I p, X.C q){ q.bar(); }" +
				"  Z(X.C r, Object s){" +
				"    r.bar();" +
				"    Z z = new Z((I)r, (C)r);" +
				"  }" +
				"  Y.B f;" +
				"}"));
	}
	
	public void test2() {
		testSucc("p.X.C", new String[]{"foo(p.X.B)"}, "q", "I",
				Program.fromCompilationUnits(
				new RawCU("X.java",
				"package p;" +
				"class X {" +
				"  private class B { }" +
				"  static class C {" +
				"    public void foo(B b){}" +
				"    public void bar(){}" +
				"  }" +
				"}" +
				"interface Y {" +
				"  public class B { }" +
				"}" +
				"class Z extends X implements Y {" +
				"  Z(X.C p, X.C q){ q.bar(); }" +
				"  Z(X.C r, Object s){" +
				"    r.bar();" +
				"    Z z = new Z(r,r);" +
				"  }" +
				"  B f;" +
				"}")),
				Program.fromCompilationUnits(
				new RawCU("I.java",
				"package q;" +
				"public interface I {" +
				"  abstract public void foo(p.X.B b);" +
				"}"),
				new RawCU("X.java",
				"package p;" +
				"public class X {" +
				"  public class B { }" +
				"  static class C implements q.I {" +
				"    public void foo(B b){}" +
				"    public void bar(){}" +
				"  }" +
				"}" +
				"interface Y {" +
				"  public class B { }" +
				"}" +
				"class Z extends X implements Y {" +
				"  Z(q.I p, X.C q){ q.bar(); }" +
				"  Z(X.C r, Object s){" +
				"    r.bar();" +
				"    Z z = new Z((q.I)r, (C)r);" +
				"  }" +
				"  Y.B f;" +
				"}")));
	}
	
	public void test3() {
		testFail("A", new String[]{"m()"}, "p", "I", Program.fromCompilationUnits(
				new RawCU("A.java",
						  "package p;" +
						  "class A {" +
						  "  public static void m() { }" +
						  "}")));
	}
	
	/* rtt test 2010_10_19 12_36: methods:firePropertyChange, firePropertyChange, addPropertyChangeListener, removePropertyChangeListener, addPropertyChangeListener, firePropertyChange, hasListeners, removePropertyChangeListener, firePropertyChange, , interface package:RTT_NEW_PACKAGE, class:org.w3c.tools.jdbc.JdbcPropertyChangeSupport, interface name:RTT_NEW_INTERFACE, */
	public void test4() {
		testSucc("A", new String[]{"m()"}, "", "I", 
			Program.fromCompilationUnits(
					new RawCU("A.java", "class A { public synchronized void m() {} } "),
					new RawCU("B.java", "class B { A a; void n(){a.m();}}")),
			Program.fromCompilationUnits(
					new RawCU("A.java","class A implements I { public synchronized void m() {} } "),
					new RawCU("B.java","class B { I a; void n(){a.m();}}"),
					new RawCU("I.java","interface I {abstract public void m();}")));
	}
	
	public void test5() {
		testSucc("A", new String[]{}, "p", "I", 
			Program.fromCompilationUnits(
				new RawCU("A.java",
						  "package p;" +
						  "class A {" +
						  "  static A m() {return null;}" +
						  "  void n() {new A().m().n();}" +
						  "}"),
				new RawCU("B.java",
						  "package p;" +
						  "class B extends A {" +
						  "   static A m() {return null;}" + 
						  "}")),
			Program.fromCompilationUnits(
				new RawCU("A.java",
						  "package p;" +
						  "class A implements I {" +
						  "  static A m() {return null;}" +
						  "  void n() {new A().m().n();}" +
						  "}"),
				new RawCU("B.java",
						  "package p;" +
						  "class B extends A {" +
						  "   static A m() {return null;}" + 
						  "}"),
				new RawCU("I.java",
						  "package p;"+
						  "interface I {}")));
    }
	
	public void test6() {
		testFail("A", new String[]{}, "p", "I", 
			Program.fromCompilationUnits(
				new RawCU("A.java",
						  "package p;" +
						  "class A extends Exception {}"),
				new RawCU("B.java",
						  "package p;" +
						  "class B {" +
						  "   A a; " +
						  "   void m() throws A { throw a; }" + 
						  "}")));
    }
	
	public void test7() {
		testSucc("A", new String[]{"m()"}, "q", "I", 
			Program.fromCompilationUnits(
				new RawCU("A.java",
						  "package p;" +
						  "public class A {" +
						  "  private void m() {}" +
						  "  A a;" +
						  "  void n() {a.m();}" +
						  "}")),
			Program.fromCompilationUnits(
				new RawCU("A.java",
						  "package p;" +
						  "public class A implements q.I {" +
						  "  public void m() {}" +
						  "  A a;" +
						  "  void n() {a.m();}" +
						  "}"),
				new RawCU("I.java",
						  "package q;"+
						  "public interface I {abstract public void m();}")));
    }
	
	public void test8() {
		testSucc("p.A", new String[]{"n(p.Y)"}, "p", "J",
				Program.fromCompilationUnits(new RawCU("A.java",
				"package p;" +
				"class X {}" +
				"class Y extends X {}" +
				"interface I {X m(X x);}" +
				"class A implements I {" +
				"  public X m(X x) {return null;}" +
				"  public Y n(Y y) {return null;}" +
				"}" +
				"class B {" +
				"  void k(I i) {i = new A();}" +
				"  void l(A a) {k(a);}"+
				"  I ii;"+
				"  void m() {k(ii);}"+
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"package p;" +
				"class X {}" +
				"class Y extends X {}" +
				"interface I {X m(X x);}" +
				"class A implements I, J {" +
				"  public X m(X x) {return null;}" +
				"  public Y n(Y y) {return null;}" +
				"}" +
				"class B {" +
				"  void k(I i) {i = new A();}" +
				"  void l(A a) {k(a);}"+
				"  I ii;"+
				"  void m() {k(ii);}"+
				"}"),
				new RawCU("J.java",
				"package p;" +
				"interface J {abstract public Y n(Y y);}")));
	}
	
	public void test9() {
		testSucc("p.A", new String[]{"getA()"}, "p", "I",
				Program.fromCompilationUnits(new RawCU("A.java",
				"package p;" +
				"class A {" +
				"  public A getA() {return new A();}" +
				"}" +
				"class B {" +
				"  A a;" +
				"  void n(){" +
				"    A b = a.getA();" +
				"    A array[] = {b};" +
				"  }" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"package p;" +
				"class A implements I {" +
				"  public A getA() {return new A();}" +
				"}" +
				"class B {" +
				"  I a;" +
				"  void n(){" +
				"    A b = a.getA();" +
				"    A array[] = {b};" +
				"  }" +
				"}"), new RawCU("I.java",
				"package p;" +
				"interface I {" +
				"  abstract public A getA();" +
				"}")));
	}
	
	public void test10() {
		testSucc("p.A", new String[]{"m()"}, "q", "I",
				Program.fromCompilationUnits(new RawCU("A.java",
				"package p;" +
				"class A {" +
				"  @B public void m(){}" +
				"}"),
				new RawCU("B.java","package p; public @interface B {}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"package p;" +
				"class A implements q.I {" +
				"  @B public void m(){}" +
				"}"),
				new RawCU("B.java","package p; public @interface B {}"),
				new RawCU("I.java","package q; public interface I {public abstract void m(); }")));
	}
	
	public void test11() {
		testSucc("p.A", new String[]{"toString()"}, "p", "I",
				Program.fromCompilationUnits(new RawCU("A.java",
				"package p;" +
				"class A {" +
				"  @Override public String toString(){return new String();}" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"package p;" +
				"class A implements I {" +
				"  @Override public String toString(){return new String();}" +
				"}"),
				new RawCU("I.java","package p; interface I {abstract public String toString();}")));
	}
	
	public void test12() {
		testSucc("p.A", new String[]{}, "p", "I",
				Program.fromCompilationUnits(new RawCU("A.java",
				"package p;" +
				"class A extends B {" +
				"  public A(A a){super(a);}" +
				"}" +
				"class B<T> { public B(B<T> b){} }")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"package p;" +
				"class A extends B implements I {" +
				"  public A(A a){super(a);}" +
				"}" +
				"class B<T> { public B(B<T> b){} }"),
				new RawCU("I.java","package p; interface I {}")));
	}
	
	public void test13() {
		testSucc("p.A", new String[]{}, "p", "I",
				Program.fromCompilationUnits(new RawCU("A.java",
				"package p;" +
				"abstract class A implements java.lang.Iterable {}" +
				"class B { " +
				"  A a;" +
				"  void m(){" +
				"    for(Object o : a) {}" +
				"  } " +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"package p;" +
				"abstract class A implements java.lang.Iterable, I {}" +
				"class B { " +
				"  A a;" +
				"  void m(){" +
				"    for(Object o : a) {}" +
				"  } " +
				"}"),
				new RawCU("I.java","package p; interface I {}")));
	}
	
	public void test14() {
		testSucc("p.C", new String[]{"m(p.C.B)"}, "p", "I",
				Program.fromCompilationUnits(new RawCU("C.java",
				"package p;" +
				"class C {" +
				"  private class B { }" +
				"  void m(B b) { }" +
				"  void n() { }" +
				"}" +
				"" +
				"interface J {" +
				"  class B { }" +
				"}" +
				"" +
				"class D extends C implements J {" +
				"  D(C c1, D d) { c1.m(null); }" +
				"  D(C c2, C o) { c2.n(); D d = new D(c2, null); }" +
				"  B f;" +
				"}")),
				Program.fromCompilationUnits(new RawCU("C.java",
				"package p;" +
				"class C implements I {" +
				"  class B { }" +
				"  public void m(B b) { }" +
				"  void n() { }" +
				"}" +
				"" +
				"interface J {" +
				"  class B { }" +
				"}" +
				"" +
				"class D extends C implements J {" +
				"  D(I c1, D d) { c1.m(null); }" +
				"  D(C c2, I o) { c2.n(); D d = new D((I)c2, (D)null); }" +
				"  J.B f;" +
				"}"),
				new RawCU("I.java",
				"package p;" +
				"interface I {" +
				"  abstract public void m(C.B b);" +
				"}")));
	}
	
	public void test15() {
		testSucc("p.C", new String[]{"m(p.C.B)"}, "q", "I",
				Program.fromCompilationUnits(new RawCU("C.java",
				"package p;" +
				"class C {" +
				"  private class B { }" +
				"  void m(B b) { }" +
				"  void n() { }" +
				"}" +
				"" +
				"interface J {" +
				"  class B { }" +
				"}" +
				"" +
				"class D extends C implements J {" +
				"  D(C c1, D d) { c1.m(null); }" +
				"  D(C c2, C o) { c2.n(); D d = new D(c2, null); }" +
				"  B f;" +
				"}")),
				Program.fromCompilationUnits(new RawCU("C.java",
				"package p;" +
				"public class C implements q.I {" +
				"  public class B { }" +
				"  public void m(B b) { }" +
				"  void n() { }" +
				"}" +
				"" +
				"interface J {" +
				"  class B { }" +
				"}" +
				"" +
				"class D extends C implements J {" +
				"  D(q.I c1, D d) { c1.m(null); }" +
				"  D(C c2, q.I o) { c2.n(); D d = new D((q.I)c2, (D)null); }" +
				"  J.B f;" +
				"}"),
				new RawCU("I.java",
				"package q;" +
				"public interface I {" +
				"  abstract public void m(p.C.B b);" +
				"}")));
	}
	
	public void test16() {
		testSucc("A", new String[]{"m()"}, "", "I",
				Program.fromCompilationUnits(new RawCU("A.java",
				"interface J {" +
				"  void m();" +
				"}" +
				"interface K extends J { }" +
				"class A implements K {" +
				"  public void m() { }" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"interface J {" +
				"  void m();" +
				"}" +
				"interface K extends J { }" +
				"class A implements K, I {" +
				"  public void m() { }" +
				"}"),
				new RawCU("I.java",
				"interface I {" +
				"  public abstract void m();" +
				"}")));
	}
}