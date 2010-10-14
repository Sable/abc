package tests;

import java.util.Collection;

import AST.Program;
import AST.TypeDecl;
import junit.framework.TestCase;

public class UtilTests extends TestCase {

	public void testSupertypestransitive() {
		Program p = Program.fromClasses(
				"interface I {}",
				"interface J extends I {}",
				"class A implements I {}",
				"class B extends A implements J {}",
				"class C extends B implements J {}");
		Collection<TypeDecl> supertypesC = p.findType("C").supertypestransitive();
		assertTrue(supertypesC.contains(p.findType("java.lang.Object")));
		assertTrue(supertypesC.contains(p.findType("I")));
		assertTrue(supertypesC.contains(p.findType("J")));
		assertTrue(supertypesC.contains(p.findType("A")));
		assertTrue(supertypesC.contains(p.findType("B")));
		assertFalse(supertypesC.contains(p.findType("C")));
		assertEquals(5, supertypesC.size());
		
		Collection<TypeDecl> supertypesA = p.findType("A").supertypestransitive();
		assertTrue(supertypesA.contains(p.findType("java.lang.Object")));
		assertTrue(supertypesA.contains(p.findType("I")));
		assertFalse(supertypesA.contains(p.findType("J")));
		assertFalse(supertypesA.contains(p.findType("A")));
		assertFalse(supertypesA.contains(p.findType("B")));
		assertFalse(supertypesA.contains(p.findType("C")));
		assertEquals(2, supertypesA.size());

		Collection<TypeDecl> supertypesJ = p.findType("J").supertypestransitive();
		assertTrue(supertypesJ.contains(p.findType("java.lang.Object")));
		assertTrue(supertypesJ.contains(p.findType("I")));
		assertFalse(supertypesJ.contains(p.findType("J")));
		assertFalse(supertypesJ.contains(p.findType("A")));
		assertFalse(supertypesJ.contains(p.findType("B")));
		assertFalse(supertypesJ.contains(p.findType("C")));
		assertEquals(2, supertypesJ.size());
	}
	
	public void testChildtypestransitive() {
		Program p = Program.fromClasses(
				"interface I {}",
				"interface J extends I {}",
				"class A implements I {}",
				"class B extends A implements J {}",
				"class C extends B implements J {}",
				"class D extends B implements J {}");
		Collection<TypeDecl> subtypesC = p.findType("C").childtypestransitive();
		assertFalse(subtypesC.contains(p.findType("java.lang.Object")));
		assertFalse(subtypesC.contains(p.findType("I")));
		assertFalse(subtypesC.contains(p.findType("J")));
		assertFalse(subtypesC.contains(p.findType("A")));
		assertFalse(subtypesC.contains(p.findType("B")));
		assertFalse(subtypesC.contains(p.findType("C")));
		assertFalse(subtypesC.contains(p.findType("D")));
		assertEquals(0, subtypesC.size());
		
		Collection<TypeDecl> subtypesA = p.findType("A").childtypestransitive();
		assertFalse(subtypesA.contains(p.findType("java.lang.Object")));
		assertFalse(subtypesA.contains(p.findType("I")));
		assertFalse(subtypesA.contains(p.findType("J")));
		assertFalse(subtypesA.contains(p.findType("A")));
		assertTrue(subtypesA.contains(p.findType("B")));
		assertTrue(subtypesA.contains(p.findType("C")));
		assertTrue(subtypesA.contains(p.findType("D")));
		assertEquals(3, subtypesA.size());
		
		Collection<TypeDecl> subtypesI = p.findType("I").childtypestransitive();
		assertFalse(subtypesI.contains(p.findType("java.lang.Object")));
		assertFalse(subtypesI.contains(p.findType("I")));
		assertTrue(subtypesI.contains(p.findType("J")));
		assertTrue(subtypesI.contains(p.findType("A")));
		assertTrue(subtypesI.contains(p.findType("B")));
		assertTrue(subtypesI.contains(p.findType("C")));
		assertTrue(subtypesI.contains(p.findType("D")));
		assertEquals(5, subtypesI.size());
	}
}
