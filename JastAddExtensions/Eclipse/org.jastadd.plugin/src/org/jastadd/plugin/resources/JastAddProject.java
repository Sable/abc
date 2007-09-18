package org.jastadd.plugin.resources;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.jastadd.plugin.model.JastAddModel;

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
		return project != null ? getProgram().mainTypes() : new ClassDecl[0];
	}
	
	public Program createProgram() {
		return initProgram();
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
		
		IWorkspaceRoot workspaceRoot = project.getWorkspace().getRoot();
		
		String workspacePath = workspaceRoot.getRawLocation().toOSString();			
		String projectFullPath = project.getFullPath().toOSString();
		
		program.addOptions(new String[] { "-classpath", workspacePath + projectFullPath });

		try {
			Map<String,IFile> map = JastAddModel.sourceMap(project);
			for(String fileName : map.keySet())
				program.addSourceFile(fileName);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return program;
	}
}
