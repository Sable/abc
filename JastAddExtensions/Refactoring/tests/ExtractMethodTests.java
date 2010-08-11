package tests;

import junit.framework.TestCase;
import tests.AllTests;
import AST.ASTNode;
import AST.Block;
import AST.CompilationUnit;
import AST.ConstructorDecl;
import AST.MethodDecl;
import AST.Program;
import AST.RawCU;
import AST.RefactoringException;
import AST.Stmt;
import AST.TypeDecl;

public class ExtractMethodTests extends TestCase {
	public ExtractMethodTests(String name) {
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
			block.doExtractMethod(ASTNode.VIS_PROTECTED, "extracted", start, end);
			assertEquals(out.toString(), in.toString());
		} catch (RefactoringException e) {
			assertEquals(out.toString(), "<failure>");
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}
	public void testSucc(String className, String methodName, int begin, int end, String newMethodName, int visibility, Program in, Program out) {
        assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
        assertNotNull(out);
        TypeDecl A = in.findType(className);
        Block b;
        MethodDecl m = A.findMethod(methodName);
        if(m != null) {
        	b = m.getBlock();
        } else {
        	b = ((ConstructorDecl)A.constructors().iterator().next()).getBlock();
        }
        try {
			b.doExtractMethod(visibility, newMethodName, begin, end);
			assertEquals(out.toString(), in.toString());
		} catch (RefactoringException e) {
			assertEquals(out.toString(), "<failure>");
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}
	public void testSucc(String className, String methodName, int begin, int end, String newMethodName, Program in, Program out) {
		testSucc(className, methodName, begin, end, newMethodName, ASTNode.VIS_PRIVATE, in, out);
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
			block.doExtractMethod(ASTNode.VIS_PROTECTED, "extracted", start, end);
			assertEquals("<failure>", in.toString());
		} catch (RefactoringException e) {
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}
	public void testFail(String className, String methodName, int begin, int end, String newMethodName, int visibility, Program in) {
        assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
        TypeDecl A = in.findType(className);
        Block b;
        MethodDecl m = A.findMethod(methodName);
        if(m != null) {
        	b = m.getBlock();
        } else {
        	b = ((ConstructorDecl)A.constructors().iterator().next()).getBlock();
        }
        try {
			b.doExtractMethod(visibility, newMethodName, begin, end);
			assertEquals("<failure>", in.toString());
		} catch (RefactoringException e) {
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}
	public void testFail(String className, String methodName, int begin, int end, String newMethodName, Program in) {
		testFail(className, methodName, begin, end, newMethodName, ASTNode.VIS_PRIVATE, in);
	}
	
	public void test0() {
		testSucc("A", "m", 0, 0, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n" +
				"\n" +
				"class A {\n" +
				"	void m() throws FileNotFoundException {\n" +
				"		int i;\n" +
				"		i = 2;\n" +
				"		for(int j=0;j<i;++j) {\n" +
				"			if(j==4)\n" +
				"				throw new FileNotFoundException(\"\");\n" +
				"			++i;\n" +
				"		}\n" +
				"		int j = ++i;\n" +
				"	}\n" +
				"}")),
		Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n"+
				"\n"+
				"class A {\n"+
				"  void m() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    n();\n"+
				"    i = 2;\n"+
				"    for(int j = 0; j < i; ++j) {\n"+
				"      if(j == 4) \n"+
				"        throw new FileNotFoundException(\"\");\n"+
				"      ++i;\n"+
				"    }\n"+
				"    int j = ++i;\n"+
				"  }\n"+
				"  private void n() {\n"+
				"  }\n"+
				"}")));
	}

	public void test1() {
		testSucc("A", "m", 0, 1, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n" +
				"\n" +
				"class A {\n" +
				"	void m() throws FileNotFoundException {\n" +
				"		int i;\n" +
				"		i = 2;\n" +
				"		for(int j=0;j<i;++j) {\n" +
				"			if(j==4)\n" +
				"				throw new FileNotFoundException(\"\");\n" +
				"			++i;\n" +
				"		}\n" +
				"		int j = ++i;\n" +
				"	}\n" +
				"}")),
		Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n"+
				"\n"+
				"class A {\n"+
				"  void m() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    i = n();\n"+
				"    for(int j = 0; j < i; ++j) {\n"+
				"      if(j == 4) \n"+
				"        throw new FileNotFoundException(\"\");\n"+
				"      ++i;\n"+
				"    }\n"+
				"    int j = ++i;\n"+
				"  }\n"+
				"  private int n() {\n"+
				"    int i;\n"+
				"    i = 2;\n"+
				"    return i;\n"+
				"  }\n"+
				"}")));
	}

	public void test2() {
		testSucc("A", "m", 0, 2, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n" +
				"\n" +
				"class A {\n" +
				"	void m() throws FileNotFoundException {\n" +
				"		int i;\n" +
				"		i = 2;\n" +
				"		for(int j=0;j<i;++j) {\n" +
				"			if(j==4)\n" +
				"				throw new FileNotFoundException(\"\");\n" +
				"			++i;\n" +
				"		}\n" +
				"		int j = ++i;\n" +
				"	}\n" +
				"}")),
		Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n"+
				"\n"+
				"class A {\n"+
				"  void m() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    i = n();\n"+
				"    int j = ++i;\n"+
				"  }\n"+
				"  private int n() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    i = 2;\n"+
				"    for(int j = 0; j < i; ++j) {\n"+
				"      if(j == 4) \n"+
				"        throw new FileNotFoundException(\"\");\n"+
				"      ++i;\n"+
				"    }\n"+
				"    return i;\n"+
				"  }\n"+
				"}")));
	}

	public void test3() {
		testSucc("A", "m", 0, 3, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n" +
				"\n" +
				"class A {\n" +
				"	void m() throws FileNotFoundException {\n" +
				"		int i;\n" +
				"		i = 2;\n" +
				"		for(int j=0;j<i;++j) {\n" +
				"			if(j==4)\n" +
				"				throw new FileNotFoundException(\"\");\n" +
				"			++i;\n" +
				"		}\n" +
				"		int j = ++i;\n" +
				"	}\n" +
				"}")),
		Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n"+
				"\n"+
				"class A {\n"+
				"  void m() throws FileNotFoundException {\n"+
				"    n();\n"+
				"  }\n"+
				"  private void n() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    i = 2;\n"+
				"    for(int j = 0; j < i; ++j) {\n"+
				"      if(j == 4) \n"+
				"        throw new FileNotFoundException(\"\");\n"+
				"      ++i;\n"+
				"    }\n"+
				"    int j = ++i;\n"+
				"  }\n"+
				"}")));
	}

	public void test4() {
		testSucc("A", "m", 0, 1, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n" +
				"\n" +
				"class A {\n" +
				"	void m() throws FileNotFoundException {\n" +
				"		int i;\n" +
				"		i = 2;\n" +
				"		for(int j=0;j<i;++j) {\n" +
				"			if(j==4)\n" +
				"				throw new FileNotFoundException(\"\");\n" +
				"			++i;\n" +
				"		}\n" +
				"		int j = ++i;\n" +
				"	}\n" +
				"}")),
		Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n"+
				"\n"+
				"class A {\n"+
				"  void m() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    i = n();\n"+
				"    for(int j = 0; j < i; ++j) {\n"+
				"      if(j == 4) \n"+
				"        throw new FileNotFoundException(\"\");\n"+
				"      ++i;\n"+
				"    }\n"+
				"    int j = ++i;\n"+
				"  }\n"+
				"  private int n() {\n"+
				"    int i;\n"+
				"    i = 2;\n"+
				"    return i;\n"+
				"  }\n"+
				"}")));
	}

	public void test5() {
		testSucc("A", "m", 1, 1, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n" +
				"\n" +
				"class A {\n" +
				"	void m() throws FileNotFoundException {\n" +
				"		int i;\n" +
				"		i = 2;\n" +
				"		for(int j=0;j<i;++j) {\n" +
				"			if(j==4)\n" +
				"				throw new FileNotFoundException(\"\");\n" +
				"			++i;\n" +
				"		}\n" +
				"		int j = ++i;\n" +
				"	}\n" +
				"}")),
		Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n"+
				"\n"+
				"class A {\n"+
				"  void m() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    i = n();\n"+
				"    for(int j = 0; j < i; ++j) {\n"+
				"      if(j == 4) \n"+
				"        throw new FileNotFoundException(\"\");\n"+
				"      ++i;\n"+
				"    }\n"+
				"    int j = ++i;\n"+
				"  }\n"+
				"  private int n() {\n"+
				"    int i;\n"+
				"    i = 2;\n"+
				"    return i;\n"+
				"  }\n"+
				"}")));
	}

	public void test6() {
		testSucc("A", "m", 1, 2, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n" +
				"\n" +
				"class A {\n" +
				"	void m() throws FileNotFoundException {\n" +
				"		int i;\n" +
				"		i = 2;\n" +
				"		for(int j=0;j<i;++j) {\n" +
				"			if(j==4)\n" +
				"				throw new FileNotFoundException(\"\");\n" +
				"			++i;\n" +
				"		}\n" +
				"		int j = ++i;\n" +
				"	}\n" +
				"}")),
		Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n"+
				"\n"+
				"class A {\n"+
				"  void m() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    i = n();\n"+
				"    int j = ++i;\n"+
				"  }\n"+
				"  private int n() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    i = 2;\n"+
				"    for(int j = 0; j < i; ++j) {\n"+
				"      if(j == 4) \n"+
				"        throw new FileNotFoundException(\"\");\n"+
				"      ++i;\n"+
				"    }\n"+
				"    return i;\n"+
				"  }\n"+
				"}")));
	}

	public void test7() {
		testSucc("A", "m", 1, 3, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n" +
				"\n" +
				"class A {\n" +
				"	void m() throws FileNotFoundException {\n" +
				"		int i;\n" +
				"		i = 2;\n" +
				"		for(int j=0;j<i;++j) {\n" +
				"			if(j==4)\n" +
				"				throw new FileNotFoundException(\"\");\n" +
				"			++i;\n" +
				"		}\n" +
				"		int j = ++i;\n" +
				"	}\n" +
				"}")),
		Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n"+
				"\n"+
				"class A {\n"+
				"  void m() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    n();\n"+
				"  }\n"+
				"  private void n() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    i = 2;\n"+
				"    for(int j = 0; j < i; ++j) {\n"+
				"      if(j == 4) \n"+
				"        throw new FileNotFoundException(\"\");\n"+
				"      ++i;\n"+
				"    }\n"+
				"    int j = ++i;\n"+
				"  }\n"+
				"}")));
	}

	public void test8() {
		testSucc("A", "m", 2, 2, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n" +
				"\n" +
				"class A {\n" +
				"	void m() throws FileNotFoundException {\n" +
				"		int i;\n" +
				"		i = 2;\n" +
				"		for(int j=0;j<i;++j) {\n" +
				"			if(j==4)\n" +
				"				throw new FileNotFoundException(\"\");\n" +
				"			++i;\n" +
				"		}\n" +
				"		int j = ++i;\n" +
				"	}\n" +
				"}")),
		Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n"+
				"\n"+
				"class A {\n"+
				"  void m() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    i = 2;\n"+
				"    i = n(i);\n"+
				"    int j = ++i;\n"+
				"  }\n"+
				"  private int n(int i) throws FileNotFoundException {\n"+
				"    for(int j = 0; j < i; ++j) {\n"+
				"      if(j == 4) \n"+
				"        throw new FileNotFoundException(\"\");\n"+
				"      ++i;\n"+
				"    }\n"+
				"    return i;\n"+
				"  }\n"+
				"}")));
	}

	public void test9() {
		testSucc("A", "m", 2, 3, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n" +
				"\n" +
				"class A {\n" +
				"	void m() throws FileNotFoundException {\n" +
				"		int i;\n" +
				"		i = 2;\n" +
				"		for(int j=0;j<i;++j) {\n" +
				"			if(j==4)\n" +
				"				throw new FileNotFoundException(\"\");\n" +
				"			++i;\n" +
				"		}\n" +
				"		int j = ++i;\n" +
				"	}\n" +
				"}")),
		Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n"+
				"\n"+
				"class A {\n"+
				"  void m() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    i = 2;\n"+
				"    n(i);\n"+
				"  }\n"+
				"  private void n(int i) throws FileNotFoundException {\n"+
				"    for(int j = 0; j < i; ++j) {\n"+
				"      if(j == 4) \n"+
				"        throw new FileNotFoundException(\"\");\n"+
				"      ++i;\n"+
				"    }\n"+
				"    int j = ++i;\n"+
				"  }\n"+
				"}")));
	}

	public void test10() {
		testSucc("A", "m", 3, 3, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n" +
				"\n" +
				"class A {\n" +
				"	void m() throws FileNotFoundException {\n" +
				"		int i;\n" +
				"		i = 2;\n" +
				"		for(int j=0;j<i;++j) {\n" +
				"			if(j==4)\n" +
				"				throw new FileNotFoundException(\"\");\n" +
				"			++i;\n" +
				"		}\n" +
				"		int j = ++i;\n" +
				"	}\n" +
				"}")),
		Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n"+
				"\n"+
				"class A {\n"+
				"  void m() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    i = 2;\n"+
				"    for(int j = 0; j < i; ++j) {\n"+
				"      if(j == 4) \n"+
				"        throw new FileNotFoundException(\"\");\n"+
				"      ++i;\n"+
				"    }\n"+
				"    n(i);\n"+
				"  }\n"+
				"  private void n(int i) {\n"+
				"    int j = ++i;\n"+
				"  }\n"+
				"}")));
	}

	public void test11() {
		testSucc("A", "m", 1, 3, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n" +
				"\n" +
				"class A {\n" +
				"	void m(int k) throws FileNotFoundException {\n" +
				"		int i = k+1;\n" +
				"		i = 2;\n" +
				"		for(int j=0;j<i;++j) {\n" +
				"			if(j==4)\n" +
				"				throw new FileNotFoundException(\"\");\n" +
				"			++i;\n" +
				"		}\n" +
				"		int j = ++i;\n" +
				"	}\n" +
				"}")),
		Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n"+
				"\n"+
				"class A {\n"+
				"  void m(int k) throws FileNotFoundException {\n"+
				"    int i = k + 1;\n"+
				"    n();\n"+
				"  }\n"+
				"  private void n() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    i = 2;\n"+
				"    for(int j = 0; j < i; ++j) {\n"+
				"      if(j == 4) \n"+
				"        throw new FileNotFoundException(\"\");\n"+
				"      ++i;\n"+
				"    }\n"+
				"    int j = ++i;\n"+
				"  }\n"+
				"}")));
	}

	public void test12() {
		testFail("A", "m", 3, 3, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
				"class A {\n" +
				"    void m(int k) throws Throwable {\n" +
				"	class MyExn extends Throwable { }\n" +
				"	int i = k+1;\n" +
				"	i = 2;\n" +
				"	for(int j=0;j<i;++j) {\n" +
				"	    if(j==4)\n" +
				"		throw new MyExn();\n" +
				"	    ++i;\n" +
				"	}\n" +
				"	int j = ++i;\n" +
				"    }\n" +
				"}\n" +
		"")));
	}

	public void test13() {
		testSucc("A", "m", 2, 2, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
				"class A {\n" +
				"    class MyExn extends Throwable { }\n" +
				"    void m(int k) throws Throwable {\n" +
				"	int i = k+1;\n" +
				"	i = 2;\n" +
				"	for(int j=0;j<i;++j) {\n" +
				"	    if(j==4)\n" +
				"		throw new MyExn();\n" +
				"	    ++i;\n" +
				"	}\n" +
				"	int j = ++i;\n" +
				"    }\n" +
				"}")),
		Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  \n"+
				"  class MyExn extends Throwable { }\n"+
				"  void m(int k) throws Throwable {\n"+
				"    int i = k + 1;\n"+
				"    i = 2;\n"+
				"    i = n(i);\n"+
				"    int j = ++i;\n"+
				"  }\n"+
				"  private int n(int i) throws MyExn {\n"+
				"    for(int j = 0; j < i; ++j) {\n"+
				"      if(j == 4) \n"+
				"        throw new MyExn();\n"+
				"      ++i;\n"+
				"    }\n"+
				"    return i;\n"+
				"  }\n"+
				"}")));
	}

	public void test14() {
		testFail("A", "m", 3, 3, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
				"class A {\n" +
				"  class MyExn extends Throwable { }\n" +
				"  void m(int k) throws Throwable {\n" +
				"    class MyExn extends Throwable { }\n" +
				"    int i = k+1;\n" +
				"    i = 2;\n" +
				"    for(int j=0;j<i;++j) {\n" +
				"      if(j==4)\n" +
				"	throw new MyExn();\n" +
				"      ++i;\n" +
				"    }\n" +
				"    int j = ++i;\n" +
				"  }\n" +
				"}\n" +
		"")));
	}

	public void test16() {
		testFail("B", "m", 0, 0, "n", ASTNode.VIS_PACKAGE, Program.fromCompilationUnits(new RawCU("A.java",
				"class B {\n" +
				"    void m() {\n" +
				"	System.out.println(1);\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"class C extends B {\n" +
				"    void n() {\n" +
				"	System.out.println(2);\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"public class Tst2 {\n" +
				"    public static void main(String[] args) {\n" +
				"	B b = new C();\n" +
				"	b.m();\n" +
				"    }\n" +
				"}")));
	}

	public void test17() {
		testSucc("A", "m", 1, 2, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n" +
				"\n" +
				"class A {\n" +
				"    static void m() throws FileNotFoundException {\n" +
				"	int i;\n" +
				"	i = 2;\n" +
				"	for(int j=0;j<i;++j) {\n" +
				"	    if(j==4)\n" +
				"		throw new FileNotFoundException(\"\");\n" +
				"	    ++i;\n" +
				"	}\n" +
				"	int j = ++i;\n" +
				"    }\n" +
				"}")),
		Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n"+
				"\n"+
				"class A {\n"+
				"  static void m() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    i = n();\n"+
				"    int j = ++i;\n"+
				"  }\n"+
				"  private static int n() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    i = 2;\n"+
				"    for(int j = 0; j < i; ++j) {\n"+
				"      if(j == 4) \n"+
				"        throw new FileNotFoundException(\"\");\n"+
				"      ++i;\n"+
				"    }\n"+
				"    return i;\n"+
				"  }\n"+
				"}")));
	}

	public void test18() {
		testSucc("A", "m", 1, 2, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n" +
				"\n" +
				"class A {\n" +
				"    void m() throws FileNotFoundException {\n" +
				"	int i;\n" +
				"	i = 2;\n" +
				"	if(i==2)\n" +
				"	    throw new FileNotFoundException(\"\");\n" +
				"	int j = ++i;\n" +
				"    }\n" +
				"}")),
		Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n"+
				"\n"+
				"class A {\n"+
				"  void m() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    i = n();\n"+
				"    int j = ++i;\n"+
				"  }\n"+
				"  private int n() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    i = 2;\n"+
				"    if(i == 2) \n"+
				"      throw new FileNotFoundException(\"\");\n"+
				"    return i;\n"+
				"  }\n"+
				"}")));
	}

	public void test19() {
		testFail("A", "m", 1, 1, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
				"class A {\n" +
				"    int m() {\n" +
				"	int i;\n" +
				"	i = 2;\n" +
				"	return ++i;\n" +
				"    }\n" +
				"    void n() {\n" +
				"	System.out.println(\"Hello, world!\");\n" +
				"    }\n" +
				"}")));
	}

	public void test20() {
		testFail("B", "m", 1, 1, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
				"class A {\n" +
				"    int n() {\n" +
				"	return 23;\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"class B extends A {\n" +
				"    int m() {\n" +
				"	int i;\n" +
				"	i = 2;\n" +
				"	return ++i;\n" +
				"    }\n" +
				"}")));
	}

	public void test21() {
		testFail("B", "m", 1, 1, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
				"class A {\n" +
				"    int n() {\n" +
				"	return 23;\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"class B extends A {\n" +
				"    int m() {\n" +
				"	int i;\n" +
				"	i = 2;\n" +
				"	return ++i;\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"class C {\n" +
				"    static void main(String[] args) {\n" +
				"	A a = new B();\n" +
				"	a.n();\n" +
				"    }\n" +
				"}")));
	}

	public void test22() {
		testSucc("A", "m", 1, 3, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n" +
				"\n" +
				"class A {\n" +
				"    int m() throws FileNotFoundException {\n" +
				"	int i;\n" +
				"	i = 2;\n" +
				"	if(i==2)\n" +
				"	    throw new FileNotFoundException(\"\");\n" +
				"	int j = ++i;\n" +
				"	return j;\n" +
				"    }\n" +
				"}")),
		Program.fromCompilationUnits(new RawCU("A.java",
				"import java.io.FileNotFoundException;\n"+
				"\n"+
				"class A {\n"+
				"  int m() throws FileNotFoundException {\n"+
				"    int i;\n"+
				"    int j;\n"+
				"    j = n();\n"+
				"    return j;\n"+
				"  }\n"+
				"  private int n() throws FileNotFoundException {\n"+
				"    int j;\n"+
				"    int i;\n"+
				"    i = 2;\n"+
				"    if(i == 2) \n"+
				"      throw new FileNotFoundException(\"\");\n"+
				"    j = ++i;\n"+
				"    return j;\n"+
				"  }\n"+
				"}")));
	}

	public void test23() {
		testFail("A", "m", 1, 3, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
				"class A {\n" +
				"    int m() {\n" +
				"	int i;\n" +
				"	int j;\n" +
				"	i = 2;\n" +
				"	j = 3;\n" +
				"	return i+j;\n" +
				"    }\n" +
				"}")));
	}

	public void test24() {
		testSucc("A", "m", 3, 4, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
				"class A {\n" +
				"    void m() {\n" +
				"	int y;\n" +
				"	int z;\n" +
				"	y = 2;\n" +
				"	int x = (0<0 ? y=0 : 1);\n" +
				"	z=y;\n" +
				"    }\n" +
				"}")),
		Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  void m() {\n"+
				"    int y;\n"+
				"    int z;\n"+
				"    y = 2;\n"+
				"    n(y);\n"+
				"  }\n"+
				"  private void n(int y) {\n"+
				"    int z;\n"+
				"    int x = (0 < 0 ? y = 0 : 1);\n"+
				"    z = y;\n"+
				"  }\n"+
				"}")));
	}

	public void test25() {
		testSucc("A", "m", 3, 4, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
				"class A {\n" +
				"    void m() {\n" +
				"	int y;\n" +
				"	int z;\n" +
				"	y = 2;\n" +
				"	int x = (0<0 ? y=0 : (y=1));\n" +
				"	z=y;\n" +
				"    }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  void m() {\n"+
				"    int y;\n"+
				"    int z;\n"+
				"    y = 2;\n"+
				"    n();\n"+
				"  }\n"+
				"  private void n() {\n"+
				"    int z;\n"+
				"    int y;\n"+
				"    int x = (0 < 0 ? y = 0 : (y = 1));\n"+
				"    z = y;\n"+
				"  }\n"+
				"}")));
	}

	public void test26() {
		testSucc("A", "m", 2, 3, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
				"class A {\n" +
				"    void m() {\n" +
				"	int y;\n" +
				"	int z;\n" +
				"	{ y = 2; }\n" +
				"	z=y;\n" +
				"    }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  void m() {\n"+
				"    int y;\n"+
				"    int z;\n"+
				"    n();\n"+
				"  }\n"+
				"  private void n() {\n"+
				"    int z;\n"+
				"    int y;\n"+
				"    {\n"+
				"      y = 2;\n"+
				"    }\n"+
				"    z = y;\n"+
				"  }\n"+
				"}")));
	}

	public void test27() {
		testSucc("A", "m", 2, 3, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
				"class A {\n" +
				"    void m() {\n" +
				"	int y;\n" +
				"	int z;\n" +
				"	try {\n" +
				"	    if(3==3)\n" +
				"		y = 1;\n" +
				"	    else\n" +
				"		throw new Exception(\"boo\");\n" +
				"	} catch(Throwable t) {\n" +
				"	    y=2;\n" +
				"	}\n" +
				"	z=y;\n" +
				"    }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  void m() {\n"+
				"    int y;\n"+
				"    int z;\n"+
				"    n();\n"+
				"  }\n"+
				"  private void n() {\n"+
				"    int z;\n"+
				"    int y;\n"+
				"    try {\n"+
				"      if(3 == 3) \n"+
				"        y = 1;\n"+
				"      else \n"+
				"        throw new Exception(\"boo\");\n"+
				"    }\n"+
				"    catch (Throwable t) {\n"+
				"      y = 2;\n"+
				"    }\n"+
				"    z = y;\n"+
				"  }\n"+
				"}")));
	}

	public void test28() {
		testSucc("A", "m", 3, 4, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
				"class A {\n" +
				"    void m() {\n" +
				"	int y;\n" +
				"	int z;\n" +
				"	y=2;\n" +
				"	try {\n" +
				"	    if(3==3)\n" +
				"		y = 1;\n" +
				"	    else\n" +
				"		throw new Exception(\"boo\");\n" +
				"	} catch(Throwable t) {\n" +
				"	}\n" +
				"	z=y;\n" +
				"    }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  void m() {\n"+
				"    int y;\n"+
				"    int z;\n"+
				"    y = 2;\n"+
				"    n(y);\n"+
				"  }\n"+
				"  private void n(int y) {\n"+
				"    int z;\n"+
				"    try {\n"+
				"      if(3 == 3) \n"+
				"        y = 1;\n"+
				"      else \n"+
				"        throw new Exception(\"boo\");\n"+
				"    }\n"+
				"    catch (Throwable t) {\n"+
				"    }\n"+
				"    z = y;\n"+
				"  }\n"+
				"}")));
	}

	public void test29() {
		testSucc("A", "m", 0, 0, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
				"class A {\n" +
				"    <T> int m() {\n" +
				"	T t;\n" +
				"	return 42;\n" +
				"    }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"   <T extends java.lang.Object> int m() {\n"+
				"    n();\n"+
				"    return 42;\n"+
				"  }\n"+
				"  private  <T extends java.lang.Object> void n() {\n"+
				"    T t;\n"+
				"  }\n"+
				"}")));
	}

	public void test31() {
		testFail("A", "A", 0, 0, "m", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
				"class A {\n" +
				"    final int x;\n" +
				"    A() {\n" +
				"    	x = 23;\n" +
				"    }\n" +
				"}")));
	}

	public void test32() {
		testSucc("A", "m", 0, 1, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
				"class A {\n" +
				"  Object m() {\n" +
				"    class B {\n" +
				"    }\n" +
				"    return new B();\n" +
				"  }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  Object m() {\n"+
				"    return n();\n"+
				"  }\n"+
				"  private Object n() {\n"+
				"      class B {\n"+
				"      }\n"+
				"    return new B();\n"+
				"  }\n"+
				"}")));
	}

	public void test33() {
		testFail("A", "m", 0, 0, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
				"class A {\n" +
				"  Object m() {\n" +
				"    class B {\n" +
				"    }\n" +
				"    return new B();\n" +
				"  }\n" +
				"}")));
	}

	public void test34() {
		testFail("A", "m", 1, 1, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
				"class A {\n" +
				"  Object m() {\n" +
				"    class B {\n" +
				"    }\n" +
				"    return new B();\n" +
				"  }\n" +
				"}")));
	}

	public void test35() {
		testSucc("A", "test", 1, 1, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
				"class A {\n" +
				"    int test(int y) {\n" +
				"	int x;\n" +
				"	if(y > 0) {\n" +
				"	    x = 1;\n" +
				"	    y = y + x;\n" +
				"	}\n" +
				"	x = y;\n" +
				"	y = y + x;\n" +
				"	return y;\n" +
				"    }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  int test(int y) {\n"+
				"    int x;\n"+
				"    y = n(y);\n"+
				"    x = y;\n"+
				"    y = y + x;\n"+
				"    return y;\n"+
				"  }\n"+
				"  private int n(int y) {\n"+
				"    int x;\n"+
				"    if(y > 0) {\n"+
				"      x = 1;\n"+
				"      y = y + x;\n"+
				"    }\n"+
				"    return y;\n"+
				"  }\n"+
				"}")));
	}

	public void test36() {
		testSucc(Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n" +
				"    void test(int x, int y) {\n" +
				"	while(x < 0) {\n" +
				"	    // from\n" +
				"	    doStuff(--x);\n" +
				"	    y++;\n" +
				"	    // to\n" +
				"	    x = y - 1;\n" +
				"	}\n" +
				"    }\n" +
				"    void doStuff(int x) { }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  void test(int x, int y) {\n"+
				"    while(x < 0){\n"+
				"      y = extracted(x, y);\n"+
				"      x = y - 1;\n"+
				"    }\n"+
				"  }\n"+
				"  protected int extracted(int x, int y) {\n"+
				"    doStuff(--x);\n"+
				"    y++;\n"+
				"    return y;\n"+
				"  }\n"+
				"  void doStuff(int x) {\n"+
				"  }\n"+
				"}")));
	}

	public void test37() {
		testFail(Program.fromCompilationUnits(new RawCU("A.java",
				"public class A {\n" +
				"    public void foo() {\n" +
				"	while (1 == 1) {\n" +
				"	    // from\n" +
				"	    if (false)\n" +
				"		break;\n" +
				"	    return;\n" +
				"	    // to\n" +
				"	}\n" +
				"	return;\n" +
				"    }\n" +
				"}")));
	}

	public void test39() {
		testSucc(Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n" +
				"    void m() {\n" +
				"	int[] a = { 23 };\n" +
				"	// from\n" +
				"	a = new int[] { 42 };\n" +
				"	// to\n" +
				"	System.out.println(a[0]);\n" +
				"    }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  void m() {\n"+
				"    int[] a = { 23 } ;\n"+
				"    a = extracted();\n"+
				"    System.out.println(a[0]);\n"+
				"  }\n"+
				"  protected int[] extracted() {\n"+
				"    int[] a;\n"+
				"    a = new int[]{ 42 } ;\n"+
				"    return a;\n"+
				"  }\n"+
				"}")));
	}

	public void test40() {
		testSucc(Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n" +
				"    void m() {\n" +
				"	int i = 23;\n" +
				"	// from\n" +
				"	i = i + 19;\n" +
				"	// to\n" +
				"    }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  void m() {\n"+
				"    int i = 23;\n"+
				"    extracted(i);\n"+
				"  }\n"+
				"  protected void extracted(int i) {\n"+
				"    i = i + 19;\n"+
				"  }\n"+
				"}")));
	}

	public void test41() {
		testSucc(Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n" +
				"    void m() {\n" +
				"	boolean b = false;\n" +
				"	int i = 23;\n" +
				"	// from\n" +
				"	if(b) i = 42;\n" +
				"	// to\n" +
				"	System.out.println(i);\n" +
				"    }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  void m() {\n"+
				"    boolean b = false;\n"+
				"    int i = 23;\n"+
				"    i = extracted(b, i);\n"+
				"    System.out.println(i);\n"+
				"  }\n"+
				"  protected int extracted(boolean b, int i) {\n"+
				"    if(b) \n"+
				"      i = 42;\n"+
				"    return i;\n"+
				"  }\n"+
				"}")));
	}

	public void test42() {
		testSucc(Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n" +
				"    void m() {\n" +
				"	boolean b = true;\n" +
				"	int i = 0;\n" +
				"	while(b) {\n" +
				"	    // from\n" +
				"	    System.out.println(i++);\n" +
				"   	    // to\n" +
				"	}\n" +
				"    }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  void m() {\n"+
				"    boolean b = true;\n"+
				"    int i = 0;\n"+
				"    while(b){\n"+
				"      i = extracted(i);\n"+
				"    }\n"+
				"  }\n"+
				"  protected int extracted(int i) {\n"+
				"    System.out.println(i++);\n"+
				"    return i;\n"+
				"  }\n"+
				"}")));
	}

	public void test43() {
		testSucc("A", "m", 1, 1, "extracted", ASTNode.VIS_PROTECTED, Program.fromCompilationUnits(new RawCU("A.java",
				"/* from the Eclipse test suite */\n" +
				"\n" +
				"class A {\n" +
				"    void m() {\n" +
				"	final int i = 42;\n" +
				"	// from\n" +
				"	Runnable run =\n" +
				"	    new Runnable() {\n" +
				"		public void run() {\n" +
				"		    System.out.println(i);\n" +
				"		}\n" +
				"	    };\n" +
				"	// to\n" +
				"	run.run();\n" +
				"    }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  void m() {\n"+
				"    final int i = 42;\n"+
				"    Runnable run;\n"+
				"    run = extracted(i);\n"+
				"    run.run();\n"+
				"  }\n"+
				"  protected Runnable extracted(final int i) {\n"+
				"    Runnable run;\n"+
				"    run = new Runnable() {\n"+
				"        public void run() {\n"+
				"          System.out.println(i);\n"+
				"        }\n"+
				"    };\n"+
				"    return run;\n"+
				"  }\n"+
				"}")));
	}

	public void test44() {
		testSucc("A", "foo", 1, 1, "extracted", ASTNode.VIS_PROTECTED, Program.fromCompilationUnits(new RawCU("A.java",
				"/* from the Eclipse test suite */\n" +
				"\n" +
				"public class A {\n" +
				"    public volatile boolean flag;\n" +
				"    \n" +
				"    protected void foo() {\n" +
				"	int i= 0;\n" +
				"	try {\n" +
				"	    if (flag)\n" +
				"		throwException();\n" +
				"	    i= 10;\n" +
				"	} catch (Exception e) {\n" +
				"	}\n" +
				"	read(i);\n" +
				"    }\n" +
				"    \n" +
				"    private void read(int i) {\n" +
				"    }\n" +
				"    \n" +
				"    private void throwException() throws Exception {\n" +
				"	throw new Exception();\n" +
				"    }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"public class A {\n"+
				"  public volatile boolean flag;\n"+
				"  protected void foo() {\n"+
				"    int i = 0;\n"+
				"    i = extracted(i);\n"+
				"    read(i);\n"+
				"  }\n"+
				"  protected int extracted(int i) {\n"+
				"    try {\n"+
				"      if(flag) \n"+
				"        throwException();\n"+
				"      i = 10;\n"+
				"    }\n"+
				"    catch (Exception e) {\n"+
				"    }\n"+
				"    return i;\n"+
				"  }\n"+
				"  private void read(int i) {\n"+
				"  }\n"+
				"  private void throwException() throws Exception {\n"+
				"    throw new Exception();\n"+
				"  }\n"+
				"}")));
	}

	public void test45() {
		testSucc("A", "m", 0, 0, "extracted", ASTNode.VIS_PROTECTED, Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n" +
				"    int m() {\n" +
				"	     do {\n" +
				"	         return 42;\n" +
				"	     } while(false);\n" +
				"    }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  int m() {\n"+
				"    return extracted();\n"+
				"  }\n"+
				"  protected int extracted() {\n"+
				"    do {\n"+
				"      return 42;\n"+
				"    }while(false);\n"+
				"  }\n"+
				"}")));
	}

	public void test46() {
		testSucc("A", "foo", 2, 2, "extracted", ASTNode.VIS_PROTECTED, Program.fromCompilationUnits(new RawCU("A.java",
				"/* from the Eclipse test suite */\n" +
				"\n" +
				"public class A {\n" +
				"    public void foo() {\n" +
				"	Object runnable= null;\n" +
				"	Object[] disposeList= null;\n" +
				"	for (int i=0; i < disposeList.length; i++) {\n" +
				"	    if (disposeList [i] == null) {\n" +
				"		disposeList [i] = runnable;\n" +
				"		return;\n" +
				"	    }\n" +
				"	}\n" +
				"    }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"public class A {\n"+
				"  public void foo() {\n"+
				"    Object runnable = null;\n"+
				"    Object[] disposeList = null;\n"+
				"    extracted(disposeList, runnable);\n"+
				"  }\n"+
				"  protected void extracted(Object[] disposeList, Object runnable) {\n"+
				"    for(int i = 0; i < disposeList.length; i++) {\n"+
				"      if(disposeList[i] == null) {\n"+
				"        disposeList[i] = runnable;\n"+
				"        return ;\n"+
				"      }\n"+
				"    }\n"+
				"  }\n"+
				"}")));
	}

	public void test48() {
		testSucc(Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n" +
				"    void m(String... args) {\n" +
				"	// from\n" +
				"	System.out.println(args[0]);\n" +
				"	// to\n" +
				"    }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  void m(String ... args) {\n"+
				"    extracted(args);\n"+
				"  }\n"+
				"  protected void extracted(String[] args) {\n"+
				"    System.out.println(args[0]);\n"+
				"  }\n"+
				"}")));
	}

	public void test53() {
		testSucc(Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n" +
				"    int f(int n) throws Exception {\n" +
				"        int i = 0;\n" +
				"        while (i < n) {\n" +
				"	    // from\n" +
				"            i++;\n" +
				"            if (i == 23) {\n" +
				"                n += 42;\n" +
				"                throw new Exception(\"\" + n);\n" +
				"            }\n" +
				"	    // to\n" +
				"        }\n" +
				"        return n;\n" +
				"    }\n" +
				"}")),
		Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  int f(int n) throws Exception {\n"+
				"    int i = 0;\n"+
				"    while(i < n){\n"+
				"      i = extracted(i, n);\n"+
				"    }\n"+
				"    return n;\n"+
				"  }\n"+
				"  protected int extracted(int i, int n) throws Exception {\n"+
				"    i++;\n"+
				"    if(i == 23) {\n"+
				"      n += 42;\n"+
				"      throw new Exception(\"\" + n);\n"+
				"    }\n"+
				"    return i;\n"+
				"  }\n"+
				"}")));
	}

	public void test54() {
		testSucc(Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n" +
				"    int m() {\n" +
				"	// from\n" +
				"	try {\n" +
				"	    return 23;\n" +
				"	} finally {\n" +
				"	    System.out.println(42);\n" +
				"	}\n" +
				"	// to\n" +
				"    }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  int m() {\n"+
				"    return extracted();\n"+
				"  }\n"+
				"  protected int extracted() {\n"+
				"    try {\n"+
				"      return 23;\n"+
				"    }\n"+
				"    finally {\n"+
				"      System.out.println(42);\n"+
				"    }\n"+
				"  }\n"+
				"}")));
	}

	public void test55() {
		testSucc(Program.fromCompilationUnits(new RawCU("A.java",
				"/* from the IntelliJ test suite */\n" +
				"\n" +
				"class A {\n" +
				"    private void bar() {\n" +
				"        String text = null;\n" +
				"        try {\n" +
				"            // from\n" +
				"	    text = getString();\n" +
				"	    // to\n" +
				"        }\n" +
				"        catch(Exception e) {\n" +
				"            System.out.println(text);\n" +
				"        }\n" +
				"    }\n" +
				"    private String getString() {\n" +
				"        return \"hello\";\n" +
				"    }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  private void bar() {\n"+
				"    String text = null;\n"+
				"    try {\n"+
				"      extracted();\n"+
				"    }\n"+
				"    catch (Exception e) {\n"+
				"      System.out.println(text);\n"+
				"    }\n"+
				"  }\n"+
				"  protected void extracted() {\n"+
				"    String text;\n"+
				"    text = getString();\n"+
				"  }\n"+
				"  private String getString() {\n"+
				"    return \"hello\";\n"+
				"  }\n"+
				"}")));
	}

	public void test56() {
		testSucc(Program.fromCompilationUnits(new RawCU("A.java",
				"/* from the IntelliJ test suite */\n" +
				"\n" +
				"class A {\n" +
				"    int f() {\n" +
				"        try {\n" +
				"            // from\n" +
				"	    int k = 0;\n" +
				"            return k;\n" +
				"	    // to\n" +
				"        } finally {\n" +
				"        }\n" +
				"    }\n" +
				"}")),
				Program.fromCompilationUnits(new RawCU("A.java",
				"class A {\n"+
				"  int f() {\n"+
				"    try {\n"+
				"      return extracted();\n"+
				"    }\n"+
				"    finally {\n"+
				"    }\n"+
				"  }\n"+
				"  protected int extracted() {\n"+
				"    int k = 0;\n"+
				"    return k;\n"+
				"  }\n"+
				"}")));
	}

	public void test58() {
		testFail(Program.fromCompilationUnits(new RawCU("A.java",
				"public class A {\n" +
				"    void m(boolean b) {\n" +
				"	int x = 42;\n" +
				"	try {\n" +
				"	    // from\n" +
				"	    if(b) {\n" +
				"		x = 23;\n" +
				"		throw new Exception();\n" +
				"	    }\n" +
				"	    // to\n" +
				"	} catch(Exception e) {\n" +
				"	    System.out.println(x);\n" +
				"	}\n" +
				"    }\n" +
				"\n" +
				"    public static void main(String[] args) {\n" +
				"        new A().m(true);\n" +
				"    }\n" +
				"}")));
	}
}