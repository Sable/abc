package org.jastadd.plugin.jastaddj.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ArchiveSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ExternalArchiveSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.core.sourcelookup.containers.ZipEntryStorage;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.AST.IOutlineNode;
import org.jastadd.plugin.editor.JastAddStorageEditorInput;
import org.jastadd.plugin.editor.highlight.JastAddColors;
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
import org.jastadd.plugin.jastaddj.editor.highlight.JastAddJScanner;
import org.jastadd.plugin.jastaddj.model.repair.JavaLexer;
import org.jastadd.plugin.jastaddj.nature.JastAddJNature;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.repair.JastAddStructureModel;
import org.jastadd.plugin.model.repair.LexicalNode;
import org.jastadd.plugin.model.repair.Recovery;
import org.jastadd.plugin.model.repair.RecoveryLexer;
import org.jastadd.plugin.model.repair.SOF;

import AST.ASTNode;
import AST.Access;
import AST.CompilationUnit;
import AST.Expr;
import AST.JavaParser;
import AST.MethodAccess;
import AST.ParExpr;
import AST.Problem;
import AST.Program;
import AST.BytecodeParser;

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
		String javaType = "java";
		registerFileType(javaType);
		registerScanner(new JastAddJScanner(new JastAddColors()), javaType);
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
		if(file == null)
			return false;
		for (String str : getFileExtensions()) {
			if (file.getFileExtension() != null && file.getFileExtension().equals(str)) {
				return isModelFor(file.getProject());
			}
		}
		return false;
	}
	
	public boolean isModelFor(FileInfo fileInfo) {
		for (String str : getFileExtensions()) {
			if (fileInfo.getPath().getFileExtension().equals(str)) {
				return isModelFor(fileInfo.getProject());
			}
		}
		return false;
	}

	public boolean isModelFor(IDocument document) {
		return documentToFileInfo(document) != null;
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
		// Populate VM library source containers
		IVMInstall vm = getVMInstall(project, buildConfiguration);
		LibraryLocation[] libraryLocations = vm.getLibraryLocations();
		if (libraryLocations == null)
			libraryLocations = vm.getVMInstallType().getDefaultLibraryLocations(vm.getInstallLocation());
		
		if (libraryLocations != null) {
			for(LibraryLocation libraryLocation : libraryLocations) {
				IPath path = libraryLocation.getSystemLibrarySourcePath();
				if (path == null) continue;
				java.io.File file =  path.toFile();
				if (!file.exists()) continue;
				result.add(new ExternalArchiveSourceContainer(file.getAbsolutePath(), false));
			}
		}
		
		// Populate project files
		result.add(new FolderSourceContainer(project, true));
		
		// Populate source containers		
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

	public IVMInstall getVMInstall(IProject project, JastAddJBuildConfiguration buildConfiguration) {
		return JavaRuntime.getDefaultVMInstall();
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
		//synchronized (this) {
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
		//}
		notifyModelListeners();
	}

	protected void completeBuild(IProject project, IProgressMonitor monitor) {
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
		
				// Parsing source files
				Map<String, IFile> map = sourceMap(project, buildConfiguration);
				
				monitor.beginTask("Building files in project " + project.getName(), 100);
				SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 50);
				subMonitor.beginTask("", map.keySet().size());
				if(map != null) {
					if (monitor != null) {
						monitor.beginTask("Building files in " + project.getName(), map.keySet().size()*3);
					}
					for (String fileName : map.keySet()) {
						program.addSourceFile(fileName);
						subMonitor.worked(1);
					}
				}
				subMonitor.done();
				
				// Check semantics
				boolean build = true;
				subMonitor = new SubProgressMonitor(monitor, 50);
				subMonitor.beginTask("", map.keySet().size()*2);
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
						subMonitor.worked(1);					 
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
						
						// Generate bytecode
						if (build) {
							unit.transformation();
							unit.generateClassfile();
						}
						subMonitor.worked(1);
					}
				}
				subMonitor.done();

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
		} finally {
			monitor.done();
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
			/* Old recovery
			new JastAddStructureModel(buf).doRecovery(0);
			*/
			/* New recovery */
			SOF sof = getRecoveryLexer().parse(buf);
			Recovery.doRecovery(sof);
			buf = Recovery.prettyPrint(sof);
			
			// build the current document
			program.addSourceFile(fileName, buf.toString());
		} catch (Throwable e) {
			logError(e, "Updating model failed!");
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
		program.initBytecodeReader(new BytecodeParser());
		program.initJavaParser(new JavaParser() {
			public CompilationUnit parse(java.io.InputStream is, String fileName)
					throws java.io.IOException, beaver.Parser.Exception {
				return new parser.JavaParser().parse(is, fileName);
			}
		});
		program.options().initOptions();
		try {
			program.addKeyValueOption("-classpath");
			program.addKeyValueOption("-bootclasspath");
			program.addKeyValueOption("-d");
			addBuildConfigurationOptions(project, program, buildConfiguration);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return program;
	}

	protected void reinitProgram(IProject project, IProgram program,
			JastAddJBuildConfiguration buildConfiguration) {
		Program realProgram = (Program) program;

		// Init
		program.initOptions();
		program.addKeyValueOption("-classpath");
		program.addKeyValueOption("-bootclasspath");
		program.addKeyValueOption("-d");
		if (buildConfiguration != null)
			addBuildConfigurationOptions(project, realProgram, buildConfiguration);
	}

	protected void addBuildConfigurationOptions(IProject project,
			IProgram program, JastAddJBuildConfiguration buildConfiguration) {
		Collection<String> options = new ArrayList<String>();
		
		// Boot classpath
		List<String> bootClassPath = buildBootClassPath(project, buildConfiguration);
		if (bootClassPath.size() > 0) {
			StringBuffer buffer = new StringBuffer();
			for (String item : bootClassPath) {
				buffer.append(item);
				buffer.append(File.pathSeparatorChar);
			}
			options.add("-bootclasspath");
			options.add(buffer.toString());
		}
		
		// Claspath
		List<String> classPath = buildClassPath(project, buildConfiguration);
		if (classPath.size() > 0) {
			StringBuffer buffer = new StringBuffer();
			for (String item : classPath) {
				buffer.append(item);
				buffer.append(File.pathSeparatorChar);
			}
			options.add("-classpath");
			options.add(buffer.toString());
		}
		
		// Output path
		String projectPath = project.getLocation().toOSString();			
		options.add("-d");
		if (buildConfiguration.outputPath != null)
			options.add(projectPath + File.separator
					+ buildConfiguration.outputPath);
		else
			options.add(projectPath);
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

	protected List<String> buildBootClassPath(IProject project,
			final JastAddJBuildConfiguration buildConfiguration) {
		List<String> result = new ArrayList<String>();
		
		IVMInstall vm = getVMInstall(project, buildConfiguration);
		LibraryLocation[] libraryLocations = vm.getLibraryLocations();
		if (libraryLocations == null)
			libraryLocations = vm.getVMInstallType().getDefaultLibraryLocations(vm.getInstallLocation());
		
		if (libraryLocations != null) {
			for(LibraryLocation libraryLocation : libraryLocations) {
				IPath path = libraryLocation.getSystemLibraryPath();
				Object object = resolveResourceOrFile(project, path);
				if (object == null) continue;

				if (object instanceof IResource)
					result.add(((IResource) object).getRawLocation().toOSString());
				else
					result.add(((java.io.File) object).getAbsolutePath());
			}
		}
		return result;
	}
	
	protected RecoveryLexer lexer;
	public RecoveryLexer getRecoveryLexer() {
		if (lexer == null) {
			lexer = new JavaLexer();
		}
		return lexer;
	}
	
	
	/**
	 * Opens the file corresponding to the given compilation unit with a
	 * selection corresponding to the given line, column and length.
	 */
	protected void openFile(ICompilationUnit unit, int line, int column,
			int length) {
		try {
			IProject project = getProject(unit);
			
			String pathName = unit.pathName();
			String relativeName = unit.relativeName();
			
			if (project == null || pathName == null || relativeName == null) return;
			
			// Try to work as with resource
			if (pathName.equals(relativeName)) {
				IPath path = Path.fromOSString(pathName);
				IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
				if (files.length > 0 && isModelFor(files[0])) {
					openEditor(new FileEditorInput(files[0]), line, column, length, buildFileInfo(files[0]));
					return;
				}
			}
			
			// Try to work as with class file 
			if (relativeName.endsWith(".class")) {
				openJavaSource(project, computeSourceName(unit), line, column, length);
				return;
			}
			
			// Try to work with source file paths 
			IStorage storage = null;
			
			IPath path = Path.fromOSString(pathName);
			File file = path.toFile();
			if (file.exists()) {
				storage = new LocalFileStorage(file);
			} else {
				IPath rootPath = path.removeTrailingSeparator();
				while (!rootPath.isEmpty() && !rootPath.toFile().exists()) {
					rootPath = rootPath.removeLastSegments(1).removeTrailingSeparator();
				}
				if (!rootPath.isEmpty() && rootPath.toFile().exists()) {
					IPath entryPath = path.removeFirstSegments(rootPath.segmentCount());
					try {
						storage = new ZipEntryStorage(new ZipFile(rootPath.toFile()), new ZipEntry(entryPath.toString()));
					} 
					catch(IOException e) {
						logError(e, "Failed parsing ZIP entry");
					}
				}
			}
			
			if (storage != null)
				openEditor(new JastAddStorageEditorInput(project, storage, this), line, column, length, buildFileInfo(project, storage.getFullPath()));
		}
		catch(CoreException e) {
			logCoreException(e);
		}
	}

	protected void openJavaSource(IProject project, String sourceName, int line, int column, int length) throws CoreException {
		JastAddJBuildConfiguration buildConfiguration = getBuildConfiguration(project);
		if (buildConfiguration != null) {
			List<ISourceContainer> result= new ArrayList<ISourceContainer>();
			popupateSourceContainers(project, buildConfiguration, result);
			
			IEditorInput targetEditorInput = null;
			for(ISourceContainer sourceContainer : result) {
				try {
					Object[] elements = sourceContainer.findSourceElements(sourceName);
					if (elements.length == 1) {
						Object item = elements[0];
						if (item instanceof IFile) {
							IFile file = (IFile)item;
							if (isModelFor(file))
								targetEditorInput = new FileEditorInput((IFile)item);
							else
								targetEditorInput = new JastAddStorageEditorInput(project, new LocalFileStorage(file.getRawLocation().toFile()), this);
						}
						
						if (item instanceof LocalFileStorage)
							targetEditorInput = new JastAddStorageEditorInput(project, (IStorage)item, this);
						
						if (item instanceof ZipEntryStorage)
							targetEditorInput = new JastAddStorageEditorInput(project, (IStorage)item, this);												
						break;
					}
				}
				catch(CoreException e) {
					logCoreException(e);
				}
			}
			
			if (targetEditorInput != null) {
				openEditor(targetEditorInput, line, column, length, buildFileInfo(targetEditorInput));
				return;
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

	private void openEditor(IEditorInput targetEditorInput,
			int line, int column, int length, FileInfo fileInfo)
			throws CoreException {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		page.openEditor(targetEditorInput, getEditorID(), true);
		IDocument targetDoc = fileInfoToDocument(fileInfo);
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
	
	protected String computeSourceName(ICompilationUnit cu) {
		String sourceName = cu.relativeName();
		while (sourceName.endsWith(".class"))
			sourceName = sourceName.substring(0, sourceName.length() - 6);
		if (sourceName.contains("$")) {
			sourceName = sourceName.substring(0, sourceName.indexOf("$"));
		}
		if (!sourceName.endsWith(".java"))
			sourceName = sourceName + ".java";
		return sourceName;
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
			// Try recovery
			
			/* Old recovery 
			documentOffset += (new JastAddStructureModel(buf))
					.doRecovery(documentOffset); // Return recovery offset
													// change
			node = findNodeInDocument(project, fileName, new Document(buf
					.toString()), documentOffset - 1);
			*/
			/* New recovery */
			SOF sof = getRecoveryLexer().parse(buf);
			LexicalNode recoveryNode = Recovery.findNodeForOffset(sof, documentOffset);
			Recovery.doRecovery(sof);
			buf = Recovery.prettyPrint(sof);
			documentOffset += recoveryNode.getInterval().getPushOffset();
			
			node = findNodeInDocument(project, fileName, new Document(buf.toString()), documentOffset - 1);
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
