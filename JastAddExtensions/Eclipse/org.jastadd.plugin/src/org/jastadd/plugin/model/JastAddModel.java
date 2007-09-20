package org.jastadd.plugin.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
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
import org.jastadd.plugin.editor.JastAddEditor;
import org.jastadd.plugin.editor.completion.JastAddCompletionProcessor;
import org.jastadd.plugin.editor.highlight.JastAddAutoIndentStrategy;
import org.jastadd.plugin.editor.highlight.JastAddColors;
import org.jastadd.plugin.editor.highlight.JastAddScanner;
import org.jastadd.plugin.editor.hover.JastAddTextHover;
import org.jastadd.plugin.model.repair.StructureModel;
import org.jastadd.plugin.providers.JastAddContentProvider;
import org.jastadd.plugin.providers.JastAddLabelProvider;
import org.jastadd.plugin.resources.JastAddNature;

import AST.ASTNode;
import AST.ClassDecl;
import AST.CompilationUnit;
import AST.JavaParser;
import AST.Problem;
import AST.Program;

public class JastAddModel {
	
	private JastAddEditorConfiguration editorConfig;
	
	public JastAddModel() {
		editorConfig = new JastAddEditorConfiguration(this);
	}
	
	
    //************************ document to file mapping - belongs to core
	
	private Map<IDocument, IFile> mapDocToFile = new HashMap<IDocument, IFile>();
	private Map<IFile, IDocument> mapFileToDoc = new HashMap<IFile, IDocument>();
	
	public IFile documentToFile(IDocument document) {
		return mapDocToFile.get(document);
	}
	
	public IDocument fileToDocument(IFile file) {
		return mapFileToDoc.get(file);
	}

	public void linkFileToDoc(IFile file, IDocument document) {
		mapDocToFile.put(document, file);
		mapFileToDoc.put(file, document);
	}
	
	
	//************************* model mapping - belongs to core
	
	// Should be abstract in core
	public boolean isModelFor(IProject project) {
		try {
			if (project != null && project.isNatureEnabled(JastAddNature.NATURE_ID)) {
				return true;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}

	// Should be abstract in core
	public boolean isModelFor(IFile file) {
		for (String str : getFileExtensions()) {
			if(file.getFileExtension().equals(str)) {
				return true;
			}
		}
		return false;
	}

	// Should be abstract in core
	public boolean isModelFor(IDocument document) {
		return mapDocToFile.containsKey(document);
	}

	// Should be abstract in core
	public boolean isModelFor(ASTNode node) {
		return getProject(node) != null;
	}


	
	//***************** Listener things -- belongs to core

	private Set<JastAddModelListener> modelListeners = new HashSet<JastAddModelListener>();
	
	private void notifyModelListeners() {
		for(JastAddModelListener listener : modelListeners)
			listener.modelChangedEvent();
	}
	
	public void addListener(JastAddModelListener listener) {
		modelListeners.add(listener);
	}	

	public void removeListener(JastAddModelListener listener) {
		modelListeners.remove(listener);
	}
	

	
	//******************* Find node things -- belongs to core

	// not used but probably belongs here
	public ASTNode findNodeInDocument(IDocument document, int offset) {
		return findNodeInDocument(documentToFile(document), offset);
	}

	public ASTNode findNodeInDocument(IDocument document, int line, int column) {
		return findNodeInDocument(documentToFile(document), line, column);
	}
	
	public ASTNode findNodeInDocument(IFile file, int offset) {
		IProject project = file.getProject();
		String fileName = file.getRawLocation().toOSString();
		IDocument document = fileToDocument(file);
		return findNodeInDocument(project, fileName, document, offset);
	}

	public ASTNode findNodeInDocument(IFile file, int line, int column) {
		IProject project = file.getProject();
		String fileName = file.getRawLocation().toOSString();
		IDocument document = fileToDocument(file);
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

	// If the use of CompilationUnit could be prevented this belongs as is in core otherwise it should be
	// abstract -- if abstract findLocation (which is generic) "can't" be included in core 
	public ASTNode findNodeInDocument(IProject project, String fileName, IDocument document, int line, int column) {
		CompilationUnit cu = buildDocument(document, fileName, project);
		if(cu != null)
			return findLocation(cu, line + 1, column + 1);
		return null;
	}

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

	
	//******************************* Build stuff
	// Use of CompilationUnit prevents it from being included in core -- if ASTNode can be used
	// instead -- ASTNode representing a compilation unit instead of using a specific subclass...
	
	/**
	 * Update the model using the document instead of the compilation unit fileName.
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
	 */
	// Problem with CompilationUnit
	public CompilationUnit getCompilationUnit(IDocument document) {
		IFile file = documentToFile(document);
		if(file != null) {
			return getCompilationUnit(file);
		}
		throw new UnsupportedOperationException("Can only get the current " +
			"compilation unit for a document that belongs to a JastAdd project");
	}

	/**
	 * Get the latest built compilation unit for a project file.
	 * @return The Program node if successful, otherwise null
	 */
	// problem with CompilationUnit
	public CompilationUnit getCompilationUnit(IFile file) {
		try {
			return getCompilationUnit(file.getProject(), file.getRawLocation().toOSString());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// problem with CompilationUnit
	public synchronized CompilationUnit getCompilationUnit(IProject project, String filePath) {
		if(filePath == null)
			return null;
		Program program = getProgram(project);
		if (program == null) 
			return null;
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
	
	// can be included in core as is
	public void updateProjectModel(IDocument document) {
		IFile file = documentToFile(document);
		if(file == null) return;
		String fileName = file.getRawLocation().toOSString();
		updateProjectModel(document, fileName, file.getProject());
	}

	// probably should be abstract in core -- the call to notifyModelListeners() should be enforced 
	public synchronized void updateProjectModel(IDocument document, String fileName, IProject project) {
		Program program = getProgram(project);
		if (program == null)
			return;
		program.files().clear();
		Map<String,IFile> map = sourceMap(project);
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
		
		notifyModelListeners();
	}

	protected final String ERROR_MARKER_TYPE = "org.jastadd.plugin.marker.ErrorMarker";
	protected final String PARSE_ERROR_MARKER_TYPE = "org.jastadd.plugin.marker.ParseErrorMarker";

	/**
	 * Builds a JastAdd project with or without error checks
	 */
	// Should be abstract in core -- 
	public synchronized void fullBuild(IProject project) {
		// Build a new project from saved files only.
		try {
			Program program = getProgram(project);
			if (program == null) 
				return;
			
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
			
			   // Use for the bootstrapped version of JastAdd
			/*
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
	 */
	// As is in core
	protected void addParseErrorMarker(IFile file, String message, int lineNumber, int columnNumber, int severity) {
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
	 */
	// As is in core
	protected void addErrorMarker(IFile file, String message, int lineNumber, int severity) throws CoreException {
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
	 * @return The content of the file as a String object.
	 */
	// As is in core
	protected String readTextFile(String fullPathFilename) throws IOException {
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
	// As is in core
	protected void deleteErrorMarkers(IResource[] resources) throws CoreException {
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
	// As is in core
	protected void deleteParseErrorMarkers(IResource[] resources) throws CoreException {
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

	
	//****************************** Source maps
	
	/**
	 * A map from source files ending with ".java" to IFiles.
	 */
	// Should be in jastAddJ
	private Map<String,IFile> sourceMap(IProject project) {
		HashMap<String,IFile> map = new HashMap<String,IFile>();
		try {
			buildSourceMap(project.members(), map);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return map;
	}
	/**
	 * Populate map with entries mapping java source file names to IFiles. 
	 */
	// Should be in jastAddJ
	private void buildSourceMap(IResource[] resources, Map<String,IFile> sourceMap) throws CoreException {
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


	
	//*********************** Extensions
	
	// Should be abstract in core
	public List<String> getFileExtensions() {
		List<String> list = new ArrayList<String>();
		list.add("java");
		return list;
	}

	// Should be abstract in core
	public String[] getFilterExtensions() {
		return new String[] { ".project", "*.java.dummy", "*.class"};
	}

	public JastAddEditorConfiguration getEditorConfiguration() {
		return editorConfig;
	}
	
	
	// MOVEed to JastAddEditorCOnfiguration
	// TODO redirect all calls to the editorConfig object
	
	//**************************** Indent stuff
	
	public void getDocInsertionAfterNewline(IDocument doc, DocumentCommand cmd) {
		editorConfig.getDocInsertionAfterNewline(doc, cmd);
	}
	
	public void getDocInsertionOnKeypress(IDocument doc, DocumentCommand cmd) {
		editorConfig.getDocInsertionOnKeypress(doc, cmd);
	}	
	
	//********************************* Folding
	
	public ArrayList<Position> getFoldingPositions(IDocument document) {
		return editorConfig.getFoldingPositions(document);
	}
	
	//************************** Editor things
	
	public ITextHover getTextHover() {
		return editorConfig.getTextHover();
	}

	public ITokenScanner getScanner() {
		return editorConfig.getScanner();
	}

	public IAutoEditStrategy getAutoIndentStrategy() {
		return editorConfig.getAutoIndentStrategy();
	}

	public IContentAssistProcessor getCompletionProcessor() {
		return editorConfig.getCompletionProcessor();
	}

	public IContentProvider getContentProvider() {
		return editorConfig.getContentProvider();
	}

	public IBaseLabelProvider getLabelProvider() {
		return editorConfig.getLabelProvider();
	}

		
	
	//********************** method from EditorTools ...
	

	/**
	 * Opens the file from which the given ASTNode origins
	 * @param node The node to look up
	 */
	public void openFile(ASTNode node) {
		int targetLine = node.declarationLocationLine();
		int targetColumn = node.declarationLocationColumn();
		int targetLength = node.declarationLocationLength();
		CompilationUnit cu = node.declarationCompilationUnit();
		openFile(cu, targetLine, targetColumn, targetLength);
	}

	
	public ClassDecl[] getMainTypes(IProject project) {
		Program program  = getProgram(project);
		if (program != null) {
			return 	program.mainTypes();
		}
		return new ClassDecl[0];
	}

	
	/**
	 * Opens the file corresponding to the given compilation unit with a selection
	 * corresponding to the given line, column and length.
	 * @param unit The compilation unit to open.
	 * @param line The line on which to start the selection
	 * @param column The column on which to start the selection
	 * @param length The length of the selection
	 */
	// should be abstract in core
	private void openFile(CompilationUnit unit, int line, int column, int length) {
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
						pathName = pathName.replace(JAVA_FILE_EXT, CLASS_FILE_EXT);
					}
				}
			} else if (pathName.endsWith(CLASS_FILE_EXT) || pathName.endsWith(JAR_FILE_EXT)) {
				try {
					openClassFile(pathName, unit.relativeName(), pathName.endsWith(JAR_FILE_EXT), line, column, length);
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
	 * Opens the file corresponding to the given pathName with a selection corresponding
	 * to line, column and length. 
	 * @param pathName Path to a java file
	 * @param line Line on which to start the selection
	 * @param column Column on which to start the selection 
	 * @param length The length of the selection
	 * @throws PartInitException Thrown when somethings wrong with the path
	 * @throws URISyntaxException Thrown when somethings wrong with the URI syntax of the path
	 */
	// belongs in jastAddJ
	private void openJavaFile(String pathName, int line, int column, int length)
			throws PartInitException, URISyntaxException {
		IPath path = Path.fromOSString(pathName);//URIUtil.toPath(new URI("file:/" + pathName));
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
				.findFilesForLocation(path);
		if (files.length >= 1) {
			IEditorInput targetEditorInput = new FileEditorInput(files[0]);
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();
			page.openEditor(targetEditorInput, JastAddEditor.ID, true);
			IDocument targetDoc = fileToDocument(files[0]);
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

	/**
	 * Opens a class corresponding to the given path name and makes a selection
	 * corresponding to line, column and length.
	 * @param pathName The path to the class
	 * @param relativeName The relative name of the class
	 * @param inJarFile true if the class resides in a jar file
	 * @param line Line on which to start selection
	 * @param column Column on which to start selection
	 * @param length The length of the selection
	 * @throws PartInitException Thrown if somethings wrong with the path
	 * @throws URISyntaxException Thrown if somethings wrong with the URI syntax of the path
	 */
	// belongs in jastAddJ
	private void openClassFile(String pathName, String relativeName,
			boolean inJarFile, int line, int column, int length)
			throws PartInitException, URISyntaxException {
		
		IPath path = Path.fromOSString(pathName);//URIUtil.toPath(new URI("file:/" + pathName));
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

	
	//********************* Mapping Project to Node and initialization of root node -- 
	// belongs in jastAddJ

	private HashMap<IProject,Program> projectToNodeMap = new HashMap<IProject,Program>();
	private HashMap<Program,IProject> nodeToProjectMap = new HashMap<Program,IProject>();

	private Program getProgram(IProject project) {
		if (projectToNodeMap.containsKey(project)) {
			return projectToNodeMap.get(project);
		} else {
			if (isModelFor(project)) {
				Program program = initProgram(project);
				projectToNodeMap.put(project, program);
				nodeToProjectMap.put(program, project);
				return program;
			}
		}
		return null;
	}

	private IProject getProject(ASTNode node) {
		if (node == null)
			return null;
		while (node.getParent() != null) {
			node = node.getParent();
		}
		return nodeToProjectMap.get(node);
	}

	private Program initProgram(IProject project) {
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
			Map<String,IFile> map = sourceMap(project);
			for(String fileName : map.keySet())
				program.addSourceFile(fileName);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return program;	   
	}

}