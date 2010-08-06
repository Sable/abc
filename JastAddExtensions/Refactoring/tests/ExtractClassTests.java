package tests;

import java.util.ArrayList;

import junit.framework.TestCase;
import AST.ClassDecl;
import AST.FieldDeclaration;
import AST.Program;
import AST.RawCU;
import AST.RefactoringException;
import AST.TypeDecl;

public class ExtractClassTests extends TestCase {
	public ExtractClassTests(String name) {
		super(name);
	}
	
	public void testSucc(String newClassName, String newFieldName, String[] fns, Program in, Program out, boolean encapsulate) {
		assertNotNull(in);
		String originalProgram = in.toString();
		Program.startRecordingASTChanges();
		assertNotNull(out);
		TypeDecl td = in.findType("p", "A");
		assertTrue(td instanceof ClassDecl);
		ArrayList<FieldDeclaration> fds = new ArrayList<FieldDeclaration>();
		for(String fn : fns) {
			FieldDeclaration fd = td.findField(fn);
			assertNotNull(fd);
			fds.add(fd);
		}
		try {
			((ClassDecl)td).doExtractClass(fds, newClassName, newFieldName, encapsulate, false);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
		in.undoAll();
		assertEquals(originalProgram, in.toString());
	}
	
	public void testFail(String newClassName, String newFieldName, String[] fns, Program in, boolean encapsulate) {
		assertNotNull(in);
		String originalProgram = in.toString();
		Program.startRecordingASTChanges();
		TypeDecl td = in.findType("p", "A");
		assertTrue(td instanceof ClassDecl);
		ArrayList<FieldDeclaration> fds = new ArrayList<FieldDeclaration>();
		for(String fn : fns) {
			FieldDeclaration fd = td.findField(fn);
			assertNotNull(fd);
			fds.add(fd);
		}
		try {
			((ClassDecl)td).doExtractClass(fds, newClassName, newFieldName, encapsulate, false);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) {
		}
		in.undoAll();
		assertEquals(originalProgram, in.toString());
		
	}
	
    public void test1() {
        testSucc("Data", "data", new String[] { "x", "y" },
            Program.fromCompilationUnits(new RawCU("A.java",
            "package p;" +
            "" +
            "class B {" +
            "	int data = 0;" +
            "}" +
            "" +
            "public class A extends B {" +
            "	" +
            "	int x = init();" +
            "	Data y = new Data();" +
            "	" +
            "	public void f() {" +
            "		int data;" +
            "		x = 0;" +
            "		y = new Data();" +
            "		this.x = 2;" +
            "		this.y.z = 3;" +
            "	}" +
            "" +
            "	public int init() {" +
            "		return 4 + data;" +
            "	}" +
            "	" +
            "	public void g() {" +
            "		Data data;" +
            "	}" +
            "	" +
            "}" +
            "" +
            "class Data {" +
            "	int z;" +
            "}" +
            "")),
            Program.fromCompilationUnits(new RawCU("A.java",
            "package p;" +
            "" +
            "class B {" +
            "  int data = 0;" +
            "}" +
            "" +
            "public class A extends B {" +
            "  static class Data {" +
            "    private int x;" +
            "    private p.Data y;" +
            "    Data(int x, p.Data y) {" +
            "      super();" +
            "      this.setX(x);" +
            "      this.setY(y);" +
            "    }" +
            "    Data() {" +
            "      super();" +
            "    }" +
            "    int getX() {" +
            "      return x;" +
            "    }" +
            "    int setX(int x) {" +
            "      return this.x = x;" +
            "    }" +
            "    p.Data getY() {" +
            "      return y;" +
            "    }" +
            "    p.Data setY(p.Data y) {" +
            "      return this.y = y;" +
            "    }" +
            "  }" +
            "  Data data = new Data();" +
            "  {" +
            "    data.setX(init());" +
            "    data.setY(new p.Data());" +
            "  }" +
            "  public void f() {" +
            "    int data;" +
            "    this.data.setX(0);" +
            "    this.data.setY(new p.Data());" +
            "    this.data.setX(2);" +
            "    this.data.getY().z = 3;" +
            "  }" +
            "  public int init() {" +
            "    return 4 + super.data;" +
            "  }" +
            "  public void g() {" +
            "    p.Data data;" +
            "  }" +
            "}" +
            "" +
            "class Data {" +
            "  int z;" +
            "}")), true);
    }

    public void test2() {
        testSucc("Data", "d", new String[] { "x" },
            Program.fromCompilationUnits(new RawCU("A.java",
            "package p;" +
            "" +
            "class B {" +
            "	int d = 0;" +
            "}" +
            "" +
            "public class A extends B {" +
            "	" +
            "	int x = init();" +
            "	" +
            "	public int init() {" +
            "		return 4 + d;" +
            "	}" +
            "	" +
            "}" +
            "")),
            Program.fromCompilationUnits(new RawCU("A.java",
            "package p;" +
            "" +
            "class B {" +
            "  int d = 0;" +
            "}" +
            "" +
            "public class A extends B {" +
            "  static class Data {" +
            "    private int x;" +
            "    Data(int x) {" +
            "      super();" +
            "      this.setX(x);" +
            "    }" +
            "    Data() {" +
            "      super();" +
            "    }" +
            "    int getX() {" +
            "      return x;" +
            "    }" +
            "    int setX(int x) {" +
            "      return this.x = x;" +
            "    }" +
            "  }" +
            "  Data d = new Data(init());" +
            "  public int init() {" +
            "    return 4 + super.d;" +
            "  }" +
            "  " +
            "}")), true);
    }
    
    public void test3() {
        testSucc("Data", "data", new String[] { "x" },
            Program.fromCompilationUnits(new RawCU("A.java",
            "package p;" +
            "" +
            "public class A {" +
            "	Data x;" +
            "}" +
            "" +
            "class Data {" +
            "" +
            "}")),
            Program.fromCompilationUnits(new RawCU("A.java",
            "package p;" +
            "" +
            "public class A {" +
            "  " +
            "  static class Data {" +
            "    private p.Data x;" +
            "    Data(p.Data x) {" +
            "      super();" +
            "      this.setX(x);" +
            "    }" +
            "    Data() {" +
            "      super();" +
            "    }" +
            "    p.Data getX() {" +
            "      return x;" +
            "    }" +
            "    p.Data setX(p.Data x) {" +
            "      return this.x = x;" +
            "    }" +
            "  }" +
            "  Data data = new Data();" +
            "}" +
            "" +
            "class Data {" +
            "}")), true);
    }

    public void test4() {
        testSucc("Data", "java", new String[] { "x", "y" },
            Program.fromCompilationUnits(new RawCU("A.java",
            "package p;" +
            "" +
            "class A {" +
            "    int x;" +
            "    int y;" +
            "    { java.lang.System.out.println(); }" +
            "}" +
            "")),
            Program.fromCompilationUnits(new RawCU("A.java",
            "package p;" +
            "" +
            "class A {" +
            "  static class Data {" +
            "    private int x;" +
            "    private int y;" +
            "    Data(int x, int y) {" +
            "      super();" +
            "      this.setX(x);" +
            "      this.setY(y);" +
            "    }" +
            "    Data() {" +
            "      super();" +
            "    }" +
            "    int getX() {" +
            "      return x;" +
            "    }" +
            "    int setX(int x) {" +
            "      return this.x = x;" +
            "    }" +
            "    int getY() {" +
            "      return y;" +
            "    }" +
            "    int setY(int y) {" +
            "      return this.y = y;" +
            "    }" +
            "  }" +
            "  Data java = new Data();" +
            "  {" +
            "    System.out.println();" +
            "  }" +
            "}")), true);
    }
    
    /*public void test5() {
    	testFail("Data", "data", new String[]{"x", "y"},
    		Program.fromCompilationUnits(new RawCU("A.java",
    		"package p;" +
    		"class Super {" +
    		"  int f() { return 23; }" +
    		"}" +
    		"" +
    		"class A extends Super {" +
    		"  int x = f();" +
    		"  int y = x + 19;" +
    		"}")), true);
    }*/
    

    
    public void test6() {
    	testSucc("Data", "data", new String[]{"x", "y"},
    		Program.fromCompilationUnits(new RawCU("A.java",
    		"package p;" +
    		"class Super {" +
    		"  int f() { return 23; }" +
    		"}" +
    		"" +
    		"class A extends Super {" +
    		"  int x = 12;" +
    		"  int y = x + 19;" +
    		"}")),
    		Program.fromCompilationUnits(new RawCU("A.java",
    				"package p;" +
    	            "" +
    	            "class A extends Super  {" +
    	            "  " +
    	            "  static class Data  {" +
    	            "    private int x;" +
    	            "    private int y;" +
    	            "    Data(int x, int y) {" +
    	            "      super();" +
    	            "      this.setX(x);" +
    	            "      this.setY(y);" +
    	            "    }" +
    	            "    Data() {" +
    	            "      super();" +
    	            "    }" +
    	            "    int getX() {" +
    	            "      return x;" +
    	            "    }" +
    	            "    int setX(int x) {" +
    	            "      return this.x = x;" +
    	            "    }" +
    	            "    int getY() {" +
    	            "      return y;" +
    	            "    }" +
    	            "    int setY(int y) {" +
    	            "      return this.y = y;" +
    	            "    }" +
    	            "  }" +
    	            "  Data data = new Data();" +
    	            "  {" +
    	            "    data.setX(12);" +
    	            "    data.setY(data.getX() + 19);" +
    	            "  }" +
    	            "}" +
    	            "" +
    	            "class Super  {" +
    	            "  int f() {" +
    	            "    return 23;" +
    	            "  }" +
    	            "}")), true);
    }
    
    public void test7() {
    	// initializer moving test
        testSucc("Data", "data", new String[] { "x", "y" },
            Program.fromCompilationUnits(new RawCU("A.java",
            "package p;" +
            "" +
            "class A {" +
            "    int x = 10;" +
            "    int z = 20;" +
            "    int y = 30;" +
            "    { java.lang.System.out.println(); }" +
            "}" +
            "")),
            Program.fromCompilationUnits(new RawCU("A.java",
            "package p;" +
            "" +
            "class A {" +
            "  static class Data {" +
            "    private int x;" +
            "    private int y;" +
            "    Data(int x, int y) {" +
            "      super();" +
            "      this.setX(x);" +
            "      this.setY(y);" +
            "    }" +
            "    Data() {" +
            "      super();" +
            "    }" +
            "    int getX() {" +
            "      return x;" +
            "    }" +
            "    int setX(int x) {" +
            "      return this.x = x;" +
            "    }" +
            "    int getY() {" +
            "      return y;" +
            "    }" +
            "    int setY(int y) {" +
            "      return this.y = y;" +
            "    }" +
            "  }" +
            "  Data data = new Data(10, 30);" +
            "    int z = 20;" +
            "  {" +
            "    java.lang.System.out.println();" +
            "  }" +
            "}")), true);
    }
    
    public void test8() {
    	// initializer moving test
        testSucc("Data", "data", new String[] { "x", "y" },
            Program.fromCompilationUnits(new RawCU("A.java",
            "package p;" +
            "" +
            "class A {" +
            "    int x = 10;" +
            "    int z = 20;" +
            "    int y = z;" +
            "    { java.lang.System.out.println(); }" +
            "}" +
            "")),
            Program.fromCompilationUnits(new RawCU("A.java",
            "package p;" +
            "" +
            "class A {" +
            "  static class Data {" +
            "    int x;" +
            "    int y;" +
            "  }" +
            "  Data data = new Data();" +
            "  {" +
            "    data.x = 10;" +
            "  }" +
            "    int z = 20;" +
            "  {" +
            "    data.y = z;" +
            "  }" +
            "  {" +
            "    java.lang.System.out.println();" +
            "  }" +
            "}")), false);
    }
    
    public void test9() {
	    testSucc("Data", "data", new String[] { "x", "w", "ww", "y" },
	            Program.fromCompilationUnits(new RawCU("A.java",
	            "package p;" +
	            "" +
	            "class A {" +
	            "    int x = 10;" +
	            "    int z = 20;" +
	            "    int w;" +
	            "    java.util.List<Object> ww;" +
	            "    double y;" +
	            "    { java.lang.System.out.println(); }" +
	            "}" +
	            "")),
	            Program.fromCompilationUnits(new RawCU("A.java",
	            "package p;" +
	            "" +
	            "class A {" +
	            "  static class Data {" +
	            "    private int x;" +
	            "    private int w;" +
	            "    private java.util.List<Object> ww;" +
	            "    private double y;" +
	            "    Data(int x, int w, java.util.List<Object> ww, double y) {" +
	            "      super();" +
	            "      this.setX(x);" +
	            "      this.setW(w);" +
	            "      this.setWw(ww);" +
	            "      this.setY(y);" +
	            "    }" +
	            "    Data() {" +
	            "      super();" +
	            "    }" +
	            "    int getX() {" +
	            "      return x;" +
	            "    }" +
	            "    int setX(int x) {" +
	            "      return this.x = x;" +
	            "    }" +
	            "    int getW() {" +
	            "      return w;" +
	            "    }" +
	            "    int setW(int w) {" +
	            "      return this.w = w;" +
	            "    }" +
	            "    java.util.List<Object> getWw() {" +
	            "      return ww;" +
	            "    }" +
	            "    java.util.List<Object> setWw(java.util.List<Object> ww) {" +
	            "      return this.ww = ww;" +
	            "    }" +
	            "    double getY() {" +
	            "      return y;" +
	            "    }" +
	            "    double setY(double y) {" +
	            "      return this.y = y;" +
	            "    }" +
	            "  }" +
	            "  Data data = new Data(10, 0, null, 0);" +
	            "    int z = 20;" +
	            "  {" +
	            "    java.lang.System.out.println();" +
	            "  }" +
	            "}")), true);
	    }
	
	
}