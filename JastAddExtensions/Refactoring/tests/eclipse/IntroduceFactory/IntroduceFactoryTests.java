/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Max Schaefer    - adapted to work with JRRT
 *******************************************************************************/
package tests.eclipse.IntroduceFactory;

import java.util.Collections;
import java.util.Iterator;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import tests.eclipse.PromoteTempToField.PromoteTempToFieldTests;
import AST.ClassInstanceExpr;
import AST.CompilationUnit;
import AST.ConstructorDecl;
import AST.FileRange;
import AST.MemberDecl;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

/**
 * @author rfuhrer@watson.ibm.com
 */
public class IntroduceFactoryTests extends TestCase {
	static class RefactoringStatus {
		static final int FATAL = 23;
		static final int ERROR = 42;
	}

	public IntroduceFactoryTests(String name) {
		super(name);
	}

	/**
	 * Produces a test file name based on the name of this JUnit testcase.
	 * For input files, trims off the trailing part of the test name that
	 * begins with a '_', to get rid of the options part, so that we can
	 * have a single (suite of) input file(s) but several outputs dependent
	 * on the option settings.
	 * @param input true iff the requested file is an input file.
	 * @return the name of the test file, with a trailing "_in.java" if an input
	 * file and a trailing "_XXX.java" if an output file and the test name/options
	 * are "_XXX".
	 */
	private String getSimpleTestFileName(boolean input) {
		String	testName = getName();
		int		usIdx=  testName.indexOf('_');
		int		endIdx= (usIdx >= 0) ? usIdx : testName.length();
		String	fileName = (input ? (testName.substring(4, endIdx) + "_in") : testName.substring(4));

		return fileName + ".java";
	}

	/**
	 * Produces a test file name based on the name of this JUnit testcase,
	 * like getSimpleTestFileName(), but also prepends the appropriate version
	 * of the resource path (depending on the value of <code>positive</code>).
	 * Test files are assumed to be located in the resources directory.
	 * @param positive true iff the requested file is for a positive unit test
	 * @param input true iff the requested file is an input file
	 * @return the test file name
	 */
	private String getTestFileName(boolean positive, boolean input) {
		String path= "tests/eclipse/IntroduceFactory/";
		path += (positive ? "positive/": "negative/");
		return path + getSimpleTestFileName(input);
	}

	/**
	 * Produces a compilation unit from an input source file whose name
	 * is based on the testcase name.
	 * Test files are assumed to be located in the resources directory.
	 * @param pack
	 * @param positive
	 * @param input
	 * @return the ICompilationUnit created from the specified test file
	 * @throws Exception
	 */
	private Program compile(boolean positive, boolean input) {
		String	fileName= getTestFileName(positive, input);
		return CompileHelper.compile(fileName);
	}

	/**
	 * Produces a test file name based on the name of this JUnit testcase,
	 * like getSimpleTestFileName(), but also prepends the appropriate version
	 * of the resource path (depending on the value of <code>positive</code>).
	 * Test files are assumed to be located in the resources directory.
	 * @param project the project
	 * @param pack the package fragment
	 * @param fileName the file name
	 * @param input true iff the requested file is an input file
	 * @return the test file name
	 */
	private String getBugTestFileName(String project, String pack, String fileName, boolean input) {
		String testName= getName();
		String testNumber= testName.substring("test".length());
		String path= "tests/eclipse/IntroduceFactory/Bugzilla/" + testNumber + "/" +
									(project == null ? "" : project + "/") +
									(pack == null ? "" : pack + "/");
		return path + fileName + (input ? "" : "_out") + ".java";
	}

	private CompilationUnit createCUForBugTestCase(String project, String pack, String baseName, boolean input) {
		String	fileName= getBugTestFileName(project, pack, baseName, input);
		String	cuName= baseName + (input ? "" : "_out") + ".java";
		Program prog = CompileHelper.compile(fileName);
		CompilationUnit cu = null;
		for(Iterator<CompilationUnit> iter=prog.compilationUnitIterator();iter.hasNext();) {
			CompilationUnit next = iter.next();
			if(next.fromSource())
				cu = next;
		}
		assertNotNull(cu);
		return cu;
	}

	private CompilationUnit createCUForSimpleTest(boolean positive, boolean input) {
		String	fileName= getTestFileName(positive, input);
		Program prog = CompileHelper.compile(fileName);
		CompilationUnit cu = null;
		for(Iterator<CompilationUnit> iter=prog.compilationUnitIterator();iter.hasNext();) {
			CompilationUnit next = iter.next();
			if(next.fromSource())
				cu = next;
		}
		assertNotNull(cu);
		return cu;
	}

	static final String SELECTION_START_HERALD= "/*[*/";
	static final String SELECTION_END_HERALD= "/*]*/";

	/**
	 * Finds and returns the selection markers in the given source string,
	 * i.e. the first occurrences of <code>SELECTION_START_HERALD</code> and
	 * <code>SELECTION_END_HERALD</code>. Fails an assertion if either of these
	 * markers is not present in the source string.
	 * @param source
	 * @return an ISourceRange representing the marked selection
	 * @throws Exception
	 */
	private FileRange findSelectionInSource(CompilationUnit cu) {
		FileRange begin = cu.findComment(SELECTION_START_HERALD);
		FileRange end = cu.findComment(SELECTION_END_HERALD);
		assertNotNull(begin);
		assertNotNull(end);
		return new FileRange(begin.filename, begin.el, begin.ec+1, end.sl, end.sc);
	}

	private void doSingleUnitTest(boolean protectConstructor, CompilationUnit cu, String outputFileName) {
		Program out = CompileHelper.compile(outputFileName);
		assertNotNull(out);
		
		FileRange selection= findSelectionInSource(cu);
		ConstructorDecl cd;
		ClassInstanceExpr cie = PromoteTempToFieldTests.findNode(cu, ClassInstanceExpr.class, selection.sl, selection.sc, selection.el, selection.ec-1);
		if(cie != null) {
			cd = cie.decl();
		} else {
			cd = PromoteTempToFieldTests.findNode(cu, ConstructorDecl.class, selection.sl, selection.sc, selection.el, selection.ec-1);
			assertNotNull(cd);
		}
		
		try {
			cd.doIntroduceFactory(protectConstructor);
			assertEquals(out.toString(), cu.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
	}

	/**
	 * Tests the IntroduceFactoryRefactoring refactoring on a single input source file
	 * whose name is the test name (minus the "test" prefix and any trailing
	 * options indicator such as "_FFF"), and compares the transformed code
	 * to a source file whose name is the test name (minus the "test" prefix).
	 * Test files are assumed to be located in the resources directory.
	 * @param protectConstructor true iff IntroduceFactoryRefactoring should make the constructor private
	 * @throws Exception
	 */
	void singleUnitHelper(boolean protectConstructor)
	{
		CompilationUnit cu= createCUForSimpleTest(true, true);
		doSingleUnitTest(protectConstructor, cu, getTestFileName(true, false));
	}

	/**
	 * Tests the IntroduceFactoryRefactoring refactoring on a single input source file
	 * whose name is the test name (minus the "test" prefix and any trailing
	 * options indicator such as "_FFF"), and compares the transformed code
	 * to a source file whose name is the test name (minus the "test" prefix).
	 * Test files are assumed to be located in the resources directory.
	 * @param baseFileName the base file name
	 * @param protectConstructor true iff IntroduceFactoryRefactoring should make the constructor private
	 * @throws Exception
	 */
	protected void singleUnitBugHelper(String baseFileName, boolean protectConstructor)
		throws Exception
	{
		CompilationUnit	cu= createCUForBugTestCase(null, null, baseFileName, true);
		doSingleUnitTest(protectConstructor, cu, getBugTestFileName(null, null, baseFileName, false));
	}

	/**
	 * Like singleUnitHelper(), but allows for the specification of the names of
	 * the generated factory method, class, and interface, as appropriate.
	 * @param factoryMethodName the name to use for the generated factory method
	 * @param factoryClassName the name of the factory class
	 * @throws Exception
	 */
	void namesHelper(String factoryMethodName, String factoryClassName)
		throws Exception
	{
		assertNotNull(factoryMethodName);
		assertNull(factoryClassName);
		CompilationUnit cu= createCUForSimpleTest(true, true);
		Program out = CompileHelper.compile(getTestFileName(true, false));
		assertNotNull(out);
		
		FileRange selection= findSelectionInSource(cu);
		ConstructorDecl cd;
		ClassInstanceExpr cie = PromoteTempToFieldTests.findNode(cu, ClassInstanceExpr.class, selection.sl, selection.sc, selection.el, selection.ec-1);
		if(cie != null) {
			cd = cie.decl();
		} else {
			cd = PromoteTempToFieldTests.findNode(cu, ConstructorDecl.class, selection.sl, selection.sc, selection.el, selection.ec-1);
			assertNotNull(cd);
		}
		
		try {
			MethodDecl md = cd.doIntroduceFactory(true);
			md.rename(factoryMethodName);
			assertEquals(out.toString(), cu.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
	}

	/**
	 * Creates a compilation unit for a source file with a given base name (plus
	 * "_in" suffix) in the given package. The source file is assumed to be
	 * located in the test resources directory.<br>
	 * Currently only handles positive tests.
	 * @param fileName the base name of the source file (minus the "_in" suffix)
	 * @param pack an IPackageFragment for the containing package
	 * @return the ICompilationUnit for the newly-created unit
	 * @throws Exception
	 */
	private CompilationUnit[] createCUsFromBaseNames(String... baseNames) {
		String[] fileNames = new String[baseNames.length];
		for(int i=0;i<baseNames.length;++i)
			fileNames[i] = "tests/eclipse/IntroduceFactory/positive/" + baseNames[i] + "_in.java";
		Program prog = CompileHelper.compile(fileNames);
		CompilationUnit[] cus = new CompilationUnit[baseNames.length];
		for(Iterator<CompilationUnit> iter=prog.compilationUnitIterator();iter.hasNext();) {
			CompilationUnit next = iter.next();
			if(next.fromSource()) {
				for(int i=0;i<baseNames.length;++i)
					if(next.getID().equals(baseNames[i]+"_in"))
						cus[i] = next;
			}
		}
		return cus;
	}

	private CompilationUnit[] createCUsForBugTestCase(String... baseNames) {
		String testName= getName();
		String testNumber= testName.substring("test".length());
		String[] fileNames = new String[baseNames.length];
		for(int i=0;i<baseNames.length;++i)
			fileNames[i] = "tests/eclipse/IntroduceFactory/Bugzilla/" + testNumber + "/" + baseNames[i] + ".java";
		Program prog = CompileHelper.compile(fileNames);
		CompilationUnit[] cus = new CompilationUnit[baseNames.length];
		for(Iterator<CompilationUnit> iter=prog.compilationUnitIterator();iter.hasNext();) {
			CompilationUnit next = iter.next();
			if(next.fromSource()) {
				for(int i=0;i<baseNames.length;++i)
					if(next.relativeName().equals(fileNames[i]))
						cus[i] = next;
			}
		}
		return cus;
	}
	
	private void doMultiUnitTest(CompilationUnit[] CUs, String testPath, String[] outputFileBaseNames, String factoryClassName) {
		String	testName= getName();
		String[] outputFileNames = new String[outputFileBaseNames.length];
		for (int i = 0; i < CUs.length; i++) {
			int optIdx= testName.indexOf("_");
			String testOptions= (optIdx >= 0) ? testName.substring(optIdx) : "";
			outputFileNames[i] = testPath + outputFileBaseNames[i] + testOptions + "_out.java";
		}
		Program out = CompileHelper.compile(outputFileNames);
		FileRange selection= findSelectionInSource(CUs[0]);
		ConstructorDecl cd;
		ClassInstanceExpr cie = PromoteTempToFieldTests.findNode(CUs[0], ClassInstanceExpr.class, selection.sl, selection.sc, selection.el, selection.ec-1);
		if(cie != null) {
			cd = cie.decl();
		} else {
			cd = PromoteTempToFieldTests.findNode(CUs[0], ConstructorDecl.class, selection.sl, selection.sc, selection.el, selection.ec-1);
			assertNotNull(cd);
		}
		
		try {
			Program in = CUs[0].programRoot();
			MethodDecl factory = cd.doIntroduceFactory(true);
			if(factoryClassName != null) {
				TypeDecl factoryClass = in.findType(factoryClassName);
				assertNotNull(factoryClass);
				factory.hostType().doMoveMembers(Collections.singleton((MemberDecl)factory), factoryClass);
			}
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
	}

	/**
	 * Tests the IntroduceFactoryRefactoring refactoring on a set of input source files
	 * whose names are supplied in the <code>fileBaseNames</code> argument,
	 * and compares the transformed code to source files whose names are
	 * the input base names plus the options suffix (e.g. "_FFF").
	 * Test files are assumed to be located in the resources directory.
	 * @param staticFactoryMethod true iff IntroduceFactoryRefactoring should make the factory method static
	 * @param inputFileBaseNames an array of input source file base names
	 * @throws Exception
	 */
	void multiUnitHelper(boolean staticFactoryMethod, String[] inputFileBaseNames) {
		CompilationUnit[] cus = createCUsFromBaseNames(inputFileBaseNames);
		String	testPath= "tests/eclipse/IntroduceFactory/positive/";
		doMultiUnitTest(cus, testPath, inputFileBaseNames, null);
	}

	/**
	 * Tests the IntroduceFactoryRefactoring refactoring on a set of input source files
	 * whose names are supplied in the <code>fileBaseNames</code> argument,
	 * and compares the transformed code to source files whose names are
	 * the input base names plus the options suffix (e.g. "_FFF").
	 * Test files are assumed to be located in the resources directory.
	 * @param staticFactoryMethod true iff IntroduceFactoryRefactoring should make the factory method static
	 * @param inputFileBaseNames an array of input source file base names
	 * @param factoryClassName the fully-qualified name of the class to receive the factory method, or null
	 * if the factory method is to be placed on the class defining the given constructor
	 * @throws Exception
	 */
	void multiUnitBugHelper(boolean staticFactoryMethod, String[] inputFileBaseNames, String factoryClassName) {
		CompilationUnit[] cus = createCUsForBugTestCase(inputFileBaseNames);
		String	testName= getName();
		String	testNumber= testName.substring("test".length());
		String	testPath= "tests/eclipse/IntroduceFactory/Bugzilla/" + testNumber + "/";

		doMultiUnitTest(cus, testPath, inputFileBaseNames, factoryClassName);
	}

	private void failHelper(int expectedStatus) throws Exception {
		CompilationUnit	cu= createCUForSimpleTest(false, true);
		assertNotNull(cu);
		FileRange selection= findSelectionInSource(cu);
		assertNotNull(selection);
		ConstructorDecl cd;
		ClassInstanceExpr cie = PromoteTempToFieldTests.findNode(cu, ClassInstanceExpr.class, selection.sl, selection.sc, selection.el, selection.ec-1);
		if(cie != null) {
			cd = cie.decl();
		} else {
			cd = PromoteTempToFieldTests.findNode(cu, ConstructorDecl.class, selection.sl, selection.sc, selection.el, selection.ec-1);
			assertNotNull(cd);
		}
		
		try {
			cd.doIntroduceFactory();
			assertEquals("<failure>", cu.toString());
		} catch(RefactoringException rfe) {
		}
	}

	//--- TESTS
	public void testStaticContext_FFF() throws Exception {
		singleUnitHelper(false);
	}
	//
	// ================================================================================
	//
	public void testInstanceContext_FFF() throws Exception {
		singleUnitHelper(false);
	}
	//
	// ================================================================================
	//
	static final String[]	k_Names = { "createThing", "ThingFactory", "IThingFactory" };

	public void testNames_FFF() throws Exception {
		namesHelper(k_Names[0], null);
	}
	//
	// ================================================================================
	//
	public void testMultipleCallers_FFF() throws Exception {
		singleUnitHelper(false);
	}
	//
	// ================================================================================
	//
	public void testSelectConstructor() throws Exception {
		singleUnitHelper(false);
	}
	//
	// ================================================================================
	//
	public void testDifferentSigs() throws Exception {
		singleUnitHelper(false);
	}

	public void testDifferentArgs1() throws Exception {
		singleUnitHelper(false);
	}

	public void testDifferentArgs2() throws Exception {
		singleUnitHelper(false);
	}

	public void testDifferentArgs3() throws Exception {
		singleUnitHelper(false);
	}
	//
	// ================================================================================
	//
	public void testUnmovableArg1() throws Exception {
		singleUnitHelper(false);
	}

	public void testUnmovableArg2() throws Exception {
		singleUnitHelper(false);
	}

	public void testDontMoveArgs1() throws Exception {
		singleUnitHelper(false);
	}

	public void testDontMoveArgs2() throws Exception {
		singleUnitHelper(false);
	}
	//
	// ================================================================================
	//
	public void testProtectConstructor1() throws Exception {
		singleUnitHelper(true);
	}

	public void testProtectConstructor2() throws Exception {
		singleUnitHelper(true);
	}
	//
	// ================================================================================
	//
	public void testStaticInstance() throws Exception {
		singleUnitHelper(false);
	}
	//
	// ================================================================================
	//
	/* disabled: does not compile
	public void testCtorThrows() throws Exception {
		singleUnitHelper(true);
	}*/
	//
	// ================================================================================
	//
	public void testJavadocRef() throws Exception {
		singleUnitHelper(true);
	}
	//
	// ================================================================================
	//
	public void testNestedClass() throws Exception {
		failHelper(RefactoringStatus.FATAL);
	}



    //
    // ================================================================================
    // Generics-related tests
    public void testTypeParam() throws Exception {
        singleUnitHelper(true);
    }

    public void testTwoTypeParams() throws Exception {
        singleUnitHelper(true);
    }

    public void testBoundedTypeParam() throws Exception {
        singleUnitHelper(true);
    }

    public void testTwoBoundedTypeParams() throws Exception {
        singleUnitHelper(true);
    }

	public void testWildcardParam() throws Exception {
		singleUnitHelper(true);
	}

	/* disabled: no support for creating factory class
    public void testTypeParam2() throws Exception {
        namesHelper(null, "p.Factory");
    }*/
    //
	// ================================================================================
	// Other J2SE 5.0 tests
    public void testEnum() throws Exception {
    	failHelper(RefactoringStatus.FATAL);
    }

    public void testAnnotation1() throws Exception {
   		singleUnitHelper(true);
    }

    public void testAnnotation2() throws Exception {
   		singleUnitHelper(true);
    }

    public void testAnnotation3() throws Exception {
   		singleUnitHelper(true);
    }

	public void testVarArgsCtor() throws Exception {
	    // RMF - As of I20050202, search engine doesn't reliably find call sites to varargs methods
		singleUnitHelper(true);
	}
    //
	// ================================================================================
	//
	public void testMultipleUnits_FFF() throws Exception {
		multiUnitHelper(false, new String[] { "MultiUnit1A", "MultiUnit1B", "MultiUnit1C" });
	}
	//
	// ================================================================================
	// Bugzilla bug regression tests
	// ================================================================================
	//
	/* disabled: does not compile
	public void test45942() throws Exception {
		multiUnitBugHelper(true, new String[] { "TestClass", "UseTestClass" }, null);
	}*/

	public void test46189() throws Exception {
		singleUnitBugHelper("TestClass", true);
	}

	public void test46189B() throws Exception {
		singleUnitBugHelper("TestClass", true);
	}

	public void test46373() throws Exception {
		singleUnitBugHelper("ImplicitCtor", false);
	}

	public void test46374() throws Exception {
		singleUnitBugHelper("QualifiedName", false);
	}

	public void test46608() throws Exception {
		multiUnitBugHelper(true, new String[] { "p1/TT", "p2/TT" }, null);
	}

	public void test59284() throws Exception {
		singleUnitBugHelper("ArgTypeImport", true);
	}

	public void test59280() throws Exception {
		singleUnitBugHelper("ExplicitSuperCtorCall", true);
	}

	public void test48504() throws Exception {
		multiUnitBugHelper(true, new String[] { "p1/A", "p1/B" }, "p1.B");
	}

	public void test58293() throws Exception {
		singleUnitBugHelper("ImplicitSuperCtorCall", true);
	}

	/* disabled: tests idiosyncratic feature
	public void test59283() throws Exception {
		multiProjectBugHelper(new String[] { "proj1/pA/A", "proj2/pB/B" },
				new String[] { "proj2:proj1" });
	}*/

	public void test84807() throws Exception {
		singleUnitBugHelper("CtorOfParamType", true);
	}

	public void test85465() throws Exception {
		singleUnitBugHelper("Varargs1", true);
	}

	public void test97507() throws Exception {
		singleUnitBugHelper("CtorTypeArgBounds", true);
	}

	public void test250660() throws Exception {
		singleUnitBugHelper("HasAnonymous", true);
	}
	
	public void test74759() throws Exception {
		singleUnitBugHelper("Test", true);
	}
	
	public void test298281() throws Exception {
		singleUnitBugHelper("Thing", true);
	}
	
	public void testFactoryClash() throws Exception {
		failHelper(RefactoringStatus.ERROR);
	}
}
