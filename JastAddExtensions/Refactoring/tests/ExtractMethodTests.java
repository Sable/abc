package tests;

import junit.framework.TestCase;
import AST.MethodDecl;
import AST.Program;
import AST.RawCU;
import AST.RefactoringException;
import AST.TypeDecl;

public class ExtractMethodTests extends TestCase {
	public ExtractMethodTests(String name) {
		super(name);
	}
	
	public void testSucc(String className, String methodName, int begin, int end, String newMethodName, Program in, Program out) {
        assertNotNull(in);
        assertNotNull(out);
        TypeDecl A = in.findType(className);
        MethodDecl m = A.findMethod(methodName);
        assertNotNull(m);
        try {
			m.getBlock().doExtractMethod("private", newMethodName, begin, end);
			assertEquals(out.toString(), in.toString());
		} catch (RefactoringException e) {
			assertEquals(out.toString(), "<failure>");
		}
	}
	
	public void testFail(String className, String methodName, int begin, int end, String newMethodName, Program in) {
        assertNotNull(in);
        TypeDecl A = in.findType(className);
        MethodDecl m = A.findMethod(methodName);
        assertNotNull(m);
        try {
			m.getBlock().doExtractMethod("private", newMethodName, begin, end);
			assertEquals("<failure>", in.toString());
		} catch (RefactoringException e) {
		}
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

	public void test15() {
		testSucc("B", "m", 0, 0, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
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
				"}")),
		Program.fromCompilationUnits(new RawCU("A.java",
				"class B {\n"+
				"  void m() {\n"+
				"    n();\n"+
				"  }\n"+
				"  private void n() {\n"+
				"    System.out.println(1);\n"+
				"  }\n"+
				"}\n"+
				"\n"+
				"class C extends B {\n"+
				"  void n() {\n"+
				"    System.out.println(2);\n"+
				"  }\n"+
				"}\n"+
				"\n"+
				"public class Tst2 {\n"+
				"  public static void main(String[] args) {\n"+
				"    B b = new C();\n"+
				"    b.m();\n"+
				"  }\n"+
				"}")));
	}

	public void test16() {
		testFail("B", "m", 0, 0, "n", Program.fromCompilationUnits(new RawCU("A.java",
				"\n" +
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
				"}\n" +
		"")));
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
				"}\n" +
		"")));
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
				"}\n" +
		"")));
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
				"}\n" +
		"")));
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
				"}\n" +
		"")));
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
				"}\n" +
		"")));
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
				"}\n" +
		"")));
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
				"}\n" +
		"")));
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
}