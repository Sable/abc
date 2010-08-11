/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package tests.eclipse.IntroduceParameter;

import java.io.BufferedReader;
import java.io.FileReader;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import tests.eclipse.ExtractTemp.ExtractTempTests;
import AST.Expr;
import AST.Program;
import AST.RefactoringException;

public class IntroduceParameterTests extends TestCase {
	
	public IntroduceParameterTests(String name) {
		super(name);
	}

	protected String getResourceLocation() {
		return "IntroduceParameter/";
	}

	static class IntroduceParameterData {
		String name;
		int startLine, startColumn, endLine, endColumn;
		public IntroduceParameterData(String name, int startLine, int startColumn, int endLine, int endColumn) {
			this.name = name;
			this.startLine = startLine;
			this.startColumn = startColumn;
			this.endLine = endLine;
			this.endColumn = endColumn;
		}
	}
	
	private IntroduceParameterData getData(String fileName) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String firstLine = br.readLine();
		assertTrue(firstLine.matches("^//selection: .*$"));
		String[] fields = firstLine.substring("//selection: ".length()).split(",\\s*");
		assertTrue(fields.length == 4);
		String secondLine = br.readLine();
		String[] names;
		if(secondLine.matches("^//name: .*$")) {
			names = secondLine.substring("//name: ".length()).split("\\s*->\\s*");
		} else {
			names = new String[]{"", "arg"};
		}
		assertTrue(names.length == 2);
		return new IntroduceParameterData(names[1], Integer.parseInt(fields[0]), Integer.parseInt(fields[1]), Integer.parseInt(fields[2]), Integer.parseInt(fields[3]));
	}

	private void performOK() throws Exception {
		perform(true);
	}

	private void performInvalidSelection() throws Exception {
		perform(false);
	}

	private void perform(boolean succeed) throws Exception {
		String in_name = "tests/eclipse/IntroduceParameter/" + (succeed ? "simple" : "invalid") + "/" + getName().substring(succeed ? 11 : 12) + ".java";
		Program in = CompileHelper.compile(in_name);
		Program out = succeed ? CompileHelper.compile("tests/eclipse/IntroduceParameter/simple/out/" + getName().substring(11) + ".java") : null;
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertTrue(!succeed || out != null);
		IntroduceParameterData data = getData(in_name);
		Expr e = ExtractTempTests.findExpr(in, data.startLine, data.startColumn, data.endLine, data.endColumn);
		if(!succeed && e == null)
			return;
		assertNotNull(e);
		try {
			e.doIntroduceParameter(data.name);
			assertEquals(succeed ? out.toString() : "<failure>", in.toString());
		} catch(RefactoringException rfe) {
			if(succeed)
				assertEquals(out.toString(), rfe.toString());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

// ---

	public void testInvalid_NotInMethod1() throws Exception {
		performInvalidSelection();
	}
	public void testInvalid_NotInMethod2() throws Exception {
		performInvalidSelection();
	}
	public void testInvalid_NotInMethod3() throws Exception {
		performInvalidSelection();
	}

	public void testInvalid_PartName1() throws Exception {
		performInvalidSelection();
	}

	public void testInvalid_PartString() throws Exception {
		performInvalidSelection();
	}

	/* disabled: does not compile
	public void testInvalid_NoMethodBinding() throws Exception {
		performInvalidSelection();
	}*/

	public void testInvalid_NoExpression1() throws Exception {
		performInvalidSelection();
	}

	//	---

	/* disabled: conservative data flow
	public void testSimple_Capture() throws Exception {
		performOK();
	}*/

	/* disabled: clone detection
	public void testSimple_ConstantExpression1() throws Exception {
		performOK();
	}*/

	/* disabled: conservative data flow
	public void testSimple_ConstantExpression2() throws Exception {
		performOK();
	}*/

	public void testSimple_NewInstance1() throws Exception {
		performOK();
	}

	/* disabled: conservative data flow
	public void testSimple_NewInstanceImport() throws Exception {
		performOK();
	}*/

	public void testSimple_StaticGetter1() throws Exception {
		performOK();
	}

	public void testSimple_Formatting1() throws Exception {
		performOK();
	}

	public void testSimple_Javadoc1() throws Exception {
		performOK();
	}

	public void testSimple_Javadoc2() throws Exception {
		performOK();
	}

	public void testSimple_Constructor1() throws Exception {
		performOK();
	}

	public void testSimple_Vararg1() throws Exception {
		performOK();
	}

	/* disabled: conservative data flow
	public void testSimple_Wildcard1() throws Exception {
		performOK();
	}*/

	/* disabled: conservative data flow
	public void testSimple_Wildcard2() throws Exception {
		performOK();
	}*/
}
