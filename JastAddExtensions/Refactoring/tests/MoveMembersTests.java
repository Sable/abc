package tests;

import java.util.Collection;
import java.util.LinkedList;

import junit.framework.TestCase;
import AST.ASTNode;
import AST.FieldDeclaration;
import AST.MemberDecl;
import AST.MemberTypeDecl;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.SimpleSet;
import AST.TypeDecl;

public class MoveMembersTests extends TestCase {
	public MoveMembersTests(String name) {
		super(name);
	}
	
	private void testSucc(String sourceType, String[] members, String targetType, Program in, Program out) {
		assertNotNull(in);
		assertNotNull(out);
		TypeDecl source = in.findType(sourceType);
		assertNotNull(source);
		TypeDecl target = in.findType(targetType);
		assertNotNull(target);
		Collection<MemberDecl> mds = findMembers(members, source);
		try {
			source.doMoveMembers(mds, target);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
	}
	
	private void testFail(String sourceType, String[] members, String targetType, Program in) {
		assertNotNull(in);
		TypeDecl source = in.findType(sourceType);
		assertNotNull(source);
		TypeDecl target = in.findType(targetType);
		assertNotNull(target);
		Collection<MemberDecl> mds = findMembers(members, source);
		try {
			source.doMoveMembers(mds, target);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) {
		}
	}

	private Collection<MemberDecl> findMembers(String[] members, TypeDecl source) {
		Collection<MemberDecl> mds = new LinkedList<MemberDecl>();
		for(String member : members) {
			SimpleSet res = source.memberFields(member);
			if(res instanceof FieldDeclaration) {
				mds.add((FieldDeclaration)res);
				continue;
			}
			res = source.memberTypes(member);
			if(res instanceof TypeDecl) {
				mds.add((MemberTypeDecl)((ASTNode)res).getParent());
				continue;
			}
			res = source.localMethodsSignature(member);
			if(res instanceof MethodDecl)
				mds.add((MethodDecl)res);
			fail("no member named " + member);
		}
		return mds;
	}
	
	public void test1() {
		testSucc("A", new String[]{"f"}, "B",
				 Program.fromClasses(
				 "class A { int f; }",
				 "class B { }"),
				 Program.fromClasses(
				 "class A { }",
				 "class B { int f; }"));
	}
	
	public void test2() {
		testSucc("A", new String[]{"f"}, "B",
				 Program.fromClasses(
				 "class A { final int f = 23; }",
				 "class B { }"),
				 Program.fromClasses(
				 "class A { }",
				 "class B { final int f = 23; }"));
	}
	
	public void test3() {
		testFail("A", new String[]{"f"}, "B",
				 Program.fromClasses(
				 "class A { final int f; { f = 23; } }",
				 "class B { }"));
	}
	
	public void test4() {
		testSucc("A", new String[]{"f"}, "B",
				 Program.fromClasses(
				 "class A { class C { } C f; }",
				 "class B { }"),
				 Program.fromClasses(
				 "class A { class C { } }",
				 "class B { A.C f; }"));
	}
	
	public void test5() {
		testFail("A", new String[]{"f"}, "B",
				 Program.fromClasses(
				 "class A extends B { final int f; { f = 23; } }",
				 "class B { }"));
	}
}