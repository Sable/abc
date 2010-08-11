/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package tests.eclipse.RenamePrivateField;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.FieldDeclaration;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class RenamePrivateFieldTests extends TestCase {
	public RenamePrivateFieldTests(String name) {
		super(name);
	}

	private void helper1_0(String fieldName, String newFieldName, String typeName, boolean renameGetter, boolean renameSetter) throws Exception{
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenamePrivateField/"+getName()+"/in");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		TypeDecl td = in.findSimpleType(typeName);
		assertNotNull(td);
		FieldDeclaration fd = td.findField(fieldName);
		assertNotNull(fd);
		try {
			fd.rename(newFieldName);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) { }
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper1_0(String fieldName, String newFieldName) throws Exception{
		helper1_0(fieldName, newFieldName, "A", false, false);
	}

	private void helper1() throws Exception{
		helper1_0("f", "g");
	}

	private void helper2(String fieldName, String newFieldName, boolean updateReferences, boolean updateTextualMatches,
											boolean renameGetter, boolean renameSetter,
											boolean expectedGetterRenameEnabled, boolean expectedSetterRenameEnabled) throws Exception{
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenamePrivateField/"+getName()+"/in");
		Program out = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenamePrivateField/"+getName()+"/out");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		TypeDecl td = in.findSimpleType("A");
		assertNotNull(td);
		FieldDeclaration fd = td.findField(fieldName);
		assertNotNull(fd);
		try {
			fd.rename(newFieldName);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	private void helper2(boolean updateReferences) throws Exception{
		helper2("f", "g", updateReferences, false, false, false, false, false);
	}

	private void helper2() throws Exception{
		helper2(true);
	}

	//--------- tests ----------
	public void testFail0() throws Exception{
		helper1();
	}

	public void testFail1() throws Exception{
		helper1();
	}

	public void testFail2() throws Exception{
		helper1();
	}

	public void testFail3() throws Exception{
		helper1();
	}

	public void testFail4() throws Exception{
		helper1();
	}

	/* disabled: we can do this
	public void testFail5() throws Exception{
		helper1();
	}*/

	/* disabled: we can do this
	public void testFail6() throws Exception{
		helper1();
	}*/

	/* disabled: we can do this
	public void testFail7() throws Exception{
		helper1();
	}*/

	/* disabled: we can do this
	public void testFail8() throws Exception{
		helper1_0("gg", "f", "A", false, false);
	}*/

	/* disabled: we can do this
	public void testFail9() throws Exception{
		helper1_0("y", "e", "getE", true, true);
	}*/

	/* disabled: we can do this
	public void testFail10() throws Exception{
		helper1_0("y", "e", "setE", true, true);
	}*/

	// ------
	public void test0() throws Exception{
		helper2();
	}

	public void test1() throws Exception{
		helper2();
	}

	/* disabled: does not compile
	public void test2() throws Exception{
		helper2(false);
	}*/

	/* disabled: unimplemented feature
	public void test3() throws Exception{
		helper2("f", "gg", true, true, false, false, false, false);
	}*/

	/* disabled: unimplemented feature
	public void test4() throws Exception{
		helper2("fMe", "fYou", true, false, true, true, true, true);
	}*/

	/* disabled: unimplemented feature
	public void test5() throws Exception{
		//regression test for 9895
		helper2("fMe", "fYou", true, false, true, false, true, false);
	}*/

	/* disabled: unimplemented feature
	public void test6() throws Exception{
		//regression test for 9895 - opposite case
		helper2("fMe", "fYou", true, false, false, true, false, true);
	}*/

	/* disabled: unimplemented feature
	public void test7() throws Exception{
		//regression test for 21292
		helper2("fBig", "fSmall", true, false, true, true, true, true);
	}*/

	/* disabled: unimplemented feature
	public void test8() throws Exception{
		//regression test for 26769
		helper2("f", "g", true, false, true, false, true, false);
	}*/

	/* disabled: unimplemented feature
	public void test9() throws Exception{
		//regression test for 30906
		helper2("fBig", "fSmall", true, false, true, true, true, true);
	}*/

	public void test10() throws Exception{
		//regression test for 81084
		helper2("fList", "fElements", true, false, false, false, false, false);
	}

	/* disabled: does not compile
	public void test11() throws Exception{
		helper2("fList", "fElements", true, false, true, true, true, true);
	}*/

	/* disabled: unimplemented feature
	public void testUnicode01() throws Exception{
		//regression test for 180331
		helper2("field", "feel", true, false, true, true, true, true);
	}*/
}
