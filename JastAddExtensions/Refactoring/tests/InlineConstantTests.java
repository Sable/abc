package tests;

import junit.framework.TestCase;
import tests.AllTests;
import AST.FieldDeclaration;
import AST.Program;
import AST.RawCU;
import AST.RefactoringException;

public class InlineConstantTests extends TestCase {
	public InlineConstantTests(String name) {
		super(name);
	}
	
	public void testSucc(Program in, Program out, boolean remove) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		FieldDeclaration fd = in.findField("C");
		assertNotNull(fd);
		try {
			fd.doInlineConstant(remove);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.toString());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}
	
	public void testSucc(Program in, Program out) {
		testSucc(in, out, false);
	}

	public void testFail(Program in, boolean remove) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		FieldDeclaration fd = in.findField("C");
		assertNotNull(fd);
		try {
			fd.doInlineConstant(remove);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) { }
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}
	
	public void testFail(Program in) {
		testFail(in, false);
	}

    public void test1() {
        testSucc(
        	Program.fromBodyDecls(
        	"static final float C = 2.3f;",
        	"float m() {" +
        	"  return C + C;" +
        	"}"),
        	Program.fromBodyDecls(
        	"static final float C = 2.3f;",
        	"float m() {" +
        	"  return 2.3f + 2.3f;" +
        	"}"));
    }

    public void test2() {
        testFail(
        	Program.fromBodyDecls(
        	"static float C = 2.3f;",
        	"float m() {" +
        	"  return C + C;" +
        	"}"));
    }

    public void test3() {
        testFail(
        	Program.fromClasses(
        	"class A {" +
        	"  static final A C = new A();" +
        	"  static boolean f() {" +
        	"    A x = C;" +
        	"    return x == a;" +
        	"  }" +
        	"  static A a = C;" +
        	"}"));
    }

    public void test4() {
    	testSucc(
    		Program.fromCompilationUnits(
    		new RawCU("A.java",
    		"package p;" +
    		"" +
    		"public class A {" +
    		"  public static final int C = 23;" +
    		"}"),
    		new RawCU("B.java",
    		"package q;" +
    		"" +
    		"import static p.A.C;" +
    		"" +
    		"public class B {" +
    		"  int j = C;" +
    		"}")),
    		Program.fromCompilationUnits(
    	    new RawCU("A.java",
    	    "package p;" +
    	    "" +
    	    "public class A {" +
    	    "  public static final int C = 23;" +
    	    "}"),
    	    new RawCU("B.java",
    	    "package q;" +
    	    "" +
    	    "import static p.A.C;" +
    	    "" +
    	    "public class B {" +
    	    "  int j = 23;" +
    	    "}")), true);   		
    }
}