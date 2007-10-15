package org.jastadd.plugin.jastaddj.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
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
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.AST.IOutlineNode;
import org.jastadd.plugin.jastaddj.AST.ICompilationUnit;
import org.jastadd.plugin.jastaddj.AST.IProgram;
import org.jastadd.plugin.jastaddj.AST.IJastAddJFindDeclarationNode;
import org.jastadd.plugin.jastaddj.editor.JastAddJEditor;
import org.jastadd.plugin.jastaddj.nature.JastAddJNature;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.repair.JastAddStructureModel;

import parser.JavaParser.AltGoals;
import scanner.JavaScanner;
import scanner.Unicode;
import beaver.Parser.Exception;

import AST.*;

public class JastAddJModel extends JastAddModel {

	protected HashMap<IProject,IProgram> projectToNodeMap = new HashMap<IProject,IProgram>();
	protected HashMap<IProgram,IProject> nodeToProjectMap = new HashMap<IProgram,IProject>();

	
	// ************* Overridden methods
	
	@Override
	protected void initModel() {
		editorConfig = new JastAddJEditorConfiguration(this);
	}

	
	// ************** Implementations of abstract methods
	
	public boolean isModelFor(IProject project) {
		try {
			if (project != null && project.isNatureEnabled(JastAddJNature.NATURE_ID)) {
				return true;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean isModelFor(IFile file) {
		for (String str : getFileExtensions()) {
			if(file.getFileExtension().equals(str)) {
				return isModelFor(file.getProject());
			}
		}
		return false;
	}

	public boolean isModelFor(IDocument document) {
		return documentToFile(document) != null;
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
		return new String[] { ".project", "*.java.dummy", "*.class"};
	}

	
	public void openFile(IJastAddNode node) {
		if (node instanceof IJastAddJFindDeclarationNode) {
			IJastAddJFindDeclarationNode n = (IJastAddJFindDeclarationNode)node;
			int targetLine = n.selectionLine();
			int targetColumn = n.selectionColumn();
			int targetLength = n.selectionLength();
			ICompilationUnit cu = n.declarationCompilationUnit();
			openFile(cu, targetLine, targetColumn, targetLength);
		}
	}

	protected void completeBuild(IProject project) {
		// Build a new project from saved files only.
		try {
			IProgram program = initProgram(project);
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
	

	protected void updateModel(IDocument document, String fileName, IProject project) {
		IProgram program = getProgram(project);
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
		new JastAddStructureModel(buf).doRecovery(0);
		// build the current document
		program.addSourceFile(fileName, buf.toString());		
	}

	
	protected IJastAddNode getTreeRootNode(IProject project, String filePath) {
		if(filePath == null)
			return null;
		IProgram program = getProgram(project);
		if (program == null) 
			return null;
		for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
			ICompilationUnit cu = (ICompilationUnit)iter.next();
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

	
	// ***************** Additional public methods

	public IOutlineNode[] getMainTypes(IProject project) {
		IProgram program  = getProgram(project);
		if (program != null) {
			return 	program.mainTypes();
		}
		return new IOutlineNode[0];
	}

	
	//*************** Protected methods
	

	protected IProgram getProgram(IProject project) {
		if (projectToNodeMap.containsKey(project)) {
			return projectToNodeMap.get(project);
		} else {
			if (isModelFor(project)) {
				IProgram program = initProgram(project);
				projectToNodeMap.put(project, program);
				nodeToProjectMap.put(program, project);
				return program;
			}
		}
		return null;
	}

	protected IProject getProject(IJastAddNode node) {
		if (node == null)
			return null;
		while (node.getParent() != null) {
			node = node.getParent();
		}
		return nodeToProjectMap.get(node);
	}

	protected IProgram initProgram(IProject project) {
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

	/**
	 * A map from source files ending with ".java" to IFiles.
	 */
	protected Map<String,IFile> sourceMap(IProject project) {
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
	protected void buildSourceMap(IResource[] resources, Map<String,IFile> sourceMap) throws CoreException {
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
	
	/**
	 * Opens the file corresponding to the given compilation unit with a selection
	 * corresponding to the given line, column and length.
	 */
	protected void openFile(ICompilationUnit unit, int line, int column, int length) {
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
	 */
	protected void openJavaFile(String pathName, int line, int column, int length)
			throws PartInitException, URISyntaxException {
		IPath path = Path.fromOSString(pathName);//URIUtil.toPath(new URI("file:/" + pathName));
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
				.findFilesForLocation(path);
		if (files.length >= 1) {
			IEditorInput targetEditorInput = new FileEditorInput(files[0]);
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();
			page.openEditor(targetEditorInput, getEditorID(), true);
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
	
	@Override
	public String getEditorID() {
		return JastAddJEditor.EDITOR_ID;
	}

	/**
	 * Opens a class corresponding to the given path name and makes a selection
	 * corresponding to line, column and length.
	 */
	protected void openClassFile(String pathName, String relativeName,
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


	public Collection recoverCompletion(int documentOffset, String[] linePart, StringBuffer buf, IProject project, String fileName, IJastAddNode node) throws IOException, Exception {
		if(node == null) {
			// Try a structural recovery
			documentOffset += (new JastAddStructureModel(buf)).doRecovery(documentOffset); // Return recovery offset change
	
			node = findNodeInDocument(project, fileName, new Document(buf.toString()), documentOffset - 1);
			if (node == null) {
				System.out.println("Structural recovery failed");
				return new ArrayList();
			}
		}
		if(node instanceof Access) {
			Access n = (Access)node;
			System.out.println("Automatic recovery");
			System.out.println(n.getParent().getParent().dumpTree());
			return n.completion(linePart[1]);
		} 
		else if(node instanceof ASTNode) {
			ASTNode n = (ASTNode)node;
			System.out.println("Manual recovery");
			Expr newNode;
			if(linePart[0].length() != 0) {
				String nameWithParan = "(" + linePart[0] + ")";
				ByteArrayInputStream is = new ByteArrayInputStream(nameWithParan.getBytes());
				scanner.JavaScanner scanner = new scanner.JavaScanner(new scanner.Unicode(is));
				newNode = (Expr)((ParExpr)new parser.JavaParser().parse(
						scanner,parser.JavaParser.AltGoals.expression)
				).getExprNoTransform();
				newNode = newNode.qualifiesAccess(new MethodAccess("X", new AST.List()));
			}
			else {
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
}
 