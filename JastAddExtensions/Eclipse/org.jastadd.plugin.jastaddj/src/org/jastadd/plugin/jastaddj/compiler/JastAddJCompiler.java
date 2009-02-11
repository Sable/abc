package org.jastadd.plugin.jastaddj.compiler;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.jastadd.plugin.compiler.AbstractCompiler;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IError;
import org.jastadd.plugin.compiler.recovery.Recovery;
import org.jastadd.plugin.compiler.recovery.RecoveryLexer;
import org.jastadd.plugin.compiler.recovery.SOF;
import org.jastadd.plugin.jastaddj.Activator;
import org.jastadd.plugin.jastaddj.AST.ICompilationUnit;
import org.jastadd.plugin.jastaddj.AST.IParser;
import org.jastadd.plugin.jastaddj.AST.IProblem;
import org.jastadd.plugin.jastaddj.AST.IProgram;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.compiler.recovery.JavaLexerIII;
import org.jastadd.plugin.jastaddj.nature.JastAddJNature;
import org.jastadd.plugin.jastaddj.util.BuildUtil;

import AST.BytecodeParser;
import AST.CompilationUnit;
import AST.Program;

public class JastAddJCompiler extends AbstractCompiler {

	// Recovery lexer used for bridge parsing
	protected RecoveryLexer fLexer;
	// Java Parser used to build CompilationUnit's
	protected IParser fParser;
	// File extensions accepted by this compiler
	protected Collection<String> fAcceptedExtensions;
	
	public JastAddJCompiler() {
		fLexer = new JavaLexerIII();
	}	
	
	/*
	 * (non-Javadoc)
	 * @see org.jastadd.plugin.compiler.AbstractCompiler#acceptedFileExtensions()
	 */
	@Override
	protected Collection<String> acceptedFileExtensions() {
		if (fAcceptedExtensions == null) {
			fAcceptedExtensions = new ArrayList<String>();
			fAcceptedExtensions.add("java");
		}
		return fAcceptedExtensions;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jastadd.plugin.compiler.AbstractCompiler#acceptedNatureID()
	 */
	@Override
	protected String acceptedNatureID() {
		return JastAddJNature.NATURE_ID;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jastadd.plugin.compiler.AbstractCompiler#compileToRootAST(org.eclipse.core.resources.IProject, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IASTNode compileToProjectAST(IProject project, IProgressMonitor monitor) {
		// Remove error markers on the project. Needed?
		deleteErrorMarkers(project, PARSE_ERROR_MARKER_ID);
		deleteErrorMarkers(project, ERROR_MARKER_ID);

		// Get the build configuration for this project
		JastAddJBuildConfiguration buildConfiguration;
		try {
			buildConfiguration = BuildUtil.readBuildConfiguration(project);
		} catch (CoreException e) {
			addCompilationFailedMarker(project, 
					"Build failed because build configuration could not be loaded: "
					+ e.getMessage());
			return null;
		}

		// Create a new program node
		IProgram program = initProgram(project, buildConfiguration);

		// Create a map with path-file
		Map<String, IFile> map = BuildUtil.sourceMap(project, buildConfiguration);
		if(map == null)
			return null;

		// Monitor code
		monitor.beginTask("Building files in project " + project.getName(), 100);
		if (monitor.isCanceled()) {
			return null;
		}
		SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 50);
		subMonitor.beginTask("", map.keySet().size());				
		if (monitor != null) {
			monitor.beginTask("Building files in " + project.getName(), map.keySet().size()*3);
		}

		// Add all source files to the new program node
		for (String fileName : map.keySet()) {
			program.addSourceFile(fileName);

			// Monitor code
			if (monitor.isCanceled()) {
				return null;
			}
			subMonitor.worked(1);
		}

		// Monitor code
		subMonitor.done();
		subMonitor = new SubProgressMonitor(monitor, 50);
		subMonitor.beginTask("", map.keySet().size()*2);

		for (Iterator iter = program.compilationUnitIterator(); iter.hasNext();) {
			ICompilationUnit unit = (ICompilationUnit) iter.next();
			if (unit.fromSource()) {
				IFile unitFile = map.get(unit.getFileName());
				// Check errors and update markers
				//boolean hasErrors = updateErrorsInFile(unit, unitFile, true);

				// Parse errors
				Collection errors = unit.parseErrors();
				boolean hasErrors = !errors.isEmpty();
				updateErrorMarkers(unitFile, errors, PARSE_ERROR_MARKER_ID, unit);
				errors.clear();

				// Semantic errors
				if (!hasErrors) {
					Collection warnings = new LinkedList();
					unit.errorCheck(errors, warnings);
					hasErrors = !errors.isEmpty();
					errors.addAll(warnings);
				}
				updateErrorMarkers(unitFile, errors, ERROR_MARKER_ID, unit);

				// Generate bytecode
				if (!hasErrors) { 
					unit.transformation();
					unit.generateClassfile();
				}

				if (monitor.isCanceled()) {
					// Remove class files before return
					try {
						removeAllGeneratedClassFiles(project, buildConfiguration, monitor);
					} catch (CoreException e) {
						String message = "Failed to remove generated class files after interruping a build"; 
						IStatus status = new Status(IStatus.ERROR, 
								Activator.JASTADDJ_PLUGIN_ID,
								IStatus.ERROR, message, e);
						Activator.INSTANCE.getLog().log(status);
					}
					return null;
				}
				subMonitor.worked(1);
			}
		}

		// Monitor code
		subMonitor.done();
		// Use for the bootstrapped version of JastAdd
		/*
		 * if(build) { program.generateIntertypeDecls();
		 * program.java2Transformation(); program.generateClassfile(); }
		 */
		return (IASTNode)program;
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.jastadd.plugin.compiler.AbstractCompiler#compileToAST(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.reconciler.DirtyRegion, org.eclipse.jface.text.IRegion, org.eclipse.core.resources.IFile)
	 */
	@Override
	protected IASTNode compileToAST(IDocument document, DirtyRegion dirtyRegion, IRegion region, IFile file) {
		
		// New build code
		String fileName = file.getRawLocation().toOSString();
		ICompilationUnit unit = null;
		if (document != null) {
			unit = createCompilationUnit(fileName, document.get());
		} else {
			unit = createCompilationUnit(file);
		}
		if (unit != null) {
			// Currently only updating parse errors. To update semantic errors the
			// node need to be added to the Program node which happens when the
			// node is added to the registry. The method calling this one could be
			// overidden to add such behavior
			Collection<IError> errors = unit.parseErrors();
			updateErrorMarkers(file, errors, PARSE_ERROR_MARKER_ID, unit);
			return (IASTNode)unit;
		}
		return null;

		/*
		 * 		
		 * // Build configuration
			JastAddJBuildConfiguration buildConfiguration = BuildUtil.getBuildConfiguration(project);
			if (buildConfiguration == null)
				return null;
			IProgram program = BuildUtil.getProgram(project);
			if (program == null)
				return null;
			program.files().clear();
			Map<String,IFile> map = BuildUtil.sourceMap(project, buildConfiguration);
			program.files().addAll(map.keySet());
			Collection changedFileNames = new ArrayList();
			if(fileName != null)
				changedFileNames.add(fileName);
			// remove files already built and the current document from work list
			program.flushSourceFiles(changedFileNames);
			if(fileName != null)
				program.files().remove(fileName);
			// build new files
			for(Iterator iter = program.files().iterator(); iter.hasNext(); ) {
				String name = (String)iter.next();
				program.addSourceFile(name);
			}
			//fireEvent = addSourceFileWithRecovery(project, program, document, fileName);	
			addSourceFileWithRecovery(project, program, document, fileName);
		 */
	}

	/*
	 * (non-Javadoc)
	 * @see org.jastadd.plugin.compiler.AbstractCompiler#compileToAST(org.eclipse.core.resources.IFile)
	 */
	@Override
	protected IASTNode compileToAST(IFile file) {
		return compileToAST(null, null, null, file);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jastadd.plugin.compiler.AbstractCompiler#displaySemanticErrors(org.jastadd.plugin.compiler.ast.IASTNode, org.eclipse.core.resources.IFile)
	 */
	/*
	@SuppressWarnings("unchecked")
	@Override
	protected void displaySemanticErrors(IASTNode node, IFile file) {
		if (!(node instanceof ICompilationUnit)) {
			return;
		}
		ICompilationUnit unit = (ICompilationUnit)node;	
		Collection warnings = new LinkedList();
		Collection errors = new LinkedList();
		unit.errorCheck(errors, warnings);
		errors.addAll(warnings);
		if (!errors.isEmpty()) {
			updateErrorMarkers(file, errors, ERROR_MARKER_ID, unit);
		} else {
			deleteErrorMarkers(file, ERROR_MARKER_ID);
		}
	}
	*/
	/*
	 * Change from the default error marker to the parser error marker for lexical and syntactic errors
	 * (non-Javadoc)
	 * @see org.jastadd.plugin.compiler.AbstractCompiler#addErrorMarker(org.eclipse.core.resources.IResource, org.jastadd.plugin.compiler.ast.IError, java.lang.String)
	 */
	@Override
	protected void addErrorMarker(IResource resource, IError error, String markerID) {
		// Change to the parse error marker for lexical and syntactical errors
		if (error.getKind() == IError.LEXICAL || error.getKind() == IError.SYNTACTIC)
			markerID = PARSE_ERROR_MARKER_ID;
		super.addErrorMarker(resource, error, markerID);
	}

		
	/**
	 * Converts line and column information to offsets in errors using the offset
	 * converter provided by ICompilationUnit
	 * @param resource The resource to put markers on
	 * @param errorList The list of errors
	 * @param markerID The marker ID to use
	 * @param unit The compilation unit with offset information
	 */
	protected void updateErrorMarkers(IResource resource,
			Collection<IError> errorList, String markerID, ICompilationUnit unit) {
		for (IError error : errorList) {
			// Convert column,line to start end offsets
			if (error instanceof IProblem) {
				IProblem problem = (IProblem)error;
				int startOffset = unit.offset(problem.line(), problem.column());
				int endOffset = unit.offset(problem.endLine(), problem.endColumn());
				problem.setStartOffset(startOffset);
				problem.setEndOffset(endOffset);
			}
		}
		super.updateErrorMarkers(resource, errorList, markerID);
	}
	
	/**
	 * Creates a CompilationUnit node. Tries to recover once if no TypeDecls are created.
	 * @param path The path to the corresponding file
	 * @param contents The content to parse
	 * @return A Compilation Unit or null if there was a failure
	 */
	protected ICompilationUnit createCompilationUnit(String path, String contents) {

		StringBuffer buf = new StringBuffer(contents);
		int keepOn = 2;
		ICompilationUnit unit = null;
		while (keepOn > 0) {
			
			java.io.InputStream is = new java.io.ByteArrayInputStream(buf.toString().getBytes());
			try {
				unit = fParser.parse(is, path);
				is.close();
				is = null;

				if (unit.getNumTypeDecl() > 0) {
					unit.setPathName(path);
					unit.setRelativeName(path);
					unit.setFromSource(true);

					/* TODO: add package name into global list of packages
		        		String packageName = unit.getPackageDecl();
	            		if(packageName != null && !packages.contains(packageName)) {
	              			packages.add(packageName);
	              			int pos = 0;
		              		while(packageName != null && -1 != (pos = packageName.indexOf('.', pos + 1))) {
	    	            		String n = packageName.substring(0, pos);
	        	        		if(!packages.contains(n))
	            	      			packages.add(n);
	              			}	
		            	}
					 */
					return unit;
				} else {
					if (keepOn == 1) {
						System.out.println("Bridge parsing failed");
					}
				}
			} catch (Throwable e) {
				/* Catch parse exceptions and try to recover */
			}

			if (keepOn > 1) {	
				System.out.println("Applying bridge parsing");
				SOF sof = fLexer.parse(buf);
				Recovery.doRecovery(sof);
				buf = Recovery.prettyPrint(sof);
				//System.out.println("Recovered to:\n" + buf.toString() + "\n-------");
			}
			keepOn--;
		}
		return null;
	}

	/**
	 * Creates a CompilationUnit node.
	 * @param file The file to parse
	 * @return A CompilationUnit or null if something went wrong
	 */
	protected ICompilationUnit createCompilationUnit(IFile file) {
		String path = file.getRawLocation().toOSString();
		ICompilationUnit unit = null;
		try {
			java.io.InputStream is = new FileInputStream(file.getRawLocation().toFile());
			unit = fParser.parse(is, path);
			is.close();
			is = null;

			if (unit.getNumTypeDecl() > 0) {
				unit.setPathName(path);
				unit.setRelativeName(path);
				unit.setFromSource(true);
				return unit;
			}
		} catch (Throwable e) {
			/* Catch parse exceptions */
		}
		return null;
	}
	
	/**
	 * Creates a new IProgram node which will act as a project root node
	 * @param project The corresponding project
	 * @param buildConfiguration The build configuration to use
	 * @return A new IProgram node
	 */
	protected IProgram initProgram(IProject project,
			JastAddJBuildConfiguration buildConfiguration) {
		if (fParser == null) {
			if (fParser == null) {
				fParser = new IParser() {
					public AST.JavaParser parser = new AST.JavaParser() {
						public CompilationUnit parse(java.io.InputStream is, String fileName) 
						throws java.io.IOException, beaver.Parser.Exception {
							return new parser.JavaParser().parse(is, fileName);
						}	
					};
					public ICompilationUnit parse(java.io.InputStream is, String fileName) 
					throws java.io.IOException, beaver.Parser.Exception {
						return parser.parse(is, fileName);
					}
					public Object newInternalParser() {
						return new AST.JavaParser() {
							public CompilationUnit parse(java.io.InputStream is, String fileName) 
							throws java.io.IOException, beaver.Parser.Exception {
								return new parser.JavaParser().parse(is, fileName);
							}	
						};
					}
				}; 
			}
		}
		
		// Init
		Program program = new Program();
		program.initBytecodeReader(new BytecodeParser());
		program.initJavaParser((AST.JavaParser)fParser.newInternalParser());
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
	
	/**
	 * Re-initializes an IProgram node.
	 * @param project The corresponding project
	 * @param program The IProgram node to re-initialize
	 * @param buildConfiguration The build configuration to use
	 */
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

	/**
	 * Removes all generated class files. 
	 * @param project The project in which to remove class files
	 * @param buildConfiguration The build configuration to use
	 * @param monitor The progress monitor
	 * @throws CoreException 
	 */
	protected void removeAllGeneratedClassFiles(IProject project, 
			JastAddJBuildConfiguration buildConfiguration, IProgressMonitor monitor) 
	throws CoreException {
		String outputPath = buildConfiguration.outputPath;
		IFolder folder = project.getFolder(outputPath);
		IResource[] members = folder.members();
		for (int i = 0; i < members.length; i++) {
			members[i].delete(true, monitor);
		}
	}



	/*
	public Collection recoverAndCompletion(int documentOffset, StringBuffer buf, 
			IProject project, String fileName, IJastAddNode node, String filter, 
			String leftContent) throws IOException, Exception {

		if (node == null) {
			// Try recovery
			SOF sof = fLexer.parse(buf);
			LexicalNode recoveryNode = Recovery.findNodeForOffset(sof, documentOffset);
			Recovery.doRecovery(sof);
			buf = Recovery.prettyPrint(sof);
			documentOffset += recoveryNode.getInterval().getPushOffset();			
			node = NodeLocator.findNodeInDocument(project, fileName, new Document(buf.toString()), documentOffset - 1);
			if (node == null) {
				System.out.println("Structural recovery failed");
				return new ArrayList();
			}
		}

		synchronized (node.treeLockObject()) {
			if (node instanceof Access) {
				Access n = (Access) node;
				System.out.println("Automatic recovery");
				System.out.println(n.getParent().getParent().dumpTree());
				return n.completion(filter);
			} else if (node instanceof ASTNode) {
				ASTNode n = (ASTNode) node;
				System.out.println("Manual recovery");
				Expr newNode;
				if (leftContent.length() != 0) {
					String nameWithParan = "(" + leftContent + ")";
					ByteArrayInputStream is = new ByteArrayInputStream(
							nameWithParan.getBytes());
					scanner.JavaScanner scanner = new scanner.JavaScanner(
							new scanner.Unicode(is));
					newNode = (Expr) ((ParExpr) new parser.JavaParser().parse(
							scanner, parser.JavaParser.AltGoals.expression))
							.getExprNoTransform();
					newNode = newNode.qualifiesAccess(new MethodAccess("X",
							new AST.List()));
				} else {
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
				return n.completion(filter);
			}
			return new ArrayList();
		}
	}
	*/
	
	/*
    protected boolean addSourceFileWithRecovery(IProject project, IProgram program, IDocument doc, String fileName) throws Exception {
    	ICompilationUnit unit = program.addSourceFileWithRecovery(fileName, doc.get(), getRecoveryLexer());
    	if (unit != null) {
    		IPath path = Path.fromOSString(fileName);
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
			.findFilesForLocation(path);
			if (files.length == 1)
				return updateErrorsInFile(unit, files[0], true);
    	}
    	return false;
      }
      
	protected static RecoveryLexer lexer;
	public static RecoveryLexer getRecoveryLexer() {
		if (lexer == null) {
			lexer = new JavaLexerIII();
		}
		return lexer;
	}

	public Object getASTRootForLock(IProject project) {
		ProgramInfo info = BuildUtil.getProgramInfo(project);
		if (info == null)
			return JastAddJCompiler.class;
		IProgram root = info.program;
		return ((IJastAddNode)root).treeLockObject();
	}
	
	public JastAddJBuildConfiguration getBuildConfiguration(IProject project) {
		ProgramInfo programInfo = getProgramInfo(project);
		if (programInfo != null)
			return programInfo.buildConfiguration;
		return null;
	}

	public IProgram getProgram(IProject project) {
		ProgramInfo programInfo = getProgramInfo(project);
		if (programInfo != null)
			return programInfo.program;
		return null;
	}

	public Object getASTRootForLock(IProject project) {
		ProgramInfo info = projectToNodeMap.get(project);
		if (info == null)
			return JastAddJModel.class;
		IProgram root = info.program;
		return ((IJastAddNode)root).treeLockObject();
	}
*/
	
		
	/*
	protected boolean updateErrorsInFile(ICompilationUnit unit, IFile file, boolean checkSemantics) throws CoreException {
		String content;
		try {
			content = FileUtil.readTextFile(file.getRawLocation().toOSString());
			return updateErrorsInFile(unit, file, content, checkSemantics);
		} catch (IOException e) {
			String message = "Problem reading file content when updating errors markers";
			IStatus status = new Status(IStatus.ERROR, 
					Activator.JASTADDJ_PLUGIN_ID,
					IStatus.ERROR, message, e);
			Activator.INSTANCE.getLog().log(status);
		}
		return false;
	}
	
	protected boolean updateErrorsInFile(ICompilationUnit unit, IFile file, boolean checkSemantics) throws CoreException {
		deleteErrorMarkers(file, PARSE_ERROR_MARKER_ID);
		Collection errors = unit.parseErrors();
		Collection warnings = new LinkedList();
		boolean noParseErrors = errors.isEmpty();
		if (checkSemantics && noParseErrors) { // only run semantic checks if there's no parse errors and if its asked for
			deleteErrorMarkers(file, ERROR_MARKER_ID);
			unit.errorCheck(errors, warnings);
		}
		errors.addAll(warnings);
		if (!errors.isEmpty()) {
			addErrorMarkers(file, errors, ERROR_MARKER_ID);
			
			for (Iterator i2 = errors.iterator(); i2.hasNext();) {
				org.jastadd.plugin.jastaddj.AST.IProblem error = 
					(org.jastadd.plugin.jastaddj.AST.IProblem) i2.next();
				int line = error.line();
				int endLine = error.endLine();
				int column = error.column();
				int endColumn = error.endColumn();
				if (line == -1)
					line = 1;
				int startOffset = lookupOffset(line-1, column-1, content);
				if (endLine == -1)
					endLine = 1;
				int endOffset = lookupOffset(endLine-1, endColumn-1, content); 

				if (startOffset == endOffset)
					endOffset++;

				String message = error.message();
				int severity = IMarker.SEVERITY_INFO;
				if (error.severity() == IDEProblem.Severity.ERROR)
					severity = IMarker.SEVERITY_ERROR;
				else if (error.severity() == IDEProblem.Severity.WARNING)
					severity = IMarker.SEVERITY_WARNING;
				
				if (error.kind() == IDEProblem.Kind.LEXICAL
						|| error.kind() == IDEProblem.Kind.SYNTACTIC) {
					addParseErrorMarker(file, message, line, startOffset, endOffset, severity);
				} else if (error.kind() == IDEProblem.Kind.SEMANTIC) {
					addErrorMarker(file, message, line, startOffset, endOffset, severity);
				}	
			}
			
			return noParseErrors;
		}
		return noParseErrors;
	}
	
	protected int lookupOffset(int line, int column, String content) {
		
		int curLine = 0;
		int offset = 0;
		int previous = 0;
		int cur = 0;
		
		// Find line
		while (offset < content.length() && curLine < line) {
			previous = cur;
			cur = content.charAt(offset);
			if (isNewline(cur, previous)) {
				curLine++;
			}
			offset++;
		}
		
		// Add Column
		offset += column;
		return offset;
	} 
	
	private boolean isNewline(int c, int previous) {
	     return (c == 0x0a && previous != 0x0d)  // LF
	       || c == 0x0d // CR
	       || c == 0x85 // NEL
	       || c == 0x0c // FF
	       || c == 0x2028 // LS
	       || c == 0x2029; // PS
	   }

	*/
	

	/*
	public JastAddJBuildConfiguration readBuildConfiguration(IProject project)
	throws CoreException {
		try {
			JastAddJBuildConfiguration buildConfiguration = getEmptyBuildConfiguration();
			doReadBuildConfiguration(project, buildConfiguration);
			return buildConfiguration;
		} catch (Exception e) {
			String message = "Loading build configuration failed";
			IStatus status = new Status(IStatus.ERROR, 
					Activator.JASTADDJ_PLUGIN_ID,
					IStatus.ERROR, message, e);
			Activator.INSTANCE.getLog().log(status);
			throw new CoreException(status);
		}
	}
	
	protected JastAddJBuildConfiguration getEmptyBuildConfiguration() {
		return new JastAddJBuildConfiguration();
	}

	protected void doReadBuildConfiguration(IProject project,
			JastAddJBuildConfiguration buildConfiguration) throws Exception {
		JastAddJBuildConfigurationUtil.readBuildConfiguration(project,
				buildConfiguration);
	}

	protected void doWriteBuildConfiguration(IProject project,
			JastAddJBuildConfiguration buildConfiguration) throws Exception {
		JastAddJBuildConfigurationUtil.writeBuildConfiguration(project,
				buildConfiguration);
	}


	
	protected Map<String, IFile> sourceMap(IProject project,
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
							if (!isModelFor(file))
								break;
							if (!matcher.match(resource))
								break;
							result
									.put(file.getRawLocation().toOSString(),
											file);
							break;
						}
						return true;
					}
				});
			}
			return result;
		} catch (CoreException e) {
			String message = "Problem in JastAdddJCompiler.sourceMap";
			IStatus status = new Status(IStatus.ERROR, 
					Activator.JASTADDJ_PLUGIN_ID,
					IStatus.ERROR, message, e);
			Activator.INSTANCE.getLog().log(status);
			return null;
		}
	}
	
	protected boolean isModelFor(IFile file) {
		if(file == null)
			return false;
		for (String str : getFileExtensions()) {
			if (file.getFileExtension() != null && file.getFileExtension().equals(str)) {
				return isModelFor(file.getProject());
			}
		}
		return false;
	}
	
	protected boolean isModelFor(IProject project) {
		try {
			if (project != null && project.isOpen()
					&& project.isNatureEnabled(getNatureID())) {
				return true;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	protected String getNatureID() {
		return JastAddJNature.NATURE_ID;
	}
	
	protected static List<String> getFileExtensions() {
		List<String> list = new ArrayList<String>();
		list.add("java");
		return list;
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
	*/

	
	/*
	protected void addBuildConfigurationOptions(IProject project,
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
		synchronized (program.treeLockObject()) {
			program.addOptions(options.toArray(new String[0]));
		}
	}

	protected List<String> buildBootClassPath(IProject project,
			final JastAddJBuildConfiguration buildConfiguration) {
		List<String> result = new ArrayList<String>();
		
		IVMInstall vm = getVMInstall(project, buildConfiguration);
		LibraryLocation[] libraryLocations = vm.getLibraryLocations();
		if (libraryLocations == null)
			libraryLocations = vm.getVMInstallType().getDefaultLibraryLocations(vm.getInstallLocation());
		
		if (libraryLocations != null) {
			for(LibraryLocation libraryLocation : libraryLocations) {
				IPath path = libraryLocation.getSystemLibraryPath();
				Object object = resolveResourceOrFile(project, path);
				if (object == null) continue;

				if (object instanceof IResource)
					result.add(((IResource) object).getRawLocation().toOSString());
				else
					result.add(((java.io.File) object).getAbsolutePath());
			}
		}
		return result;
	}
	
	protected List<String> buildClassPath(IProject project, final JastAddJBuildConfiguration buildConfiguration) {
		List<String> result = new ArrayList<String>();
		for (ClassPathEntry classPathEntry : buildConfiguration.classPathList) {
			Path path = new Path(classPathEntry.classPath);
			Object object = resolveResourceOrFile(project, path);
			if (object == null)
				continue;

			if (object instanceof IResource)
				result.add(((IResource) object).getRawLocation().toOSString());
			else
				result.add(((java.io.File) object).getAbsolutePath());
		}
		return result;
	}
	
	public IVMInstall getVMInstall(IProject project, JastAddJBuildConfiguration buildConfiguration) {
		return JavaRuntime.getDefaultVMInstall();
	}
	
	protected Object resolveResourceOrFile(IProject project, IPath path) {
		IResource resource;
		if (!path.isAbsolute())
			resource = project.findMember(path);
		else
			resource = project.getWorkspace().getRoot().findMember(path);

		if (resource != null)
			return resource;

		java.io.File file = path.toFile();
		if (file.exists())
			return file;

		return null;
	}



	public void checkForErrors(IProject project, IProgressMonitor monitor) {
		//try {
			//try {		
				//deleteErrorMarkers(PARSE_ERROR_MARKER_TYPE, project);
				//deleteErrorMarkers(ERROR_MARKER_TYPE, project);
				
				JastAddJBuildConfiguration buildConfiguration;
				try {
					buildConfiguration = BuildUtil.readBuildConfiguration(project);
				} catch (CoreException e) {
					//addErrorMarker(project,
						//	"Error check failed because build configuration could not be loaded: "
							//		+ e.getMessage(), -1,
							//IMarker.SEVERITY_ERROR);
					return;
				}

				IProgram program = BuildUtil.initProgram(project, buildConfiguration);
				// Parsing source files
				Map<String, IFile> map = BuildUtil.sourceMap(project, buildConfiguration);				
				if(map != null) {
					monitor.beginTask("Building files in project " + project.getName(), 100);
					if (monitor.isCanceled()) {
						return;
					}
					SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 50);
					subMonitor.beginTask("", map.keySet().size());
					for (String fileName : map.keySet()) {
						program.addSourceFile(fileName);
						subMonitor.worked(1);
						if (monitor.isCanceled()) {
							return;
						}
					}
					subMonitor.done();
					subMonitor = new SubProgressMonitor(monitor, 50);
					subMonitor.beginTask("", map.keySet().size());
					for (Iterator iter = program.compilationUnitIterator(); iter.hasNext();) {
						ICompilationUnit unit = (ICompilationUnit) iter.next();
						if (unit.fromSource()) {
							IFile unitFile = map.get(unit.getFileName());
							//updateErrorsInFile(unit, unitFile, true);
							subMonitor.worked(1);
							if (monitor.isCanceled()) {
								return;
							}
						}
					}
					subMonitor.done();
				}
			//} catch (CoreException e) {
				//addErrorMarker(project, "Problem checking errors: "
				//		+ e.getMessage(), -1, IMarker.SEVERITY_ERROR);
				//logCoreException(e);
			//}
		//} catch (Throwable e) {
			//logError(e, "Problem checking errors");
		//} finally {
		//	monitor.done();
		//}	
	}
*/
	/* This code isn't used seems like its been replaced by recoverAndCompletion 
	 * now is located in JastAddJCompletionProcessor
	 * 
	public Collection recoverCompletion(int documentOffset, String[] linePart,
				StringBuffer buf, IProject project, String fileName,
				IJastAddNode node) throws IOException, Exception {
		synchronized (node.treeLockObject()) {
			if (node == null) {
				// Try recovery
				SOF sof = JastAddJCompiler.getRecoveryLexer().parse(buf);
				LexicalNode recoveryNode = Recovery.findNodeForOffset(sof, documentOffset);
				Recovery.doRecovery(sof);
				buf = Recovery.prettyPrint(sof);
				documentOffset += recoveryNode.getInterval().getPushOffset();			
				node = NodeLocator.findNodeInDocument(project, fileName, new Document(buf.toString()), documentOffset - 1);
				if (node == null) {
					System.out.println("Structural recovery failed");
					return new ArrayList();
				}
			}
			if (node instanceof Access) {
				Access n = (Access) node;
				System.out.println("Automatic recovery");
				System.out.println(n.getParent().getParent().dumpTree());
				return n.completion(linePart[1]);
			} else if (node instanceof ASTNode) {
				ASTNode n = (ASTNode) node;
				System.out.println("Manual recovery");
				Expr newNode;
				if (linePart[0].length() != 0) {
					String nameWithParan = "(" + linePart[0] + ")";
					ByteArrayInputStream is = new ByteArrayInputStream(
							nameWithParan.getBytes());
					scanner.JavaScanner scanner = new scanner.JavaScanner(
							new scanner.Unicode(is));
					newNode = (Expr) ((ParExpr) new parser.JavaParser().parse(
							scanner, parser.JavaParser.AltGoals.expression))
							.getExprNoTransform();
					newNode = newNode.qualifiesAccess(new MethodAccess("X",
							new AST.List()));
				} else {
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
	*/
	


}
