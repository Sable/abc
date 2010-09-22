/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package tests.eclipse.ExtractClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.BodyDecl;
import AST.ClassDecl;
import AST.FieldDeclaration;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class ExtractClassTests extends TestCase {

	public ExtractClassTests(String name) {
		super(name);
	}

	public void runRefactoring(String structName, boolean expectError) {
		runRefactoring(structName, "parameterObject", (Collection<String>)null, false, true, expectError);
	}
	
	public void runRefactoring(String structName, String[] fieldnames, boolean expectError) {
		runRefactoring(structName, "parameterObject", Arrays.asList(fieldnames), false, true, expectError);
	}
	
	public void runRefactoring(String structName, String fieldName, Collection<String> fieldnames, boolean encapsulate, boolean toplevel, boolean expectError) {
		runRefactoring(getName().substring(4), structName, fieldName, fieldnames, encapsulate, toplevel, expectError);
	}
	
	public void runRefactoring(String className, String structName, String fieldName, Collection<String> fieldnames, boolean encapsulate, boolean toplevel, boolean expectError) {
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/ExtractClass/"+getName()+"/in");
		Program out = expectError ? null : CompileHelper.compileAllJavaFilesUnder("tests/eclipse/ExtractClass/"+getName()+"/out");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertTrue(expectError || out != null);
		TypeDecl td = in.findSimpleType(className);
		assertTrue(td instanceof ClassDecl);
		ArrayList<FieldDeclaration> fields = new ArrayList<FieldDeclaration>();
		for(BodyDecl bd : td.getBodyDecls()) {
			if(bd instanceof FieldDeclaration) {
				FieldDeclaration fd = (FieldDeclaration)bd;
				if(fieldnames == null || fieldnames.contains(fd.name()))
					fields.add(fd);
			}
		}
		try {
			((ClassDecl)td).doExtractClass(fields, structName, fieldName, encapsulate, toplevel);
			assertEquals(expectError ? "<failure>" : out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			if(!expectError)
				assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	public void testComplexExtract() throws Exception {
		runRefactoring("ComplexExtractParameter", false);
	}

	/* disabled: the proposed refactoring is wrong
	public void testInitializerProblem() throws Exception {
		runRefactoring("InitializerProblemParameter", false);
	}*/

	public void testMethodUpdate() throws Exception {
		runRefactoring("MethodUpdateParameter", false);
	}

	public void testInheritanceUpdate() throws Exception {
		runRefactoring("InheritanceUpdateParameter", false);
	}

	public void testInheritanceUpdateGetterSetter() throws Exception {
		runRefactoring("InheritanceUpdateGetterSetterParameter", "parameterObject", (Collection<String>)null, true, true, false);
	}

	public void testComplexExtractGetterSetter() throws Exception {
		runRefactoring("ComplexExtractGetterSetterParameter", "parameterObject", (Collection<String>)null, true, true, false);
	}

	public void testComplexExtractNested() throws Exception {
		runRefactoring("ComplexExtractNestedParameter", "parameterObject", null, false, false, false);
	}

	public void testStaticInstanceFields() throws Exception {
		runRefactoring("StaticInstanceFieldsParameter", true);
	}

	public void testImportRemove() throws Exception {
		runRefactoring("ImportRemoveParameter", false);
	}

	public void testSwitchCase() throws Exception {
		runRefactoring("SwitchCaseParameter", true);
	}

	/* disabled: we can do this
	public void testCopyModifierAnnotations() throws Exception {
		runRefactoring("CopyModifierAnnotationsParameter", true);
	}*/

	public void testUFOGetter() throws Exception {
		runRefactoring("Position", "position", Arrays.asList("x", "y", "z"), true, true, false);
	}

	public void testControlBodyUpdates() throws Exception {
		runRefactoring("ControlBodyUpdatesParameter", "parameterObject", null, true, true, false);
	}

	public void testArrayInitializer() throws Exception {
		runRefactoring("ArrayInitializerParameter", "parameterObject", null, true, true, false);
	}

	public void testVariableDeclarationInitializer() throws Exception {
		runRefactoring("VariableDeclarationInitializerParameter", "parameterObject", null, true, true, false);
	}

	public void testUpdateSimpleName() throws Exception {
		runRefactoring("UpdateSimpleNameParameter", "parameterObject", null, true, true, false);
	}

	public void testArrayLengthAccess() throws Exception {
		runRefactoring("ArrayLengthAccessParameter", "parameterObject", null, true, true, false);
	}

	/* disabled: cannot insert static class into inner class
	public void testInnerDocumentedClass() throws Exception {
		runRefactoring("InnerClass", "InnerClassParameter", "parameterObject", null, true, true, false);
	}*/

	/* disabled: we can handle this
	public void testPackageReferences() throws Exception {
		runRefactoring("PackageReferencesParameter", true);
	}*/

	/* disabled: no support for renaming parameters
	public void testDuplicateParamName() throws Exception {
		runRefactoring("DuplicateParamNameParameter", false);
	}*/

	public void testLowestVisibility() throws Exception {
		runRefactoring("LowestVisibilityParameter", "parameterObject", null, true, true, false);
	}

	public void testSwitchCaseUpdates() throws Exception {
		runRefactoring("SwitchCaseUpdatesParameter", "parameterObject", null, true, true, false);
	}

	public void testFieldsWithJavadoc() throws Exception {
		runRefactoring("FieldsWithJavadocData", "data", null, true, true, false);
	}

	/* disabled: conservative dataflow
	public void testQualifiedIncrements() throws Exception {
		runRefactoring("QualifiedIncrementsParameter", "parameterObject", null, true, true, false);
	}*/

}
