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
package tests.eclipse.IntroduceFactory;

import java.util.Iterator;

import junit.framework.TestCase;
import tests.CompileHelper;
import tests.eclipse.PromoteTempToField.PromoteTempToFieldTests;
import AST.ClassInstanceExpr;
import AST.CompilationUnit;
import AST.ConstructorDecl;
import AST.FileRange;
import AST.Program;
import AST.RefactoringException;

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
			cd.createFactoryMethod();
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
	/*
	void namesHelper(String factoryMethodName, String factoryClassName)
		throws Exception
	{
		ICompilationUnit	cu= createCUForSimpleTest(getPackageP(), true, true);
		ISourceRange		selection= findSelectionInSource(cu.getSource());
		IntroduceFactoryRefactoring	ref= new IntroduceFactoryRefactoring(cu, selection.getOffset(), selection.getLength());

		RefactoringStatus	activationResult= ref.checkInitialConditions(new NullProgressMonitor());

		assertTrue("activation was supposed to be successful", activationResult.isOK());

		if (factoryMethodName != null)
			ref.setNewMethodName(factoryMethodName);
		if (factoryClassName != null)
			ref.setFactoryClass(factoryClassName);

		RefactoringStatus	checkInputResult= ref.checkFinalConditions(new NullProgressMonitor());

		assertTrue("precondition was supposed to pass but was " + checkInputResult.toString(), checkInputResult.isOK());

		performChange(ref, false);

		String newSource = cu.getSource();

		assertEqualLines(getName() + ": ", getFileContents(getTestFileName(true, false)), newSource);
	}*/

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
	/*private ICompilationUnit createCUFromFileName(String fileName, IPackageFragment pack) throws Exception {
		String fullName = TEST_PATH_PREFIX + getRefactoringPath() + "positive/" + fileName + "_in.java";

		return createCU(pack, fileName + "_in.java", getFileContents(fullName));
	}

	private void doMultiUnitTest(ICompilationUnit[] CUs, String testPath, String[] outputFileBaseNames, String factoryClassName) throws Exception, JavaModelException, IOException {
		ISourceRange selection= findSelectionInSource(CUs[0].getSource());
		IntroduceFactoryRefactoring	ref= new IntroduceFactoryRefactoring(CUs[0], selection.getOffset(), selection.getLength());

		RefactoringStatus activationResult= ref.checkInitialConditions(new NullProgressMonitor());

		assertTrue("activation was supposed to be successful", activationResult.isOK());

		if (factoryClassName != null)
			ref.setFactoryClass(factoryClassName);

		RefactoringStatus	checkInputResult= ref.checkFinalConditions(new NullProgressMonitor());

		assertTrue("precondition was supposed to pass but was " + checkInputResult.toString(), checkInputResult.isOK());

		performChange(ref, false);

		String	testName= getName();

		for (int i = 0; i < CUs.length; i++) {
			int optIdx= testName.indexOf("_");
			String testOptions= (optIdx >= 0) ? testName.substring(optIdx) : "";
			String outFileName= testPath + outputFileBaseNames[i] + testOptions + "_out.java";
			String xformedSrc= CUs[i].getSource();
			String expectedSrc= getFileContents(outFileName);

			assertEqualLines(getName() + ": ", expectedSrc, xformedSrc);
		}
	}*/

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
	/*void multiUnitHelper(boolean staticFactoryMethod, String[] inputFileBaseNames)
		throws Exception
	{
		IPackageFragment	pkg= getPackageP();
		ICompilationUnit	CUs[]= new ICompilationUnit[inputFileBaseNames.length];

		for (int i = 0; i < inputFileBaseNames.length; i++)
			CUs[i] = createCUFromFileName(inputFileBaseNames[i], pkg);

		String	testPath= TEST_PATH_PREFIX + getRefactoringPath() + "positive/";

		doMultiUnitTest(CUs, testPath, inputFileBaseNames, null);
	}*/

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
	/*void multiUnitBugHelper(boolean staticFactoryMethod, String[] inputFileBaseNames, String factoryClassName)
		throws Exception
	{
		ICompilationUnit CUs[]= new ICompilationUnit[inputFileBaseNames.length];

		for(int i= 0; i < inputFileBaseNames.length; i++) {
			int pkgEnd= inputFileBaseNames[i].lastIndexOf('/')+1;
			boolean explicitPkg= (pkgEnd > 0);
			IPackageFragment pkg= explicitPkg ? getRoot().createPackageFragment(inputFileBaseNames[i].substring(0, pkgEnd-1), true, new NullProgressMonitor()) : getPackageP();

			CUs[i]= createCUForBugTestCase(null, pkg, inputFileBaseNames[i].substring(pkgEnd), true);
		}

		String	testName= getName();
		String	testNumber= testName.substring("test".length());
		String	testPath= TEST_PATH_PREFIX + getRefactoringPath() + "Bugzilla/" + testNumber + "/";

		doMultiUnitTest(CUs, testPath, inputFileBaseNames, factoryClassName);
	}

	void multiProjectBugHelper(String[] inputFileBaseNames, String[] dependencies) throws Exception {
		Map projName2PkgNames= collectProjectPackages(inputFileBaseNames);
		Map projName2Project= new HashMap();
		Map proj2PkgRoot= new HashMap();

		try {
			createProjectPackageStructure(projName2PkgNames, projName2Project, proj2PkgRoot);

			ICompilationUnit[] CUs= createCUs(inputFileBaseNames, projName2Project, proj2PkgRoot);

			addProjectDependencies(dependencies, projName2Project);

			String testName= getName();
			String testNumber= testName.substring("test".length());
			String testPath= TEST_PATH_PREFIX + getRefactoringPath() + "Bugzilla/" + testNumber + "/";

			doMultiUnitTest(CUs, testPath, inputFileBaseNames, null);

		} finally {
			for (Iterator iter= proj2PkgRoot.keySet().iterator(); iter.hasNext();) {
				IJavaProject project= (IJavaProject) iter.next();
				if (project.exists()) {
					try {
						project.getProject().delete(true, null);
					} catch (CoreException e) {
						// swallow exception to avoid destroying the original one
						e.printStackTrace();
					}
				}
			}
		}
	}

	private ICompilationUnit[] createCUs(String[] inputFileBaseNames, Map projName2Project, Map proj2PkgRoot) throws Exception {
		ICompilationUnit CUs[]= new ICompilationUnit[inputFileBaseNames.length];

		for(int i= 0; i < inputFileBaseNames.length; i++) {
			String filePath= inputFileBaseNames[i];

			int projEnd= filePath.indexOf('/');
			int pkgEnd= filePath.lastIndexOf('/');
			int fileBegin= pkgEnd+1;

			String projName= filePath.substring(0, projEnd);
			String pkgName= filePath.substring(projEnd+1, pkgEnd).replace('/', '.');

			IJavaProject project= (IJavaProject) projName2Project.get(projName);
			IPackageFragmentRoot root= (IPackageFragmentRoot) proj2PkgRoot.get(project);
			IPackageFragment pkg= root.getPackageFragment(pkgName);

			CUs[i]= createCUForBugTestCase(project, pkg, filePath.substring(fileBegin), true);
		}
		return CUs;
	}

	private void addProjectDependencies(String[] dependencies, Map projName2Project) throws JavaModelException {
		for(int i= 0; i < dependencies.length; i++) {
			// dependent:provider
			String dependency= dependencies[i];
			int colonIdx= dependency.indexOf(':');
			String depName= dependency.substring(0, colonIdx);
			String provName= dependency.substring(colonIdx+1);

			IJavaProject depProj= (IJavaProject) projName2Project.get(depName);
			IJavaProject provProj= (IJavaProject) projName2Project.get(provName);

			JavaProjectHelper.addRequiredProject(depProj, provProj);
		}
	}

	private void createProjectPackageStructure(Map projName2PkgNames, Map projName2Project, Map proj2PkgRoot) throws CoreException, JavaModelException {
		for(Iterator iter= projName2PkgNames.keySet().iterator(); iter.hasNext(); ) {
			String projName= (String) iter.next();
			Set projPkgNames= (Set) projName2PkgNames.get(projName);

			IJavaProject project= JavaProjectHelper.createJavaProject(projName, "bin");
			IPackageFragmentRoot root= JavaProjectHelper.addSourceContainer(project, CONTAINER);

			JavaProjectHelper.addRTJar(project);

			Set pkgs= new HashSet();

			projName2Project.put(projName, project);
			proj2PkgRoot.put(project, root);
			for(Iterator pkgIter= projPkgNames.iterator(); pkgIter.hasNext(); ) {
				String pkgName= (String) pkgIter.next();

				pkgs.add(root.createPackageFragment(pkgName, true, null));
			}
		}
	}

	private Map collectProjectPackages(String[] inputFileBaseNames) {
		Map proj2Pkgs= new HashMap();

		for(int i= 0; i < inputFileBaseNames.length; i++) {
			String filePath= inputFileBaseNames[i];
			int projEnd= filePath.indexOf('/');
			String projName= filePath.substring(0, projEnd);
			String pkgName= filePath.substring(projEnd+1, filePath.lastIndexOf('/'));

			Set projPkgs= (Set) proj2Pkgs.get(projName);

			if (projPkgs == null)
				proj2Pkgs.put(projName, projPkgs= new HashSet());
			projPkgs.add(pkgName);
		}
		return proj2Pkgs;
	}*/

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
			cd.createFactoryMethod();
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

	/* disabled: TODO
	public void testNames_FFF() throws Exception {
		namesHelper(k_Names[0], null);
	}*/
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

	/* disabled: TODO
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
	/* disabled: TODO
	public void testMultipleUnits_FFF() throws Exception {
		multiUnitHelper(false, new String[] { "MultiUnit1A", "MultiUnit1B", "MultiUnit1C" });
	}*/
	//
	// ================================================================================
	// Bugzilla bug regression tests
	// ================================================================================
	//
	/* disabled: TODO
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

	/* disabled: TODO
	public void test46608() throws Exception {
		multiUnitBugHelper(true, new String[] { "p1/TT", "p2/TT" }, null);
	}*/

	public void test59284() throws Exception {
		singleUnitBugHelper("ArgTypeImport", true);
	}

	public void test59280() throws Exception {
		singleUnitBugHelper("ExplicitSuperCtorCall", true);
	}

	/* disabled: TODO
	public void test48504() throws Exception {
		multiUnitBugHelper(true, new String[] { "p1/A", "p1/B" }, "p1.B");
	}*/

	public void test58293() throws Exception {
		singleUnitBugHelper("ImplicitSuperCtorCall", true);
	}

	/* disabled: TODO
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
	
	public void testFactoryClash() throws Exception {
		failHelper(RefactoringStatus.ERROR);
	}
}
