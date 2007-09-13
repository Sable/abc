package org.jastadd.plugin;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

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
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ISelection;

import AST.ASTNode;
import AST.CompilationUnit;
import AST.Problem;
import AST.Program;

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
	 * Update the model using the document instead of the compilation unit fileName.
	 * @param document
	 * @param fileName
	 * @param project
	 * @return
	 */
	public synchronized CompilationUnit buildDocument(IDocument document, String fileName, IProject project) {
		try {
			updateProjectModel(document, fileName, project);
			return getCompilationUnit(project, fileName);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get the latest built compilation unit for a project document.
	 * @param document
	 * @return
	 */
	public CompilationUnit getCompilationUnit(IDocument document) {
		IFile file = JastAddDocumentProvider.documentToFile(document);
		if(file != null) {
			return getCompilationUnit(file);
		}
		throw new UnsupportedOperationException("Can only get the current " +
			"compilation unit for a document that belongs to a JastAdd project");
	}

	/**
	 * Get the latest built compilation unit for a project file.
	 * @param file
	 * @return The Program node if successful, otherwise null
	 */
	public CompilationUnit getCompilationUnit(IFile file) {
		try {
			return getCompilationUnit(file.getProject(), file.getRawLocation().toOSString());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public synchronized CompilationUnit getCompilationUnit(IProject project, String filePath) {
		if(filePath == null)
			return null;
		JastAddProject jaProject = getJastAddProject(project);
		if (jaProject == null) {
			return null;
		}

		Program program = jaProject.getProgram();
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
		return null;
	}
	
	public void updateProjectModel(IDocument document) {
		IFile file = JastAddDocumentProvider.documentToFile(document);
		if(file == null) return;
		
		String fileName = file.getRawLocation().toOSString();

		updateProjectModel(document, fileName, file.getProject());
	}
	
	public synchronized void updateProjectModel(IDocument document, String fileName, IProject project) {
		JastAddProject jaProject = getJastAddProject(project);
		if (jaProject == null) return;

		Program program = jaProject.getProgram();
		program.files().clear();
		Map<String,IFile> map = JastAddModel.sourceMap(project);
		program.files().addAll(map.keySet());
		// remove files already built and the current document from worklist
		program.flushSourceFiles(fileName);
		// build new files
		for(Iterator iter = program.files().iterator(); iter.hasNext(); ) {
			String name = (String)iter.next();
			program.addSourceFile(name);
		}
		// recover the current document
		StringBuffer buf = new StringBuffer(document.get());
		new StructureModel(buf).doRecovery(0);
		// build the current document
		program.addSourceFile(fileName, buf.toString());
	}
	
	public ASTNode findNodeInDocument(IDocument document, int offset) {
		return findNodeInDocument(JastAddDocumentProvider.documentToFile(document), offset);
	}

	public ASTNode findNodeInDocument(IDocument document, int line, int column) {
		return findNodeInDocument(JastAddDocumentProvider.documentToFile(document), line, column);
	}
	
	public ASTNode findNodeInDocument(IFile file, int offset) {
		IProject project = file.getProject();
		String fileName = file.getRawLocation().toOSString();
		IDocument document = JastAddDocumentProvider.fileToDocument(file);
		return findNodeInDocument(project, fileName, document, offset);
	}

	public ASTNode findNodeInDocument(IFile file, int line, int column) {
		IProject project = file.getProject();
		String fileName = file.getRawLocation().toOSString();
		IDocument document = JastAddDocumentProvider.fileToDocument(file);
		return findNodeInDocument(project, fileName, document, line, column);
	}

	public ASTNode findNodeInDocument(IProject project, String fileName, IDocument document, int offset) {
		try {
			int line = document.getLineOfOffset(offset);
			int column = offset - document.getLineOffset(line);
			return findNodeInDocument(project, fileName, document, line, column);
		} catch (BadLocationException e) {
			return null;
		}
	}

	public ASTNode findNodeInDocument(IProject project, String fileName, IDocument document, int line, int column) {
		CompilationUnit cu = buildDocument(document, fileName, project);
		if(cu != null)
			return findLocation(cu, line + 1, column + 1);
		return null;
	}
	
	
   // ---- JastAddProject / Workspace stuff ----
	
   private ArrayList<JastAddProject> jastAddProjects;   
	
	// ---- Build stuff ----
    
	private static JastAddModel instance = null;	
	private static final String ERROR_MARKER_TYPE = "org.jastadd.plugin.marker.ErrorMarker";
	private static final String PARSE_ERROR_MARKER_TYPE = "org.jastadd.plugin.marker.ParseErrorMarker";

	/**
	 * Builds a JastAdd project with or without error checks
	 * @param jaProject The project to build
	 */
	public synchronized void fullBuild(JastAddProject jaProject) {
		// Build a new project from saved files only.
		try {
			Program program = jaProject.createProgram();
			IProject project = jaProject.getProject();

			deleteParseErrorMarkers(project.members());
			deleteErrorMarkers(project.members());
			
			Map<String,IFile> map = sourceMap(project);
			boolean build = true;
			for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
			    CompilationUnit unit = (CompilationUnit)iter.next();
			    
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
			    	  unit.java2Transformation();
			    	  unit.generateClassfile();
			      }
			    }
			}
			/*
			   // Use for the bootstrapped version of JastAdd
			if(build) {
				program.generateIntertypeDecls();
				program.java2Transformation();
				program.generateClassfile();
			}
			*/
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (Throwable e) {
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
		if (file != null) {
			IMarker marker = file.createMarker(ERROR_MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		}
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

	public void getDocInsertionAfterNewline(IDocument doc, DocumentCommand cmd) {
		StringBuffer buf = new StringBuffer(doc.get());
		try {
			StructureModel structModel = new StructureModel(buf);
			int change = structModel.doRecovery(cmd.offset);
			structModel.insertionAfterNewline(doc, cmd, change);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getDocInsertionOnKeypress(IDocument doc, DocumentCommand cmd) {
		char c = cmd.text.charAt(0);
		
		String content = doc.get();
		char previousKeypress = content.charAt(cmd.offset-1);
		
		if (StructureModel.OPEN_PARAN == c) {
		    cmd.caretOffset = cmd.offset + 1;
		    cmd.shiftsCaret = false;
			cmd.text += String.valueOf(StructureModel.CLOSE_PARAN);
		} else if ('[' == c) {
		    cmd.caretOffset = cmd.offset + 1;
		    cmd.shiftsCaret = false;
			cmd.text += "]";
		} else if (StructureModel.CLOSE_PARAN == c && previousKeypress == StructureModel.OPEN_PARAN) {
			cmd.text = "";
			cmd.caretOffset = cmd.offset + 1;
		} else if (']' == c &&  previousKeypress == '[') {
			cmd.text = "";
			cmd.caretOffset = cmd.offset + 1;
		} else if ('"' == c) {
			if (previousKeypress != '"') {	
				cmd.caretOffset = cmd.offset + 1;
				cmd.shiftsCaret = false;
				cmd.text += '"';
			} else {
				cmd.text = "";
				cmd.caretOffset = cmd.offset + 1;
			}
		} else if (StructureModel.CLOSE_BRACE == c) {
		
			StringBuffer buf = new StringBuffer(doc.get());
			try {
				StructureModel structModel = new StructureModel(buf);
				int change = structModel.doRecovery(cmd.offset);
				structModel.insertionCloseBrace(doc, cmd, change);
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		previousKeypress = c;
	}

	public ArrayList<Position> getFoldingPositions(IDocument document) {
		try {
		CompilationUnit cu = getCompilationUnit(document);
		if (cu != null) {
			return cu.foldingPositions(document);
		}
		} catch (Exception e) {
			
		}
		return new ArrayList<Position>();
	}

	/**
	 * A map from source files ending with ".java" to IFiles.
	 * @param resources
	 * @return
	 * @throws CoreException
	 */
	public static Map<String, IFile> sourceMap(IProject project) {
		HashMap<String, IFile> map = new HashMap<String,IFile>();
		try {
			JastAddModel.buildSourceMap(project.members(), map);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return map;
	}
	/**
	 * Populate map with entries mapping java source file names to IFiles. 
	 * @param resources
	 * @param sourceMap
	 * @throws CoreException
	 */
	public static void buildSourceMap(IResource[] resources, Map<String,IFile> sourceMap) throws CoreException {
		for (int i = 0; i < resources.length; i++) {
			IResource res = resources[i];
			if (res instanceof IFile && res.getName().endsWith(".java")) {
				IFile file = (IFile) res;
				String fileName = file.getRawLocation().toOSString();
				sourceMap.put(fileName, file);
			} else if (res instanceof IFolder) {
				IFolder folder = (IFolder) res;
				buildSourceMap(folder.members(), sourceMap);
			}
		}
	}
}