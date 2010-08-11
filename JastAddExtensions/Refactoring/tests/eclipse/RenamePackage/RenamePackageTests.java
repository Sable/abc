/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package tests.eclipse.RenamePackage;

import junit.framework.TestCase;
import tests.AllTests;
import tests.CompileHelper;
import AST.PackageDecl;
import AST.Program;
import AST.RefactoringException;


public class RenamePackageTests extends TestCase {

	public RenamePackageTests(String name) {
		super(name);
	}
	
	public void renamePackageTest(String old_name, String new_name, boolean succeed) {
		String name = this.getName();
		Program in = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenamePackage/"+name+"/in");
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		PackageDecl pd = in.getPackageDecl(old_name);
		assertNotNull(pd);
		Program out = null;
		try {
			if(succeed) {
				out = CompileHelper.compileAllJavaFilesUnder("tests/eclipse/RenamePackage/"+name+"/out");
				assertNotNull(out);
			}
			pd.rename(new_name);
			if(succeed)
				assertEquals(out.toString(), in.toString());
			else
				assertEquals("<failure>", in.toString());
		} catch(RefactoringException e) {
			if(succeed)
				assertEquals(out.toString(), e.toString());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	// ---------- tests -------------


/*	public void testPackageRenameWithResource1() throws Exception {
		IPackageFragment fragment= getRoot().createPackageFragment("org.test", true, null);

		StringBuffer buf= new StringBuffer();
		buf.append("package org.test;\n");
		buf.append("public class MyClass {\n");
		buf.append("	org.test.MyClass me;\n");
		buf.append("}\n");
		fragment.createCompilationUnit("MyClass.java", buf.toString(), true, null);

		IFile file= ((IFolder) getRoot().getResource()).getFile("x.properties");
		byte[] content= "This is about 'org.test' and more".getBytes();
		file.create(new ByteArrayInputStream(content), true, null);
		file.refreshLocal(IResource.DEPTH_ONE, null);

		RenameJavaElementDescriptor descriptor= new RenameJavaElementDescriptor(IJavaRefactorings.RENAME_PACKAGE);
		descriptor.setJavaElement(fragment);
		descriptor.setNewName("org.test2");
		descriptor.setUpdateReferences(true);
		descriptor.setUpdateQualifiedNames(true);
		descriptor.setFileNamePatterns("*.properties");
		Refactoring refactoring= createRefactoring(descriptor);
		RefactoringStatus status= performRefactoring(refactoring);
		if (status != null)
			assertTrue(status.toString(), status.isOK());

		RefactoringProcessor processor= ((RenameRefactoring) refactoring).getProcessor();
		IResourceMapper rm= (IResourceMapper) processor.getAdapter(IResourceMapper.class);
		IJavaElementMapper jm= (IJavaElementMapper) processor.getAdapter(IJavaElementMapper.class);
		checkMappingUnchanged(jm, rm, new Object[] { getRoot().getJavaProject(), getRoot(), file });
		IFile newFile= ((IContainer) getRoot().getResource()).getFile(new Path("x.properties"));
		assertEquals("This is about 'org.test2' and more", getContents(newFile));
		checkMappingChanged(jm, rm, new Object[][] {
				{ fragment, getRoot().getPackageFragment("org.test2") }
		});
	}

	public void testPackageRenameWithResource2() throws Exception {
		IPackageFragment fragment= getRoot().createPackageFragment("org.test", true, null);

		StringBuffer buf= new StringBuffer();
		buf.append("package org.test;\n");
		buf.append("public class MyClass {\n");
		buf.append("}\n");
		fragment.createCompilationUnit("MyClass.java", buf.toString(), true, null);

		IFile file= ((IFolder) fragment.getResource()).getFile("x.properties");
		byte[] content= "This is about 'org.test' and more".getBytes();
		file.create(new ByteArrayInputStream(content), true, null);
		file.refreshLocal(IResource.DEPTH_ONE, null);

		RenameJavaElementDescriptor descriptor= new RenameJavaElementDescriptor(IJavaRefactorings.RENAME_PACKAGE);
		descriptor.setJavaElement(fragment);
		descriptor.setNewName("org.test2");
		descriptor.setUpdateReferences(true);
		descriptor.setUpdateQualifiedNames(true);
		descriptor.setFileNamePatterns("*.properties");
		Refactoring refactoring= createRefactoring(descriptor);
		RefactoringStatus status= performRefactoring(refactoring);
		if (status != null)
			assertTrue(status.toString(), status.isOK());

		RefactoringProcessor processor= ((RenameRefactoring) refactoring).getProcessor();
		IResourceMapper rm= (IResourceMapper) processor.getAdapter(IResourceMapper.class);
		IJavaElementMapper jm= (IJavaElementMapper) processor.getAdapter(IJavaElementMapper.class);
		checkMappingUnchanged(jm, rm, new Object[] { getRoot().getJavaProject(), getRoot() });
		IPackageFragment newFragment= getRoot().getPackageFragment("org.test2");
		IFile newFile= ((IContainer) newFragment.getResource()).getFile(new Path("x.properties"));
		assertEquals("This is about 'org.test2' and more", getContents(newFile));
		checkMappingChanged(jm, rm, new Object[][] {
				{ fragment, newFragment },
				{ file, newFile },
		});
	}

	public void testPackageRenameWithResource3() throws Exception {
		// regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=108019
		fIsPreDeltaTest= true;

		fQualifiedNamesFilePatterns= "*.txt";
		String textFileName= "Textfile.txt";

		String textfileContent= getFileContents(getTestPath() + getName() + TEST_INPUT_INFIX + "my/pack/" + textFileName);
		IFolder myPackFolder= getRoot().getJavaProject().getProject().getFolder("my").getFolder("pack");
		CoreUtility.createFolder(myPackFolder, true, true, null);
		IFile textfile= myPackFolder.getFile(textFileName);
		textfile.create(new ByteArrayInputStream(textfileContent.getBytes()), true, null);

		helper2(new String[]{"my.pack", "my"}, new String[][]{{}, {}}, "my");

		InputStreamReader reader= new InputStreamReader(textfile.getContents(true));
		StringBuffer newContent= new StringBuffer();
		try {
			int ch;
			while((ch= reader.read()) != -1)
				newContent.append((char)ch);
		} finally {
			reader.close();
		}
		String definedContent= getFileContents(getTestPath() + getName() + TEST_OUTPUT_INFIX + "my/" + textFileName);
		assertEqualLines("invalid updating", definedContent, newContent.toString());
	}*/

	public void testHierarchical01() throws Exception {
		renamePackageTest("my", "your", true);
	}

	public void testHierarchical02() throws Exception {
		renamePackageTest("my", "your", false);
	}

	public void testHierarchical03() throws Exception {
		renamePackageTest("my", "your", true);
	}

	/*public void testHierarchicalToSubpackage() throws Exception {
		fRenameSubpackages= true;

		PackageRename rename= new PackageRename(
				new String[]{"a", "a.b", "a.b.c", "a.b.c.d", "p"},
				new String[][]{{},{"B"},{"C"},{"D"}},
				"a.b",
				true
		);
		IPackageFragment thisPackage= rename.fPackages[0];

		IFolder src= (IFolder) getRoot().getResource();
		IFolder ab= src.getFolder("a/b");
		IFolder abc= ab.getFolder("c");
		IFolder abcd= abc.getFolder("d");

		IFolder abb= ab.getFolder("b");
		IFolder abbc= abb.getFolder("c");
		IFolder abbcd= abbc.getFolder("d");

		ParticipantTesting.reset();

		String[] createHandles= ParticipantTesting.createHandles(abb, abbc, abbcd);
		String[] deleteHandles= {};
		String[] moveHandles= ParticipantTesting.createHandles(ab.getFile("B.java"), abc.getFile("C.java"), abcd.getFile("D.java"));
		String[] renameHandles= ParticipantTesting.createHandles(JavaElementUtil.getPackageAndSubpackages(thisPackage));

		rename.createAndPerform(RefactoringStatus.WARNING);
		rename.checkExpectedState();

		ParticipantTesting.testCreate(createHandles);
		ParticipantTesting.testDelete(deleteHandles);
		ParticipantTesting.testMove(moveHandles, new MoveArguments[] {
				new MoveArguments(abb, true),
				new MoveArguments(abbc, true),
				new MoveArguments(abbcd, true),
		});
		ParticipantTesting.testRename(renameHandles, new RenameArguments[] {
				new RenameArguments(rename.getNewPackageName(rename.fPackageNames[0]), true),
				new RenameArguments(rename.getNewPackageName(rename.fPackageNames[1]), true),
				new RenameArguments(rename.getNewPackageName(rename.fPackageNames[2]), true),
				new RenameArguments(rename.getNewPackageName(rename.fPackageNames[3]), true),
		});

		performUndo();
		rename.checkOriginalState();
	}

	public void testHierarchicalToSuperpackage() throws Exception {
		fRenameSubpackages= true;

		PackageRename rename= new PackageRename(
				new String[]{"a.b", "a.b.b", "a", "p"},
				new String[][]{{"B"},{"BB"},{}},
				"a",
				true
		);
		IPackageFragment thisPackage= rename.fPackages[0];
		IFolder src= (IFolder) getRoot().getResource();
		IFolder a= src.getFolder("a");
		IFolder ab= src.getFolder("a/b");
		IFolder abb= src.getFolder("a/b/b");

		ParticipantTesting.reset();

		String[] createHandles= {};
		String[] deleteHandles= {};
		String[] moveHandles= ParticipantTesting.createHandles(ab.getFile("B.java"), abb.getFile("BB.java"));
		String[] renameHandles= ParticipantTesting.createHandles(JavaElementUtil.getPackageAndSubpackages(thisPackage));

		rename.createAndPerform(RefactoringStatus.OK);
		rename.checkExpectedState();

		ParticipantTesting.testCreate(createHandles);
		ParticipantTesting.testDelete(deleteHandles);
		ParticipantTesting.testMove(moveHandles, new MoveArguments[] {
				new MoveArguments(a, true),
				new MoveArguments(ab, true),
		});
		ParticipantTesting.testRename(renameHandles, new RenameArguments[] {
				new RenameArguments("a", true),
				new RenameArguments("a.b", true),
		});

		performUndo();
		rename.checkOriginalState();
	}

	public void testHierarchicalToSuperpackage2() throws Exception {
		fRenameSubpackages= true;

		PackageRename rename= new PackageRename(
				new String[]{"a.b", "a.b.c", "a.c", "p"},
				new String[][]{{"B"},{"BC"},{}},
				"a",
				true
		);
		IPackageFragment thisPackage= rename.fPackages[0];
		IFolder src= (IFolder) getRoot().getResource();
		IFolder a= src.getFolder("a");
		IFolder ab= src.getFolder("a/b");
		IFolder ac= src.getFolder("a/c");
		IFolder abc= src.getFolder("a/b/c");

		ParticipantTesting.reset();

		String[] createHandles= {};
		String[] deleteHandles= ParticipantTesting.createHandles(ab);
		String[] moveHandles= ParticipantTesting.createHandles(ab.getFile("B.java"), abc.getFile("BC.java"));
		String[] renameHandles= ParticipantTesting.createHandles(JavaElementUtil.getPackageAndSubpackages(thisPackage));

		rename.createAndPerform(RefactoringStatus.OK);
		rename.checkExpectedState();

		ParticipantTesting.testCreate(createHandles);
		ParticipantTesting.testDelete(deleteHandles);
		ParticipantTesting.testMove(moveHandles, new MoveArguments[] {
				new MoveArguments(a, true),
				new MoveArguments(ac, true),
		});
		ParticipantTesting.testRename(renameHandles, new RenameArguments[] {
				new RenameArguments("a", true),
				new RenameArguments("a.c", true),
		});

		performUndo();
		rename.fPackageNames= new String[] {"a.b", "a.b.c", "a", "p"};// empty package is not recreated, but that's OK
		rename.checkOriginalState();
	}

	public void testHierarchicalToSuperpackageFail() throws Exception {
		fRenameSubpackages= true;

		PackageRename rename= new PackageRename(
				new String[]{"a.b", "a.b.c", "a.c", "a", "p"},
				new String[][]{{"B"},{"BC"},{"AC"}},
				"a",
				true
		);

		rename.createAndPerform(RefactoringStatus.FATAL);
		rename.checkOriginalState();
	}

	public void testHierarchicalDisabledImport() throws Exception {
		fRenameSubpackages= true;
		fUpdateTextualMatches= true;

		PackageRename rename= new PackageRename(new String[]{"my", "my.pack"}, new String[][]{{},{"C"}}, "your");
		IPackageFragment thisPackage= rename.fPackages[0];

		ParticipantTesting.reset();

		List toRename= new ArrayList(Arrays.asList(JavaElementUtil.getPackageAndSubpackages(thisPackage)));
		toRename.add(thisPackage.getResource());
		String[] renameHandles= ParticipantTesting.createHandles(toRename.toArray());

		rename.execute();

		ParticipantTesting.testRename(renameHandles, new RenameArguments[] {
				new RenameArguments(rename.getNewPackageName(rename.fPackageNames[0]), true),
				new RenameArguments(rename.getNewPackageName(rename.fPackageNames[1]), true),
				new RenameArguments("your", true)
		});
	}

	public void testHierarchicalJUnit() throws Exception {
		fRenameSubpackages= true;

		File junitSrcArchive= JavaTestPlugin.getDefault().getFileInPlugin(JavaProjectHelper.JUNIT_SRC_381);
		Assert.assertTrue(junitSrcArchive != null && junitSrcArchive.exists());
		IPackageFragmentRoot src= JavaProjectHelper.addSourceContainerWithImport(getRoot().getJavaProject(), "src", junitSrcArchive, JavaProjectHelper.JUNIT_SRC_ENCODING);

		String[] packageNames= new String[]{"junit", "junit.extensions", "junit.framework", "junit.runner", "junit.samples", "junit.samples.money", "junit.tests", "junit.tests.extensions", "junit.tests.framework", "junit.tests.runner", "junit.textui"};
		ICompilationUnit[][] cus= new ICompilationUnit[packageNames.length][];
		for (int i= 0; i < cus.length; i++) {
			cus[i]= src.getPackageFragment(packageNames[i]).getCompilationUnits();
		}
		IPackageFragment thisPackage= src.getPackageFragment("junit");

		ParticipantTesting.reset();
		PackageRename rename= new PackageRename(packageNames, new String[packageNames.length][0],"jdiverge");

		RenameArguments[] renameArguments= new RenameArguments[packageNames.length + 1];
		for (int i= 0; i < packageNames.length; i++) {
			renameArguments[i]= new RenameArguments(rename.getNewPackageName(packageNames[i]), true);
		}
		renameArguments[packageNames.length]= new RenameArguments("jdiverge", true);
		String[] renameHandles= new String[packageNames.length + 1];
		System.arraycopy(ParticipantTesting.createHandles(JavaElementUtil.getPackageAndSubpackages(thisPackage)), 0, renameHandles, 0, packageNames.length);
		renameHandles[packageNames.length]= ParticipantTesting.createHandles(thisPackage.getResource())[0];

		// --- execute:
		RenameJavaElementDescriptor descriptor= createRefactoringDescriptor(thisPackage, "jdiverge");
		descriptor.setUpdateReferences(fUpdateReferences);
		descriptor.setUpdateTextualOccurrences(fUpdateTextualMatches);
		setFilePatterns(descriptor);
		descriptor.setUpdateHierarchy(fRenameSubpackages);
		Refactoring ref= createRefactoring(descriptor);

		performDummySearch();
		IUndoManager undoManager= getUndoManager();
		CreateChangeOperation create= new CreateChangeOperation(
			new CheckConditionsOperation(ref, CheckConditionsOperation.ALL_CONDITIONS),
			RefactoringStatus.FATAL);
		PerformChangeOperation perform= new PerformChangeOperation(create);
		perform.setUndoManager(undoManager, ref.getName());
		ResourcesPlugin.getWorkspace().run(perform, new NullProgressMonitor());
		RefactoringStatus status= create.getConditionCheckingStatus();
		assertTrue("Change wasn't executed", perform.changeExecuted());
		Change undo= perform.getUndoChange();
		assertNotNull("Undo doesn't exist", undo);
		assertTrue("Undo manager is empty", undoManager.anythingToUndo());

		assertFalse(status.hasError());
		assertTrue(status.hasWarning());
		RefactoringStatusEntry[] statusEntries= status.getEntries();
		for (int i= 0; i < statusEntries.length; i++) {
			RefactoringStatusEntry entry= statusEntries[i];
			assertTrue(entry.isWarning());
			assertTrue(entry.getCode() == RefactoringStatusCodes.MAIN_METHOD);
		}

		assertTrue("package not renamed: " + rename.fPackageNames[0], ! src.getPackageFragment(rename.fPackageNames[0]).exists());
		IPackageFragment newPackage= src.getPackageFragment(rename.fNewPackageName);
		assertTrue("new package does not exist", newPackage.exists());
		// ---

		ParticipantTesting.testRename(renameHandles, renameArguments);

		PerformChangeOperation performUndo= new PerformChangeOperation(undo);
		ResourcesPlugin.getWorkspace().run(performUndo, new NullProgressMonitor());

		assertTrue("new package still exists", ! newPackage.exists());
		assertTrue("original package does not exist: " + rename.fPackageNames[0], src.getPackageFragment(rename.fPackageNames[0]).exists());

		ZipInputStream zis= new ZipInputStream(new BufferedInputStream(new FileInputStream(junitSrcArchive)));
		ZipTools.compareWithZipped(src, zis, JavaProjectHelper.JUNIT_SRC_ENCODING);
	}*/

	public void testFail0() throws Exception{
		renamePackageTest("r", "9", false);
	}

	/* disabled: by Eclipse
	public void testFail1() throws Exception{
		printTestDisabledMessage("needs revisiting");
		//helper1(new String[]{"r.p1"}, new String[][]{{"A"}}, "r");
	}*/

	/* disabled: Eclipse doesn't rename packages with classes that contain native methods
	public void testFail3() throws Exception{
		renamePackageTest("r", "fred", false);
	}

	public void testFail4() throws Exception{
		renamePackageTest("r", "p1", false);
	}*/

	/* disabled: Eclipse doesn't rename packages with classes that contain a main method
	public void testFail5() throws Exception{
		renamePackageTest("r", "p1", false);
	}

	public void testFail6() throws Exception{
		renamePackageTest("r", "p1", false);
	}*/

	/* disabled: by Eclipse
	public void testFail7() throws Exception{
		//printTestDisabledMessage("1GK90H4: ITPJCORE:WIN2000 - search: missing package reference");
		printTestDisabledMessage("corner case - name obscuring");
//		helper1(new String[]{"r", "p1"}, new String[][]{{"A"}, {"A"}}, "fred");
	}

	public void testFail8() throws Exception{
		printTestDisabledMessage("corner case - name obscuring");
//		helper1(new String[]{"r", "p1"}, new String[][]{{"A"}, {"A"}}, "fred");
	}

	//native method used r.A as a parameter
	public void testFail9() throws Exception{
		printTestDisabledMessage("corner case - qualified name used  as a parameter of a native method");
		//helper1(new String[]{"r", "p1"}, new String[][]{{"A"}, {"A"}}, "fred");
	}*/

	public void testFail10() throws Exception{
		renamePackageTest("r.p1", "r", false);
	}

	//-------
	public void test0() throws Exception{
		renamePackageTest("r", "p1", true);
	}

	public void test1() throws Exception{
		renamePackageTest("r", "p1", true);
	}

	public void test2() throws Exception{
		renamePackageTest("r", "p1", true);
	}

	/* disabled: differing interpretations
	public void test3() throws Exception{
		renamePackageTest("fred", "r", true);
	}*/

	public void test4() throws Exception{
		renamePackageTest("r.p1", "q", true);
	}

	/* disabled: "no ref update"
	public void test5() throws Exception{
		renamePackageTest("r", "p1", true);
	}

	public void test6() throws Exception{
		renamePackageTest("r", "p1", true);
	}*/

	/* disabled: differing interpretations
	public void test7() throws Exception{
		renamePackageTest("r", "q", true);
	}*/

	/* disabled: differing interpretations
	public void test8() throws Exception{
		renamePackageTest("java.lang.reflect", "nonjava", true);
	}*/

	public void testToEmptyPack() throws Exception{
		renamePackageTest("r.p1", "fred", true);
	}

	/* disabled: no test files
	public void testToEmptySubPack() throws Exception{
		renamePackageTest("p", "p.q", true);
	}

	public void testWithEmptySubPack() throws Exception{
		renamePackageTest("p", "p1", true);
	}*/

	/* disabled: "no ref update"
	public void testReadOnly() throws Exception{
		renamePackageTest("r", "p1", true);
	}*/

	/* disabled: multiple roots not supported
	public void testImportFromMultiRoots1() throws Exception {
		fUpdateTextualMatches= true;
		helperProjectsPrgTest(
			new String[][] {
				new String[] { "p.p" }, new String[] { "p.p", "tests" }
				},
			"q",
			new String[][][] {
				new String[][] { new String[] { "A" }},
				new String[][] { new String[] { "ATest" }, new String[] { "AllTests" }}
		});
	}

	public void testImportFromMultiRoots2() throws Exception {
		helperProjectsPrgTest(
				new String[][] {
							new String[]{"p.p"},
							new String[]{"p.p", "tests"}
							},
			"q",
			new String[][][] {
							  new String[][] {new String[]{"A"}},
							  new String[][] {new String[]{"ATest", "TestHelper"}, new String[]{"AllTests", "QualifiedTests"}}
							  }
			);
	}

	public void testImportFromMultiRoots3() throws Exception {
		helperMultiRoots(new String[]{"srcPrg", "srcTest"},
			new String[][] {
							new String[]{"p.p"},
							new String[]{"p.p"}
							},
			"q",
			new String[][][] {
							  new String[][] {new String[]{"ToQ"}},
							  new String[][] {new String[]{"Ref"}}
							  }
			);
	}

	public void testImportFromMultiRoots4() throws Exception {
		//circular buildpath references
		IJavaProject projectPrg= null;
		IJavaProject projectTest= null;
		Hashtable options= JavaCore.getOptions();
		Object cyclicPref= JavaCore.getOption(JavaCore.CORE_CIRCULAR_CLASSPATH);
		try {
			projectPrg= JavaProjectHelper.createJavaProject("RenamePack1", "bin");
			assertNotNull(JavaProjectHelper.addRTJar(projectPrg));
			IPackageFragmentRoot srcPrg= JavaProjectHelper.addSourceContainer(projectPrg, "srcPrg");

			projectTest= JavaProjectHelper.createJavaProject("RenamePack2", "bin");
			assertNotNull(JavaProjectHelper.addRTJar(projectTest));
			IPackageFragmentRoot srcTest= JavaProjectHelper.addSourceContainer(projectTest, "srcTest");

			options.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.WARNING);
			JavaCore.setOptions(options);
			JavaProjectHelper.addRequiredProject(projectTest, projectPrg);
			JavaProjectHelper.addRequiredProject(projectPrg, projectTest);

			helperMultiProjects(new IPackageFragmentRoot[] {srcPrg, srcTest},
				new String[][] {
						new String[]{"p"},
						new String[]{"p"}
				},
				"a.b.c",
				new String[][][] {
						new String[][] {new String[]{"A", "B"}},
						new String[][] {new String[]{"ATest"}}
				}
			);
		} finally {
			options.put(JavaCore.CORE_CIRCULAR_CLASSPATH, cyclicPref);
			JavaCore.setOptions(options);
			JavaProjectHelper.delete(projectPrg);
			JavaProjectHelper.delete(projectTest);
		}
	}

	public void testImportFromMultiRoots5() throws Exception {
		//rename srcTest-p.p to q => ATest now must import p.p.A
		IJavaProject projectPrg= null;
		IJavaProject projectTest= null;
		try {
			projectPrg= JavaProjectHelper.createJavaProject("RenamePack1", "bin");
			assertNotNull(JavaProjectHelper.addRTJar(projectPrg));
			IPackageFragmentRoot srcPrg= JavaProjectHelper.addSourceContainer(projectPrg, "srcPrg");

			projectTest= JavaProjectHelper.createJavaProject("RenamePack2", "bin");
			assertNotNull(JavaProjectHelper.addRTJar(projectTest));
			IPackageFragmentRoot srcTest= JavaProjectHelper.addSourceContainer(projectTest, "srcTest");

			JavaProjectHelper.addRequiredProject(projectTest, projectPrg);

			helperMultiProjects(new IPackageFragmentRoot[] { srcTest, srcPrg },
				new String[][] {
					new String[] {"p.p"}, new String[] {"p.p"}
				},
				"q",
				new String[][][] {
					new String[][] {new String[] {"ATest"}},
					new String[][] {new String[] {"A"}}
				}
			);
		} finally {
			JavaProjectHelper.delete(projectPrg);
			JavaProjectHelper.delete(projectTest);
		}

	}

	public void testImportFromMultiRoots6() throws Exception {
		//rename srcTest-p.p to a.b.c => ATest must retain import p.p.A
		helperMultiRoots(new String[]{"srcTest", "srcPrg"},
				new String[][] {
								new String[]{"p.p"},
								new String[]{"p.p"}
								},
				"cheese",
				new String[][][] {
								  new String[][] {new String[]{"ATest"}},
								  new String[][] {new String[]{"A"}}
								  }
		);
	}

	public void testImportFromMultiRoots7() throws Exception {
		IJavaProject prj= null;
		IJavaProject prjRef= null;
		IJavaProject prjOther= null;
		try {
			prj= JavaProjectHelper.createJavaProject("prj", "bin");
			assertNotNull(JavaProjectHelper.addRTJar(prj));
			IPackageFragmentRoot srcPrj= JavaProjectHelper.addSourceContainer(prj, "srcPrj"); //$NON-NLS-1$

			prjRef= JavaProjectHelper.createJavaProject("prj.ref", "bin");
			assertNotNull(JavaProjectHelper.addRTJar(prjRef));
			IPackageFragmentRoot srcPrjRef= JavaProjectHelper.addSourceContainer(prjRef, "srcPrj.ref"); //$NON-NLS-1$

			prjOther= JavaProjectHelper.createJavaProject("prj.other", "bin");
			assertNotNull(JavaProjectHelper.addRTJar(prjOther));
			IPackageFragmentRoot srcPrjOther= JavaProjectHelper.addSourceContainer(prjRef, "srcPrj.other"); //$NON-NLS-1$

			JavaProjectHelper.addRequiredProject(prjRef, prj);
			JavaProjectHelper.addRequiredProject(prjRef, prjOther);

			helperMultiProjects(
				new IPackageFragmentRoot[] { srcPrj, srcPrjRef, srcPrjOther },
				new String[][] {
					new String[] {"pack"},
					new String[] {"pack", "pack.man"},
					new String[] {"pack"}
				},
				"com.packt",
				new String[][][] {
					new String[][] {new String[] {"DingsDa"}},
					new String[][] {new String[] {"Referer"}, new String[] {"StarImporter"}},
					new String[][] {new String[] {"Namesake"}}
				}
			);
		} finally {
			JavaProjectHelper.delete(prj);
			JavaProjectHelper.delete(prjRef);
			JavaProjectHelper.delete(prjOther);
		}
	}*/

	public void testStatic1() throws Exception {
		renamePackageTest("s1.j.l", "s1.java.lang", true);
	}

	/* disabled: multiple roots not supported
	public void testStaticMultiRoots1() throws Exception {
		renamePackageTest("p.p", "q", true);
	}*/
}
