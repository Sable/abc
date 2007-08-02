package org.jastadd.plugin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;

import AST.ASTNode;
import AST.CompilationUnit;
import AST.JavaParser;
import AST.Program;
import AST.Problem;

public class JastAddModel {

	
	public JastAddModel() {
		if (instance == null) {
		  JastAddModel.instance = this;
		  classpathEntry = new LinkedList();
		}
	}	
	
	
	/**
	 * Returns a references to the JastAddModel
	 * @return
	 */
	public static JastAddModel getInstance() {
		return JastAddModel.instance;
	}
	
	/**
	 * Builds all files ending with ".java" in the given project
	 * @param project
	 */
	public void fullBuild(IProject project) {
		buildProject(project, true);
	}

	/**
	 * Builds a single file without error checks.
	 * @param file
	 * @return The Program node if successful, otherwise null
	 */
	public Program buildFile(IFile file) {

		if (file == null)
			return null;

		Program program = initProgram();
		fillInClasspaths(program, file.getProject());

		String filePath = file.getRawLocation().toOSString();
		program.addOptions(new String[] { filePath });
		compileFiles(program);
		return program;
	}

	/**
	 * Finds an ASTNode which corresponds to the position in the given IFile.
	 * 
	 * @param file
	 * @param line
	 * @param column
	 * @return An ASTNode or null if no node was found.
	 */
	public ASTNode findNodeInFile(IFile file, int line, int column) {

		if (file == null)
			return null;

		//Program program = buildProject(file.getProject(), false);
		Program program = buildFile(file);

		if (program == null)
			return null;		

		String rawFilePath = file.getRawLocation().toOSString();
		for (Iterator iter = program.compilationUnitIterator(); iter.hasNext();) {
			CompilationUnit unit = (CompilationUnit) iter.next();
			if(unit.fromSource()) {
				String pathName = unit.pathName();
				if (rawFilePath.equals(pathName)) {
					ASTNode node = findLocation(unit, line + 1, column + 1);
					if (node != null)
						return node;
				}
			}
		}

		return null;
	}

	/**
	 * Returns classpath entries.
	 * @return
	 */
	public String[] getClasspathEntries() {
		String[] res = new String[classpathEntry.size()];
		int i = 0;
		for (Iterator itr = classpathEntry.iterator(); itr.hasNext();i++) {
			res[i] = (String)itr.next();
		}
		return res;
	}

	
	
	
	
	/* 
	 * In case handling of classpaths will be included..
	 * 
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
	*/
	
	

	
	
	
    
	private LinkedList classpathEntry;
	
	private static JastAddModel instance = null;	
	private static final String ERROR_MARKER_TYPE = "org.jastadd.plugin.marker.ErrorMarker";
	private static final String PARSE_ERROR_MARKER_TYPE = "org.jastadd.plugin.marker.ParseErrorMarker";


	/**
	 * Builds all files in the given project with  or without errors checks.
	 * @param project
	 * @param doErrorChecks true if errors checks should be included
	 * @return A Program node or null if the build was unsuccessfull
	 */
	private Program buildProject(IProject project, boolean doErrorChecks) {
	     if (project == null)
	    	 return null;     
	     Program program = initProgram();
	     fillInClasspaths(program, project);
	     try {
	    	HashMap<String,IFile> pathToFileMap = new HashMap<String,IFile>();
			addSourceFiles(program, project.members(), pathToFileMap);
			deleteParseErrorMarkers(project.members()); 
			compileFiles(program);
			checkErrors(program, pathToFileMap, doErrorChecks, project);
			return program;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Adds a line marker, with a message, to a file.
	 * @param file
	 * @param message
	 * @param lineNumber
	 * @param columnNumber
	 * @param severity
	 */
	private void addParseErrorMarker(IFile file, String message, int lineNumber, int columnNumber, int severity) {
		try {
			IMarker marker = file.createMarker(PARSE_ERROR_MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			DefaultLineTracker t = new DefaultLineTracker();
			t.set(readTextFile(file.getRawLocation().toOSString()));
			int offset = t.getLineOffset(lineNumber-1);
			offset += columnNumber - 1;
			marker.setAttribute(IMarker.CHAR_START, offset);
			marker.setAttribute(IMarker.CHAR_END, offset+1);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		} catch (IOException e) {
		} catch (BadLocationException e) {
		}
	}
	
	/**
	 * Adds a line marker with a message to a file.
	 * @param file
	 * @param message
	 * @param lineNumber
	 * @param severity
	 * @throws CoreException
	 */
	
	private void addErrorMarker(IFile file, String message, int lineNumber, int severity) throws CoreException {
		IMarker marker = file.createMarker(ERROR_MARKER_TYPE);
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.SEVERITY, severity);
		if (lineNumber == -1) {
			lineNumber = 1;
		}
		marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
	}
	
	/** 
	 * Reads a text file from disk.
	 * @param fullPathFilename
	 * @return The content of the file as a String object.
	 * @throws IOException
	 */
	private static String readTextFile(String fullPathFilename) throws IOException {
		StringBuffer sb = new StringBuffer(1024);
		BufferedReader reader = new BufferedReader(new FileReader(fullPathFilename));
				
		char[] chars = new char[1024];
		while(reader.read(chars) > -1){
			sb.append(String.valueOf(chars));	
		}
		reader.close();
		return sb.toString();
	}
	
	
	/**
	 * Delete markers from all files.
	 * @param resources Array corresponding to files in a project or a folder.
	 * @throws CoreException
	 */
	private void deleteErrorMarkers(IResource[] resources) throws CoreException {
		for (int i = 0; i < resources.length; i++) {
			IResource res = resources[i];
			if (res instanceof IFile && res.getName().endsWith(".java")) {
				IFile file = (IFile) res;
				file.deleteMarkers(ERROR_MARKER_TYPE, false, IResource.DEPTH_ZERO);
			} else if (res instanceof IFolder) {
			    IFolder folder = (IFolder) res;
			    deleteErrorMarkers(folder.members());
		   }
		}
	}
	
	/**
	 * Delete markers from all files.
	 * @param resources Array corresponding to files in a project or a folder.
	 * @throws CoreException
	 */
	private void deleteParseErrorMarkers(IResource[] resources) throws CoreException {
		for (int i = 0; i < resources.length; i++) {
			IResource res = resources[i];
			if (res instanceof IFile && res.getName().endsWith(".java")) {
				IFile file = (IFile) res;
				file.deleteMarkers(PARSE_ERROR_MARKER_TYPE, false, IResource.DEPTH_ZERO);
			} else if (res instanceof IFolder) {
			    IFolder folder = (IFolder) res;
			    deleteParseErrorMarkers(folder.members());
		   }
		}
	}
	
	
	/**
	 * Checks for errors in a Program node
	 * @param program
	 */
	private void checkErrors(Program program, HashMap<String,IFile> pathToFileMap, boolean doErrorChecks, IProject project) throws CoreException {
		if (doErrorChecks) {
			 deleteErrorMarkers(project.members());
		}
		for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
            CompilationUnit unit = (CompilationUnit)iter.next();
            
            if(unit.fromSource()) {
            	
              Collection errors = unit.parseErrors();
              Collection warnings = new LinkedList();
              if(doErrorChecks) {
                unit.errorCheck(errors, warnings);
              }
              boolean build = errors.isEmpty() && doErrorChecks;
              errors.addAll(warnings);
              if(!errors.isEmpty()) {
            	  for(Iterator i2 = errors.iterator(); i2.hasNext(); ) {
            		  Problem error = (Problem)i2.next();
            		  int line = error.line();
            		  int column = error.column();
            		  String message = error.message();
            		  IFile unitFile = pathToFileMap.get(error.fileName());
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
            	  unit.java2Transformation();
            	  unit.generateClassfile();
              }
            }
		}
	}
	
	/**
	 * Adds source files ending with ".java" to the given Program node. 
	 * @param program
	 * @param resources Array corresponding to the members of a project or folder.
	 * @throws CoreException
	 */
	private void addSourceFiles(Program program, IResource[] resources, HashMap<String,IFile> pathToFileMap)
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
	
	/**
	 * Compiles all files added to the Program object.
	 * @param program
	 * @param pathToFileMap
	 */
	private void compileFiles(Program program) {
		Collection files = program.files();
		try {
			for (Iterator iter = files.iterator(); iter.hasNext();) {
				String name = (String) iter.next();
				program.addSourceFile(name);
			}
		} 
		catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
	}

	/**
	 * Adds classpaths based on the given project to the given Program node.
	 * @param program
	 * @param project
	 */
	private void fillInClasspaths(Program program, IProject project) {
		
		program.addKeyValueOption("-classpath");
		//program.addKeyOption("-verbose");
		//program.addOptions(new String[] { "-verbose" });
		
		IWorkspace workspace = project.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		
		String workspacePath = workspaceRoot.getRawLocation().toOSString();			
		String projectFullPath = project.getFullPath().toOSString();
		
		String[] paths = new String[2];
		paths[0] = "-classpath";
		paths[1] = workspacePath;
		paths[1] += ":" + workspacePath + projectFullPath;
		for (Iterator itr = classpathEntry.iterator(); itr.hasNext();) {
			paths[1] += ":" + (String)itr.next();
		}
		
		program.addOptions(paths);	
	}
	
	/**
	 * Creates and initializes a Program node.
	 * @return An initialized Program node.
	 */
	private Program initProgram() {
		
		Program program = new Program();
		
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
        
		return program;
	}
	
	
	/**
	 * Find location of a node in an abstract syntax tree based on the line and column
	 * provided by an editor.
	 * @param node
	 * @param line
	 * @param column
	 * @return A node on with the right line and column or null
	 */
	private ASTNode findLocation(ASTNode node, int line, int column) {
		if(node == null) return node;
		int beginLine = ASTNode.getLine(node.getStart());
		int beginColumn = ASTNode.getColumn(node.getStart());
		int endLine = ASTNode.getLine(node.getEnd());
		int endColumn = ASTNode.getColumn(node.getEnd());

		if(beginLine == 0 && beginColumn == 0 && endLine == 0 && endColumn == 0) {
			for(int i = 0; i < node.getNumChild(); i++) {
				if(node.getChild(i) != null) {
					ASTNode result = findLocation(node.getChild(i), line, column);
					if(result != null)
						return result;
				}
			}
			return null;
		}
		
		if((line >= beginLine && line <= endLine) &&
		   (line == beginLine && column >= beginColumn || line != beginLine) &&
		   (line == endLine && column <= endColumn || line != endLine)) {
			for(int i = 0; i < node.getNumChild(); i++) {
				if(node.getChild(i) != null) {
					ASTNode result = findLocation(node.getChild(i), line, column);
					if(result != null)
						return result;
				}
			}
			return node;
		}
		return null;
	}
}