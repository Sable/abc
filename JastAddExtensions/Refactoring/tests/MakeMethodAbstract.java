package tests;

import junit.framework.TestCase;
import tests.AllTests;
import AST.MethodDecl;
import AST.Program;
import AST.RawCU;
import AST.RefactoringException;
import AST.SimpleSet;
import AST.TypeDecl;

public class MakeMethodAbstract extends TestCase {
	public MakeMethodAbstract(String name) {
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
			md.makeAbstract();
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
			md.makeAbstract();
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
            "public class A {"+
            "    public void m() { }" +
            "    public void n() { m(); }"+
            "}"),
            Program.fromClasses(
                    "abstract public class A {"+
                    "    abstract public void m();" +
                    "    public void n() { m(); }"+
                    "}"));
    }
	
	public void test2() {
        testSucc("A", "m()",
                Program.fromClasses(
                		"public class A {"+
                        "    public void m() { }" +
                        "    public void n() { m(); }"+
                        "}",
                        "public class B extends A {"+
                        "    public void m() { }"+
                        "}",
                        "public class C extends A {"+
                        "    public void m() { }"+
                        "}"),
                        Program.fromClasses(
                        		"abstract public class A {"+
                                "    abstract public void m();" +
                                "    public void n() { m(); }"+
                                "}",
                                "public class B extends A {"+
                                "    public void m() { }"+
                                "}",
                                "public class C extends A {"+
                                "    public void m() { }"+
                                "}"));
	}
	
	public void test3() {
        testSucc("A", "m()",
        		Program.fromCompilationUnits(
        				new RawCU("A.java",
        						"package p;" +
        						"public class A {" +
        						"  void m() { }" +
        						"  void n() { m(); }" +
        						"}"),
        				new RawCU("B.java",
        						"package q;" +
        						"public class B extends p.A {" +
        						"  void m() { }" +
        						"}")
        				),
                		Program.fromCompilationUnits(
                				new RawCU("A.java",
                						"package p;" +
                						"abstract public class A {" +
                						"  abstract void m();" +
                						"  void n() { m(); }" +
                						"}"),
                				new RawCU("B.java",
                						"package q;" +
                						"abstract public class B extends p.A {" +
                						"  void m() { }" +
                						"}")
                				));
	}
	
	public void test4() {
        testSucc("A", "m()",
        		Program.fromCompilationUnits(
        				new RawCU("A.java",
        						"package p;" +
        						"public class A {" +
        						"  void m() { }" +
        						"  void n() { m(); }" +
        						"}"),
        				new RawCU("B.java",
        						"package q;" +
        						"public class B extends p.A {" +
        						"  " +
        						"}")
        				),
                		Program.fromCompilationUnits(
                				new RawCU("A.java",
                						"package p;" +
                						"abstract public class A {" +
                						"  abstract void m();" +
                						"  void n() { m(); }" +
                						"}"),
                				new RawCU("B.java",
                						"package q;" +
                						"abstract public class B extends p.A {" +
                						"  " +
                						"}")
                				));
	}
	
	public void test5() {
        testSucc("A", "m()",
        		Program.fromCompilationUnits(
        				new RawCU("A.java",
        						"package p;" +
        						"public class A {" +
        						"  void m() { }" +
        						"  void n() { m(); }" +
        						"}"),
        				new RawCU("B.java",
        						"package q;" +
        						"public class B extends p.A {" +
        						"  void m() { }" +
        						"}"),
        				new RawCU("C.java",
           						"package p;" +
                				"public class C extends q.B {" +
                				"  void m() { }" +
        						"}")
        				),
        				Program.fromCompilationUnits(
                				new RawCU("A.java",
                						"package p;" +
                						"abstract public class A {" +
                						"  abstract void m();" +
                						"  void n() { m(); }" +
                						"}"),
                				new RawCU("B.java",
                						"package q;" +
                						"abstract public class B extends p.A {" +
                						"  void m() { }" +
                						"}"),
                				new RawCU("C.java",
                   						"package p;" +
                        				"public class C extends q.B {" +
                        				"  void m() { }" +
                						"}")
                				));
	}
	
	public void test6() {
        testFail("A", "m()",
                Program.fromClasses(
                		"public class A {"+
                        "    public void m() { }" +
                        "}",
                        "public class B extends A {"+
                        "    public void m() { super.m(); }"+
                        "}",
                        "public class C extends A {"+
                        "    public void m() { }"+
                        "}"));
	}

}