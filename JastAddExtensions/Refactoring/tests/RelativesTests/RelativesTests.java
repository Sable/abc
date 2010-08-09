package tests.RelativesTests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;

import tests.CompileHelper;

import AST.MethodDecl;
import AST.Program;
import AST.SimpleSet;
import AST.TypeDecl;
import AST.ClassDecl;
import AST.InterfaceDecl;
import junit.framework.TestCase;
import tests.AllTests;



public class RelativesTests extends TestCase {
	
	/** relatives in each in form pkg,type(or type.inner),signature
	 * @throws Exception 
	 */
	public void relativesCheck(String[][] relatives) throws Exception {
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/RelativesTests/" + getName());
		if (in == null)
			throw new Exception("Compilation error");
		Collection<MethodDecl> rels = new LinkedHashSet<MethodDecl>();
		for(int i = 0; i < relatives.length; i++) {
			TypeDecl td = in.findType(relatives[i][0], relatives[i][1]);
			assertNotNull(td);
			SimpleSet s = td.localMethodsSignature(relatives[i][2]);
			assertTrue(s.isSingleton());
			assertFalse(rels.contains(s.iterator().next())); // possible duplicates in input
			rels.add((MethodDecl) s.iterator().next());
		}
		
		for (MethodDecl md : rels) {
			Collection<MethodDecl> res = md.relatives();
			if (!res.containsAll(rels) || !rels.containsAll(res))
				fail(errorPrint(md, rels, res));
		}
	}
	
	String typeDeclIdent(TypeDecl td) {
		if (td.isInnerType())
			return typeDeclIdent((TypeDecl)td.getParent().getParent()) + "." + td.name();
		else
			return td.compilationUnit().packageName() + "." + td.name();
	}
	
	String methodIdent(MethodDecl md) {
		return typeDeclIdent(md.hostType()) + "." + md.signature();
	}
	
	String errorPrint(MethodDecl md, Collection<MethodDecl> expected, Collection<MethodDecl> was) {
		StringBuffer s = new StringBuffer();
		s.append("\nExpected relatives for ");
		s.append(methodIdent(md));
		s.append(" are:\n");
		java.util.List<String> expected_ = new ArrayList<String>();
		for (MethodDecl m : expected)
			expected_.add(methodIdent(m));
		Collections.sort(expected_);
		for (String m : expected_) {
			s.append(m);
			s.append("\n");
		}
		s.append("but were:\n");
		java.util.List<String> was_ = new ArrayList<String>();
		for (MethodDecl m : was)
			was_.add(methodIdent(m));
		Collections.sort(was_);
		for (String m : was_) {
			s.append(m);
			s.append("\n");
		}
		return s.toString();
	}
	
	// simple hierarchy
	public void test1() throws Exception {
		String[][] relatives = {
				{"p", "A", "m()"},
				{"p", "B", "m()"},
				{"p", "C", "m()"}
		};
		relativesCheck(relatives);
	}
	
	// simple interfaces
	public void test2() throws Exception {
		String[][] relatives = {
				{"p", "I", "m()"},
				{"p", "A", "m()"},
				{"p", "B", "m()"},
				{"p", "C", "m()"}
		};
		relativesCheck(relatives);
	}
	
	// abstract classes
	public void test3() throws Exception {
		String[][] relatives = {
				{"p", "B", "m()"},
				{"p", "D", "m()"}
		};
		relativesCheck(relatives);
		

		String[][] relativess = {
				{"p", "A", "n()"},
				{"p", "B", "n()"},
				{"p", "C", "n()"},
				{"p", "E", "n()"}
		};
		relativesCheck(relativess);
	}
	
	// interface chains
	public void test4() throws Exception {
		String[][] relatives = {
				{"p", "AI", "m()"},
				{"p", "AIII", "m()"},
				{"p", "A", "m()"},
				{"p", "B", "m()"}
		};
		relativesCheck(relatives);
		

		String[][] relativess = {
				{"p", "BI", "n()"},
				{"p", "B", "n()"}
		};
		relativesCheck(relativess);
	}
	
	// interfaces + abstract
	public void test5() throws Exception {
		String[][] relatives = {
				{"p", "AI", "m()"},
				{"p", "B", "m()"},
				{"p", "BI", "m()"}
		};
		relativesCheck(relatives);
	}
	
	// related through parent class
	public void test6a() throws Exception {
		String[][] relatives = {
				{"p", "A", "m()"},
				{"p", "C", "m()"},
				{"p", "D", "m()"}
		};
		relativesCheck(relatives);
	}
	
	// related through parent interface
	public void test6b() throws Exception {
		String[][] relatives = {
				{"p", "C", "m()"},
				{"p", "I", "m()"},
				{"p", "D", "m()"}
		};
		relativesCheck(relatives);
	}
	
	// related through interface in a child
	public void test7() throws Exception {
		String[][] relatives = {
				{"p", "B", "m()"},
				{"p", "A", "m()"},
				{"p", "C", "m()"},
				{"p", "I", "m()"},
				{"p", "Link", "m()"},
				{"p", "D", "m()"}
		};
		relativesCheck(relatives);
	}
	
	// related through interface in a child -- distant link
	public void test7b() throws Exception {
		String[][] relatives = {
				{"p", "Link", "m()"},
				{"p", "A1", "m()"},
				{"p", "A2", "m()"}
		};
		relativesCheck(relatives);
	}
	
	// complicated relation chain
	public void test8() throws Exception {
		String[][] relatives = {
				{"p", "A1", "m()"},
				{"p", "C1", "m()"},
				{"p", "A2", "m()"},
				{"p", "A3", "m()"},
				{"p", "B3", "m()"},
				{"p", "C3", "m()"},
				{"p", "Link12", "m()"},
				{"p", "Link23", "m()"},
				{"p", "I3", "m()"}
		};
		relativesCheck(relatives);
	}
	
	// generics subclass/signature matching
	public void test9() throws Exception {
		String[][] relatives = {
				{"p", "A", "m(java.lang.Object)"},
				{"p", "B", "m(java.lang.Object)"}
		};
		relativesCheck(relatives);
	}
	
	// package permissions check
	public void test10() throws Exception {
		String[][] relatives = {
				{"q", "B", "m()"}
		};
		relativesCheck(relatives);
		
		String[][] relativess = {
				{"p", "A", "m()"}
		};
		relativesCheck(relativess);
	}
	
	
	// generics signature change
	public void test11() throws Exception {
		String[][] relatives = {
				{"p", "A", "m(java.lang.Object)"},
				{"p", "B", "m(java.lang.String)"},
				{"p", "C", "m(java.lang.Object)"}
		};
		relativesCheck(relatives);
		
		String[][] relativess = {
				{"p", "I", "n(java.lang.Object)"},
				{"p", "B", "n(java.lang.Number)"},
				{"p", "C", "n(java.lang.Object)"}
		};
		relativesCheck(relativess);
	}
	
}