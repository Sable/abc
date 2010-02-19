package tests;

import junit.framework.TestCase;
import AST.Program;
import AST.RawCU;
import AST.RefactoringException;

public class RenamePackageTests extends TestCase {
	public RenamePackageTests(String name) {
		super(name);
	}
	
	public void testSucc(String old_name, String new_name, Program in, Program out) {
		assertNotNull(in);
		assertNotNull(out);
		try {
			in.getPackageDecl(old_name).rename(new_name);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException e) {
			assertEquals(out.toString(), e.toString());
		}
	}
	
	public void testFail(String old_name, String new_name, Program in) {
		assertNotNull(in);
		try {
			in.getPackageDecl(old_name).rename(new_name);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException e) {
		}
	}

	public void test1() {
		testSucc("p", "q",
			Program.fromCompilationUnits(new RawCU("A.java", "package p; class A { }")),
			Program.fromCompilationUnits(new RawCU("A.java", "package q; class A { }")));
	}

	public void test2() {
		testSucc("p", "q",
			Program.fromCompilationUnits(new RawCU("A.java", "package p.r; class A { }")),
			Program.fromCompilationUnits(new RawCU("A.java", "package q.r; class A { }")));
	}
	
	public void test3() {		
		testSucc("p", "q",
				Program.fromCompilationUnits(new RawCU("A.java", "package p; public class A { }"),
											 new RawCU("B.java", "package r; class B extends p.A { }")),
				Program.fromCompilationUnits(new RawCU("A.java", "package q; public class A { }"),
											 new RawCU("B.java", "package r; class B extends q.A { }")));
	}
	
	public void test4() {		
		testSucc("p", "q",
				Program.fromCompilationUnits(new RawCU("A.java", "package p; public class A { }"),
											 new RawCU("B.java", "package r; import p.A; class B extends A { }")),
				Program.fromCompilationUnits(new RawCU("A.java", "package q; public class A { }"),
											 new RawCU("B.java", "package r; import q.A; class B extends A { }")));
	}
	
	public void test5() {
		testSucc("p", "q.r",
				Program.fromCompilationUnits(new RawCU("A.java", "package p; class A { }")),
				Program.fromCompilationUnits(new RawCU("A.java", "package q.r; class A { }")));		
	}
	
	public void test6() {
		testFail("p", "q", Program.fromCompilationUnits(new RawCU("A.java", "package p; class A { }"),
				                                        new RawCU("B.java", "package q; class B { }")));
	}
	
	public void test7() {
		testFail("p", "q", Program.fromCompilationUnits(new RawCU("A.java", "package p; class A { }"),
				                                        new RawCU("B.java", "package q.r; class B { }")));
	}
	
	public void test8() {
		testSucc("p", "q.r",
				Program.fromCompilationUnits(new RawCU("A.java", "package p; class A { }"),
						                     new RawCU("B.java", "package q; class B { }")),
				Program.fromCompilationUnits(new RawCU("A.java", "package q.r; class A { }"),
						                     new RawCU("B.java", "package q; class B { }")));		
	}

	public void test9() {
		testFail("p", "q.r", Program.fromCompilationUnits(new RawCU("A.java", "package p; class A { }"),
				                                          new RawCU("B.java", "package q; class r { }")));
	}
	
	public void test10() {
		testFail("p", "q", Program.fromCompilationUnits(new RawCU("A.java", "package p; public class A { }"),
                                                        new RawCU("B.java", "package r; class q { } class B extends p.A { }")));
	}
}