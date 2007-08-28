package org.jastadd.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.jastadd.plugin.builder.JastAddNature;

import AST.ClassDecl;
import AST.CompilationUnit;
import AST.JavaParser;
import AST.Program;

public class JastAddProject {
	
	private IProject project;
	private Program program;
	
	private JastAddProject(IProject project) {
		this.project = project;
		this.program = null;	
	}
	
	/**
	 * Factory method for creation of JastAddProjects
	 * @param project
	 * @return A JastAddProject object for projects with JastAdd nature, otherwise null
	 */
	public static JastAddProject createJastAddProject(IProject project) {
		try {
			if (project != null && project.isNatureEnabled(JastAddNature.NATURE_ID)) {
				return new JastAddProject(project);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public IProject getProject() {
		return project;
	}

	public Program getProgram() {
		if (program == null) {
			program = initProgram();
		}
		return program;
	}

	public ClassDecl[] getMainTypes() {
		if (project != null) {
		  if (program == null) {
			  JastAddModel.getInstance().fullBuild(this);
		  }
		  return program.mainTypes();
		}
		return new ClassDecl[0];
	}
	
	
	
	
	private Program initProgram() {
        Program program = new Program();
		
        // Init
		program.initBytecodeReader(new bytecode.Parser());
		program.initJavaParser(
				new JavaParser() {
					public CompilationUnit parse(java.io.InputStream is, String fileName) 
					  throws java.io.IOException, beaver.Parser.Exception {
						return new parser.JavaParser().parse(is, fileName);
					}
				}
		);
		program.initOptions();
		
		// Add project classpath
		program.addKeyValueOption("-classpath");
		//program.addKeyOption("-verbose");
		//program.addOptions(new String[] { "-verbose" });
		
		IWorkspace workspace = project.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		
		String workspacePath = workspaceRoot.getRawLocation().toOSString();			
		String projectFullPath = project.getFullPath().toOSString();
		
		String[] paths = new String[2];
		paths[0] = "-classpath";
		paths[1] = workspacePath + projectFullPath;
		
		program.addOptions(paths);
		
		try {
			HashMap<String, IFile> pathToFileMap = new HashMap<String, IFile>();
			addSourceFiles(program, project.members(), pathToFileMap);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		
		return program;
	}

	/**
	 * Adds source files ending with ".java" to the given Program node. 
	 * @param program
	 * @param resources Array corresponding to the members of a project or folder.
	 * @throws CoreException
	 */
	public static void addSourceFiles(Program program, IResource[] resources, HashMap<String,IFile> pathToFileMap)
			throws CoreException {
		
		List<String> fileList = new ArrayList<String>();
		
		for (int i = 0; i < resources.length; i++) {
			IResource res = resources[i];
			if (res instanceof IFile && res.getName().endsWith(".java")) {
				IFile file = (IFile) res;
				String filePath = file.getRawLocation().toOSString();
				fileList.add(filePath);
				pathToFileMap.put(filePath, file);
				
			} else if (res instanceof IFolder) {
				IFolder folder = (IFolder) res;
				addSourceFiles(program, folder.members(), pathToFileMap);
			}
		}
		
		Object[] tmpObjs = fileList.toArray();
		String[] stringObjs = new String[tmpObjs.length];
		for (int k = 0; k < tmpObjs.length; k++) {
			stringObjs[k] = (String) tmpObjs[k];
		}
		program.addOptions(stringObjs);
	}

}
