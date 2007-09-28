package org.jastadd.plugin.jastadd;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.jastadd.plugin.jastadd.AST.*;
import org.jastadd.plugin.jastaddj.AST.ICompilationUnit;
import org.jastadd.plugin.jastaddj.AST.IProgram;
import org.jastadd.plugin.jastaddj.model.JastAddJModel;

public class Model extends JastAddJModel {

	public boolean isModelFor(IProject project) {
		try {
			if (project != null && project.isNatureEnabled(Nature.NATURE_ID)) {
				return true;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}

	//*************** Protected methods
	

	protected IProgram initProgram(IProject project) {
		Program program = new Program();
		// Init
		program.initBytecodeReader(new BytecodeParser());
		program.initJavaParser(
				new JavaParser() {
					public CompilationUnit parse(java.io.InputStream is, String fileName) 
					throws java.io.IOException, beaver.Parser.Exception {
						return new org.jastadd.plugin.jastadd.parser.JavaParser().parse(is, fileName);
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
			Map<String,IFile> map = sourceMap(project);
			for(String fileName : map.keySet())
				program.addSourceFile(fileName);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return program;	   
	}
	
	protected void completeBuild(IProject project) {
		// Build a new project from saved files only.
		try {
			Program program = (Program)initProgram(project);
			if (program == null) 
				return;
			
			deleteParseErrorMarkers(project.members());
			deleteErrorMarkers(project.members());
			
			Map<String,IFile> map = sourceMap(project);
			boolean build = true;
			for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
			    ICompilationUnit unit = (ICompilationUnit)iter.next();
			    
			    if(unit.fromSource()) {
			      Collection errors = unit.parseErrors();
			      Collection warnings = new LinkedList();
			      if(errors.isEmpty()) { // only run semantic checks if there are no parse errors
			        unit.errorCheck(errors, warnings);
			      }
			      if(!errors.isEmpty())
			    	  build = false;
			      errors.addAll(warnings);
			      if(!errors.isEmpty()) {
			    	  for(Iterator i2 = errors.iterator(); i2.hasNext(); ) {
			    		  Problem error = (Problem)i2.next();
			    		  int line = error.line();
			    		  int column = error.column();
			    		  String message = error.message();
			    		  IFile unitFile = map.get(error.fileName());
			    		  int severity = IMarker.SEVERITY_INFO;
			    		  if(error.severity() == Problem.Severity.ERROR)
			    			  severity = IMarker.SEVERITY_ERROR;
			    		  else if(error.severity() == Problem.Severity.WARNING)
			    			  severity = IMarker.SEVERITY_WARNING;
			    		  if(error.kind() == Problem.Kind.LEXICAL || error.kind() == Problem.Kind.SYNTACTIC) {
			    			  addParseErrorMarker(unitFile, message, line, column, severity);
			    		  }
			    		  else if(error.kind() == Problem.Kind.SEMANTIC) {
			        		  addErrorMarker(unitFile, message, line, severity);
			    		  }
			    	  }
			      }
			      if(build) {
			    	  //unit.java2Transformation();
			    	  //unit.generateClassfile();
			      }
			    }
			}
			
			   // Use for the bootstrapped version of JastAdd
			
			if(build) {
				program.generateIntertypeDecls();
				program.java2Transformation();
				program.generateClassfile();
			}
			
			
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
