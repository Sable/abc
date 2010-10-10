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
	
	public void test1() {
		testSucc("X.C", new String[]{"foo(X.B)"}, null, "I",
				Program.fromClasses(
				"class X {" +
				"  private class B { }" +
				"  static class C {" +
				"    public void foo(B b){}" +
				"    public void bar(){}" +
				"  }" +
				"}",
				"interface Y {" +
				"  public class B { }" +
				"}",
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
				"}",
				"class X {" +
				"  class B { }" +
				"  static class C implements I {" +
				"    public void foo(B b){}" +
				"    public void bar(){}" +
				"  }" +
				"}",
				"interface Y {" +
				"  public class B { }" +
				"}",
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
				"" +
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
}
