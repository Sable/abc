package tests;

import junit.framework.TestCase;
import tests.AllTests;
import AST.MethodDecl;
import AST.Program;
import AST.RawCU;
import AST.RefactoringException;
import AST.SimpleSet;
import AST.TypeDecl;

public class MakeMethodStaticTests extends TestCase {
	public MakeMethodStaticTests(String name) {
		super(name);
	}
	
	public void testSucc(String tp_name, String sig, Program in, Program out) {		
		assertNotNull(in);
		assertNotNull(out);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		TypeDecl tp = in.findType(tp_name);
		assertNotNull(tp);
		SimpleSet s = tp.localMethodsSignature(sig);
		assertTrue(s.isSingleton());
		MethodDecl md = (MethodDecl)s.iterator().next();
		try {
			md.makeStatic();
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			fail("Refactoring was supposed to succeed; failed with "+rfe);
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	public void testFail(String tp_name, String sig, Program in) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		TypeDecl tp = in.findType(tp_name);
		assertNotNull(tp);
		SimpleSet s = tp.localMethodsSignature(sig);
		assertTrue(s.isSingleton());
		MethodDecl md = (MethodDecl)s.iterator().next();
		try {
			md.makeStatic();
			fail("Refactoring was supposed to fail; succeeded with "+in);
		} catch(RefactoringException rfe) {
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

    public void test1() {
        testSucc("A", "m()",
            Program.fromClasses(
            "class A {"+
            "    public void m() { }"+
            "}"),
            Program.fromClasses(
            "class A {"+
            "    public void m() { m(this); }"+
            "    public static void m(A a) { }"+
            "}"));
    }

    public void test2() {
        testSucc("A", "m()",
            Program.fromClasses(
            "class A {"+
            "    static int i;"+
            "    public int m() { return i; }"+
            "}"),
            Program.fromClasses(
            "class A {"+
            "    static int i;"+
            "    public int m() { return m(this); }"+
            "    public static int m(A a) {	return i; }"+
            "}"));
    }

    public void test3() {
        testSucc("A", "m()",
            Program.fromClasses(
            "class A {"+
            "    int i;"+
            "    public int m() { return i; }"+
            "}"),
            Program.fromClasses(
            "class A {"+
            "    int i;"+
            "    public int m() { return m(this); }"+
            "    public static int m(A a) { return a.i; }"+
            "}"));
    }

    public void test4() {
        testSucc("A", "m()",
            Program.fromClasses(
            "class A {"+
            "    public int i;"+
            "    public int m() { return this.i; }"+
            "}"),
            Program.fromClasses(
            "class A {"+
            "    public int i;"+
            "    public int m() { return m(this); }"+
            "    public static int m(A a) { return a.i; }"+
            "}"));
    }

    public void test5() {
        testSucc("A", "m()",
            Program.fromClasses(
            "class Super { public int i; }",
            "class A extends Super {"+
            "    public int i;"+
            "    public int m() { return super.i; }"+
            "}"),
            Program.fromClasses(
            "class Super { public int i; }",
            "class A extends Super {"+
            "    public int i;"+
            "    public int m() { return m(this); }"+
            "    public static int m(A a) { return ((Super)a).i; }"+
            "}"));
    }

    public void test7() {
        testSucc("A", "m(int)",
            Program.fromClasses(
            "class A {"+
            "    public int f(int i) { return 0; }"+
            "    public int m(int i) { return f(i); }"+
            "}"),
            Program.fromClasses(
            "class A {"+
            "    public int f(int i) { return 0; }"+
            "    public int m(int i) { return m(this, i); }"+
            "    public static int m(A a, int i) { return a.f(i); }"+
            "}"));
    }

    public void test8() {
        testSucc("A", "m(int)",
            Program.fromClasses(
            "class A {"+
            "    public int myData;"+
            "    int m(int i) {"+
            "        new Runnable () {"+
            "            void f(int i) {};"+
            "            public void run() {"+
            "                this.f(myData);"+
            "            }"+
            "        };"+
            "        return this.myData + myData;"+
            "    }"+
            "}"),
            Program.fromClasses(
            "class A {"+
            "    public int myData;"+
            "    int m(int i) { return m(this, i); }"+
            "    static int m(final A a, int i) {"+
            "        new Runnable () {"+
            "            void f(int i) {};"+
            "            public void run() {"+
            "                this.f(a.myData);"+
            "            }"+
            "        };"+
            "        return a.myData + a.myData;"+
            "    }"+
            "}"));
    }

    public void test9() {
        testSucc("A", "m(int)",
            Program.fromClasses(
            "class A {"+
            "    public int myData;"+
            "    int m(int i) { return myData + i; }"+
            "}",
            "class B extends A {"+
            "    int a(int b) { return m(b*2); }"+
            "}"),
            Program.fromClasses(
            "class A {"+
            "    public int myData;"+
            "    int m(int i) { return m(this, i); }"+
            "    static int m(A a, int i) { return a.myData + i; }"+
            "}"+
            ""+
            "class B extends A {"+
            "    int a(int b) { return m(b*2); }"+
            "}"));
    }

    public void test10() {
        testSucc("A", "m(int)",
            Program.fromClasses(
            "class A {"+
            "    public int myData;"+
            "    int m(int i) { return myData+i; }"+
            "}",
            "class B {"+
            "    public A myA;"+
            "    int a(int b) { return myA.m(b*2); }"+
            "}"),
            Program.fromClasses(
            "class A {"+
            "    public int myData;"+
            "    int m(int i) { return m(this, i); }"+
            "    static int m(A a, int i) { return a.myData+i; }"+
            "}",
            "class B {"+
            "    public A myA;"+
            "    int a(int b) { return myA.m(b*2); }"+
            "}"));
    }

    public void test11() {
        testSucc("A", "m(int)",
            Program.fromClasses(
            "class A {"+
            "    public int myData;"+
            "    int m(int i) { return myData+i; }"+
            "}",
            "class B extends A {"+
            "    int m(int b) {"+
            "        return super.m(b*2);"+
            "    }"+
            "}"),
            Program.fromClasses(
            "class A {"+
            "    public int myData;"+
            "    int m(int i) { return m(this, i); }"+
            "    static int m(A a, int i) { return a.myData+i; }"+
            "}",
            "class B extends A {"+
            "    int m(int b) {"+
            "        return super.m(b*2);"+
            "    }"+
            "}"));
    }

    public void test12() {
        testFail("A", "m()",
            Program.fromClasses(
            "class A extends java.util.Vector {"+
            "    String m() { return super.toString(); }"+
            "}"+
            ""));
    }

    public void test13() {
        testSucc("A", "m()",
            Program.fromClasses(
            "class A {"+
            "   A m() { return this; }"+
            "}"),
            Program.fromClasses(
            "class A {" +
            "   A m() { return m(this); }"+
            "   static A m(A a) { return a; }"+
            "}"));
    }

    public void test14() {
        testSucc("A", "m()",
            Program.fromClasses(
            "class A {"+
            "   A m() {"+
            "     final A[] result = new A[1];"+
            "     new Runnable() {"+
            "         public void run() {"+
            "            result[0] = A.this;"+
            "         }"+
            "     }.run();"+
            "     return result[0];"+
            "   }"+
            "}"),
            Program.fromClasses(
            "class A {" +
            "   A m() { return m(this); }"+
            "   static A m(final A a) {"+
            "     final A[] result = new A[1];"+
            "     new Runnable() {"+
            "         public void run() {"+
            "            result[0] = a;"+
            "         }"+
            "     }.run();"+
            "     return result[0];" +
            "   }"+
            "}"));
    }

    public void test15() {
        testSucc("A", "m(boolean)",
            Program.fromClasses(
            "class A {"+
            "  A f() { return this; }"+
            "  void m(boolean b) {"+
            "    if(b) f().m(!b);"+
            "  }"+
            "}"),
            Program.fromClasses(
            "class A {"+
            "  A f() { return this; }"+
            "  void m(boolean b) { m(this, b); }"+
            "  static void m(A a, boolean b) {"+
            "    if(b) a.f().m(!b);"+
            "  }"+
            "}"));
    }

    public void test16() {
        testSucc("A", "m()",
            Program.fromClasses(
            "class A {"+
            "    void f(String s) {}"+
            "    String field;"+
            "    void m() {"+
            "        f(field);"+
            "    }"+
            "}"),
            Program.fromClasses(
            "class A {"+
            "    void f(String s) {}"+
            "    String field;" +
            "    void m() { m(this); }"+
            "    static void m(A a) {"+
            "      a.f(a.field);"+
            "    }"+
            "}"));
    }

    public void test18() {
        testSucc("A", "m()",
            Program.fromClasses(
            "class A<T> {"+
            "    void m() { System.out.println(this); }"+
            "}"),
            Program.fromClasses(
            "class A<T> {" +
            "    void m() { m(this); }"+
            "    static <T> void m(A<T> a) {"+
            "      System.out.println(a);"+
            "    }"+
            "}"));
    }

    public void test19() {
        testSucc("A", "m(int)",
            Program.fromClasses(
            "class A {"+
            "    int myField;"+
            "    void m(int i) { myField = i; }"+
            "}"),
            Program.fromClasses(
            "class A {"+
            "    int myField;" +
            "    void m(int i) { m(this, i); }"+
            "    static void m(A a, int i) {"+
            "      a.myField = i;"+
            "    }"+
            "}"));
    }

    public void test20() {
        testSucc("A", "m()",
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "import javax.swing.*;"+
            "import java.awt.event.WindowAdapter;"+
            "import java.awt.event.WindowEvent;"+
            ""+
            "final class A extends JFrame {"+
            "    public A() {"+
            "        m();"+
            "    }"+
            ""+
            "    public void m() {"+
            "        addWindowListener(new MyWindowListener());"+
            "    }"+
            ""+
            "    private class MyWindowListener extends WindowAdapter {"+
            "        public void windowActivated(WindowEvent e) {"+
            "        }"+
            "    }"+
            "}")),
            Program.fromCompilationUnits(
            new RawCU("A.java",
            "import javax.swing.*;"+
            "import java.awt.event.WindowAdapter;"+
            "import java.awt.event.WindowEvent;"+
            ""+
            "final class A extends JFrame {"+
            "    public A() {"+
            "        m();"+
            "    }"+
            "    public void m() { m(this); }"+
            "    private class MyWindowListener"+
            "       extends WindowAdapter {"+
            "        public void windowActivated(WindowEvent e) {"+
            "        }"+
            "    }"+
            "    public static void m(A a) {"+
            "      a.addWindowListener(a.new MyWindowListener());"+
            "    }"+
            "}")));
    }

    public void test21() {
        testSucc("A", "m()",
            Program.fromClasses(
            "class A {"+
            "    public void m() {"+
            "        this.m();"+
            "    }"+
            "}"),
            Program.fromClasses(
            "class A {" +
            "    public void m() { m(this); }"+
            "    public static void m(A a) {"+
            "      a.m();"+
            "    }"+
            "}"));
    }

    public void test22() {
        testSucc("A", "m()",
            Program.fromClasses(
            "class A {"+
            "    public void m() {"+
            "        n();"+
            "    }"+
            "    void n() {}"+
            "    class B {"+
            "        void f() {"+
            "            m();"+
            "        }"+
            "    }"+
            "}"),
            Program.fromClasses(
            "class A {" +
            "    public void m() { m(this); }"+
            "    void n() { }"+
            "    class B {"+
            "        void f() {"+
            "            m();"+
            "        }"+
            "    }"+
            "    public static void m(A a) {"+
            "      a.n();"+
            "    }"+
            "}"));
    }

    public void test23() {
        testSucc("A", "m(java.lang.Object)",
            Program.fromClasses(
            "class A<T> {"+
            "    public T m(T t) { return t; }"+
            "}"),
            Program.fromClasses(
            "class A<T> {" +
            "    public T m(T t) { return m(this, t); }"+
            "    public static <T> T m(A<T> a, T t) {"+
            "      return t;"+
            "    }"+
            "}"));
    }

}