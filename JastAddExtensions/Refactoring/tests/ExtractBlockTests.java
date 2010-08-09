package tests;

import junit.framework.TestCase;
import tests.AllTests;
import AST.Block;
import AST.CompilationUnit;
import AST.Program;
import AST.RefactoringException;
import AST.Stmt;

public class ExtractBlockTests extends TestCase {
	public ExtractBlockTests(String name) {
		super(name);
	}
	
	public void testSucc(Program in, Program out) {
        assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
        assertNotNull(out);
        CompilationUnit cu = in.lookupType("", "A").compilationUnit();
        assertNotNull(cu);
        Stmt from = cu.findStmtFollowingComment("// from\n");
        assertNotNull(from);
        Stmt to = cu.findStmtPrecedingComment("// to\n");
        assertNotNull(to);
        Block block = from.enclosingBlock();
        assertEquals(block, to.enclosingBlock());
        int start = block.getStmtList().getIndexOfChild(from);
        int end = block.getStmtList().getIndexOfChild(to);
        assertNotSame(start, -1);
        assertNotSame(end, -1);
        try {
			block.doExtractBlock(start, end);
			assertEquals(out.toString(), in.toString());
		} catch (RefactoringException e) {
			assertEquals(out, "<failure>");
		}
		in.undoAll();
		if (Program.isRecordingASTChanges()) assertEquals(originalProgram, in.toString());
	}
	
	public void testFail(Program in) {
        assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
        CompilationUnit cu = in.lookupType("", "A").compilationUnit();
        assertNotNull(cu);
        Stmt from = cu.findStmtFollowingComment("// from\n");
        assertNotNull(from);
        Stmt to = cu.findStmtPrecedingComment("// to\n");
        assertNotNull(to);
        Block block = from.enclosingBlock();
        assertEquals(block, to.enclosingBlock());
        int start = block.getStmtList().getIndexOfChild(from);
        int end = block.getStmtList().getIndexOfChild(to);
        assertNotSame(start, -1);
        assertNotSame(end, -1);
        try {
			block.doExtractBlock(start, end);
			assertEquals("<failure>", in);
		} catch (RefactoringException e) {
		}
		in.undoAll();
		if (Program.isRecordingASTChanges()) assertEquals(originalProgram, in.toString());
	}

	public void test1() {
		testSucc(Program.fromClasses(
				"class A {\n" +
				"  void m() {\n" +
				"    int i;\n" +
				"    // from\n" +
				"    i = 23;\n" +
				"    // to\n" +
				"    System.out.println(i);\n" +
				"  }\n" +
				"}"),
				Program.fromClasses(
				"class A {\n" +
				"  void m() {\n" +
				"    int i;\n" +
				"    {\n" +
				"      i = 23;\n" +
				"    }\n" +
				"    System.out.println(i);\n" +
				"  }\n" +
				"}"));
	}

	public void test2() {
		testSucc(Program.fromClasses(
				"class A {\n" +
				"  void m() {\n" +
				"    int i;\n" +
				"    // from\n" +
				"    i = 23;\n" +
				"    // to\n" +
				"  }\n" +
				"}"),
				Program.fromClasses(
				"class A {\n" +
				"  void m() {\n" +
				"    int i;\n" +
				"    {\n" +
				"      i = 23;\n" +
				"    }\n" +
				"  }\n" +
				"}\n"));
	}

	public void test3() {
		testSucc(Program.fromClasses(
				"class A {\n" +
				"  void m() {\n" +
				"    // from\n" +
				"    int i;\n" +
				"    i = 23;\n" +
				"    // to\n" +
				"  }\n" +
				"}"),
				Program.fromClasses(
				"class A {\n" +
				"  void m() {\n" +
				"    {\n" +
				"      int i;\n" +
				"      i = 23;\n" +
				"    }\n" +
				"  }\n" +
				"}"));
	}

	public void test4() {
		testSucc(Program.fromClasses(
				"class A {\n" +
				"  void m() {\n" +
				"    // from\n" +
				"    int i;\n" +
				"    i = 23;\n" +
				"    // to\n" +
				"    System.out.println(i);\n" +
				"  }\n" +
				"}"),
				Program.fromClasses(
				"class A {\n" +
				"  void m() {\n" +
				"    int i;\n" +
				"    {\n" +
				"      i = 23;\n" +
				"    }\n" +
				"    System.out.println(i);\n" +
				"  }\n" +
				"}"));
	}

	public void test5() {
		testSucc(Program.fromClasses(
				"class A {\n" +
				"  void m() {\n" +
				"    // from\n" +
				"    int i = 23;\n" +
				"    // to\n" +
				"    System.out.println(i);\n" +
				"  }\n" +
				"}"),
				Program.fromClasses(
				"class A {\n" +
				"  void m() {\n" +
				"    int i;\n" +
				"    {\n" +
				"      i = 23;\n" +
				"    }\n" +
				"    System.out.println(i);\n" +
				"  }\n" +
				"}"));
	}

	public void test6() {
		testSucc(Program.fromClasses(
				"class A {\n" +
				"  void m() {\n" +
				"    // from\n" +
				"    System.out.println(42);\n" +
				"    int i = 23;\n" +
				"    // to\n" +
				"    System.out.println(i);\n" +
				"  }\n" +
				"}"),
				Program.fromClasses(
				"class A {\n" +
				"  void m() {\n" +
				"    int i;\n" +
				"    {\n" +
				"      System.out.println(42);\n" +
				"      i = 23;\n" +
				"    }\n" +
				"    System.out.println(i);\n" +
				"  }\n" +
				"}"));
	}

	public void test7() {
		testSucc(Program.fromClasses(
				"class A {\n" +
				"  void m() {\n" +
				"    // from\n" +
				"    System.out.println();\n" +
				"    System.out.println();\n" +
				"    int j;\n" +
				"    j = 42;\n" +
				"    System.out.println(j);\n" +
				"    int i = 23;\n" +
				"    // to\n" +
				"    System.out.println(i+j);\n" +
				"  }\n" +
				"}"),
				Program.fromClasses(
				"class A {\n" +
				"  void m() {\n" +
				"    int i;\n" +
				"    int j;\n" +
				"    {\n" +
				"      System.out.println();\n" +
				"      System.out.println();\n" +
				"      j = 42;\n" +
				"      System.out.println(j);\n" +
				"      i = 23;\n" +
				"    }\n" +
				"    System.out.println(i + j);\n" +
				"  }\n" +
				"}"));
	}

	public void test8() {
		testFail(Program.fromClasses(
				"class A {\n" +
				"  void m() {\n" +
				"    switch(42) {\n" +
				"    // from\n" +
				"    case 42:\n" +
				"      System.out.println(\"Leibniz was right\");\n" +
				"      break;\n" +
				"    // to\n" +
				"    }\n" +
				"  }\n" +
				"}"));
	}

	public void test9() {
		testFail(Program.fromClasses(
				"class A {\n" +
				"  void m() {\n" +
				"    switch(42) {\n" +
				"    // from\n" +
				"    case 42:\n" +
				"    // to\n" +
				"      System.out.println(\"Leibniz was right\");\n" +
				"      break;\n" +
				"    }\n" +
				"  }\n" +
				"}"));
	}

	public void test10() {
		testSucc(Program.fromClasses(
				"class A {\n" +
				"  void m() {\n" +
				"    switch(42) {\n" +
				"    case 42:\n" +
				"    // from\n" +
				"      System.out.println(\"Leibniz was right\");\n" +
				"      break;\n" +
				"    // to\n" +
				"    }\n" +
				"  }\n" +
				"}"),
				Program.fromClasses(
				"class A {\n" +
				"  void m() {\n" +
				"    switch (42){\n" +
				"      case 42:\n" +
				"      {\n" +
				"        System.out.println(\"Leibniz was right\");\n" +
				"        break ;\n" +
				"      }\n" +
				"    }\n" +
				"  }\n" +
				"}"));
	}

	public void test11() {
		testSucc(Program.fromClasses(
				"class A {\n" +
				"	int x;\n" +
				"	void m() {\n" +
				"		// from\n" +
				"		x = 1;  \n" +
				"		int x = 2;\n" +
				"		// to\n" +
				"		System.out.println(x);\n" +
				"	}\n" +
				"}"),
				Program.fromClasses(
				"class A {\n" +
				"  int x;\n" +
				"  void m() {\n" +
				"    int x;\n" +
				"    {\n" +
				"      this.x = 1;\n" +
				"      x = 2;\n" +
				"    }\n" +
				"    System.out.println(x);\n" +
				"  }\n" +
				"}"));
	}

}