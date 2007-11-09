package org.jastadd.plugin.jastaddj.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ArchiveSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ExternalArchiveSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.AST.IOutlineNode;
import org.jastadd.plugin.jastaddj.JastAddJActivator;
import org.jastadd.plugin.jastaddj.AST.ICompilationUnit;
import org.jastadd.plugin.jastaddj.AST.IJastAddJFindDeclarationNode;
import org.jastadd.plugin.jastaddj.AST.IProgram;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfigurationUtil;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration.ClassPathEntry;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration.Pattern;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration.SourcePathEntry;
import org.jastadd.plugin.jastaddj.editor.JastAddJEditor;
import org.jastadd.plugin.jastaddj.nature.JastAddJNature;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.repair.JastAddStructureModel;

import AST.ASTNode;
import AST.Access;
import AST.CompilationUnit;
import AST.Expr;
import AST.JavaParser;
import AST.MethodAccess;
import AST.ParExpr;
import AST.Problem;
import AST.Program;

public class JastAddJModel extends JastAddModel {

	static class ProgramInfo {
		IProgram program;
		JastAddJBuildConfiguration buildConfiguration;
	}

	protected HashMap<IProject, ProgramInfo> projectToNodeMap = new HashMap<IProject, ProgramInfo>();
	protected HashMap<IProgram, IProject> nodeToProjectMap = new HashMap<IProgram, IProject>();

	// ************* Overridden methods

	@Override
	protected void initModel() {
		editorConfig = new JastAddJEditorConfiguration(this);
	}

	// ************** Implementations of abstract methods

	public boolean isModelFor(IProject project) {
		try {
			if (project != null && project.isOpen()
					&& project.isNatureEnabled(getNatureID())) {
				return true;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean isModelFor(IFile file) {
		for (String str : getFileExtensions()) {
			if (file.getFileExtension().equals(str)) {
				return isModelFor(file.getProject());
			}
		}
		return false;
	}

	public boolean isModelFor(IDocument document) {
		return documentToFile(document) != null;
	}

	public boolean isModelFor(IJastAddNode node) {
		return getProject(node) != null;
	}

	public List<String> getFileExtensions() {
		List<String> list = new ArrayList<String>();
		list.add("java");
		return list;
	}

	public String[] getFilterExtensions() {
		return new String[] { ".project", "*.java.dummy", "*.class" };
	}

	public void openFile(IJastAddNode node) {
		if (node instanceof IJastAddJFindDeclarationNode) {
			IJastAddJFindDeclarationNode n = (IJastAddJFindDeclarationNode) node;
			int targetLine = n.selectionLine();
			int targetColumn = n.selectionColumn();
			int targetLength = n.selectionLength();
			ICompilationUnit cu = n.declarationCompilationUnit();
			openFile(cu, targetLine, targetColumn, targetLength);
		}
	}

	public void populateClassPath(IProject project,
			JastAddJBuildConfiguration buildConfiguration,
			List<String> fullClassPath) {
		fullClassPath.addAll(buildClassPath(project, buildConfiguration));

		String projectPath = project.getLocation().toOSString();
		if (buildConfiguration.outputPath != null)
			fullClassPath.add(projectPath + File.separator
					+ buildConfiguration.outputPath);
		else
			fullClassPath.add(projectPath);
	}

	public void popupateSourceContainers(IProject project,
			JastAddJBuildConfiguration buildConfiguration,
			List<ISourceContainer> result) {
		result.add(new FolderSourceContainer(project, true));
		popupateSourceAttachments(project, buildConfiguration, result);
	}

	public void popupateSourceAttachments(IProject project,
			JastAddJBuildConfiguration buildConfiguration,
			List<ISourceContainer> result) {
		for (ClassPathEntry entry : buildConfiguration.classPathList) {
			if (entry.sourceAttachmentPath == null)
				continue;

			Path path = new Path(entry.sourceAttachmentPath);

			Object object = resolveResourceOrFile(project, path);
			if (object == null)
				continue;

			if (object instanceof IResource) {
				IResource resource = (IResource) object;

				if (resource instanceof IContainer)
					result.add(new FolderSourceContainer((IContainer) resource,
							true));
				else if (resource instanceof IFile)
					result.add(new ArchiveSourceContainer((IFile) resource,
							false));
			} else {
				java.io.File file = (java.io.File) object;
				if (file.isDirectory())
					result.add(new DirectorySourceContainer(file, true));
				else
					result.add(new ExternalArchiveSourceContainer(file
							.getAbsolutePath(), false));
			}
		}
	}

	public void addSourceRoots(IProject project,
			Map<IPath, IContainer> sourceRootMap) {
		JastAddJBuildConfiguration buildConfiguration = getBuildConfiguration(project);
		if (buildConfiguration != null) {
			for (SourcePathEntry sourcePathEntry : buildConfiguration.sourcePathList) {
				IResource resource = project
						.findMember(sourcePathEntry.sourcePath);
				if (resource == null || !(resource instanceof IContainer))
					continue;
				sourceRootMap
						.put(resource.getFullPath(), (IContainer) resource);
			}
		}
	}


	public void updateBuildConfiguration(IProject project) {
		synchronized (this) {
			if (!hasProgramInfo(project))
				return;

			ProgramInfo programInfo = getProgramInfo(project);
			try {
				programInfo.buildConfiguration = readBuildConfiguration(project);
				reinitProgram(project, programInfo.program,
						programInfo.buildConfiguration);
			} catch (CoreException e) {
				logCoreException(e);
			}
		}
		notifyModelListeners();
	}

	protected void completeBuild(IProject project) {
		// Build a new project from saved files only.
		try {
			try {
				deleteErrorMarkers(ERROR_MARKER_TYPE, project);
				deleteErrorMarkers(PARSE_ERROR_MARKER_TYPE, project);

				JastAddJBuildConfiguration buildConfiguration;
				try {
					buildConfiguration = readBuildConfiguration(project);
				} catch (CoreException e) {
					addErrorMarker(project,
							"Build failed because build configuration could not be loaded: "
									+ e.getMessage(), -1,
							IMarker.SEVERITY_ERROR);
					return;
				}

				IProgram program = initProgram(project, buildConfiguration);

				Map<String, IFile> map = sourceMap(project, buildConfiguration);
				boolean build = true;
				for (Iterator iter = program.compilationUnitIterator(); iter
						.hasNext();) {
					ICompilationUnit unit = (ICompilationUnit) iter.next();

					if (unit.fromSource()) {
						Collection errors = unit.parseErrors();
						Collection warnings = new LinkedList();
						if (errors.isEmpty()) { // only run semantic checks if
							// there are no parse errors
							unit.errorCheck(errors, warnings);
						}
						if (!errors.isEmpty())
							build = false;
						errors.addAll(warnings);
						if (!errors.isEmpty()) {
							for (Iterator i2 = errors.iterator(); i2.hasNext();) {
								Problem error = (Problem) i2.next();
								int line = error.line();
								int column = error.column();
								String message = error.message();
								IFile unitFile = map.get(error.fileName());
								int severity = IMarker.SEVERITY_INFO;
								if (error.severity() == Problem.Severity.ERROR)
									severity = IMarker.SEVERITY_ERROR;
								else if (error.severity() == Problem.Severity.WARNING)
									severity = IMarker.SEVERITY_WARNING;
								if (error.kind() == Problem.Kind.LEXICAL
										|| error.kind() == Problem.Kind.SYNTACTIC) {
									addParseErrorMarker(unitFile, message,
											line, column, severity);
								} else if (error.kind() == Problem.Kind.SEMANTIC) {
									addErrorMarker(unitFile, message, line,
											severity);
								}
							}
						}
						if (build) {
							unit.java2Transformation();
							unit.generateClassfile();
						}
					}
				}

				// Use for the bootstrapped version of JastAdd
				/*
				 * if(build) { program.generateIntertypeDecls();
				 * program.java2Transformation(); program.generateClassfile(); }
				 */

			} catch (CoreException e) {
				addErrorMarker(project, "Build failed because: "
						+ e.getMessage(), -1, IMarker.SEVERITY_ERROR);
				logCoreException(e);
			}
		} catch (Throwable e) {
			logError(e, "Build failed!");
		}
	}

	private void reportBuildError(IProject project, Throwable e) {
		logError(e, "Buld failed!");
		try {
			addErrorMarker(project, "Build failed: " + e.getMessage(), -1,
					IMarker.SEVERITY_ERROR);
		} catch (CoreException ce) {
			logError(ce, "Logging build exception failed!");
		}
	}
	
	protected void updateModel(Collection<IFile> changedFiles, IProject project) {
		JastAddJBuildConfiguration buildConfiguration = getBuildConfiguration(project);
		if (buildConfiguration == null)
			return;
		IProgram program = getProgram(project);
		if (program == null)
			return;
		program.files().clear();
		Map<String,IFile> map = sourceMap(project, buildConfiguration);
		program.files().addAll(map.keySet());
		
		Collection changedFileNames = new ArrayList();
		for(IFile file : changedFiles)
			changedFileNames.add(file.getRawLocation().toOSString());
		
		// remove files already built unless they have changed		
		program.flushSourceFiles(changedFileNames);
		// build new files
		for(Iterator iter = program.files().iterator(); iter.hasNext(); ) {
			String name = (String)iter.next();
			program.addSourceFile(name);
		}
	}

	
	protected void updateModel(IDocument document, String fileName, IProject project) {
		try {
			JastAddJBuildConfiguration buildConfiguration = getBuildConfiguration(project);
			if (buildConfiguration == null)
				return;
			IProgram program = getProgram(project);
			if (program == null)
				return;
			program.files().clear();
			Map<String,IFile> map = sourceMap(project, buildConfiguration);
			program.files().addAll(map.keySet());
	
			Collection changedFileNames = new ArrayList();
			if(fileName != null)
				changedFileNames.add(fileName);
			// remove files already built and the current document from worklist
			program.flushSourceFiles(changedFileNames);
			if(fileName != null)
				program.files().remove(fileName);
	
			// build new files
			for(Iterator iter = program.files().iterator(); iter.hasNext(); ) {
				String name = (String)iter.next();
				program.addSourceFile(name);
			}
			// recover the current document
			StringBuffer buf = new StringBuffer(document.get());
			new JastAddStructureModel(buf).doRecovery(0);
			// build the current document
			program.addSourceFile(fileName, buf.toString());
		} catch (Throwable e) {
			logError(e, "Updatingm model failed!");
		}
	}

	@Override protected IJastAddNode getTreeRootNode(IProject project, String filePath) {
		if(filePath == null)
			return null;
		JastAddJBuildConfiguration buildConfiguration = getBuildConfiguration(project);
		if (buildConfiguration == null)
			return null;
		IProgram program = getProgram(project);
		if (program == null)
			return null;
		for (Iterator iter = program.compilationUnitIterator(); iter.hasNext();) {
			ICompilationUnit cu = (ICompilationUnit) iter.next();
			if (cu.fromSource()) {
				String name = cu.pathName();
				if (name == null)
					System.out.println(cu);
				if (name.equals(filePath))
					return cu;
			}
		}
		return null;
	}
	
	@Override protected void discardTree(IProject project) {
		projectToNodeMap.remove(project);
	}
	
	public void logStatus(IStatus status) {
		JastAddJActivator.INSTANCE.getLog().log(status);
	}

	public IStatus makeErrorStatus(Throwable e, String message) {
		return new Status(IStatus.ERROR, JastAddJActivator.JASTADDJ_PLUGIN_ID,
				IStatus.ERROR, message, e);
	}

	public void resourceChanged(IProject project, IResourceChangeEvent event,
			IResourceDelta delta) {
		checkReloadBuildConfiguration(project, event, delta);
	}

	protected void checkReloadBuildConfiguration(IProject project,
			IResourceChangeEvent event, IResourceDelta delta) {
		switch (event.getType()) {
		case IResourceChangeEvent.POST_CHANGE:
			IResourceDelta newDelta = delta.findMember(new Path(
					JastAddJBuildConfigurationUtil.RESOURCE));
			if (newDelta != null)
				updateBuildConfiguration(project);
			break;
		}
	}

	// ***************** Additional public methods

	public IOutlineNode[] getMainTypes(IProject project) {
		JastAddJBuildConfiguration buildConfiguration = getBuildConfiguration(project);
		if (buildConfiguration == null)
			return null;
		IProgram program = getProgram(project);
		if (program != null) {
			return program.mainTypes();
		}
		return new IOutlineNode[0];
	}

	public JastAddJBuildConfiguration getBuildConfiguration(IProject project) {
		ProgramInfo programInfo = getProgramInfo(project);
		if (programInfo != null)
			return programInfo.buildConfiguration;
		return null;
	}

	public IProgram getProgram(IProject project) {
		ProgramInfo programInfo = getProgramInfo(project);
		if (programInfo != null)
			return programInfo.program;
		return null;
	}

	public IFile getFile(IJastAddNode node) {
		IJastAddNode root = node;
		while (root != null && !(root instanceof ICompilationUnit))
			root = root.getParent();
		if (root == null)
			return null;
		ICompilationUnit compilationUnit = (ICompilationUnit) root;

		IPath path = Path.fromOSString(compilationUnit.pathName());
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
				.findFilesForLocation(path);
		if (files.length == 1)
			return files[0];
		else
			return null;
	}

	public JastAddJBuildConfiguration readBuildConfiguration(IProject project)
			throws CoreException {
		try {
			JastAddJBuildConfiguration buildConfiguration = getEmptyBuildConfiguration();
			doReadBuildConfiguration(project, buildConfiguration);
			return buildConfiguration;
		} catch (Exception e) {
			throw makeCoreException(e, "Loading build configuration failed: "
					+ e.getMessage());
		}
	}

	public void writeBuildConfiguration(IProject project,
			JastAddJBuildConfiguration buildConfiguration) throws CoreException {
		try {
			doWriteBuildConfiguration(project, buildConfiguration);
		} catch (Exception e) {
			throw makeCoreException(e, "Saving build configuration failed: "
					+ e.getMessage());
		}
	}

	public JastAddJBuildConfiguration getDefaultBuildConfiguration() {
		JastAddJBuildConfiguration buildConfiguration = getEmptyBuildConfiguration();
		JastAddJBuildConfigurationUtil.populateDefaults(buildConfiguration);
		return buildConfiguration;
	}

	public void registerStopHandler(Runnable stopHandler) {
		JastAddJActivator.INSTANCE.addStopHandler(stopHandler);
	}

	public IProject getProject(IJastAddNode node) {
		if (node == null)
			return null;
		while (node.getParent() != null) {
			node = node.getParent();
		}
		return nodeToProjectMap.get(node);
	}

	// *************** Protected methods

	protected boolean hasProgramInfo(IProject project) {
		return projectToNodeMap.containsKey(project);
	}

	protected ProgramInfo getProgramInfo(IProject project) {
		if (projectToNodeMap.containsKey(project)) {
			return projectToNodeMap.get(project);
		} else {
			if (isModelFor(project)) {
				try {
					ProgramInfo programInfo = new ProgramInfo();
					programInfo.buildConfiguration = readBuildConfiguration(project);
					programInfo.program = initProgram(project,
							programInfo.buildConfiguration);
					projectToNodeMap.put(project, programInfo);
					nodeToProjectMap.put(programInfo.program, project);
					return programInfo;
				} catch (CoreException e) {
					logError(e, "Initializing program failed!");
					return null;
				} catch (Error e) {
					logError(e, "Initializing program failed!");
					return null;
				}
			}
		}
		return null;
	}

	protected IProgram initProgram(IProject project,
			JastAddJBuildConfiguration buildConfiguration) {
		// Init
		Program program = new Program();
		program.initBytecodeReader(new bytecode.Parser());
		program.initJavaParser(new JavaParser() {
			public CompilationUnit parse(java.io.InputStream is, String fileName)
					throws java.io.IOException, beaver.Parser.Exception {
				return new parser.JavaParser().parse(is, fileName);
			}
		});
		program.initOptions();
		program.addKeyValueOption("-classpath");
		program.addKeyValueOption("-d");
		addBuildConfigurationOptions(project, program, buildConfiguration);
		try {
			Map<String, IFile> map = sourceMap(project, buildConfiguration);
			for (String fileName : map.keySet())
				program.addSourceFile(fileName);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return program;
	}

	protected void reinitProgram(IProject project, IProgram program,
			JastAddJBuildConfiguration buildConfiguration) {
		Program realProgram = (Program) program;

		// TODO: Program options is a static attribute of Program ...

		// Init
		Program.initOptions();
		program.addKeyValueOption("-classpath");
		program.addKeyValueOption("-d");
		addBuildConfigurationOptions(project, realProgram, buildConfiguration);
	}

	protected void addBuildConfigurationOptions(IProject project,
			IProgram program, JastAddJBuildConfiguration buildConfiguration) {
		String projectPath = project.getLocation().toOSString();

		Collection<String> options = new ArrayList<String>();
		if (buildConfiguration != null) {
			List<String> result = buildClassPath(project, buildConfiguration);
			if (result.size() > 0) {
				StringBuffer buffer = new StringBuffer();
				for (String item : result) {
					buffer.append(item);
					buffer.append(File.pathSeparatorChar);
				}
				options.add("-classpath");
				options.add(buffer.toString());
			}
			options.add("-d");
			if (buildConfiguration.outputPath != null)
				options.add(projectPath + File.separator
						+ buildConfiguration.outputPath);
			else
				options.add(projectPath);
		}
		program.addOptions(options.toArray(new String[0]));
	}

	protected Map<String, IFile> sourceMap(IProject project,
			final JastAddJBuildConfiguration buildConfiguration) {
		try {
			final Map<String, IFile> result = new HashMap<String, IFile>();
			for (final SourcePathEntry entry : buildConfiguration.sourcePathList) {
				final IPath sourcePath = new Path(entry.sourcePath);
				final IResource sourceResource = project.findMember(sourcePath);
				if (!(sourceResource instanceof IContainer))
					continue;
				final IContainer sourceContainer = (IContainer) sourceResource;
				final SourcePathMatcher matcher = new SourcePathMatcher(entry);
				sourceContainer.accept(new IResourceVisitor() {
					public boolean visit(IResource resource)
							throws CoreException {
						switch (resource.getType()) {
						case IResource.FILE:
							IFile file = (IFile) resource;
							if (!isModelFor(file))
								break;
							if (!matcher.match(resource))
								break;
							result
									.put(file.getRawLocation().toOSString(),
											file);
							break;
						}
						return true;
					}
				});
			}
			return result;
		} catch (CoreException e) {
			logCoreException(e);
			return null;
		}
	}

	private static class SourcePathMatcher {
		final SourcePathEntry sourcePathEntry;
		char[][] inclusionPatterns;
		char[][] exclusionPatterns;

		SourcePathMatcher(SourcePathEntry sourcePathEntry) {
			this.sourcePathEntry = sourcePathEntry;
			if (sourcePathEntry.includeList.size() > 0) {
				this.inclusionPatterns = new char[sourcePathEntry.includeList
						.size()][];
				for (int i = 0; i < sourcePathEntry.includeList.size(); i++)
					this.inclusionPatterns[i] = normalizePattern(
							sourcePathEntry.includeList.get(i)).toCharArray();
			}
			if (sourcePathEntry.excludeList.size() > 0) {
				this.exclusionPatterns = new char[sourcePathEntry.excludeList
						.size()][];
				for (int i = 0; i < sourcePathEntry.excludeList.size(); i++)
					this.exclusionPatterns[i] = normalizePattern(
							sourcePathEntry.excludeList.get(i)).toCharArray();
			}
		}

		String normalizePattern(Pattern pattern) {
			return new Path(sourcePathEntry.sourcePath).append(pattern.value)
					.toString();
		}

		boolean match(IResource resource) {
			return !Util.isExcluded(resource.getProjectRelativePath(),
					inclusionPatterns, exclusionPatterns, false);
		}
	}

	protected List<String> buildClassPath(IProject project,
			final JastAddJBuildConfiguration buildConfiguration) {
		List<String> result = new ArrayList<String>();
		for (ClassPathEntry classPathEntry : buildConfiguration.classPathList) {
			Path path = new Path(classPathEntry.classPath);
			Object object = resolveResourceOrFile(project, path);
			if (object == null)
				continue;

			if (object instanceof IResource)
				result.add(((IResource) object).getRawLocation().toOSString());
			else
				result.add(((java.io.File) object).getAbsolutePath());
		}
		return result;
	}

	/**
	 * Opens the file corresponding to the given compilation unit with a
	 * selection corresponding to the given line, column and length.
	 */
	protected void openFile(ICompilationUnit unit, int line, int column,
			int length) {
		final String JAVA_FILE_EXT = ".java";
		final String JAR_FILE_EXT = ".jar";
		final String CLASS_FILE_EXT = ".class";

		String pathName = unit.pathName();
		if (pathName.endsWith(CLASS_FILE_EXT)) {
			pathName = pathName.replace(CLASS_FILE_EXT, JAVA_FILE_EXT);
		}
		boolean finishedTrying = false;
		while (!finishedTrying) {
			if (pathName.endsWith(JAVA_FILE_EXT)) {
				try {
					openJavaFile(pathName, line, column, length);
					finishedTrying = true;
				} catch (PartInitException e) {
					finishedTrying = true;
				} catch (URISyntaxException e1) {
					if (pathName.endsWith(JAVA_FILE_EXT)) {
						pathName = pathName.replace(JAVA_FILE_EXT,
								CLASS_FILE_EXT);
					}
				}
			} else if (pathName.endsWith(CLASS_FILE_EXT)
					|| pathName.endsWith(JAR_FILE_EXT)) {
				try {
					openClassFile(pathName, unit.relativeName(), pathName
							.endsWith(JAR_FILE_EXT), line, column, length);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				} catch (PartInitException e) {
					e.printStackTrace();
				}
				finishedTrying = true;
			}
		}
	}

	/**
	 * Opens the file corresponding to the given pathName with a selection
	 * corresponding to line, column and length.
	 */
	protected void openJavaFile(String pathName, int line, int column,
			int length) throws PartInitException, URISyntaxException {
		IPath path = Path.fromOSString(pathName);// URIUtil.toPath(new
													// URI("file:/" +
													// pathName));
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
				.findFilesForLocation(path);
		if (files.length >= 1) {
			IEditorInput targetEditorInput = new FileEditorInput(files[0]);
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();
			page.openEditor(targetEditorInput, getEditorID(), true);
			IDocument targetDoc = fileToDocument(files[0]);
			if (targetDoc == null)
				return;
			int lineOffset = 0;
			try {
				lineOffset = targetDoc.getLineOffset(line - 1) + column - 1;
			} catch (BadLocationException e) {
			}
			IEditorPart targetEditorPart = page.findEditor(targetEditorInput);
			if (targetEditorPart instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor) targetEditorPart;
				textEditor.selectAndReveal(lineOffset, length);
			}
		}
	}

	@Override
	public String getEditorID() {
		return JastAddJEditor.EDITOR_ID;
	}

	@Override
	public String getNatureID() {
		return JastAddJNature.NATURE_ID;
	}

	/**
	 * Opens a class corresponding to the given path name and makes a selection
	 * corresponding to line, column and length.
	 */
	protected void openClassFile(String pathName, String relativeName,
			boolean inJarFile, int line, int column, int length)
			throws PartInitException, URISyntaxException {

		IPath path = Path.fromOSString(pathName);// URIUtil.toPath(new
													// URI("file:/" +
													// pathName));
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
				.findFilesForLocation(path);
		if (files.length > 0) {
			IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();
			IEditorDescriptor desc = PlatformUI.getWorkbench()
					.getEditorRegistry().getDefaultEditor(files[0].getName());
			page.openEditor(new FileEditorInput(files[0]), desc.getId());

			// page.openEditor(targetFileEditorInput,"org.eclipse.jdt.ui.ClassFileEditor",
			// false);
			// "org.eclipse.jdt.ui.CompilationUnitEditor",
			// false);
		}
	}

	protected Object resolveResourceOrFile(IProject project, IPath path) {
		IResource resource;
		if (!path.isAbsolute())
			resource = project.findMember(path);
		else
			resource = project.getWorkspace().getRoot().findMember(path);

		if (resource != null)
			return resource;

		java.io.File file = path.toFile();
		if (file.exists())
			return file;

		return null;
	}

	public Collection recoverCompletion(int documentOffset, String[] linePart,
			StringBuffer buf, IProject project, String fileName,
			IJastAddNode node) throws IOException, Exception {
		if (node == null) {
			// Try a structural recovery
			documentOffset += (new JastAddStructureModel(buf))
					.doRecovery(documentOffset); // Return recovery offset
													// change

			node = findNodeInDocument(project, fileName, new Document(buf
					.toString()), documentOffset - 1);
			if (node == null) {
				System.out.println("Structural recovery failed");
				return new ArrayList();
			}
		}
		if (node instanceof Access) {
			Access n = (Access) node;
			System.out.println("Automatic recovery");
			System.out.println(n.getParent().getParent().dumpTree());
			return n.completion(linePart[1]);
		} else if (node instanceof ASTNode) {
			ASTNode n = (ASTNode) node;
			System.out.println("Manual recovery");
			Expr newNode;
			if (linePart[0].length() != 0) {
				String nameWithParan = "(" + linePart[0] + ")";
				ByteArrayInputStream is = new ByteArrayInputStream(
						nameWithParan.getBytes());
				scanner.JavaScanner scanner = new scanner.JavaScanner(
						new scanner.Unicode(is));
				newNode = (Expr) ((ParExpr) new parser.JavaParser().parse(
						scanner, parser.JavaParser.AltGoals.expression))
						.getExprNoTransform();
				newNode = newNode.qualifiesAccess(new MethodAccess("X",
						new AST.List()));
			} else {
				newNode = new MethodAccess("X", new AST.List());
			}

			int childIndex = n.getNumChild();
			n.addChild(newNode);
			n = n.getChild(childIndex);
			if (n instanceof Access)
				n = ((Access) n).lastAccess();
			// System.out.println(node.dumpTreeNoRewrite());

			// Use the connection to the dummy AST to do name
			// completion
			return n.completion(linePart[1]);
		}
		return new ArrayList();
	}

	protected JastAddJBuildConfiguration getEmptyBuildConfiguration() {
		return new JastAddJBuildConfiguration();
	}

	protected void doReadBuildConfiguration(IProject project,
			JastAddJBuildConfiguration buildConfiguration) throws Exception {
		JastAddJBuildConfigurationUtil.readBuildConfiguration(project,
				buildConfiguration);
	}

	protected void doWriteBuildConfiguration(IProject project,
			JastAddJBuildConfiguration buildConfiguration) throws Exception {
		JastAddJBuildConfigurationUtil.writeBuildConfiguration(project,
				buildConfiguration);
	}
}
