package tests;

import junit.framework.TestCase;
import tests.AllTests;
import AST.Expr;
import AST.Program;
import AST.RefactoringException;

public class ExtractTempTests extends TestCase {
	public ExtractTempTests(String name) {
		super(name);
	}
	
	public void testSucc(Program in, Program out) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		Expr e = in.findDoublyParenthesised();
		assertNotNull(e);
		e.unparenthesise();
		try {
			e.doExtract("x");
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			fail("Refactoring was supposed to succeed; failed with "+rfe);
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	public void testFail(Program in) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		Expr e = in.findDoublyParenthesised();
		assertNotNull(e);
		e.unparenthesise();
		try {
			e.doExtract("x");
			fail("Refactoring was supposed to fail; succeeded with "+in);
		} catch(RefactoringException rfe) { }
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

    public void test1() {
        testSucc(Program.fromStmts("System.out.println(((42)));"),
        		 Program.fromStmts("int x = 42;", "System.out.println(x);"));
    }

    public void test2() {
        testFail(Program.fromStmts("int x;", "System.out.println(((42)));"));
    }

    public void test3() {
        testFail(
        	Program.fromBodyDecls(
        	"void m() {" +
        	"  System.out.println(n()+\" \"+((n())));" +
        	"}",
        	"int x = 42;",
        	"int n() {" +
        	"  return ++x;" +
        	"}"));
    }

    public void test4() {
    	testFail(Program.fromBodyDecls(
    		"int y = 42;",
    		"void m() { System.out.println(y++ + \" \" + ((y))); }"));
    }

    public void test5() {
    	testFail(Program.fromStmts(
    		"int y = 42;",
    		"System.out.println(y++ + \" \" + ((y)));"));
    }

    public void test6() {
    	testFail(Program.fromStmts(
    		"int y = 23;",
    		"System.out.println((y=42) + \" \" + ((y=56)));",
    		"System.out.println(y);"));
    }
    
//    public void test7() {
//    	testFail(Program.fromBodyDecls(
//    		"int f;",
//    		"volatile boolean ready;",
//    		"void m() {" +
//    		"  p(f++, ((ready=true)));" +
//    		"}",
//    		"private void p(int i, boolean b) { }"));
//    }
    
    public void test8() {
    	testSucc(Program.fromStmts("int f = 10;", 
    					"int h = 13;",
    					"System.out.println(\" \" + f + ((h=12)));"),
       		 	Program.fromStmts("int f = 10;",
       		 			"int h = 13;",
       		 			"int x = h = 12;", 
       		 			"System.out.println(\" \" + f + x);"));
    }
    
    public void test9() {
    	testSucc(Program.fromStmts("int f = 10;", 
				"int h = 13;",
				"System.out.println(\" \" + f++ + ((h=12)));"),
		 	Program.fromStmts("int f = 10;",
		 			"int h = 13;",
		 			"int x = h = 12;", 
		 			"System.out.println(\" \" + f++ + x);"));
    }
    
    public void test10() {
    	// local write OVER same local read
    	testFail(Program.fromStmts("int f = 10;",
    					"int g = 12;",
    					"System.out.println(\" \" + g + ((g = 14)));"));
    }
    
    public void test10b() {
    	// local read OVER same local write
    	testFail(Program.fromStmts("int f = 10;",
    					"int g = 12;",
    					"System.out.println(\" \" + (g = 14) + ((g)));"));
    }
    
    public void test11() {
    	// local write OVER possible side effects
    	testSucc(Program.fromStmts("int g = 12;",
    					"System.out.println(\" \" + String.valueOf(47) + ((g = 14)));"),
    				Program.fromStmts("int g = 12;",
    					"int x = g = 14;",
    					"System.out.println(\" \" + String.valueOf(47) + x);"));
    }
    
    public void test11a() {
    	// possible side effects OVER local write
    	testFail(Program.fromStmts("int g = 12;",
    					"System.out.println(\" \" + (g = 14) + ((String.valueOf(47))));")
//    					,
//    				Program.fromStmts("int g = 12;",
//    					"String x = String.valueOf(47);",
//    					"System.out.println(\" \" + (g = 14) + x);")
    					);
    }
    
    public void test11b() {
    	// field write OVER possible side effects
    	testFail(Program.fromBodyDecls("int g = 12;",
    					"void m() { System.out.println(\" \" + String.valueOf(47) + ((g))); }"));
    }
    
    public void test11bw() {
    	// field write OVER possible side effects
    	testFail(Program.fromBodyDecls("int g = 12;",
    					"void m() { System.out.println(\" \" + String.valueOf(47) + ((g = 14))); }"));
    }
    
    public void test11c() {
    	// possible side effects OVER field
    	testFail(Program.fromBodyDecls("int g = 12;",
    					"void m() { System.out.println(\" \" + g + ((String.valueOf(47)))); }"));
    }
    
    public void test11cw() {
    	// possible side effects OVER field write
    	testFail(Program.fromBodyDecls("int g = 12;",
    					"void m() { System.out.println(\" \" + (g = 14) + ((String.valueOf(47)))); }"));
    }
    
    public void test11d() {
    	// possible side effects OVER possible side effects
    	testFail(Program.fromStmts("int g = 12;",
    					"System.out.println(\" \" + String.valueOf(47) + ((String.valueOf(42))));"));
    }
    
    public void test12() {
    	// field write OVER possible side effects and field write
    	testFail(Program.fromBodyDecls(
    					"int g = 7;",
    					"interface I { int m(); }",
    					"class A { public int p = 10; }",
    					"void m() { final A f = new A(); " +
    						"I i = new I() { public int m() { f.p = 11; return 6; } };" +
    						"System.out.println(\" \" + i.m() + f.p + ((g = 12))); }"));
    }
    
    public void test13() {
    	// field write OVER possible side effects
    	testFail(Program.fromBodyDecls(
						"int g = 12;",
    					"class A { public int m() { return 3; } }",
    					"A a = new A();",
    					"void m() { System.out.println(\" \" + a.m() + ((g = 12))); }"));
    }
}
