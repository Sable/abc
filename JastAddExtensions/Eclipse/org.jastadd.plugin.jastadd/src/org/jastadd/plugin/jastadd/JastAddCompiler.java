package org.jastadd.plugin.jastadd;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.compiler.recovery.LexicalNode;
import org.jastadd.plugin.compiler.recovery.Recovery;
import org.jastadd.plugin.compiler.recovery.SOF;
import org.jastadd.plugin.jastadd.generated.AST.ASTChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTDecl;
import org.jastadd.plugin.jastadd.generated.AST.ASTElementChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTListChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTNode;
import org.jastadd.plugin.jastadd.generated.AST.ASTOptionalChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTTokenChild;
import org.jastadd.plugin.jastadd.generated.AST.AttributeDecl;
import org.jastadd.plugin.jastadd.generated.AST.BytecodeParser;
import org.jastadd.plugin.jastadd.generated.AST.CompilationUnit;
import org.jastadd.plugin.jastadd.generated.AST.JavaParser;
import org.jastadd.plugin.jastadd.generated.AST.List;
import org.jastadd.plugin.jastadd.generated.AST.MethodDecl;
import org.jastadd.plugin.jastadd.generated.AST.Program;
import org.jastadd.plugin.jastadd.generated.AST.SimpleSet;
import org.jastadd.plugin.jastadd.generated.AST.TypeDecl;
import org.jastadd.plugin.jastadd.properties.FolderList;
import org.jastadd.plugin.jastadd.properties.JastAddBuildConfiguration;
import org.jastadd.plugin.jastadd.properties.FolderList.PathEntry;
import org.jastadd.plugin.jastaddj.Activator;
import org.jastadd.plugin.jastaddj.AST.ICompilationUnit;
import org.jastadd.plugin.jastaddj.AST.IProgram;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.compiler.JastAddJCompiler;
import org.jastadd.plugin.jastaddj.util.BuildUtil;
import org.jastadd.plugin.jastaddj.util.FileUtil;
import org.jastadd.plugin.jastaddj.util.BuildUtil.ProgramInfo;
import org.jastadd.plugin.util.NodeLocator;

import beaver.Parser.Exception;

public class JastAddCompiler extends JastAddJCompiler {

	@Override
	protected IASTNode compileToAST(IDocument document, DirtyRegion dirtyRegion, IRegion region, IFile file) {
		
		IProject project = file.getProject();
		String fileName = file.getRawLocation().toOSString();
		
		//boolean fireEvent = true;
		JastAddJBuildConfiguration buildConfiguration = BuildUtil.getBuildConfiguration(project);
		if (buildConfiguration == null)
			return null;
		
		JastAddBuildConfiguration jastAddBuildConfig = new JastAddBuildConfiguration(project);
		
		
		// Regenerate scanner or parser if there was a change in a flex or parser file
		if (fileName.endsWith(".flex")) {
			buildJFlexScanner(project, jastAddBuildConfig);
		} else if (fileName.endsWith(".parser")) {
			buildBeaverParser(project, jastAddBuildConfig);
		}
		
		IProgram program = BuildUtil.getProgram(project);
		//super.updateModel(document, fileName, project);

		try {
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
			if(fileName.endsWith(".ast"))
				program.addSourceFile(fileName);
			else
				//fireEvent = addSourceFileWithRecovery(project, program, document, fileName);
				addSourceFileWithRecovery(project, program, document, fileName);
			if(program instanceof Program) {
				((Program)program).flushIntertypeDecls();
			}
			
		} catch (Exception e) {
			String message = "Failed to update model!"; 
			IStatus status = new Status(IStatus.ERROR, 
					Activator.JASTADDJ_PLUGIN_ID,
					IStatus.ERROR, message, e);
			Activator.INSTANCE.getLog().log(status);
		} catch (Throwable e) {
			String message = "Failed to update model!"; 
			IStatus status = new Status(IStatus.ERROR, 
					Activator.JASTADDJ_PLUGIN_ID,
					IStatus.ERROR, message, e);
			Activator.INSTANCE.getLog().log(status);
		}
		ProgramInfo info = BuildUtil.getProgramInfo(project);
		if (info != null) {
			info.changed();
		}
		return null;
	}

	@Override
	protected IASTNode compileToAST(IFile file) {
		return null;
	}

	@Override
	protected IASTNode compileToProjectAST(IProject project,
			IProgressMonitor monitor) {
		ProgramInfo info = BuildUtil.getProgramInfo(project);
		if (info != null && info.hasChanged()) {

			// Only build if there was a change

			// Build a new project from saved files only.
			Program program = null;
			long startTime = System.currentTimeMillis();
			long newTime;
			//Runtime.getRuntime().gc();
			System.out.println("Start rebuilding");
			System.out.println("Free memory: " + Runtime.getRuntime().freeMemory());
			System.out.println("Total memory: " + Runtime.getRuntime().totalMemory());
			try {
				deleteErrorMarkers(project, PARSE_ERROR_MARKER_ID);
				deleteErrorMarkers(project, ERROR_MARKER_ID);


				JastAddJBuildConfiguration buildConfiguration = BuildUtil.readBuildConfiguration(project);
				JastAddBuildConfiguration jastAddBuildConfig = new JastAddBuildConfiguration(project); 

				program = (Program) initProgram(project, buildConfiguration);
				initProgram(program, jastAddBuildConfig);
				if (program == null)
					return null;
				
				// Generate scanner and parser
				buildJFlexScanner(project, jastAddBuildConfig);
				buildBeaverParser(project, jastAddBuildConfig);

				int numSourceFiles = 0;

				long time = System.currentTimeMillis();

				Map<String,IFile> map = BuildUtil.sourceMap(project, buildConfiguration);			

				monitor.beginTask("Building files in project " + project.getName(), 100);
				SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 10);
				subMonitor.beginTask("", map.keySet().size());
				if (map != null) {
					for(String fileName : map.keySet()) {
						program.addSourceFile(fileName);
						if (monitor.isCanceled()) {
							return null;
						}
						subMonitor.worked(1);
						numSourceFiles++;
					}
				}
				subMonitor.done();

				newTime = System.currentTimeMillis();
				System.out.println("Parsing: " + (newTime-time));
				time = newTime;

				boolean build = true;

				subMonitor = new SubProgressMonitor(monitor, 30);
				subMonitor.beginTask("", numSourceFiles);
				for (Iterator iter = program.compilationUnitIterator(); iter.hasNext();) {
					ICompilationUnit unit = (ICompilationUnit) iter.next();

					if (unit.fromSource()) {
						IFile unitFile = map.get(unit.getFileName());
						build &= updateErrorsInFile(unit, unitFile, true);
						if (monitor.isCanceled()) {
							return null;
						}
						subMonitor.worked(1);
					}
				}
				subMonitor.done();

				newTime = System.currentTimeMillis();
				System.out.println("ErrorCheck: " + (newTime-time));
				time = newTime;


				// Use for the bootstrapped version of JastAdd

				subMonitor = new SubProgressMonitor(monitor, 60);
				subMonitor.beginTask("", numSourceFiles*3);
				if (build) {
					for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
						CompilationUnit cu = (CompilationUnit)iter.next();
						if(cu.fromSource()) {
							cu.generateIntertypeDecls();
							if(monitor.isCanceled())
								return null;
							subMonitor.worked(1);
						}
					}
					newTime = System.currentTimeMillis();
					System.out.println("ITD generation: " + (newTime-time));
					time = newTime;
					for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
						CompilationUnit cu = (CompilationUnit)iter.next();
						if(cu.fromSource()) {
							cu.transformation();
							if(monitor.isCanceled())
								return null;
							subMonitor.worked(1);
						}
					}
					newTime = System.currentTimeMillis();
					System.out.println("Transformation: " + (newTime-time));
					time = newTime;
					for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
						CompilationUnit cu = (CompilationUnit)iter.next();
						if(cu.fromSource()) {
							for(int i = 0; i < cu.getNumTypeDecl(); i++) {
								cu.getTypeDecl(i).generateClassfile();
								// do not clear type decl since there may be ITD using
								// matching on introduced parents.
							}
							//cu.clear();
							if (monitor.isCanceled()) {
								// Remove class files before return
								removeAllGeneratedClassFiles(project, buildConfiguration, monitor);
								return null;
							}
							subMonitor.worked(1);
						}
					}
					newTime = System.currentTimeMillis();
					System.out.println("Codegeneration: " + (newTime-time));
					time = newTime;
				}
				subMonitor.done();
				info.clearChanges();
				
			} catch (CoreException e) {
				String message = "Build failed because: " + e.getMessage(); 
				addCompilationFailedMarker(project, message);
				Activator.INSTANCE.getLog().log(e.getStatus());
			}
			catch (Throwable e) {
				String message = "Build failed!"; 
				IStatus status = new Status(IStatus.ERROR, 
						Activator.JASTADDJ_PLUGIN_ID,
						IStatus.ERROR, message, e);
				Activator.INSTANCE.getLog().log(status);
			} finally {
				monitor.done();
				program.getCompilationUnitList().setParent(null);
				program.setCompilationUnitList(new List());
				program.flushAttributes();
				program = null;
				newTime = System.currentTimeMillis();
				System.out.println("Complete Build: " + (newTime-startTime));
				System.out.println("Free memory: " + Runtime.getRuntime().freeMemory());
				System.out.println("Total memory: " + Runtime.getRuntime().totalMemory());
				System.out.println("Running garbage collector");
				//Runtime.getRuntime().gc();
				System.out.println("Free memory: " + Runtime.getRuntime().freeMemory());
				System.out.println("Total memory: " + Runtime.getRuntime().totalMemory());
			}
		}
		return null;
	}
		
	public boolean isModelFor(IProject project) {
		try {
			if (project != null && project.isOpen() && project.isNatureEnabled(Nature.NATURE_ID)) {
				return true;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
    protected boolean addSourceFileWithRecovery(IProject project, IProgram program, IDocument doc, String fileName) throws java.lang.Exception {
    	ICompilationUnit unit = program.addSourceFileWithRecovery(fileName, doc.get(), fLexer);
    	if (unit != null) {
    		IPath path = Path.fromOSString(fileName);
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
			.findFilesForLocation(path);
			if (files.length == 1)
				return updateErrorsInFile(unit, files[0], true);
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
			
			/*
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
			*/
			
			return noParseErrors;
		}
		return noParseErrors;
	}
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
	*/
	
	
	/*
	public java.util.List<String> getFileExtensions() {
		java.util.List<String> list = super.getFileExtensions();
		list.add("jrag");
		list.add("jadd");
		list.add("ast");
		list.add("flex");
		list.add("parser");
		return list;
	}
	*/
	
	public String getNatureID() {
		//return JastAddNature.NATURE_ID;
		return Nature.NATURE_ID;
	}
	
	public JastAddCompiler() {
		/*
		String[] fileType = {"jrag", "jadd", "ast", "flex", "parser"};
 		registerFileType(fileType[0]);
 		registerFileType(fileType[1]);
 		registerFileType(fileType[2]);
 		registerFileType(fileType[3]);
 		registerFileType(fileType[4]);
		JastAddScanner scanner = new  JastAddScanner(new JastAddColors());
		registerScanner(scanner, fileType[0]);
		registerScanner(scanner, fileType[1]);
		registerScanner(new ASTScanner(new JastAddColors()), fileType[2]);
		registerScanner(new JFlexScanner(new JastAddColors()), fileType[3]);
		registerScanner(new ParserScanner(new JastAddColors()), fileType[4]);
		*/
	}	


	//*************** Protected methods
	
	
	@Override
	protected IProgram initProgram(IProject project, JastAddJBuildConfiguration buildConfiguration) {
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
		program.options().initOptions();
		program.options().addKeyValueOption("-classpath");
		program.options().addKeyValueOption("-bootclasspath");
		program.options().addKeyValueOption("-d");
		program.options().addKeyValueOption("-package");
		BuildUtil.addBuildConfigurationOptions(project, program, buildConfiguration);
		//program.options().setOption("-verbose");
		program.options().setOption("-no_visit_check");
		program.options().setOption("-inh_in_astnode");
		program.options().setOption("-no_cache_cycle");
		program.options().setOption("-no_component_check");
		//program.options().addKeyOption("-weave_inline");
		//program.options().setOption("-weave_inline");
		return program;	   
	}
	
	protected void initProgram(IProgram program, JastAddBuildConfiguration buildConfig) {
		Collection<String> options = new ArrayList<String>();
		if (buildConfig.jastadd.getPackage() != null) {
			options.add("-package");
			options.add(buildConfig.jastadd.getPackage());
		}
		synchronized (program.treeLockObject()) {
			program.addOptions(options.toArray(new String[0]));
		}
	}
	
	@Override
	protected void reinitProgram(IProject project, IProgram program, JastAddJBuildConfiguration buildConfiguration) {
		synchronized (program.treeLockObject()) {
			Program realProgram = (Program)program;
			// Init
			realProgram.options().initOptions();
			realProgram.options().addKeyValueOption("-classpath");
			realProgram.options().addKeyValueOption("-bootclasspath");
			realProgram.options().addKeyValueOption("-d");
			BuildUtil.addBuildConfigurationOptions(project, realProgram, buildConfiguration);
			realProgram.options().setOption("-verbose");
		}
	}	
	
	
	/*
	public Object getASTRootForLock(IProject project) {
		ProgramInfo info = projectToNodeMap.get(project);
		if (info == null)
			return JastAddCompiler.class;
		IProgram root = info.program;
		return ((IJastAddNode)root).treeLockObject();
	}
	*/
	
	public ArrayList<AttributeDecl> lookupJVMName(IProject project, String packageName) {
		ArrayList<AttributeDecl> nameList = new ArrayList<AttributeDecl>();
		//synchronized (getASTRootForLock(project)) {
			TypeDecl decl = getTypeDecl(project, packageName);

			if (decl == null) {
				// We didn't find a match, so we return an empty list
				return nameList;
			}

			// Find attribute declaration
			AttributeDecl aDecl = null;
			//if(decl.name().equals("Block") && decl.memberMethods("b").isEmpty())
			//	System.out.println("Strange");
			for (Iterator itr = decl.methodsIterator(); itr.hasNext();) {
				MethodDecl mDecl = (MethodDecl)itr.next();
				//	System.out.println(mDecl.signature());
				if (mDecl instanceof AttributeDecl) {
					aDecl = (AttributeDecl)mDecl;
					nameList.add(aDecl);
				}
			}
			if(decl instanceof ASTDecl) {
				ASTDecl astDecl = (ASTDecl)decl;
				for(Iterator iter = astDecl.components().iterator(); iter.hasNext(); ) {
					ASTChild c = (ASTChild)iter.next();
					if(c instanceof ASTElementChild) {
						// A ::= B;

					}
					else if(c instanceof ASTListChild) {
						// A ::= B*

					}
					else if(c instanceof ASTOptionalChild) {
						// A ::= [B]

					}
					else if(c instanceof ASTTokenChild) {
						// A ::= <ID:String>

					}

				}
			}
			/*if(nameList.isEmpty()) {
			System.out.println("Strange");
			}*/
			return nameList;
		//}
	}
	
	public LinkedList<ASTChild> lookupASTChildren(IProject project, String packageName) {
		LinkedList<ASTChild> childList = new LinkedList<ASTChild>();
		//synchronized (getASTRootForLock(project)) {
			TypeDecl decl = getTypeDecl(project, packageName);

			if (decl == null) {
				// We didn't find a match, so we return an empty list
				return childList;
			}

			if(decl instanceof ASTDecl) {
				ASTDecl astDecl = (ASTDecl)decl;
				for(Iterator iter = astDecl.components().iterator(); iter.hasNext(); ) {
					ASTChild c = (ASTChild)iter.next();
					childList.add(c);				
				}
			}
			/*if(nameList.isEmpty()) {
			System.out.println("Strange");
			}*/
			return childList;
		//}
	}

	public TypeDecl getTypeDecl(IProject project, String packageName) {
		IProgram p = BuildUtil.getProgram(project);
		if (!(p instanceof Program))
			return null;
		Program program = (Program)p;
		
		synchronized (p.treeLockObject()) {

			int packageEndIndex = packageName.lastIndexOf('.');
			String tName = packageName.substring(packageEndIndex+1, packageName.length());
			if (packageEndIndex > 1) {
				packageName = packageName.substring(0, packageEndIndex);
			} else {
				packageName = "";
			}
			String innerName = "";
			int index = tName.indexOf('$');
			if (index > 0) {
				innerName = tName.substring(index + 1, tName.length());
				tName = tName.substring(0, index);
			}

			// Find outermost class
			TypeDecl decl = null;
			boolean keepOnLooking = true;
			while (keepOnLooking) {
				decl = program.lookupType(packageName, tName);
				if (decl != null) {
					keepOnLooking = false;
				} else {
					index = innerName.indexOf('$');
					if (index < 0) {
						// Search failed -- Cannot find a type declaration and 
						// there are no $ left in the type name
						return null;
					} else {
						tName += "$" + innerName.substring(0, index);
						innerName = innerName.substring(index + 1);
					}
				}
			}

			// Find innermost class
			if (innerName.length() > 0) {
				keepOnLooking = true;
				String nextInnerName = innerName;
				innerName = "";
				while (keepOnLooking) {
					// Try another name if possible
					if (nextInnerName.length() > 0) {
						index = nextInnerName.indexOf('$');
						if (index > 0) {
							innerName += "$" + nextInnerName.substring(0, index);
							nextInnerName = nextInnerName.substring(index + 1);
						} else {
							innerName = nextInnerName;
							nextInnerName = "";
						}
					} else {
						// No more names to test and we haven't found a match
						return null;
					}
					SimpleSet typeSet = decl.memberTypes(innerName);
					if (!typeSet.isEmpty()) {
						if (typeSet.size() > 1) {
							// TODO This should not happen ... Report this?
						}
						for (Iterator itr = typeSet.iterator(); itr.hasNext();) {
							decl = (TypeDecl)itr.next();
						}
						// No more inner classes to find
						if (nextInnerName.length() == 0) {
							keepOnLooking = false;
						} else {
							innerName = "";
						}
					}	
				}
			}
			return decl;
		}
	}
	
	/*
	protected ProgramInfo getProgramInfo(IProject project) {
		if (projectToNodeMap.containsKey(project)) {
			return projectToNodeMap.get(project);
		} else {
			if (isModelFor(project)) {
				try {
					ProgramInfo programInfo = new ProgramInfo();
					programInfo.buildConfiguration = readBuildConfiguration(project);
					programInfo.program = initProgram(project, programInfo.buildConfiguration);
					JastAddBuildConfiguration jastAddBuildConfig = new JastAddBuildConfiguration(project); 
					initProgram(programInfo.program, jastAddBuildConfig);
					projectToNodeMap.put(project, programInfo);
					nodeToProjectMap.put(programInfo.program, project);
					return programInfo;
				} catch (CoreException e) {
					String message = "Initializing program failed!"; 
					IStatus status = new Status(IStatus.ERROR, 
							Activator.JASTADDJ_PLUGIN_ID,
							IStatus.ERROR, message, e);
					Activator.INSTANCE.getLog().log(status);
					return null;
				} catch (Error e) {
					String message = "Initializing program failed!"; 
					IStatus status = new Status(IStatus.ERROR, 
							Activator.JASTADDJ_PLUGIN_ID,
							IStatus.ERROR, message, e);
					Activator.INSTANCE.getLog().log(status);
					return null;
				}
			}
		}
		return null;
	}
	*/
		
	private static void clearProgram(ASTNode node) {
		node.flushCache();
		for(int i = 0; i < node.getNumChild(); i++) {
			ASTNode child = node.getChild(i);
			node.setChild(null, i);
			child.setParent(null);
			clearProgram(node.getChild(i));
		}
		
	}
	
	protected boolean updateModel(IDocument document, String fileName, IProject project) {
		boolean fireEvent = true;
		JastAddJBuildConfiguration buildConfiguration = BuildUtil.getBuildConfiguration(project);
		if (buildConfiguration == null)
			return false;
		JastAddBuildConfiguration jastAddBuildConfig = new JastAddBuildConfiguration(project);
		
		// Regenerate scanner or parser if there was a change in a flex or parser file
		if (fileName.endsWith(".flex")) {
			buildJFlexScanner(project, jastAddBuildConfig);
		} else if (fileName.endsWith(".parser")) {
			buildBeaverParser(project, jastAddBuildConfig);
		}
		
		IProgram program = BuildUtil.getProgram(project);
		//super.updateModel(document, fileName, project);

		try {
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
			if(fileName.endsWith(".ast"))
				program.addSourceFile(fileName);
			else
				fireEvent = addSourceFileWithRecovery(project, program, document, fileName);

			if(program instanceof Program) {
				((Program)program).flushIntertypeDecls();
			}
		} catch (Exception e) {
			String message = "Failed to update model!"; 
			IStatus status = new Status(IStatus.ERROR, 
					Activator.JASTADDJ_PLUGIN_ID,
					IStatus.ERROR, message, e);
			Activator.INSTANCE.getLog().log(status);

		} catch (Throwable e) {
			String message = "Failed to update model!"; 
			IStatus status = new Status(IStatus.ERROR, 
					Activator.JASTADDJ_PLUGIN_ID,
					IStatus.ERROR, message, e);
			Activator.INSTANCE.getLog().log(status);

		}
		ProgramInfo info = BuildUtil.getProgramInfo(project);
		if (info != null) {
			info.changed();
		}
		return fireEvent;
	}

	private long getTimeStamp(String name, IProject project) {
		IFile oldFile = project.getFile(name);
		if(oldFile.exists()) {
			return oldFile.getLocalTimeStamp();
		}
		/*
		File file = new File(name);
		if(file.exists())
			return file.lastModified();
			*/
		return Long.MIN_VALUE;
	}
	
	private void buildJFlexScanner(IProject project, JastAddBuildConfiguration buildConfig) {
		String flexFileName = buildConfig.flex.getOutputFolder() + File.separator + "Scanner.flex";
		
		long lastModified = getTimeStamp(flexFileName, project);
		boolean needsUpdate = false;
		for(PathEntry p : buildConfig.flex.entries()) {
			long stamp = getTimeStamp(p.getPath(), project);
			if(stamp > lastModified)
				needsUpdate = true;
		}
		if(!needsUpdate)
			return;
		
		concatFiles(project, buildConfig.flex.entries(), flexFileName);
		IFile file = project.getFile(flexFileName); 
		flexFileName = file.getLocation().toOSString();
		File jFlexFile = new File(flexFileName);
		if (jFlexFile.exists()) {
			ArrayList<String> args = new ArrayList<String>();
			args.add(flexFileName);
			args.add("--nobak");
			JFlex.Main.main(args.toArray(new String[] { })); //.generate(jFlexFile);
		} else {
			String message = "Cannot find jflex file: " + flexFileName; 
			IStatus status = new Status(IStatus.ERROR, 
					Activator.JASTADDJ_PLUGIN_ID,
					IStatus.ERROR, message, new Throwable(message));
			Activator.INSTANCE.getLog().log(status);

		}
	}

	private void buildBeaverParser(IProject project, JastAddBuildConfiguration buildConfig) {
		String parserName = buildConfig.parser.getParserName();
		if (parserName == null)
			parserName = "Parser";
		String parserFileName = buildConfig.parser.getOutputFolder() + File.separator + "Parser.parser";
		String beaverFileName = buildConfig.parser.getOutputFolder() + File.separator + parserName + ".beaver";
		String rawBeaverFileName = buildConfig.parser.getOutputFolder() + File.separator + parserName + "_raw.beaver";
		
		long lastModified = getTimeStamp(parserFileName, project);
		boolean needsUpdate = false;
		for(PathEntry p : buildConfig.parser.entries())
			if(getTimeStamp(p.getPath(), project) > lastModified)
				needsUpdate = true;
		if(!needsUpdate)
			return;
		
		try {
			concatFiles(project, buildConfig.parser.entries(), parserFileName);
			if (convertToBeaverSpec(project, parserFileName, rawBeaverFileName)) {
				
				// Concatenated beaver init file with generated beaver file
				LinkedList<PathEntry> beaverList = new LinkedList<PathEntry>();
				PathEntry entry = new FolderList.FileEntry();
				entry.setPath("src/parser/beaver.input");
				beaverList.add(entry);
				entry = new FolderList.FileEntry();
				entry.setPath(rawBeaverFileName);
				beaverList.add(entry);
				concatFiles(project, beaverList, beaverFileName);
				
				IFile file = project.getFile(beaverFileName);
				ArrayList<String> args = new ArrayList<String>();
				args.add("-c");
				args.add("-t");
				args.add("-w");
				args.add(file.getLocation().toOSString());
				beaver.comp.run.Make.main(args.toArray(new String[] { }));	
			}
		} catch (IOException e) {
			String message = "Problem generating parser"; 
			IStatus status = new Status(IStatus.ERROR, 
					Activator.JASTADDJ_PLUGIN_ID,
					IStatus.ERROR, message, e);
			Activator.INSTANCE.getLog().log(status);
		} catch (CoreException e) {
			String message = "Problem generating parser"; 
			IStatus status = new Status(IStatus.ERROR, 
					Activator.JASTADDJ_PLUGIN_ID,
					IStatus.ERROR, message, e);
			Activator.INSTANCE.getLog().log(status);

		} catch (beaver.Parser.Exception e) {
			String message = "Problem generating parser"; 
			IStatus status = new Status(IStatus.ERROR, 
					Activator.JASTADDJ_PLUGIN_ID,
					IStatus.ERROR, message, e);
			Activator.INSTANCE.getLog().log(status);
		}
		
	}
	
	private void concatFiles(IProject project, java.util.List<PathEntry> entries, String targetFileName) {
		try {
			IFile targetFile = project.getFile(targetFileName);
			boolean firstFile = true;
			for (PathEntry entry : entries) {
				IFile srcFile = project.getFile(entry.getPath());
				InputStream stream = new FileInputStream(srcFile.getLocation().toOSString());
				//InputStream stream = srcFile.getContents(true);
				if (firstFile) {
					if (!targetFile.exists()) {
						targetFile.create(stream, true, null);
					}
					targetFile.setContents(stream, true, false, null);
					firstFile = false;
				} else {
					targetFile.appendContents(stream, true, false, null);
				}
				stream.close();
			}
		} catch (IOException e) {
			String message = "Problem concatenating files"; 
			IStatus status = new Status(IStatus.ERROR, 
					Activator.JASTADDJ_PLUGIN_ID,
					IStatus.ERROR, message, e);
			Activator.INSTANCE.getLog().log(status);
		} catch (CoreException e) {
			String message = "Problem concatenating files"; 
			IStatus status = new Status(IStatus.ERROR, 
					Activator.JASTADDJ_PLUGIN_ID,
					IStatus.ERROR, message, e);
			Activator.INSTANCE.getLog().log(status);
		}
	}

  	private boolean convertToBeaverSpec(IProject project, String parserSpec, String beaverSpec) throws CoreException, IOException, Exception {
  		IFile parserFile = project.getFile(parserSpec);
  		IFile beaverFile = project.getFile(beaverSpec);
  		AST.ASTNode.sourceName = parserFile.getLocation().toOSString();
  		FileReader stream = new FileReader(parserFile.getLocation().toOSString());
  		parser.GrammarScanner scanner = new parser.GrammarScanner(stream);
  		parser.GrammarParser parser = new parser.GrammarParser();
  		Object o = parser.parse(scanner);
  		stream.close();
  		AST.Grammar root = (AST.Grammar)o;
  		Collection c = root.errorCheck();
  		if(!c.isEmpty()) {
  			for(Iterator iter = c.iterator(); iter.hasNext(); ) {
  				String message = "There were errors in " + parserFile + ", " + iter.next(); 
  				IStatus status = new Status(IStatus.ERROR, 
  						Activator.JASTADDJ_PLUGIN_ID,
  						IStatus.ERROR, message, new Throwable(message));
  				Activator.INSTANCE.getLog().log(status);
  				return false;
  			}
  		}
  		
  		// Done the old way because root.pp takes a PrintStream
  		if (beaverFile.exists()) {
  			beaverFile.delete(true, null);
  		}
  		FileOutputStream os = new FileOutputStream(beaverFile.getLocation().toOSString());
		PrintStream out = new PrintStream(os);
		root.pp(out);
		out.close();

  		return true;
  	}
  	
  	/*
	protected String[] filterNames = {"flex.xml", "parser.xml", "jastadd.xml"};
	
	public boolean filterInExplorer(String resourceName) {
		if (resourceName.endsWith("~")) {
			return true;
		}
		for (int i = 0; i < filterNames.length; i++) {
			if (resourceName.equals(filterNames[i])) {
				return true;
			}
		}
		return super.filterInExplorer(resourceName);
	}
	*/

  	
  	public void checkForErrors(IProject project, IProgressMonitor monitor) {
  		/*
		try {
			try {				
				deleteErrorMarkers(PARSE_ERROR_MARKER_TYPE, project);
				deleteErrorMarkers(ERROR_MARKER_TYPE, project);
				JastAddJBuildConfiguration buildConfiguration = readBuildConfiguration(project);
				JastAddBuildConfiguration jastAddBuildConfig = new JastAddBuildConfiguration(project);
				
				// Generate scanner and parser
				buildJFlexScanner(project, jastAddBuildConfig);
				buildBeaverParser(project, jastAddBuildConfig);
				Program program = (Program) initProgram(project, buildConfiguration);
				if (program == null)
					return;
				Map<String,IFile> map = sourceMap(project, buildConfiguration);							
				if (map != null) {
					monitor.beginTask("Building files in project " + project.getName(), 100);
					if (monitor.isCanceled()) {
						return;
					}
					SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 50);
					subMonitor.beginTask("", map.keySet().size());
					for(String fileName : map.keySet()) {
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
							updateErrorsInFile(unit, unitFile, true);
							subMonitor.worked(1);
							if (monitor.isCanceled()) {
								return;
							}
						}
					}
					subMonitor.done();
				}
			} catch (CoreException e) {
				addErrorMarker(project, "Error check failed because: "
						+ e.getMessage(), -1, IMarker.SEVERITY_ERROR);
				logCoreException(e);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			logError(e, "Error check failed");
		} finally {
			monitor.done();
		}
		*/
	}

	public Collection recoverAndCompletion(int documentOffset,
			StringBuffer buf, IProject project, String fileName,
			IJastAddNode node, String filter, String leftContent)
			throws IOException, java.lang.Exception {
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
			if (node instanceof org.jastadd.plugin.jastadd.generated.AST.Access) {
				org.jastadd.plugin.jastadd.generated.AST.Access n = 
					(org.jastadd.plugin.jastadd.generated.AST.Access) node;
				System.out.println("Automatic recovery");
				System.out.println(n.getParent().getParent().dumpTree());
				return n.completion(filter);
			} else if (node instanceof org.jastadd.plugin.jastadd.generated.AST.ASTNode) {
				org.jastadd.plugin.jastadd.generated.AST.ASTNode n = 
					(org.jastadd.plugin.jastadd.generated.AST.ASTNode) node;
				System.out.println("Manual recovery");
				org.jastadd.plugin.jastadd.generated.AST.Expr newNode;
				
				if (leftContent.length() != 0) {
					
					String nameWithParan = "(" + leftContent + ")";
					ByteArrayInputStream is = new ByteArrayInputStream(
							nameWithParan.getBytes());
					org.jastadd.plugin.jastadd.scanner.JavaScanner scanner = 
						new org.jastadd.plugin.jastadd.scanner.JavaScanner(
							new scanner.Unicode(is));
					
					Object obj = new org.jastadd.plugin.jastadd.parser.JavaParser().parse(
							scanner, org.jastadd.plugin.jastadd.parser.JavaParser.AltGoals.expression);
					newNode = (org.jastadd.plugin.jastadd.generated.AST.Expr) 
						((org.jastadd.plugin.jastadd.generated.AST.ParExpr)obj)
						.getExprNoTransform();
					
					newNode = newNode.qualifiesAccess(
							new org.jastadd.plugin.jastadd.generated.AST.MethodAccess("X",
							new org.jastadd.plugin.jastadd.generated.AST.List()));
					
				} else {
					newNode = new org.jastadd.plugin.jastadd.generated.AST.MethodAccess("X", 
							new org.jastadd.plugin.jastadd.generated.AST.List());
				}
				
				int childIndex = n.getNumChild();
				n.addChild(newNode);
				n = n.getChild(childIndex);
				if (n instanceof org.jastadd.plugin.jastadd.generated.AST.Access)
					n = ((org.jastadd.plugin.jastadd.generated.AST.Access) n).lastAccess();
				// System.out.println(node.dumpTreeNoRewrite());

				// Use the connection to the dummy AST to do name
				// completion
				return n.completion(filter);
			}
			return new ArrayList();
		}
	}

}
