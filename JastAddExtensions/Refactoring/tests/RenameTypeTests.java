package tests;

import junit.framework.TestCase;
import AST.Program;
import AST.RawCU;
import AST.RefactoringException;
import AST.TypeDecl;

public class RenameTypeTests extends TestCase {
	public RenameTypeTests(String name) {
		super(name);
	}
	
	public void testSucc(String pkg, String old_name, String new_name, Program in, Program out) {
		assertNotNull(in);
		String originalProgram = in.toString();
		in.RECORDING_CHANGES = true;
		assertNotNull(out);
		TypeDecl tp = in.findType(pkg, old_name);
		assertNotNull(tp);
		try {
			tp.rename(new_name);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException e) {
			fail("Refactoring was supposed to succeed, failed with "+e);
		}
		in.undoAll();
		assertEquals(originalProgram, in.toString());
//		assertEquals(out.toString(), in.toString());
	}
	
	public void testSucc(String old_name, String new_name, Program in, Program out) {
		assertNotNull(in);
		String originalProgram = in.toString();
		in.RECORDING_CHANGES = true;
		assertNotNull(out);
		TypeDecl tp = in.findType(old_name);
		assertNotNull(tp);
		try {
			tp.rename(new_name);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException e) {
			fail("Refactoring was supposed to succeed, failed with "+e);
		}
		in.undoAll();
		assertEquals(originalProgram, in.toString());
//		assertEquals(out.toString(), in.toString());
	}
	
	public void testFail(String pkg, String old_name, String new_name, Program in) {
		assertNotNull(in);
		String originalProgram = in.toString();
		in.RECORDING_CHANGES = true;
		TypeDecl tp = in.findType(pkg, old_name);
		assertNotNull(tp);
		try {
			tp.rename(new_name);
			fail("Refactoring was supposed to fail, succeeded with "+in);
		} catch(RefactoringException e) {
		}
		in.undoAll();
		assertEquals(originalProgram, in.toString());
	}
	
	public void testFail(String old_name, String new_name, Program in) {
		assertNotNull(in);
		String originalProgram = in.toString();
		in.RECORDING_CHANGES = true;
		TypeDecl tp = in.findType(old_name);
		assertNotNull(tp);
		try {
			tp.rename(new_name);
			fail("Refactoring was supposed to fail, succeeded with "+in);
		} catch(RefactoringException e) {
		}
		in.undoAll();
		assertEquals(originalProgram, in.toString());
	}
	
	public void test0() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "}"))
        );
    }

    public void test1() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "  A a;"+
                      "};"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  B a;"+
                      "}"))
        );
    }

    public void test2() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "   A A;"+
                      "   A A(A A){"+
                      "     A:"+
                      "        for (;;){"+
                      "          if (A.A(A)==A)"+
                      "             break A;"+
                      "        }"+
                      "      return A;"+
                      "   };"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  B A;"+
                      "  B A(B A) {"+
                      "    A:"+
                      "      for(; true; ) {"+
                      "        if(A.A(A) == A) "+
                      "          break A;"+
                      "      }"+
                      "    return A;"+
                      "  }"+
                      "}"))
        );
    }

    public void test3() {
        testSucc(
            "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "    class C { "+
                      "        public <T> C() { }"+
                      "    }"+
                      "}"+
                      "class D {"+
                      "    Object o = new A().new<A> C();"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "  "+
                      "  class C {"+
                      "    public  <T extends java.lang.Object> C() {"+
                      "      super();"+
                      "    }"+
                      "  }"+
                      "}"+
                      "class D {"+
                      "  Object o = new B().new<B> C();"+
                      "}"))
        );
    }

    public void test4() {
        testSucc(
            "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A { }"+
                      "class X {"+
                      "    class Y {"+
                      "	       <T> Y() { }"+
                      "    }"+
                      "}"+
                      "class Z extends X.Y {"+
                      "    Z() {"+
                      "	       new X().<A> super();"+
                      "    }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "}"+
                      "class X {"+
                      "  "+
                      "  class Y {"+
                      "     <T extends java.lang.Object> Y() {"+
                      "      super();"+
                      "    }"+
                      "  }"+
                      "}"+
                      "class Z extends X.Y {"+
                      "  Z() {"+
                      "    new X().<B>super();"+
                      "  }"+
                      "}"))
        );
    }

    public void test5() {
        testSucc(
            "MyThread", "Thread",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "    public static void main(String[] args) {"+
                      "	       new Thread() {"+
                      "	           public void run() {"+
                      "		           System.out.println(23);"+
                      "	           }"+
                      "	       }.start();"+
                      "    }"+
                      "}"+
                      "class MyThread {"+
                      "    public void start() {"+
                      "	       System.out.println(42);"+
                      "    }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "public class A {"+
                      "  public static void main(String[] args) {"+
                      "    new java.lang.Thread() {"+
                      "        public void run() {"+
                      "          System.out.println(23);"+
                      "        }"+
                      "    }.start();"+
                      "  }"+
                      "}"+
                      "class Thread {"+
                      "  public void start() {"+
                      "    System.out.println(42);"+
                      "  }"+
                      "}"))
        );
    }

    public void test6() {
        testSucc(
            "p", "A.C", "java",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "    class C { }"+
                      "    { java.lang.System.out.println(\"Hello, world!\"); }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "public class A {"+
                      "  "+
                      "  class java {"+
                      "  }"+
                      "  {"+
                      "    System.out.println(\"Hello, world!\");"+
                      "  }"+
                      "}"))
        );
    }

    public void test7() {
        testSucc(
            "p", "A.B.C", "F",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "  static class B {"+
                      "    static class C {"+
                      "      static class D { }"+
                      "    }"+
                      "  }"+
                      "}"+
                      "class E extends A.B.C.D { }"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "public class A {"+
                      "  static class B {"+
                      "    static class F {"+
                      "      static class D {"+
                      "      }"+
                      "    }"+
                      "  }"+
                      "}"+
                      "class E extends A.B.F.D {"+
                      "}"))
        );
    }

    public void test8() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A {"+
                      "  p.A a;"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  B a;"+
                      "}"))
        );
    }

    public void test9() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "}"))
        );
    }

    public void test10() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "  void m(){"+
                      "    A a = (A)new Object();"+
                      "  };"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  void m() {"+
                      "    B a = (B)new Object();"+
                      "  }"+
                      "}"))
        );
    }

    public void test11() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "  void m(){"+
                      "    boolean b = (new A()) instanceof A;"+
                      "  };"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  void m() {"+
                      "    boolean b = (new B()) instanceof B;"+
                      "  }"+
                      "}"))
        );
    }

    public void test12() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "  A a = new A();"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  B a = new B();"+
                      "}"))
        );
    }

    public void test13() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "   static void s(){};"+
                      "}"+
                      "class AA{"+
                      "   AA(){ "+
                      "     A.s();"+
                      "   };   "+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  static void s() {"+
                      "  }"+
                      "}"+
                      "class AA {"+
                      "  AA() {"+
                      "    super();"+
                      "    B.s();"+
                      "  }"+
                      "}"))
        );
    }

    public void test14() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "   boolean A = new A() instanceof A;"+
                      "   A A(A A){"+
                      "     A:"+
                      "        for (;;){"+
                      "          if (A.A(A)==A)"+
                      "             break A;"+
                      "        }"+
                      "      return A;"+
                      "   };"+
                      "}"+
                      "class AA extends A{"+
                      "   A A = (A) new A();"+
                      "   A A(A A){"+
                      "     A:"+
                      "        for (;;){"+
                      "          if (A.A(A)==A)"+
                      "             break A;"+
                      "        }"+
                      "      return A;"+
                      "   };"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  boolean A = new B() instanceof B;"+
                      "  B A(B A) {"+
                      "    A:"+
                      "      for(; true; ) {"+
                      "        if(A.A(A) == A) "+
                      "          break A;"+
                      "      }"+
                      "    return A;"+
                      "  }"+
                      "}"+
                      "class AA extends B {"+
                      "  B A = (B)new B();"+
                      "  B A(B A) {"+
                      "    A:"+
                      "      for(; true; ) {"+
                      "        if(A.A(A) == A) "+
                      "          break A;"+
                      "      }"+
                      "    return A;"+
                      "  }"+
                      "}"))
        );
    }

    public void test15() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "interface I{"+
                      "  int A = 0;"+
                      "}"+
                      "class A{"+
                      "  int A = I.A; "+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "interface I {"+
                      "  int A = 0;"+
                      "}"+
                      "class B {"+
                      "  int A = I.A;"+
                      "}"))
        );
    }

    public void test16() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A extends Exception{"+
                      "}"+
                      "class AA{"+
                      "  void m(){"+
                      "    try {"+
                      "      throw new A();"+
                      "    }"+
                      "    catch(A a){}"+
                      "  }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B extends Exception {"+
                      "}"+
                      "class AA {"+
                      "  void m() {"+
                      "    try {"+
                      "      throw new B();"+
                      "    }"+
                      "    catch (B a) {"+
                      "    }"+
                      "  }"+
                      "}"))
        );
    }

    public void test17() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A extends Exception{"+
                      "  void m(){"+
                      "    try {"+
                      "      throw new A();"+
                      "    }"+
                      "    catch(A A){}"+
                      "  }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B extends Exception {"+
                      "  void m() {"+
                      "    try {"+
                      "      throw new B();"+
                      "    }"+
                      "    catch (B A) {"+
                      "    }"+
                      "  }"+
                      "}"))
        );
    }

    public void test18() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "  int A;"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  int A;"+
                      "}"))
        );
    }

    public void test19() {
        testSucc(
            "A", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package A;"+
                      "class A{"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package A;"+
                      "class B {"+
                      "}"))
        );
    }

    public void test20() {
        testSucc(
            "RenameType.test21.in", "A", "C",
            Program.fromCompilationUnits(
            new RawCU("B.java", 
                      "package RenameType.test21.in;"+
                      "import RenameType.test21.in.A;"+
                      "class B {"+
                      "}"),
            new RawCU("A.java", 
                      "package RenameType.test21.in;"+
                      "class A{"+
                      "    B b;"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package RenameType.test21.in;"+
                      "import RenameType.test21.in.C;"+
                      "class B {"+
                      "}"),
            new RawCU("A.java",
                      "package RenameType.test21.in;"+
                      "class C {"+
                      "  B b;"+
                      "}"))
        );
    }

    public void test21() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "  A(){};"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
		      "  B(){};"+
                      "}"))
        );
    }

    public void test22() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "  A[] a = new A[5];"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  B[] a = new B[5];"+
                      "}"))
        );
    }

    public void test23() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A extends Exception{"+
                      "  void m() throws A"+
                      "  {};"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B extends Exception {"+
                      "  void m() throws B {"+
                      "  }"+
                      "}"))
        );
    }

    public void test24() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "  Class c = A.class;"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  Class c = B.class;"+
                      "}"))
        );
    }

    public void test25() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "  int x;"+
                      "  class Inner{"+
                      "    void m(){"+
                      "      A.this.x++;"+
                      "    }"+
                      "  }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  int x;"+
                      "  "+
                      "  class Inner {"+
                      "    void m() {"+
                      "      B.this.x++;"+
                      "    }"+
                      "  }"+
                      "}"))
        );
    }

    public void test26() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class Super{"+
                      "  int x;"+
                      "}"+
                      "class A extends Super{"+
                      "  String x;"+
                      "  class Inner{"+
                      "    void m(){"+
                      "      A.super.x++;"+
                      "    }"+
                      "  }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class Super {"+
                      "  int x;"+
                      "}"+
                      "class B extends Super {"+
                      "  String x;"+
                      "  "+
                      "  class Inner {"+
                      "    void m() {"+
                      "      B.super.x++;"+
                      "    }"+
                      "  }"+
                      "}"))
        );
    }

    public void test27() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class Super{"+
                      "  void m1(){};"+
                      "}"+
                      "class A extends Super{"+
                      "  class Inner{"+
                      "    void m(){"+
                      "      A.super.m1();"+
                      "    }"+
                      "  }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class Super {"+
                      "  void m1() {"+
                      "  }"+
                      "}"+
                      "class B extends Super {"+
                      "  "+
                      "  class Inner {"+
                      "    void m() {"+
                      "      B.super.m1();"+
                      "    }"+
                      "  }"+
                      "}"))
        );
    }

    public void test28() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A{};"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "}"))
        );
    }

    public void test29() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "}"+
                      "class AA extends A{"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "}"+
                      "class AA extends B {"+
                      "}"))
        );
    }

    public void test30() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "   static int f;"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  static int f;"+
                      "}"))
        );
    }

    public void test31() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{};"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "}"))
        );
    }

    public void test32() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "}"+
                      "class C{"+
                      "  C(A a){};"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "}"+
                      "class C {"+
                      "  C(B a) {"+
                      "    super();"+
                      "  }"+
                      "}"))
        );
    }

    public void test33() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A extends Exception{"+
                      "}"+
                      "class C{"+
                      "  C() throws A {};"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B extends Exception {"+
                      "}"+
                      "class C {"+
                      "  C() throws B {"+
                      "    super();"+
                      "  }"+
                      "}"))
        );
    }

    public void test34() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "	{A a;}"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "  {"+
                      "    B a;"+
                      "  }"+
                      "}"))
        );
    }

    public void test35() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "	static {A a;}"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "  static {"+
                      "    B a;"+
                      "  }"+
                      "}"))
        );
    }

    public void test36() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "	static {A a;}"+
                      "	static {A a;}"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "  static {"+
                      "    B a;"+
                      "  }"+
                      "  static {"+
                      "    B a;"+
                      "  }"+
                      "}"))
        );
    }

    public void test37() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "	static {A a;}"+
                      "	{A a;}"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "  static {"+
                      "    B a;"+
                      "  }"+
                      "  {"+
                      "    B a;"+
                      "  }"+
                      "}"))
        );
    }

    public void test38() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "}"+
                      "class C{"+
                      "	{A a;}"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "}"+
                      "class C {"+
                      "  {"+
                      "    B a;"+
                      "  }"+
                      "}"))
        );
    }

    public void test39() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "}"+
                      "class C{"+
                      "	static {A a;}"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "}"+
                      "class C {"+
                      "  static {"+
                      "    B a;"+
                      "  }"+
                      "}"))
        );
    }

    public void test40() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "   void A(){};"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  void A() {"+
                      "  }"+
                      "}"))
        );
    }

    public void test41() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "}"+
                      "class C {"+
                      "	void m() {"+
                      "		class A{"+
                      "		}"+
                      "		new A();"+
                      "	}"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "}"+
                      "class C {"+
                      "  void m() {"+
                      "      class A {"+
                      "      }"+
                      "    new A();"+
                      "  }"+
                      "}"))
        );
    }

    public void test42() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "	A(A A){}"+
                      "	A A(A A){"+
                      "		A= new A(new A(A));"+
                      "		return A;"+
                      "	}"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "  B(B A) {"+
                      "    super();"+
                      "  }"+
                      "  B A(B A) {"+
                      "    A = new B(new B(A));"+
                      "    return A;"+
                      "  }"+
                      "}"))
        );
    }

    public void test43() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A {"+
                      "	A(A A){}"+
                      "	A A(A A){"+
                      "		A= new A(new A(A));"+
                      "		return A;"+
                      "	}"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  B(B A) {"+
                      "    super();"+
                      "  }"+
                      "  B A(B A) {"+
                      "    A = new B(new B(A));"+
                      "    return A;"+
                      "  }"+
                      "}"))
        );
    }

    public void test44() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "	static A A;"+
                      "}"+
                      "class X extends p.A{"+
                      "	 void x(){"+
                      "    p.A.A = A.A;"+
                      "	 }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "  static B A;"+
                      "}"+
                      "class X extends B {"+
                      "  void x() {"+
                      "    B.A = A.A;"+
                      "  }"+
                      "}"))
        );
    }

    public void test45() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "interface A {"+
                      "}"+
                      "class K implements A{"+
                      "}"+
                      "interface C extends A{}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "interface B {"+
                      "}"+
                      "class K implements B {"+
                      "}"+
                      "interface C extends B {"+
                      "}"))
        );
    }

    public void test46() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A{};"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "}"))
        );
    }

    public void test47() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "};"+
                      "class C{"+
                      "	void s(){"+
                      "	new A ( );"+
                      "	}"+
                      "};"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "}"+
                      "class C {"+
                      "  void s() {"+
                      "    new B();"+
                      "  }"+
                      "}"))
        );
    }

    public void test48() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "};"+
                      "class C{"+
                      "	void s(){"+
                      "	new p . A ( );"+
                      "	}"+
                      "};"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "}"+
                      "class C {"+
                      "  void s() {"+
                      "    new B();"+
                      "  }"+
                      "}"))
        );
    }

    public void test49() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "	A	( ){};"+
                      "	static int fgT;"+
                      "};"+
                      "class C{"+
                      "	void s(){"+
                      "	new p . A ( );"+
                      "	p"+
                      "	."+
                      "	A"+
                      "	."+
                      "	fgT"+
                      "	="+
                      "	6;"+
                      "	}"+
                      "};"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
		      "  B() { };"+
                      "  static int fgT;"+
                      "}"+
                      "class C {"+
                      "  void s() {"+
                      "    new B();"+
                      "    B.fgT = 6;"+
                      "  }"+
                      "}"))
        );
    }

    public void test50() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "   void m(A a){};"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  void m(B a) {"+
                      "  }"+
                      "}"))
        );
    }

    public void test51() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "/**"+
                      " * Extends {@linkplain A A}."+
                      " * @see A#A()"+
                      " */"+
                      "class A{"+
                      "};"+
                      "class C extends A{"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "}"+
                      "class C extends B {"+
                      "}"))
        );
    }

    public void test52() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A{"+
                      " public class X{}"+
                      "};"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "  "+
                      "  public class X {"+
                      "  }"+
                      "}"))
        );
    }

    public void test53() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A{"+
                      "	A[] m(){"+
                      "		return (A[])new A[3];"+
                      "	}"+
                      "};"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "  B[] m() {"+
                      "    return (B[])new B[3];"+
                      "  }"+
                      "}"))
        );
    }

    public void test54() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A{"+
                      "	A(){}"+
                      "	A(A A){}"+
                      "	A m(){"+
                      "		return (A)new A();"+
                      "	}"+
                      "};"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
		      "  B() {}"+
                      "  B(B A) {"+
                      "    super();"+
                      "  }"+
                      "  B m() {"+
                      "    return (B)new B();"+
                      "  }"+
                      "}"))
        );
    }

    public void test55() {
        testSucc(
            "p", "A.X", "XYZ",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A{"+
                      "	class X{"+
                      "		X(X X){new X(null);}"+
                      "	}"+
                      "	A(){}"+
                      "	A(A A){}"+
                      "	A m(){"+
                      "		new X(null);"+
                      "		return (A)new A();"+
                      "	}"+
                      "};"+
                      "class B{"+
                      "	A.X ax= new A().new X(null);"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "public class A {"+
                      "  "+
                      "  class XYZ {"+
                      "    XYZ(XYZ X) {"+
                      "      super();"+
                      "      new XYZ(null);"+
                      "    }"+
                      "  }"+
                      "  A() {"+
                      "    super();"+
                      "  }"+
                      "  A(A A) {"+
                      "    super();"+
                      "  }"+
                      "  A m() {"+
                      "    new XYZ(null);"+
                      "    return (A)new A();"+
                      "  }"+
                      "}"+
                      "class B {"+
                      "  A.XYZ ax = new A().new XYZ(null);"+
                      "}"))
        );
    }

    public void test56() {
        testFail(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A{"+
                      "	class X{"+
                      "		X(X X){new X(null);}"+
                      "	}"+
                      "	A(){}"+
                      "	A(A A){}"+
                      "	A m(){"+
                      "		new X(null);"+
                      "		return (A)new A();"+
                      "	}"+
                      "};"+
                      "class B{"+
                      "	A.X ax= new A().new X(null);"+
                      "}"))
        );
    }

    public void test57() {
        testSucc(
            "a.a", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package a.a;"+
                      "class A {"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package a.a;"+
                      "class B {"+
                      "}"))
        );
    }

    public void test58() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "/**"+
                      " * p.A "+
                      " * AA A {@link p.A#a}, {@link p.A#b}"+
                      " * @see p.A"+
                      " */"+
                      "public class A{"+
                      " A a;"+
                      "	String aa= \"A\";"+
                      "};"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "  B a;"+
                      "  String aa = \"A\";"+
                      "}"))
        );
    }

    public void test59() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class Sup{"+
                      "	static int CONSTANT= 0;"+
                      "}"+
                      "class A extends Sup {"+
                      "}"+
                      "class Test {"+
                      "  public static void main(String[] arguments) {"+
                      "    System.out.println(A.CONSTANT);"+
                      "  }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class Sup {"+
                      "  static int CONSTANT = 0;"+
                      "}"+
                      "class B extends Sup {"+
                      "}"+
                      "class Test {"+
                      "  public static void main(String[] arguments) {"+
                      "    System.out.println(B.CONSTANT);"+
                      "  }"+
                      "}"))
        );
    }

    public void test60() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A {"+
                      "	void f(){"+
                      "		A a= ( A )this;"+
                      "	}"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  void f() {"+
                      "    B a = (B)this;"+
                      "  }"+
                      "}"))
        );
    }

    public void test61() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "   void m(A A){};"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  void m(B A) {"+
                      "  }"+
                      "}"))
        );
    }

    public void test62() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "/**"+
                      " * p.A "+
                      " * AA A"+
                      " */"+
                      "public class A{"+
                      " A a;"+
                      "	String aa= \"C:\\\\A.java\";"+
                      "};"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "  B a;"+
                      "  String aa = \"C:\\\\A.java\";"+
                      "}"))
        );
    }

    public void test63() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "   class Inner{"+
                      "   }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  class Inner {"+
                      "  }"+
                      "}"))
        );
    }

    public void test64() {
        testSucc(
            "p", "A.B", "C",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A {"+
                      "    class B { }"+
                      "}"+
                      "class C {"+
                      "    static class D extends A {"+
                      "	int D;"+
                      "	static int m() { return 23; }"+
                      "	int i = C.D.m();"+
                      "    }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class A {"+
                      "  class C {"+
                      "  }"+
                      "}"+
                      "class C {"+
                      "  "+
                      "  static class D extends A {"+
                      "    int D;"+
                      "    static int m() {"+
                      "      return 23;"+
                      "    }"+
                      "    int i = p.C.D.m();"+
                      "  }"+
                      "}"))
        );
    }

    public void test65() {
        testSucc(
            "S", "T",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A<S> {"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "public class A<T extends java.lang.Object> {"+
                      "}"))
        );
    }

    public void test66() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A { }"+
                      "class C<T> { }"+
                      "class D {"+
                      "    C<A> b;"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "}"+
                      "class C<T extends java.lang.Object> {"+
                      "}"+
                      "class D {"+
                      "  C<B> b;"+
                      "}"))
        );
    }

    public void test67() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A { }"+
                      "class C {"+
                      "  class D<T> { }"+
                      "}"+
                      "class E {"+
                      "    C.D<A> b = new C().new D<A>();"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "}"+
                      "class C {"+
                      "  class D<T extends java.lang.Object> {"+
                      "  }"+
                      "}"+
                      "class E {"+
                      "  C.D<B> b = new C().new D<B>();"+
                      "}"))
        );
    }

    public void test68() {
        testFail(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A {"+
                      "}"+
                      "class B extends A{"+
                      "} "))
        );
    }

    public void test69() {
        testFail(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A {"+
                      "}"+
                      "class C extends A{"+
                      "}"+
                      "class B extends C{"+
                      "}"))
        );
    }

    public void test70() {
        testFail(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A extends B{"+
                      "}"+
                      "class B{"+
                      "}"))
        );
    }

    public void test71() {
        testFail(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A extends C{"+
                      "}"+
                      "class B{"+
                      "}"+
                      "class C extends B{"+
                      "}"))
        );
    }

    public void test72() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "   A m(){return null;};"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  B m() {"+
                      "    return null;"+
                      "  }"+
                      "}"))
        );
    }

    public void test73() {
        testSucc(
            "p", "A", "C",
            Program.fromCompilationUnits(
            new RawCU("B.java", 
                      "package p;"+
                      "import p.A.*;"+
                      "class B extends D {"+
                      "}"),
            new RawCU("A.java", 
                      "package p;"+
                      "class A {"+
                      "  static class D { }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "import p.C.*;"+
                      "class B extends D {"+
                      "}"),
            new RawCU("A.java",
                      "package p;"+
                      "class C {"+
                      "  "+
                      "  static class D {"+
                      "  }"+
                      "}"))
        );
    }

    public void test74() {
        testFail(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("B.java", 
                      "package p;"+
                      "class B extends C{"+
                      "}"),
            new RawCU("C.java", 
                      "package p;"+
                      "class C extends A{"+
                      "} "),
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "}"))
        );
    }

    public void test75() {
        testFail(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("B.java", 
                      "package p;"+
                      "class B{"+
                      "}"),
            new RawCU("C.java", 
                      "package p;"+
                      "class C extends B{"+
                      "}"),
            new RawCU("A.java", 
                      "package p;"+
                      "class A extends C{"+
                      "}"))
        );
    }

    public void test76() {
        testFail(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "}"+
                      "class B{}"))
        );
    }

    public void test77() {
        testFail(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("B.java", 
                      "package p;"+
                      "class B{}"),
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "}"))
        );
    }

    public void test78() {
        testFail(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "  class B{}"+
                      "}"))
        );
    }

    public void test79() {
        testFail(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "  class AA{"+
                      "     class AAA{"+
                      "        class B{"+
                      "        }"+
                      "     }"+
                      "  }"+
                      "} "))
        );
    }

    public void test80() {
        testFail(
            "p", "B.A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class B{"+
                      "  class A{}"+
                      "} "))
        );
    }

    public void test81() {
        testFail(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "  interface B{}"+
                      "}"))
        );
    }

    public void test82() {
        testSucc(
            "p", "B.C", "D",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class B {"+
                      "    class C { }"+
                      "}"+
                      "class A {"+
                      "    class D { }"+
                      "    class E extends B {"+
                      "	D d;"+
                      "    }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  "+
                      "  class D {"+
                      "  }"+
                      "}"+
                      "class A {"+
                      "  "+
                      "  class D {"+
                      "  }"+
                      "  "+
                      "  class E extends B {"+
                      "    A.D d;"+
                      "  }"+
                      "}"))
        );
    }

    public void test83() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "   void m(){"+
                      "     A: return;"+
                      "   };"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  void m() {"+
                      "    A:"+
                      "      return ;"+
                      "  }"+
                      "}"))
        );
    }

    public void test84() {
        testSucc(
            "p", "B", "Entry",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "import static java.util.Map.Entry;"+
                      "class B {"+
                      "}"+
                      "public class A {"+
                      "    public static void main(String[] args) {"+
                      "        System.out.println(Entry.class.getName());"+
                      "    }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "import static java.util.Map.Entry;"+
                      "class Entry {"+
                      "}"+
                      "public class A {"+
                      "  public static void main(String[] args) {"+
                      "    System.out.println(java.util.Map.Entry.class.getName());"+
                      "  }"+
                      "}"))
        );
    }

    public void test85() {
        testSucc(
            "p", "B", "Entry",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "import java.util.Map.Entry;"+
                      "class B {"+
                      "}"+
                      "public class A {"+
                      "    public static void main(String[] args) {"+
                      "        System.out.println(Entry.class.getName());"+
                      "    }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class Entry {"+
                      "}"+
                      "public class A {"+
                      "  public static void main(String[] args) {"+
                      "    System.out.println(java.util.Map.Entry.class.getName());"+
                      "  }"+
                      "}"))
        );
    }

    public void test86() {
        testFail(
            "p", "Outer.A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class Outer{"+
                      "  class A{}"+
                      "  class B{}"+
                      "} "))
        );
    }

    public void test87() {
        testFail(
            "p", "Outer.A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class Outer{"+
                      "  class A{}"+
                      "  interface B{}"+
                      "} "))
        );
    }

    public void test88() {
        testFail(
            "p", "Outer.Inner.A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class Outer{"+
                      "  class Inner{"+
                      "    class A{}"+
                      "    class B{}"+
                      "  }"+
                      "} "))
        );
    }

    public void test89() {
        testFail(
            "p", "Outer.Inner.A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class Outer{"+
                      "  class Inner{"+
                      "    class A{}"+
                      "    class B{}"+
                      "  }"+
                      "} "))
        );
    }

    public void test90() {
        testSucc(
            "S", "T",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "interface A<V> { }"+
                      "interface B<S> extends A<S> { }"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "interface A<V extends java.lang.Object> {"+
                      "}"+
                      "interface B<T extends java.lang.Object> extends A<T> {"+
                      "}"))
        );
    }

    public void test91() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A {"+
                      "    void m() {"+
                      "    	 A.class.getName();"+
                      "    }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  void m() {"+
                      "    B.class.getName();"+
                      "  }"+
                      "}"))
        );
    }

    public void test92() {
        testSucc(
            "p", "D.B", "C",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class D {"+
                      "    static class B {"+
                      "	static int x = 42;"+
                      "    }"+
                      "}"+
                      "public class A {"+
                      "    static class C extends D {"+
                      "	static int x = 23;"+
                      "	static int m() { return C.x; }"+
                      "    }"+
                      "    public static void main(String[] args) {"+
                      "	System.out.println(C.m());"+
                      "    }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class D {"+
                      "  "+
                      "  static class C {"+
                      "    static int x = 42;"+
                      "  }"+
                      "}"+
                      "public class A {"+
                      "  "+
                      "  static class C extends D {"+
                      "    static int x = 23;"+
                      "    static int m() {"+
                      "      return A.C.x;"+
                      "    }"+
                      "  }"+
                      "  public static void main(String[] args) {"+
                      "    System.out.println(C.m());"+
                      "  }"+
                      "}"))
        );
    }

    public void test93() {
        testSucc(
            "p", "B", "Entry",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "import static java.util.Map.*;"+
                      "class B {"+
                      "}"+
                      "public class A {"+
                      "    public static void main(String[] args) {"+
                      "        System.out.println(Entry.class.getName());"+
                      "    }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "import static java.util.Map.*;"+
                      "class Entry {"+
                      "}"+
                      "public class A {"+
                      "  public static void main(String[] args) {"+
                      "    System.out.println(java.util.Map.Entry.class.getName());"+
                      "  }"+
                      "}"))
        );
    }

    public void test94() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "   void m(){"+
                      "     A A; "+
                      "   };"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "class B {"+
                      "  void m() {"+
                      "    B A;"+
                      "  }"+
                      "}"))
        );
    }

    public void test95() {
        testSucc(
            "p", "B", "Entry",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "import java.util.Map.*;"+
                      "class B {"+
                      "}"+
                      "public class A {"+
                      "    public static void main(String[] args) {"+
                      "        System.out.println(Entry.class.getName());"+
                      "    }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "import java.util.Map.*;"+
                      "class Entry {"+
                      "}"+
                      "public class A {"+
                      "  public static void main(String[] args) {"+
                      "    System.out.println(java.util.Map.Entry.class.getName());"+
                      "  }"+
                      "}"))
        );
    }

    public void test96() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A<T> {"+
                      "  T f;"+
                      "  T m(int g) {"+
                      "    String s = new A<String>().f;"+
                      "    return null;"+
                      "  }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B<T extends java.lang.Object> {"+
                      "  T f;"+
                      "  T m(int g) {"+
                      "    String s = new B<String>().f;"+
                      "    return null;"+
                      "  }"+
                      "}"))
        );
    }

    public void test97() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public @interface A { }"+
                      "@A class C { }"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public @interface B {"+
                      "}"+
                      "@B() class C {"+
                      "}"))
        );
    }

    public void test98() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("package-info.java", 
                      "@A package p;"),
            new RawCU("A.java", 
                      "package p;"+
                      "public @interface A { }"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public @interface B {"+
                      "}"),
            new RawCU("package-info.java",
                      "@B() package p;"))
        );
    }

    public void test99() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "}"+
                      "class C<B> {"+
                      "  A a;"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "}"+
                      "class C<B extends java.lang.Object> {"+
                      "  p.B a;"+
                      "}"))
        );
    }

    public void test100() {
        testSucc(
            "p", "A", "B",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "    public static void main(String[] args) {"+
                      "        C<A> a = new D<String>();"+
                      "    }"+
                      "}"+
                      "class C<T> { }"+
                      "class D<B> extends C<A> { }"))
            ,
            Program.fromCompilationUnits(
            new RawCU("B.java",
                      "package p;"+
                      "public class B {"+
                      "  public static void main(String[] args) {"+
                      "    C<B> a = new D<String>();"+
                      "  }"+
                      "}"+
                      "class C<T extends java.lang.Object> {"+
                      "}"+
                      "class D<B extends java.lang.Object> extends C<p.B> {"+
                      "}"))
        );
    }

    public void test101() {
        testFail(
            "S", "T",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A<S> {"+
                      "    class T { }"+
                      "    S x;"+
                      "}"))
        );
    }

    public void test102() {
        testSucc(
            "p", "B", "C",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "    <T> int m() { return 23; }"+
                      "}"+
                      "class B {"+
                      "    int x = new A().<B>m();"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "public class A {"+
                      "   <T extends java.lang.Object> int m() {"+
                      "    return 23;"+
                      "  }"+
                      "}"+
                      "class C {"+
                      "  int x = new A().<C>m();"+
                      "}"))
        );
    }

    public void test103() {
        testSucc(
            "S", "T",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "    <S> int m() { S s; return 23; }"+
                      "}"+
                      "class B {"+
                      "    int x = new A().<B>m();"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "public class A {"+
                      "   <T extends java.lang.Object> int m() {"+
                      "    T s;"+
                      "    return 23;"+
                      "  }"+
                      "}"+
                      "class B {"+
                      "  int x = new A().<B>m();"+
                      "}"))
        );
    }

    public void test104() {
        testSucc(
            "S", "T",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "  class S { }"+
                      "  public <T>A() {"+
                      "    S s;"+
                      "  }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "public class A {"+
                      "  "+
                      "  class T {"+
                      "  }"+
                      "  public  <T extends java.lang.Object> A() {"+
                      "    super();"+
                      "    A.T s;"+
                      "  }"+
                      "}"))
        );
    }
    
    public void test105() {
    	testSucc("X", "Y",
    		Program.fromClasses(
    			"class A<T> {" +
    			"  class X {" +
    			"    T x;" +
    			"  }" +
    			"}",
    			"class B {" +
    			"  String m(A<String>.X a) {" +
    			"    return a.x;" +
    			"  }" +
    			"}"),
       		Program.fromClasses(
       			"class A<T> {" +
       			"  class Y {" +
       			"    T x;" +
       			"  }" +
       			"}",
       			"class B {" +
       			"  String m(A<String>.Y a) {" +
       			"    return a.x;" +
       			"  }" +
       			"}"));    			
    }
}