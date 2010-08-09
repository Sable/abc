package tests;

import junit.framework.TestCase;
import tests.AllTests;
import AST.FieldDeclaration;
import AST.Program;
import AST.RawCU;
import AST.RefactoringException;
import AST.SimpleSet;
import AST.TypeDecl;
import AST.Variable;

public class RenameVariableTests extends TestCase {
	public RenameVariableTests(String name) {
		super(name);
	}
	
	public void testSucc(String pkg, String tp_name, String old_name, String new_name, Program in, Program out) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		TypeDecl tp = in.findType(pkg, tp_name);
		assertNotNull(tp);
		SimpleSet s = tp.localFields(old_name);
		assertTrue(s.isSingleton());
		FieldDeclaration fd = (FieldDeclaration)s.iterator().next();
		try {
			fd.rename(new_name);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			fail("Refactoring was supposed to succeed; failed with "+rfe);
		}
		in.undoAll();
		if (Program.isRecordingASTChanges()) assertEquals(originalProgram, in.toString());
	}
	
	public void testSucc(String old_name, String new_name, Program in, Program out) {
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		Variable v = in.findVariable(old_name);
		assertNotNull(v);
		try {
			v.rename(new_name);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			fail("Refactoring was supposed to succeed; failed with "+rfe);
		}
		in.undoAll();
		if (Program.isRecordingASTChanges()) assertEquals(originalProgram, in.toString());
	}
	
	public void testFail(String pkg, String tp_name, String old_name, String new_name, Program in) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		TypeDecl tp = in.findType(pkg, tp_name);
		assertNotNull(tp);
		SimpleSet s = tp.localFields(old_name);
		assertTrue(s.isSingleton());
		FieldDeclaration fd = (FieldDeclaration)s.iterator().next();
		try {
			fd.rename(new_name);
			fail("Refactoring was supposed to fail; succeeded with "+in);
		} catch(RefactoringException rfe) {
		}
		in.undoAll();
		if (Program.isRecordingASTChanges()) assertEquals(originalProgram, in.toString());
	}
	
	public void testFail(String old_name, String new_name, Program in) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		Variable v = in.findVariable(old_name);
		assertNotNull(v);
		try {
			v.rename(new_name);
			fail("Refactoring was supposed to fail; succeeded with "+in);
		} catch(RefactoringException rfe) {
		}
		in.undoAll();
		if (Program.isRecordingASTChanges()) assertEquals(originalProgram, in.toString());
	}
	
    public void test1() {
        testSucc(
            "p", "A", "f", "g",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "class A {"+
                      "	protected int f;"+
                      "	void m(){"+
                      "		f++;"+
                      "	}"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  protected int g;"+
                      "  void m() {"+
                      "    g++;"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test10() {
        testSucc(
            "p", "A", "f", "g",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "	public A f;"+
                      "	public int k;"+
                      "	void m(){"+
                      "		for (int g= 0; g < 10; g++){"+
                      "		"+
                      "		}"+
                      "		f.k=0;"+
                      "	}"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  public A g;"+
                      "  public int k;"+
                      "  void m() {"+
                      "    for(int g = 0; g < 10; g++) {"+
                      "    }"+
                      "    g.k = 0;"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test11() {
        testSucc(
            "p", "A", "f", "g",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "	int f;"+
                      "}"+
                      "class B extends A{"+
                      "	A a;"+
                      "	void m(){"+
                      "		int g= a.f;"+
                      "	}"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  int g;"+
                      "}"+
                      ""+
                      "class B extends A {"+
                      "  A a;"+
                      "  void m() {"+
                      "    int g = a.g;"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test12() {
        testSucc(
            "p", "A", "f", "g",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "	static int f= 0;"+
                      "	void m(){"+
                      "		A.f= 0; /**/"+
                      "	}"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "public class A {"+
                      "  static int g = 0;"+
                      "  void m() {"+
                      "    A.g = 0;"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test13() {
        testSucc(
            "p", "A", "f", "g",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "	static int f= 0;"+
                      "	void m(){"+
                      "		p.A.f= 0; /**/"+
                      "	}"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "public class A {"+
                      "  static int g = 0;"+
                      "  void m() {"+
                      "    p.A.g = 0;"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test14() {
        testSucc(
            "p", "A.C", "c", "b",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "public class A {"+
                      "    class B { int b; }"+
                      "    class C extends B { int c; }"+
                      "    class D { C c; int m() { return c.b; } }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "public class A {"+
                      "  "+
                      "  class B {"+
                      "    int b;"+
                      "  }"+
                      "  "+
                      "  class C extends B {"+
                      "    int b;"+
                      "  }"+
                      "  "+
                      "  class D {"+
                      "    C c;"+
                      "    int m() {"+
                      "      return ((B)c).b;"+
                      "    }"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test15() {
        testFail(
            "e", "args",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "    static void main(String[] args) {"+
                      "	try {"+
                      "	    args[23] = \"\";"+
                      "	} catch(ArrayIndexOutOfBoundsException e) {"+
                      "	    e.printStackTrace();"+
                      "	}"+
                      "    }"+
                      "}"+
                      ""))
        );
    }

    public void test16() {
        testSucc(
            "p", "A", "f", "g",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "	int f;"+
                      "	public int getF() {"+
                      "		return (this.f);"+
                      "	}	"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  int g;"+
                      "  public int getF() {"+
                      "    return (this.g);"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test17() {
        testSucc(
            "p", "A", "f", "g",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A {"+
                      "    int f;"+
                      "	   public int getF() {"+
                      "        return (this.f);"+
                      "    }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  int g;"+
                      "  public int getF() {"+
                      "    return (this.g);"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test18() {
        testSucc(
            "x", "y",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "    static void main(String[] args) {"+
                      "	     int x, z;"+
                      "	     x = 42;"+
                      "	     System.out.println(x);"+
                      "    }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  static void main(String[] args) {"+
                      "    int y;"+
                      "    int z;"+
                      "    y = 42;"+
                      "    System.out.println(y);"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test19() {
        testSucc(
            "args", "arguments",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "    static void main(String[] args) {"+
                      "	int x, z;"+
                      "	x = 42;"+
                      "	System.out.println(x);"+
                      "    }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  static void main(String[] arguments) {"+
                      "    int x;"+
                      "    int z;"+
                      "    x = 42;"+
                      "    System.out.println(x);"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test2() {
        testSucc(
            "p", "A", "f", "g",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "interface A{"+
                      "	int f= 0;"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "interface A {"+
                      "  int g = 0;"+
                      "}"+
                      ""))
        );
    }

    public void test20() {
        testSucc(
            "x", "y",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "    static void main(String[] args) {"+
                      "	int x, z;"+
                      "	x = 42;"+
                      "	if(x == 23) {"+
                      "	    System.out.println(x);"+
                      "	}"+
                      "    }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  static void main(String[] args) {"+
                      "    int y;"+
                      "    int z;"+
                      "    y = 42;"+
                      "    if(y == 23) {"+
                      "      System.out.println(y);"+
                      "    }"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test21() {
        testSucc(
            "e", "exc",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "    static void main(String[] args) {"+
                      "	try {"+
                      "	    args[23] = \"\";"+
                      "	} catch(ArrayIndexOutOfBoundsException e) {"+
                      "	    e.printStackTrace();"+
                      "	}"+
                      "    }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  static void main(String[] args) {"+
                      "    try {"+
                      "      args[23] = \"\";"+
                      "    }"+
                      "    catch (ArrayIndexOutOfBoundsException exc) {"+
                      "      exc.printStackTrace();"+
                      "    }"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test22() {
        testFail(
            "args", "e",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "    static void main(String[] args) {"+
                      "	try {"+
                      "	    args[23] = \"\";"+
                      "	} catch(ArrayIndexOutOfBoundsException e) {"+
                      "	    e.printStackTrace();"+
                      "	}"+
                      "    }"+
                      "}"+
                      ""))
        );
    }

    public void test23() {
        testSucc(
            "p", "B", "x", "y",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "class B {"+
                      "    int x;"+
                      "}"+
                      ""+
                      "public class A {"+
                      "    int y;"+
                      "    class C extends B {"+
                      "	       int m() { return y; }"+
                      "    }"+
                      "}"))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class B {"+
                      "  int y;"+
                      "}"+
                      ""+
                      "public class A {"+
                      "  int y;"+
                      "  "+
                      "  class C extends B {"+
                      "    int m() {"+
                      "      return A.this.y;"+
                      "    }"+
                      "  }"+
                      "}"))
        );
    }

    public void test24() {
        testSucc(
            "p", "A", "x", "y",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "public class A {"+
                      "    int x;"+
                      "    void m() {"+
                      "	for(int y=0;;++y)"+
                      "	    y=x;"+
                      "    }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "public class A {"+
                      "  int y;"+
                      "  void m() {"+
                      "    for(int y = 0; true; ++y) "+
                      "      y = this.y;"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test25() {
        testSucc(
            "p", "A", "x", "y",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "public class A {"+
                      "    int x;"+
                      "    void m(int[] ys) {"+
                      "	for(int y : ys)"+
                      "	    y=x;"+
                      "    }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "public class A {"+
                      "  int y;"+
                      "  void m(int[] ys) {for (int y : ys) "+
                      "      y = this.y;"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test26() {
        testSucc(
            "p", "A", "f", "out",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "import java.io.PrintStream;"+
                      "import static java.lang.System.*;"+
                      ""+
                      "public class A {"+
                      "    static PrintStream f = "+
                      "        new PrintStream(out) { "+
                      "            public void println(String s) {"+
                      "	        super.println(42);"+
                      "	    }"+
                      "        };"+
                      "    public static void main(String[] args) {"+
                      "        out.println(\"23\");"+
                      "    }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "import java.io.PrintStream;"+
                      "import static java.lang.System.*;"+
                      ""+
                      "public class A {"+
                      "  static PrintStream out = new PrintStream(System.out) {"+
                      "      public void println(String s) {"+
                      "        super.println(42);"+
                      "      }"+
                      "  };"+
                      "  public static void main(String[] args) {"+
                      "    System.out.println(\"23\");"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test27() {
        testSucc(
            "p", "A", "f", "g",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "public class A<T> {"+
                      "  T f;"+
                      "  T m(int g) {"+
                      "    return f;"+
                      "  }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "public class A<T extends java.lang.Object> {"+
                      "  T g;"+
                      "  T m(int g) {"+
                      "    return this.g;"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test28() {
        testSucc(
            "p", "A", "f", "g",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "public class A<T> {"+
                      "  T f;"+
                      "  T m(int g) {"+
                      "    String s = new A<String>().f;"+
                      "    return null;"+
                      "  }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "public class A<T extends java.lang.Object> {"+
                      "  T g;"+
                      "  T m(int g) {"+
                      "    String s = new A<String>().g;"+
                      "    return null;"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test29() {
        testSucc(
            "p", "A", "f", "g",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "public class A<T> {"+
                      "  T f;"+
                      "  String m() {"+
                      "    return new B().f;"+
                      "  }"+
                      "}"+
                      "class B extends A<String> {"+
                      "  String g;"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "public class A<T extends java.lang.Object> {"+
                      "  T g;"+
                      "  String m() {"+
                      "    return ((A<String>)new B()).g;"+
                      "  }"+
                      "}"+
                      ""+
                      "class B extends A<String> {"+
                      "  String g;"+
                      "}"+
                      ""))
        );
    }

    public void test3() {
        testSucc(
            "p", "A", "f", "g",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "	protected int f;"+
                      "	void m(){"+
                      "		f++;"+
                      "	}"+
                      "}"+
                      "class B{"+
                      "	A a;"+
                      "	protected int f;"+
                      "	void m(){"+
                      "		a.f= 0;"+
                      "	}"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  protected int g;"+
                      "  void m() {"+
                      "    g++;"+
                      "  }"+
                      "}"+
                      ""+
                      "class B {"+
                      "  A a;"+
                      "  protected int f;"+
                      "  void m() {"+
                      "    a.g = 0;"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test30() {
        testSucc(
            "p", "A", "A1", "A0",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "public enum A { A1, A2 }"+
                      ""+
                      "class B {"+
                      "    boolean m(A a) {"+
                      "	switch(a) {"+
                      "	case A1: return true;"+
                      "	case A2: return false;"+
                      "	}"+
                      "		return false;"+
                      "    }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "public enum A { A0, A2 }"+
                      ""+
                      "class B {"+
                      "  boolean m(A a) {"+
                      "    switch (a){"+
                      "      case A0:"+
                      "      return true;"+
                      "      case A2:"+
                      "      return false;"+
                      "    }"+
                      "    return false;"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test31() {
        testSucc(
            "p", "A.B", "g", "f",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "class A {"+
                      "    B f;"+
                      "    class B extends A {"+
                      "	     int g;"+
                      "	     { B b = f.f; }"+
                      "    }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  B f;"+
                      "  "+
                      "  class B extends A {"+
                      "    int f;"+
                      "    {"+
                      "      B b = ((A)super.f).f;"+
                      "    }"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test32() {
        testSucc(
            "p", "A.B", "g", "f",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "class A {"+
                      "  B f;"+
                      "  class B extends A {"+
                      "	   int g;"+
                      "	   { B b = B.this.f.f; }"+
                      "  }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  B f;"+
                      "  "+
                      "  class B extends A {"+
                      "    int f;"+
                      "    {"+
                      "      B b = ((A)((A)B.this).f).f;"+
                      "    }"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test33() {
        testSucc(
            "p", "A.B", "g", "f",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "class A {"+
                      "    B f;"+
                      "    class B extends A {"+
                      "	int g;"+
                      "	{ B b = ((B)this).f.f; }"+
                      "    }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  B f;"+
                      "  "+
                      "  class B extends A {"+
                      "    int f;"+
                      "    {"+
                      "      B b = ((A)((A)((B)this)).f).f;"+
                      "    }"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test34() {
        testSucc(
            "p", "A", "f", "g",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "class A {"+
                      "    static int f = 23;"+
                      "    public A(int g) {"+
                      "	this(f, 0);"+
                      "    }"+
                      "    public A(int x, int y) {"+
                      "	System.out.println(x);"+
                      "    }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  static int g = 23;"+
                      "  public A(int g) {"+
                      "    this(A.g, 0);"+
                      "  }"+
                      "  public A(int x, int y) {"+
                      "    super();"+
                      "    System.out.println(x);"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test35() {
        testSucc(
            "p", "A", "x", "y",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "class A {"+
                      "    int x;"+
                      "    class B {"+
                      "	int y;"+
                      "	public B() {"+
                      "	    this(x);"+
                      "	}"+
                      "	public B(int z) {"+
                      "	    System.out.println(z);"+
                      "	}"+
                      "    }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  int y;"+
                      "  "+
                      "  class B {"+
                      "    int y;"+
                      "    public B() {"+
                      "      this(A.this.y);"+
                      "    }"+
                      "    public B(int z) {"+
                      "      super();"+
                      "      System.out.println(z);"+
                      "    }"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test36() {
        testFail(
            "p", "A", "x", "void",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "class A {"+
                      "    int x;"+
                      "}"+
                      ""))
        );
    }

    public void test37() {
        testFail(
            "p", "A", "x", "++",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "class A {"+
                      "    int x;"+
                      "}"+
                      ""))
        );
    }

    public void test38() {
        testSucc(
            "p", "Indiana", "myPI", "PI",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "import static java.lang.Math.*;"+
                      ""+
                      "class Indiana {"+
                      "    static double myPI = 3.2;"+
                      "    static double circleArea(double r) {"+
		      "	       return PI*r*r;"+
                      "    }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      "import static java.lang.Math.*;"+
                      ""+
                      "class Indiana {"+
                      "  static double PI = 3.2D;"+
                      "  static double circleArea(double r) {"+
                      "    return Math.PI * r * r;"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test39() {
        testSucc(
            "p", "A", "i", "String",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "class A {"+
                      "    int i;"+
                      "    { System.out.println(String.valueOf(23)); }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  int String;"+
                      "  {"+
                      "    System.out.println(java.lang.String.valueOf(23));"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test4() {
        testSucc(
            "p", "A", "f", "g",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "	protected int f;"+
                      "	void m(){"+
                      "		f++;"+
                      "	}"+
                      "}"+
                      "class B extends A{"+
                      "	void m(){"+
                      "		f= 0;"+
                      "	}"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  protected int g;"+
                      "  void m() {"+
                      "    g++;"+
                      "  }"+
                      "}"+
                      ""+
                      "class B extends A {"+
                      "  void m() {"+
                      "    g = 0;"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test40() {
        testSucc(
            "p", "A", "i", "java",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "class A {"+
                      "    int i;"+
                      "    { System.out.println(java.lang.String.valueOf(23)); }"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  int java;"+
                      "  {"+
                      "    System.out.println(String.valueOf(23));"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test41() {
        testFail(
            "p", "A.D", "g", "f",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      ""+
                      "class A {"+
                      "  private class C {"+
                      "    int f;"+
                      "  }"+
                      "  class D extends C {"+
                      "    int g;"+
                      "  }"+
                      "}"+
                      ""+
                      "class B extends A {"+
                      "  { new D().f = 23; }"+
                      "}"+
                      ""))
        );
    }

    public void test5() {
        testSucc(
            "p", "A", "f", "g",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "	protected int f;"+
                      "	void m(){"+
                      "		f++;"+
                      "	}"+
                      "}"+
                      ""+
                      "class AA extends A{"+
                      "	protected int f;"+
                      "}"+
                      ""+
                      "class B{"+
                      "	A a;"+
                      "	void m(){"+
                      "		a.f= 0;"+
                      "	}"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  protected int g;"+
                      "  void m() {"+
                      "    g++;"+
                      "  }"+
                      "}"+
                      ""+
                      "class AA extends A {"+
                      "  protected int f;"+
                      "}"+
                      ""+
                      "class B {"+
                      "  A a;"+
                      "  void m() {"+
                      "    a.g = 0;"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test6() {
        testSucc(
            "p", "A", "f", "g",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "	protected int f;"+
                      "	void m(){"+
                      "		f++;"+
                      "	}"+
                      "}"+
                      ""+
                      "class AA extends A{"+
                      "	protected int f;"+
                      "}"+
                      ""+
                      "class B{"+
                      "	A a;"+
                      "	AA b;"+
                      "	A ab= new AA();"+
                      "	void m(){"+
                      "		a.f= 0;"+
                      "		b.f= 0;"+
                      "		ab.f= 0;"+
                      "	}"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  protected int g;"+
                      "  void m() {"+
                      "    g++;"+
                      "  }"+
                      "}"+
                      ""+
                      "class AA extends A {"+
                      "  protected int f;"+
                      "}"+
                      ""+
                      "class B {"+
                      "  A a;"+
                      "  AA b;"+
                      "  A ab = new AA();"+
                      "  void m() {"+
                      "    a.g = 0;"+
                      "    b.f = 0;"+
                      "    ab.g = 0;"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test7() {
        testSucc(
            "p", "A", "f", "g",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A {"+
                      "	int f;"+
                      "	void m(int g){"+
                      "		this.f= 0;"+
                      "	}"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  int g;"+
                      "  void m(int g) {"+
                      "    this.g = 0;"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test8() {
        testSucc(
            "p", "A", "f", "g",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "	public A f;"+
                      "	public int k;"+
                      "	void m(){"+
                      "		f.f.f.k=0;"+
                      "	}"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  public A g;"+
                      "  public int k;"+
                      "  void m() {"+
                      "    g.g.g.k = 0;"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

    public void test9() {
        testSucc(
            "p", "A", "f", "g",
            Program.fromCompilationUnits(
            new RawCU("A.java", 
                      "package p;"+
                      "class A{"+
                      "	public A f;"+
                      "	public int k;"+
                      "	void m(){"+
                      "		{"+
                      "			int g;"+
                      "		}"+
                      "		f.k=0;"+
                      "	}"+
                      "}"+
                      ""))
            ,
            Program.fromCompilationUnits(
            new RawCU("A.java",
                      "package p;"+
                      ""+
                      "class A {"+
                      "  public A g;"+
                      "  public int k;"+
                      "  void m() {"+
                      "    {"+
                      "      int g;"+
                      "    }"+
                      "    g.k = 0;"+
                      "  }"+
                      "}"+
                      ""))
        );
    }

}