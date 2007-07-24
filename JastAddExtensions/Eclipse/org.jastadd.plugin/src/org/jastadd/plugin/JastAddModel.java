package org.jastadd.plugin;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;

import AST.ASTNode;
import AST.CompilationUnit;
import AST.JavaParser;
import AST.Program;


public class JastAddModel {

	private static JastAddModel instance = null;
	
	private LinkedList classpathEntry;
	
	public JastAddModel() {
		if (instance == null) {
		  JastAddModel.instance = this;
		  classpathEntry = new LinkedList();
		}
	}
	
	public static JastAddModel getInstance() {
		return JastAddModel.instance;
	}
	
	public ASTNode findNodeInFile(IFile file, int line, int column) {
 		if(file == null)
			return null;
		Program program = new Program();
		program.initBytecodeReader(new bytecode.Parser());
		program.initJavaParser(
				new JavaParser() {
					public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
						return new parser.JavaParser().parse(is, fileName);
					}
				}
		);
		program.initPackageExtractor(new scanner.JavaScanner());
		program.initOptions();
		
		// Add classpaths and filepath 
		program.addKeyValueOption("-classpath");
		IProject project = file.getProject();
		IWorkspace workspace = project.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		String workspacePath = workspaceRoot.getRawLocation().toOSString();			
		String fileFullPath = file.getFullPath().toOSString();
		String projectFullPath = project.getFullPath().toOSString();
		JastAddModel model = JastAddModel.getInstance();
		String[] classpathEntries = model.getClasspathEntries();
		String[] paths = new String[3];
		paths[0] = "-classpath";
		paths[1] = workspacePath;
		paths[1] += ":" + workspacePath + projectFullPath;
		for (int i=0; i <  classpathEntries.length; i++) {
			paths[1] += ":" + classpathEntries[i];
		}
		paths[2] = workspacePath + fileFullPath;
		program.addOptions(paths);
				
		/* Move to save operation
		try {
		  IResource[] filesInProject = file.getProject().members();
		  for(int i = 0; i < filesInProject.length; i++) {
				IResource resource = filesInProject[i];
				if(resource instanceof IFile) {
					IFile resFile = (IFile)resource;
					String resFileName = resFile.getRawLocation().toOSString();
					if (resFileName.endsWith(".java")) {
						  program.addSourceFile(resFileName);
					}		
				}
		  }
		} catch (CoreException e) { }
		*/
		
		try {
			Collection files = program.files();
			for(Iterator iter = files.iterator(); iter.hasNext(); ) {
		        String name = (String)iter.next();
   	            program.addSourceFile(name);
		    }
			
			for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
				CompilationUnit unit = (CompilationUnit)iter.next();
				if(unit.fromSource()) {
					ASTNode node = findLocation(unit, line+1, column+1);
					if(node != null)
						return node;
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	private ASTNode findLocation(ASTNode node, int line, int column) {
		int beginLine = ASTNode.getLine(node.getStart());
		int beginColumn = ASTNode.getColumn(node.getStart());
		int endLine = ASTNode.getLine(node.getEnd());
		int endColumn = ASTNode.getColumn(node.getEnd());

		if(beginLine == 0 && beginColumn == 0 && endLine == 0 && endColumn == 0) {
			for(int i = 0; i < node.getNumChild(); i++) {
				ASTNode result = findLocation(node.getChild(i), line, column);
				if(result != null)
					return result;
			}
			return null;
		}
		
		if((line >= beginLine && line <= endLine) &&
		   (line == beginLine && column >= beginColumn || line != beginLine) &&
		   (line == endLine && column <= endColumn || line != endLine)) {
			for(int i = 0; i < node.getNumChild(); i++) {
				ASTNode result = findLocation(node.getChild(i), line, column);
				if(result != null)
					return result;
			}
			return node;
		}
		return null;
	}
	
	public void addEntryToClasspath(String path) {
		for (Iterator itr = classpathEntry.iterator(); itr.hasNext();) {
			String s = (String)itr.next();
			if (path.equals(s)) {
				return;
			}
		}
		classpathEntry.add(path);
	}
	
	public void removeEntryFromClasspath(String path) {
		for (Iterator itr = classpathEntry.iterator(); itr.hasNext();) {
			String s = (String)itr.next();
			if (path.equals(s)) {
				classpathEntry.remove(s);
				return;
			}    
		}
	}
	
	public String[] getClasspathEntries() {
		String[] res = new String[classpathEntry.size()];
		int i = 0;
		for (Iterator itr = classpathEntry.iterator(); itr.hasNext();i++) {
			res[i] = (String)itr.next();
		}
		return res;
	}

	public boolean compilationUnitInProject(CompilationUnit unit) {
		String relativeName = unit.relativeName();
		String destPathName = unit.destinationPath();
		String pathName = unit.pathName();
		String packageName = unit.packageName();
		
		

		return false;
	}
}