package org.jastadd.plugin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IDocument;

import AST.ASTNode;
import AST.CompilationUnit;
import AST.Program;
import AST.Problem;

public class JastAddModel {

	public JastAddModel() {
		if (instance == null) {
		  JastAddModel.instance = this;
		  jastAddProjects = new ArrayList<JastAddProject>();
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
	 * Finds or creates a related JastAdd project
	 * @param project 
	 * @return The related JastAdd project or null for non JastAdd projects
	 */
	public JastAddProject getJastAddProject(IProject project) {
		// Try to find jastaddproject
		JastAddProject jaProject = null;
		for (Iterator itr = jastAddProjects.iterator(); itr.hasNext();) {
			jaProject = (JastAddProject) itr.next();
			if (jaProject.getProject() == project) {
				break;
			} else {
				jaProject = null;
			}
		}
		// No matching JastAddProject found
		if (jaProject == null) {
			jaProject = JastAddProject.createJastAddProject(project);
			jastAddProjects.add(jaProject);
		}
		return jaProject;
	}

	
	/**
	 * Fully builds a JastAdd project
	 * @param jaProject The project to build
	 */
	public void fullBuild(JastAddProject jaProject) {
		buildProject(jaProject, true);
	}
	
	
	
	/**
	 * Builds a single file without error checks.
	 * @param file
	 * @return The Program node if successful, otherwise null
	 */
	public CompilationUnit buildFile(IFile file) {
		
		// TODO Map this to a compilation unit ...
		
		if (file == null)
			return null;

		// Only build file in JastAdd projects
		JastAddProject jaProject = getJastAddProject(file.getProject());
		if (jaProject == null) {
			return null;
		}
		
		
		String filePath = file.getRawLocation().toOSString();
		Program program = jaProject.getProgram();
		program.flushSourceFiles(filePath);
		program.addOptions(new String[] { filePath });
		//System.out.println("Adding " + filePath);
		
		compileFiles(program);
		
		for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
			CompilationUnit cu = (CompilationUnit)iter.next();
			if(cu.fromSource()) {
				String name = cu.pathName();
				if(name == null)
					System.out.println(cu);
				if(name.equals(filePath))
					return cu;
			}
		}
		throw new Error("Did not manage to build " + filePath);
	}
	
	
	/**
	 * Searches for maintypes within the given project.
	 * @param project the project to search in
	 * @return A String array of main Types
	 */
	/* Moved to JastAddProject
	public ClassDecl[] getMainTypes(IProject project) {
        // Find corresponding jastAddProject
		JastAddProject jaProject = getJastAddProject(project);
		
		// Project is not a JastAdd project
		Program program = null;
		if (jaProject == null) {
			// Try to build anyway?
			program = buildProject(project, false);
		} else {
			buildProject(jaProject, false);
			program = jaProject.getProgramNode();
		}
		return program.mainTypes();
	}
	*/

	public ASTNode findNodeInFile(IFile file, int offset) {
		try {
			IDocument doc = JastAddDocumentProvider.fileToDocument(file);
			int line = doc.getLineOfOffset(offset);
			int column = offset - doc.getLineOffset(line);
			return findNodeInFile(file, line, column);
		} catch (BadLocationException e) {
			return null;
		}
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
		CompilationUnit cu = buildFile(file);

		if (cu == null)
			return null;
		
		ASTNode node = findLocation(cu, line + 1, column + 1);
		return node;
	}

	/**
	 * Returns classpath entries.
	 * @return
	 */
	/* the JastAddProject object will handle build classpaths and user classpaths 
	 * will be contained in the LaunchConfiguration
	 *  
	public String[] getClasspathEntries() {
		String[] res = new String[classpathEntry.size()];
		int i = 0;
		for (Iterator itr = classpathEntry.iterator(); itr.hasNext();i++) {
			res[i] = (String)itr.next();
		}
		return res;
	}
	*/
	
	
   // ---- JastAddProject / Workspace stuff ----
	
   private ArrayList<JastAddProject> jastAddProjects;   
	
	// ---- Build stuff ----
    
	private static JastAddModel instance = null;	
	private static final String ERROR_MARKER_TYPE = "org.jastadd.plugin.marker.ErrorMarker";
	private static final String PARSE_ERROR_MARKER_TYPE = "org.jastadd.plugin.marker.ParseErrorMarker";

	/**
	 * Builds a JastAdd project with or without error checks
	 * @param jaProject The project to build
	 * @param doErrorChecks true of error checks should be made
	 */
	private void buildProject(JastAddProject jaProject, boolean doErrorChecks) {
		Program program = jaProject.getProgram();
		program.flushSourceFiles(null);
		IProject project = jaProject.getProject();
		try {
			HashMap<String, IFile> pathToFileMap = new HashMap<String, IFile>();
			JastAddProject.addSourceFiles(program, project.members(), pathToFileMap);
			deleteParseErrorMarkers(project.members());
			compileFiles(program);
			checkErrors(program, pathToFileMap, doErrorChecks, project);
		} catch (CoreException e) {
			e.printStackTrace();
		}
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
              if(doErrorChecks && errors.isEmpty()) { // only run semantic checks if there are no parse errors
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
	/*
	 * The JastAddProject object will handle build classpaths
	 *
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
	*/
	
	/**
	 * Creates and initializes a Program node.
	 * @return An initialized Program node.
	 */
	/* Moved to JastAddProject 
	 * 
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
	*/
	
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

	/**
	 * Create a dummy file where the active line has been replaced with an empty
	 * stmt.
	 * 
	 * @param document
	 *            The current document.
	 * @param modContent
	 *            The modified content of the document
	 * @return A reference to the dummy file, null if something failed
	 */
	public static IFile createDummyFile(IDocument document, String modContent) {
		// Write modified file content to dummy file
		IFile file = JastAddDocumentProvider.documentToFile(document);
		String fileName = file.getRawLocation().toString();
		String pathName = fileName + DUMMY_SUFFIX; // if this suffix changes then code in Compile.jrag needs to change
		FileWriter w;
		try {
			w = new FileWriter(pathName);
			w.write(modContent, 0, modContent.length());
			w.close();
	
			// Create IFile corresponding to the dummy file
			IPath path = URIUtil.toPath(new URI("file:/" + pathName));
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
					.findFilesForLocation(path);
			if (files.length == 1) {
				return files[0];
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static final String DUMMY_SUFFIX = ".dummy";
}