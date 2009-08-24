package tests;

import junit.framework.TestCase;
import AST.Program;
import AST.RawCU;
import AST.RefactoringException;
import AST.Variable;
import AST.VariableDeclaration;

public class InlineTempTests extends TestCase {
	public InlineTempTests(String name) {
		super(name);
	}
	
	public void testSucc(Program in, Program out) {		
		assertNotNull(in);
		assertNotNull(out);
		Variable v = in.findVariable("i");
		assertTrue(v instanceof VariableDeclaration);
		try {
			((VariableDeclaration)v).doInline();
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			fail("Refactoring was supposed to succeed; failed with "+rfe);
		}
	}

	public void testFail(Program in) {		
		assertNotNull(in);
		Variable v = in.findVariable("i");
		assertTrue(v instanceof VariableDeclaration);
		try {
			((VariableDeclaration)v).doInline();
			fail("Refactoring was supposed to fail; succeeded with "+in);
		} catch(RefactoringException rfe) { }
	}

    public void test1() {
        testSucc(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "    void m() {"+
            "        int i = 23;"+
            "        System.out.println(i+i);"+
            "    }"+
            "}")),
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "  void m() {"+
            "    System.out.println(23 + 23);"+
            "  }"+
            "  A() {"+
            "    super();"+
            "  }"+
            "}")));
    }

    public void test2() {
        testSucc(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "  void m() {"+
            "    int i;"+
            "  }"+
            "}")),
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "  void m() {"+
            "  }"+
            "  A() {"+
            "    super();"+
            "  }"+
            "}")));
    }

    public void test3() {
        testSucc(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "  int m() {"+
            "    int i = 23;"+
            "    return i;"+
            "  }"+
            "}")),
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "  int m() {"+
            "    return 23;"+
            "  }"+
            "  A() {"+
            "    super();"+
            "  }"+
            "}")));
    }

    public void test4() {
        testSucc(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "  int m() {"+
            "    int i = 23;"+
            "    return i+1;"+
            "  }"+
            "}")),
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "  int m() {"+
            "    return 23 + 1;"+
            "  }"+
            "  A() {"+
            "    super();"+
            "  }"+
            "}")));
    }

    public void test5() {
        testFail(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "  int m() {"+
            "    int i = 23;"+
            "    i = 42;"+
            "    return i;"+
            "  }"+
            "}")));
    }

    public void test6() {
        testFail(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "  int m() {"+
            "    int i = 23;"+
            "    return i++;"+
            "  }"+
            "}")));
    }

    public void test7() {
        testSucc(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "    void m() {"+
            "        int i = 'x';"+
            "        n(i);"+
            "    }"+
            "    void n(int i) {"+
            "        System.out.println(\"here\");"+
            "    }"+
            "    void n(char c) {"+
            "        System.out.println(\"there\");"+
            "    }"+
            "}")),
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "  void m() {"+
            "    n((int)'x');"+
            "  }"+
            "  void n(int i) {"+
            "    System.out.println(\"here\");"+
            "  }"+
            "  void n(char c) {"+
            "    System.out.println(\"there\");"+
            "  }"+
            "  A() {"+
            "    super();"+
            "  }"+
            "}")));
    }

    public void test8() {
        testSucc(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class B {"+
            "    class C { }"+
            "}"+
            ""+
            "class A extends B {"+
            "    void m() {"+
            "        C c = new C();"+
            "        Object i = (C)c;"+
            "        {"+
            "            class C { }"+
            "            System.out.println(i);"+
            "        }"+
            "    }"+
            "}")),
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class B {"+
            "  "+
            "  class C {"+
            "    C() {"+
            "      super();"+
            "    }"+
            "  }"+
            "  B() {"+
            "    super();"+
            "  }"+
            "}"+
            ""+
            "class A extends B {"+
            "  void m() {"+
            "    C c = new C();"+
            "    {"+
            "        class C {"+
            "          C() {"+
            "            super();"+
            "          }"+
            "        }"+
            "      System.out.println((Object)(B.C)c);"+
            "    }"+
            "  }"+
            "  A() {"+
            "    super();"+
            "  }"+
            "}")));
    }

    public void test9() {
        testFail(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "public class A {"+
            "  void m() {"+
            "    int j = 23;"+
            "    final int i = j;"+
            "    class B {"+
            "      int k = i;"+
            "    }"+
            "  }"+
            "}")));
    }

    public void test10() {
        testFail(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "public class A {"+
            "    void m() {"+
            "        int j = 23;"+
            "        final int i = j;"+
            "        new Object() {"+
            "            { System.out.println(i); }"+
            "        };"+
            "    }"+
            "}")));
    }

/*    public void test11() {
        testSucc(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "public class A {"+
            "  static final int j = 23;"+
            "  static final int i = j;"+
            "}"+
            ""+
            "class B extends A {"+
            "  int j = 42;"+
            "  int k = i;"+
            "}")),
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "public class A {"+
            "  static final int j = 23;"+
            "  static final int i = j;"+
            "  public A() {"+
            "    super();"+
            "  }"+
            "}"+
            ""+
            "class B extends A {"+
            "  int j = 42;"+
            "  int k = A.j;"+
            "  B() {"+
            "    super();"+
            "  }"+
            "}")));
    }

    public void test12() {
        testFail(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "package p;"+
            ""+
            "public class A {"+
            "  static final int j = 23;"+
            "  protected static final int i = j;"+
            "}"),
            new RawCU("B.java",
            "package q;"+
            ""+
            "class B extends p.A {"+
            "  int k = i;"+
            "}")));
    }*/

    public void test13() {
        testSucc(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "public class A {"+
            "  int m() {"+
            "    int[] i = { 23, 42 };"+
            "    i[1] = 72;"+
            "    return i[0];"+
            "  }"+
            "}")),
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "public class A {"+
            "  int m() {"+
            "    new int[]{ 23, 42 } [1] = 72;"+
            "    return new int[]{ 23, 42 } [0];"+
            "  }"+
            "  public A() {"+
            "    super();"+
            "  }"+
            "}")));
    }

/*    public void test14() {
        testSucc(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "public class A {"+
            "  static final int j = 23;"+
            "  public static final int i = j;"+
            "}"+
            ""+
            "class B {"+
            "  int k = A.i;"+
            "}")),
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "public class A {"+
            "  static final int j = 23;"+
            "  public static final int i = j;"+
            "  public A() {"+
            "    super();"+
            "  }"+
            "}"+
            ""+
            "class B {"+
            "  int k = A.j;"+
            "  B() {"+
            "    super();"+
            "  }"+
            "}")));
    }

    public void test15() {
        testSucc(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class C {"+
            "  static final int j = 23;"+
            "}"+
            ""+
            "public class A {"+
            "  public static final int i = C.j;"+
            "}"+
            ""+
            "class B {"+
            "  int k = A.i;"+
            "}")),
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class C {"+
            "  static final int j = 23;"+
            "  C() {"+
            "    super();"+
            "  }"+
            "}"+
            ""+
            "public class A {"+
            "  public static final int i = C.j;"+
            "  public A() {"+
            "    super();"+
            "  }"+
            "}"+
            ""+
            "class B {"+
            "  int k = C.j;"+
            "  B() {"+
            "    super();"+
            "  }"+
            "}")));
    }*/

    public void test16() {
        testSucc(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "  int m() {"+
            "    int j = 23;"+
            "    int i = j++;"+
            "    return i;"+
            "  }"+
            "}")),
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "  int m() {"+
            "    int j = 23;"+
            "    return j++;"+
            "  }"+
            "  A() {"+
            "    super();"+
            "  }"+
            "}")));
    }

    public void test17() {
        testFail(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "  int k;"+
            "  int incK() { ++k; return 0; }"+
            "  int m() {"+
            "    int i = incK();"+
            "    return i;"+
            "  }"+
            "}")));
    }

    public void test18() {
        testFail(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "  int k;"+
            "  int nop() { return 0; }"+
            "  int m() {"+
            "    int i = nop();"+
            "    return i;"+
            "  }"+
            "}")));
    }
}
