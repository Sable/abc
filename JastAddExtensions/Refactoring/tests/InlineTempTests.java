package tests;

import junit.framework.TestCase;
import tests.AllTests;
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
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		Variable v = in.findVariable("i");
		assertTrue(v instanceof VariableDeclaration);
		try {
			((VariableDeclaration)v).doInline();
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), "<failure>");
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	public void testFail(Program in) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		Variable v = in.findVariable("i");
		assertTrue(v instanceof VariableDeclaration);
		try {
			((VariableDeclaration)v).doInline();
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) { }
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
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
            "}")));
    }

    public void test5() {
        testSucc(Program.fromBodyDecls(
        		"int m() {" +
        		"  int i = 23;" +
        		"  i = 42;" +
        		"  return i;" +
        		"}"),
        		Program.fromBodyDecls(
        		"int m() {" +
        		"  int i;" +
        		"  i = 42;" +
        		"  return i;" +
        		"}"));
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
            "  class C {"+
            "  }"+
            "}"+
            ""+
            "class A extends B {"+
            "  void m() {"+
            "    C c = new C();"+
            "    {"+
            "        class C {"+
            "        }"+
            "      System.out.println((Object)(B.C)c);"+
            "    }"+
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

    public void test11() {
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
            "    (new int[]{ 23, 42 }) [1] = 72;"+
            "    return (new int[]{ 23, 42 }) [0];"+
            "  }"+
            "}")));
    }

    /* disabled: currently no support for inlining expressions with side effects
    public void test12() {
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
            "}")));
    }*/
    
    public void test12() {
    	testFail(Program.fromStmts(
    		"boolean b = false;" +
    		"int i = 23;" +
    		"if(b)" +
    		"  i = 42;" +
    		"int j = i;"));
    }

    public void test13() {
        testFail(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "  int k;"+
            "  int incK() { ++k; return 0; }"+
            "  int m() {"+
            "    int i = incK();" +
            "    k = 23;"+
            "    return i;"+
            "  }"+
            "}")));
    }

    /* disabled: cannot inline expressions with side effects
    public void test14() {
        testSucc(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "  int k;"+
            "  int nop() { return 0; }"+
            "  int m() {"+
            "    int i = nop();"+
            "    return i;"+
            "  }"+
            "}")),
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "  int k;"+
            "  int nop() { return 0; }"+
            "  int m() {"+
            "    return nop();"+
            "  }"+
            "}")));
    }*/
    
    public void test14() {
    	testFail(Program.fromClasses(
    		"class A {" +
    		"  int x;" +
    		"  Runnable m() {" +
    		"    final int i = x;" +
    		"    return new Thread() {" +
    		"      public void run() {" +
    		"        System.out.println(i);" +
    		"      }" +
    		"    };" +
    		"  }" +
    		"}"));
    }
    
    public void test15() {
        testFail(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "  int k, l;"+
            "  int m() {"+
            "    int i = k;" +
            "    ++k;"+
            "    return i;"+
            "  }"+
            "}")));
    }
    
    public void test16() {
        testSucc(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {"+
            "  int k, l;"+
            "  int m() {"+
            "    int i = k;" +
            "    ++l;"+
            "    return i;"+
            "  }"+
            "}")),
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {" +
            "  int k, l;" +
            "  int m() {" +
            "    ++l;" +
            "    return k;" +
            "  }" +
            "}")));
    }
    
    public void test17() {
    	testFail(
    		Program.fromCompilationUnits(
    		new RawCU("A.java",
    		"class A {" +
    		"  volatile int f;" +
    		"  void m() {" +
    		"    int i = (f = 42);" +
    		"    synchronized(this) {" +
    		"      if(i==23);" +
    		"    }" +
    		"  }" +
    		"}")));
    }
    
    public void test18() {
    	testFail(
    	    Program.fromCompilationUnits(
    	    new RawCU("A.java",
    	    "class A {" +
    	    "  volatile int f, g;" +
    	    "  void m() {" +
    	    "    int i = (f = 42);" +
    	    "    if(g==23);" +
    	    "    if(i==23);" +
    	    "  }" +
    	    "}")));
    }
    
    /* disabled: cannot inline expressions with side effects
    public void test19() {
    	testSucc(
    		Program.fromCompilationUnits(
    		new RawCU("A.java",
    		"class A {" +
    		"  int f;" +
    		"  void m() {" +
    		"    int i = (f = 42);" +
    		"    synchronized(this) {" +
    		"      if(i==23);" +
    		"    }" +
    		"  }" +
    		"}")),
    		Program.fromCompilationUnits(
    		new RawCU("A.java",
    	    "class A {" +
    	    "  int f;" +
    	    "  void m() {" +
    	    "    synchronized(this) {" +
    	    "      if((f = 42)==23);" +
    	    "    }" +
    	    "  }" +
    	    "}")));
    }
    
    public void test20() {
    	testSucc(
    	    Program.fromCompilationUnits(
    	    new RawCU("A.java",
    	    "class A {" +
    	    "  int f, g;" +
    	    "  void m() {" +
    	    "    int i = (f = 42);" +
    	    "    if(g==23);" +
    	    "    if(i==23);" +
    	    "  }" +
    	    "}")),
    	    Program.fromCompilationUnits(
    	    new RawCU("A.java",
    	    "class A {" +
    	    "  int f, g;" +
    	    "  void m() {" +
    	    "    if(g==23);" +
    	    "    if((f = 42)==23);" +
    	    "  }" +
    	    "}")));
    }
    
    public void test21() {
        testFail(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {" +
            "  int x;" +
            "  volatile boolean ready;"+
            "  void t1() {"+
            "    int i = (x=23);"+
            "    x = 42;" +
            "    int j = i;"+
            "    ready = true;"+
            "  }" +
            "  void t2() {" +
            "    while(!ready);" +
            "    assert(x==42);" +
            "  }"+
            "}")));
    }*/
    
    public void test19() {
    	testFail(
    		Program.fromBodyDecls(
    		"void m(boolean b1, boolean b2) {" +
    		"  int x = 23;" +
    		"  while(b1) {" +
    		"    int i = x;" +
    		"    while(b2) {" +
    		"      int j = i;" +
    		"      x = 42;" +
    		"    }" +
    		"  }" +
    		"}"));
    }

    
    public void test22() {
        testFail(
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "class A {" +
            "  int x;" +
            "  void m() throws InterruptedException {" +
            "    Thread t1 = new Thread() {" +
            "      public void run() {" +
            "        x = 23;" +
            "      }" +
            "    };" +
            "    t1.start();"+
            "    int i = x;"+
            "    t1.join();" +
            "    int j = i;"+
            "  }" +
            "}")));
    }
    
    public void test23() {
    	testFail(
    		Program.fromClasses(
    		"class A {" +
    		"  void m() {" +
    		"    final A i = this;" +
    		"    new Object() {" +
    		"      void n() { A myi = i; }" +
    		"    };" +
    		"  }" +
    		"}"));
    }

    
    public void test24() {
    	testFail(
    		Program.fromClasses(
    		"class Outer {" +
    		"  class A {" +
    		"    void m() {" +
    		"      final Outer i = Outer.this;" +
    		"      new Object() {" +
    		"        void n() { Outer myi = i; }" +
    		"      };" +
    		"    }" +
    		"  }" +
    		"}"));
    }
    
    public void test25() {
    	testFail(
    		Program.fromClasses(
    		"class Super {" +
    		"  int x;" +
    		"}",
    		"class A extends Super {" +
    		"  void m() {" +
    		"    final int i = super.x;" +
    		"    new Object() {" +
    		"      void n() { int myi = i; }" +
    		"    };" +
    		"  }" +
    		"}"));
    }
    
    public void test26() {
    	testFail(
    		Program.fromClasses(
    		"class Super {" +
    		"  int x;" +
    		"}",
    		"class Outer extends Super {" +
    		"  class A {" +
    		"    void m() {" +
    		"      final int i = Outer.super.x;" +
    		"      new Object() {" +
    		"        void n() { int myi = i; }" +
    		"      };" +
    		"    }" +
    		"  }" +
    		"}"));
    }
    
    public void test27() {
    	testFail(
    		Program.fromStmts(
    		"int y, z;",
    		"do {" +
    		"  y = 23;" +
    		"  do {" +
    		"    int i = y++;" +
    		"    z = y - i;" +
    		"  } while(false);" +
    		"} while(false);"));
    }
    
    public void test28() {
    	testFail(
    		Program.fromStmts(
    		"int i = 1/0;" +
    		"try {" +
    		"  int j = i;" +
    		"} catch(ArithmeticException ae) {" +
    		"  System.out.println(\"gotcha\");" +
    		"}"));
    }
    
    public void test29() {
    	testSucc(
    		Program.fromStmts(
    		"try {" +
    		"  int i = 1/0;" +
    		"  int j = i;" +
    		"} catch(ArithmeticException ae) {" +
    		"  System.out.println(\"gotcha\");" +
    		"}"),
    		Program.fromStmts(
       		"try {" +
       		"  int j = 1/0;" +
       		"} catch(ArithmeticException ae) {" +
       		"  System.out.println(\"gotcha\");" +
       		"}"));
    }
    
    public void test30() {
    	testFail(
    		Program.fromStmts(
			"int x = 23;" +
    		"try {" +
    		"  int i = 1/0;" +
    		"  x = 42;" +
    		"  int j = i;" +
    		"} catch(ArithmeticException ae) {" +
    		"  System.out.println(x);" +
    		"}"));
    }
}
