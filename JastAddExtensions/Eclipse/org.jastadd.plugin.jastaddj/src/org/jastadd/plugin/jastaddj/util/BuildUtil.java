package org.jastadd.plugin.jastaddj.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
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
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.jastadd.plugin.compiler.ICompiler;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.jastaddj.Activator;
import org.jastadd.plugin.jastaddj.AST.IProgram;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfigurationUtil;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration.ClassPathEntry;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration.Pattern;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration.SourcePathEntry;
import org.jastadd.plugin.jastaddj.compiler.JastAddJCompiler;

import AST.BytecodeParser;
import AST.CompilationUnit;
import AST.JavaParser;
import AST.Program;

public class BuildUtil {

	
	public static void populateClassPath(IProject project,
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
	

	public static void addSourceRoots(IProject project,
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

	
	public static JastAddJBuildConfiguration getBuildConfiguration(IProject project) {
		ProgramInfo programInfo = getProgramInfo(project);
		if (programInfo != null)
			return programInfo.buildConfiguration;
		return null;
	}

	public static IProgram getProgram(IProject project) {
		ProgramInfo programInfo = getProgramInfo(project);
		if (programInfo != null)
			return programInfo.program;
		return null;
	}


	protected boolean hasProgramInfo(IProject project) {
		return projectToNodeMap.containsKey(project);
	}

	public static ProgramInfo getProgramInfo(IProject project) {
		if (projectToNodeMap.containsKey(project)) {
			return projectToNodeMap.get(project);
		} else {
			for (ICompiler compiler : org.jastadd.plugin.Activator.getRegisteredCompilers()) {
				if (compiler.canCompile(project)) {
					try {
						ProgramInfo programInfo = new ProgramInfo();
						programInfo.buildConfiguration = BuildUtil.readBuildConfiguration(project);
						programInfo.program = initProgram(project,
								programInfo.buildConfiguration);
						projectToNodeMap.put(project, programInfo);
						nodeToProjectMap.put(programInfo.program, project);
						return programInfo;
					} catch (CoreException e) {
						//logError(e, "Initializing program failed!");
						return null;
					} catch (Error e) {
						//logError(e, "Initializing program failed!");
						return null;
					}
				}
			}
		}
		return null;
	}
	
	public static IProgram initProgram(IProject project,
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
			BuildUtil.addBuildConfigurationOptions(project, program, buildConfiguration);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return program;
	}

	protected void reinitProgram(IProject project, IProgram program,
			JastAddJBuildConfiguration buildConfiguration) {
		Program realProgram = (Program) program;
		synchronized (((IASTNode)program).treeLockObject()) {
			// Init
			program.initOptions();
			program.addKeyValueOption("-classpath");
			program.addKeyValueOption("-bootclasspath");
			program.addKeyValueOption("-d");
			if (buildConfiguration != null)
				BuildUtil.addBuildConfigurationOptions(project, realProgram, buildConfiguration);
		}
	}
	
	public static void populateSourceContainers(IProject project,
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

			Object object = FileUtil.resolveResourceOrFile(project, path);
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

	public static IVMInstall getVMInstall(IProject project, JastAddJBuildConfiguration buildConfiguration) {
		return JavaRuntime.getDefaultVMInstall();
	}
	
	protected static List<String> buildClassPath(IProject project,
			final JastAddJBuildConfiguration buildConfiguration) {
		List<String> result = new ArrayList<String>();
		for (ClassPathEntry classPathEntry : buildConfiguration.classPathList) {
			Path path = new Path(classPathEntry.classPath);
			Object object = FileUtil.resolveResourceOrFile(project, path);
			if (object == null)
				continue;

			if (object instanceof IResource)
				result.add(((IResource) object).getRawLocation().toOSString());
			else
				result.add(((java.io.File) object).getAbsolutePath());
		}
		return result;
	}

	protected static List<String> buildBootClassPath(IProject project,
			final JastAddJBuildConfiguration buildConfiguration) {
		List<String> result = new ArrayList<String>();
		
		IVMInstall vm = getVMInstall(project, buildConfiguration);
		LibraryLocation[] libraryLocations = vm.getLibraryLocations();
		if (libraryLocations == null)
			libraryLocations = vm.getVMInstallType().getDefaultLibraryLocations(vm.getInstallLocation());
		
		if (libraryLocations != null) {
			for(LibraryLocation libraryLocation : libraryLocations) {
				IPath path = libraryLocation.getSystemLibraryPath();
				Object object = FileUtil.resolveResourceOrFile(project, path);
				if (object == null) continue;

				if (object instanceof IResource)
					result.add(((IResource) object).getRawLocation().toOSString());
				else
					result.add(((java.io.File) object).getAbsolutePath());
			}
		}
		return result;
	}
	
	public static JastAddJBuildConfiguration readBuildConfiguration(IProject project)
	throws CoreException {
		try {
			JastAddJBuildConfiguration buildConfiguration = getEmptyBuildConfiguration();
			doReadBuildConfiguration(project, buildConfiguration);
			return buildConfiguration;
		} catch (Exception e) {
			String message = "Loading build configuration failed";
			throw new CoreException(new Status(IStatus.ERROR, Activator.JASTADDJ_PLUGIN_ID,
					IStatus.ERROR, message, e));
		}
	}

	public static void writeBuildConfiguration(IProject project,
			JastAddJBuildConfiguration buildConfiguration) throws CoreException {
		try {
			doWriteBuildConfiguration(project, buildConfiguration);
		} catch (Exception e) {
			//throw makeCoreException(e, "Saving build configuration failed: "
			//		+ e.getMessage());
		}
	}

	protected static JastAddJBuildConfiguration getEmptyBuildConfiguration() {
		return new JastAddJBuildConfiguration();
	}

	protected static void doReadBuildConfiguration(IProject project,
			JastAddJBuildConfiguration buildConfiguration) throws Exception {
		JastAddJBuildConfigurationUtil.readBuildConfiguration(project,
				buildConfiguration);
	}

	protected static void doWriteBuildConfiguration(IProject project,
			JastAddJBuildConfiguration buildConfiguration) throws Exception {
		JastAddJBuildConfigurationUtil.writeBuildConfiguration(project,
				buildConfiguration);
	}

	public static JastAddJBuildConfiguration getDefaultBuildConfiguration() {
		JastAddJBuildConfiguration buildConfiguration = getEmptyBuildConfiguration();
		JastAddJBuildConfigurationUtil.populateDefaults(buildConfiguration);
		return buildConfiguration;
	}
	
	public static void addBuildConfigurationOptions(IProject project,
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
		
		// Classpath
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
		synchronized (((IASTNode)program).treeLockObject()) {
			program.addOptions(options.toArray(new String[0]));
		}
	}

	public static Map<String, IFile> sourceMap(IProject project,
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
							for (ICompiler compiler : org.jastadd.plugin.Activator.getRegisteredCompilers()) {
								if (compiler.canCompile(file) && matcher.match(resource))
									result.put(file.getRawLocation().toOSString(), file);
							}
							break;
						}
						return true;
					}
				});
			}
			return result;
		} catch (CoreException e) {
			//logCoreException(e);
			return null;
		}
	}
	
	public void updateBuildConfiguration(IProject project) {
		if (!hasProgramInfo(project))
			return;

		ProgramInfo programInfo = getProgramInfo(project);
		try {
			programInfo.buildConfiguration = BuildUtil.readBuildConfiguration(project);
			reinitProgram(project, programInfo.program,
					programInfo.buildConfiguration);
		} catch (CoreException e) {
			//logCoreException(e);
		}
		//notifyModelListeners();
	}
	
	protected static HashMap<IProject, ProgramInfo> projectToNodeMap = new HashMap<IProject, ProgramInfo>();
	protected static HashMap<IProgram, IProject> nodeToProjectMap = new HashMap<IProgram, IProject>();
	
	public static IProject getProject(IJastAddNode node) {
		if (node == null)
			return null;
		while (node.getParent() != null) {
			node = node.getParent();
		}
		return nodeToProjectMap.get(node);
	}
	

	public static void discardTree(IProject project) {
		projectToNodeMap.remove(project);
	}

	
	public static class ProgramInfo {
		public IProgram program;
		public JastAddJBuildConfiguration buildConfiguration;
		private boolean changed = false;
		private boolean updateView = true;
		public void changed() {
			changed = true;
		}
		public void clearChanges() {
			changed = false;
		}
		public boolean hasChanged() {
			return changed;
		}
		public void updateView(boolean b) {
			updateView = b;
		}
		public boolean getUpdateView() {
			return updateView;
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
}
