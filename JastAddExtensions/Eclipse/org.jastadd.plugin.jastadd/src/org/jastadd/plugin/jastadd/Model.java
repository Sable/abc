

package org.jastadd.plugin.jastadd;

import java.io.File;
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
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.jastadd.plugin.editor.highlight.JastAddColors;
import org.jastadd.plugin.jastadd.generated.AST.ASTChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTDecl;
import org.jastadd.plugin.jastadd.generated.AST.ASTElementChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTListChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTOptionalChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTTokenChild;
import org.jastadd.plugin.jastadd.generated.AST.AttributeDecl;
import org.jastadd.plugin.jastadd.generated.AST.BytecodeParser;
import org.jastadd.plugin.jastadd.generated.AST.CompilationUnit;
import org.jastadd.plugin.jastadd.generated.AST.JavaParser;
import org.jastadd.plugin.jastadd.generated.AST.MethodDecl;
import org.jastadd.plugin.jastadd.generated.AST.Program;
import org.jastadd.plugin.jastadd.generated.AST.SimpleSet;
import org.jastadd.plugin.jastadd.generated.AST.TypeDecl;
import org.jastadd.plugin.jastadd.properties.JastAddBuildConfiguration;
import org.jastadd.plugin.jastadd.properties.FolderList.ParserFolderList;
import org.jastadd.plugin.jastadd.properties.FolderList.PathEntry;
import org.jastadd.plugin.jastaddj.AST.ICompilationUnit;
import org.jastadd.plugin.jastaddj.AST.IProgram;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.model.JastAddJModel;
import org.jastadd.plugin.resources.JastAddNature;

import beaver.Parser.Exception;

public class Model extends JastAddJModel {

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
	
	public java.util.List<String> getFileExtensions() {
		java.util.List<String> list = super.getFileExtensions();
		list.add("jrag");
		list.add("jadd");
		list.add("ast");
		list.add("flex");
		list.add("parser");
		return list;
	}
	
	@Override
	public String getNatureID() {
		return JastAddNature.NATURE_ID;
	}

	//*************** Protected methods
	
	@Override
	protected void initModel() {
		super.initModel();
		editorConfig = new EditorConfiguration(this);
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
	}	

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
		addBuildConfigurationOptions(project, program, buildConfiguration);
		program.options().setOption("-verbose");
		program.options().addKeyOption("-weave_inline");
		program.options().setOption("-weave_inline");
		return program;	   
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
			addBuildConfigurationOptions(project, realProgram, buildConfiguration);
			realProgram.options().setOption("-verbose");
		}
	}	
	
	public ArrayList<AttributeDecl> lookupJVMName(IProject project, String packageName) {
		ArrayList<AttributeDecl> nameList = new ArrayList<AttributeDecl>();
		synchronized (getASTRootForLock(project)) {
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
		}
	}
	
	public LinkedList<ASTChild> lookupASTChildren(IProject project, String packageName) {
		LinkedList<ASTChild> childList = new LinkedList<ASTChild>();
		
		synchronized (getASTRootForLock(project)) {
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
		}
	}

	public TypeDecl getTypeDecl(IProject project, String packageName) {
		IProgram p = getProgram(project);
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
	
	@Override
	protected void completeBuild(IProject project, IProgressMonitor monitor) {
		ProgramInfo info = getProgramInfo(project);
		if (info != null && info.hasChaged()) {
			
		    // Only build if there was a change
	
		// Build a new project from saved files only.
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
				// Synchronize in complete build ?
				synchronized(program) {

				Map<String,IFile> map = sourceMap(project, buildConfiguration);			
				
				monitor.beginTask("Building files in project " + project.getName(), 100);
				SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 30);
				subMonitor.beginTask("", map.keySet().size());
				if (map != null) {
					for(String fileName : map.keySet()) {
						program.addSourceFile(fileName);
						if (monitor.isCanceled()) {
							return;
						}
						subMonitor.worked(1);
					}
				}
				subMonitor.done();
				
				boolean build = true;
		
				subMonitor = new SubProgressMonitor(monitor, 30);
				subMonitor.beginTask("", map.keySet().size());
				for (Iterator iter = program.compilationUnitIterator(); iter.hasNext();) {
					ICompilationUnit unit = (ICompilationUnit) iter.next();

					if (unit.fromSource()) {
						IFile unitFile = map.get(unit.getFileName());
						build &= updateErrorsInFile(unit, unitFile, true);
						//if (build) {
							// unit.java2Transformation();
							// unit.generateClassfile();
						//}
						if (monitor.isCanceled()) {
							return;
						}
						subMonitor.worked(1);
					}
				}
				subMonitor.done();

				// Use for the bootstrapped version of JastAdd

				subMonitor = new SubProgressMonitor(monitor, 40);
				subMonitor.beginTask("", map.keySet().size()*3);
				if (build) {
					program.generateIntertypeDecls();
					if (monitor.isCanceled()) {
						return;
					}
					subMonitor.worked(1);
					
					program.transformation();
					if (monitor.isCanceled()) {
						return;
					}
					subMonitor.worked(1);
					
					for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
						CompilationUnit cu = (CompilationUnit)iter.next();
						if(cu.fromSource()) {
							for(int i = 0; i < cu.getNumTypeDecl(); i++) {
								cu.getTypeDecl(i).generateClassfile();
							}
							if (monitor.isCanceled()) {
								// Remove class files before return
								removeAllGeneratedClassFiles(project, buildConfiguration, monitor);
								return;
							}
							subMonitor.worked(1);
						}
					}
				}
				subMonitor.done();
				info.clearChanges();
				}
			} catch (CoreException e) {
				addErrorMarker(project, "Build failed because: "
						+ e.getMessage(), -1, IMarker.SEVERITY_ERROR);
				logCoreException(e);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			logError(e, "Build failed!");
		} finally {
			monitor.done();
		}
		
		} else {
			// Avoiding build because there was no change
		}
	}
	
	
	
	/*
	public Collection recoverCompletion(int documentOffset, String[] linePart, StringBuffer buf, IProject project, String fileName, IJastAddNode node) throws IOException, Exception {
		if(node == null) { 
			// Try a structural recovery
			// Old recovery 
			 //documentOffset += (new JastAddStructureModel(buf)).doRecovery(documentOffset); // Return recovery offset change
			 
			// New recovery 
			SOF sof = getRecoveryLexer().parse(buf);
			LexicalNode recoveryNode = Recovery.findNodeForOffset(sof, documentOffset);
			Recovery.doRecovery(sof);
			buf = Recovery.prettyPrint(sof);
			documentOffset += recoveryNode.getInterval().getPushOffset();
	
			node = findNodeInDocument(project, fileName, new Document(buf.toString()), documentOffset - 1);
			if (node == null) {
				System.out.println("Recovery failed");
				return new ArrayList();
			}
		}
		if(node instanceof Access) {
			synchronized (node.treeLockObject()) {
				Access n = (Access)node;
				System.out.println("Automatic recovery");
				System.out.println(n.getParent().getParent().dumpTree());
				return n.completion(linePart[1]);
			}
		} 
		else if(node instanceof ASTNode) {
			synchronized (node.treeLockObject()) {
				ASTNode n = (ASTNode)node;
				System.out.println("Manual recovery");
				Expr newNode;
				if(linePart[0].length() != 0) {
					String nameWithParan = "(" + linePart[0] + ")";
					ByteArrayInputStream is = new ByteArrayInputStream(nameWithParan.getBytes());
					org.jastadd.plugin.jastadd.scanner.JavaScanner scanner = new org.jastadd.plugin.jastadd.scanner.JavaScanner(new scanner.Unicode(is));
					newNode = ((ParExpr)new org.jastadd.plugin.jastadd.parser.JavaParser().parse(
							scanner, org.jastadd.plugin.jastadd.parser.JavaParser.AltGoals.expression)
					).getExprNoTransform();
					newNode = newNode.qualifiesAccess(new MethodAccess("X", new List()));
				}
				else {
					newNode = new MethodAccess("X", new List());
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
		}	
		return new ArrayList();
	}	
	*/
	protected void updateModel(IDocument document, String fileName, IProject project) {
		JastAddJBuildConfiguration buildConfiguration = getBuildConfiguration(project);
		if (buildConfiguration == null)
			return;
		JastAddBuildConfiguration jastAddBuildConfig = new JastAddBuildConfiguration(project);
		
		// Regenerate scanner or parser if there was a change in a flex or parser file
		if (fileName.endsWith(".flex")) {
			buildJFlexScanner(project, jastAddBuildConfig);
		} else if (fileName.endsWith(".parser")) {
			buildBeaverParser(project, jastAddBuildConfig);
		}
		
		IProgram program = getProgram(project);
		//super.updateModel(document, fileName, project);

		try {
			program.files().clear();
			Map<String,IFile> map = sourceMap(project, buildConfiguration);
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
				addSourceFileWithRecovery(project, program, document, fileName);

			if(program instanceof Program) {
				((Program)program).flushIntertypeDecls();
			}
		} catch (Exception e) {
			logError(e, "Failed to update model!");
		} catch (Throwable e) {
			e.printStackTrace();
			logError(e, "Updating model failed!");
		}
		JastAddJModel.ProgramInfo info = getProgramInfo(project);
		if (info != null) {
			info.changed();
		}
	}

	private long getTimeStamp(String name, IProject project) {
		IFile oldFile = project.getFile(name);
		if(oldFile.exists())
			name = oldFile.getLocation().toOSString();
		File file = new File(name);
		if(file.exists())
			return file.lastModified();
		return Long.MAX_VALUE;
	}
	
	private void buildJFlexScanner(IProject project, JastAddBuildConfiguration buildConfig) {
		String flexFileName = buildConfig.flex.getOutputFolder() + File.separator + "Scanner.flex";
		
		long lastModified = getTimeStamp(flexFileName, project);
		boolean needsUpdate = false;
		for(PathEntry p : buildConfig.flex.entries())
			if(getTimeStamp(p.getPath(), project) > lastModified)
				needsUpdate = true;
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
			logError(new Throwable("Cannot find jflex file: " + flexFileName), 
			"Problem generating scanner");
		}
	}

	private void buildBeaverParser(IProject project, JastAddBuildConfiguration buildConfig) {
		String parserName = ((ParserFolderList)buildConfig.parser).getParserName();
		if (parserName == null)
			parserName = "Parser";
		String parserFileName = buildConfig.parser.getOutputFolder() + File.separator + "Parser.parser";
		String beaverFileName = buildConfig.parser.getOutputFolder() + File.separator + parserName + ".beaver";
		
		long lastModified = getTimeStamp(parserFileName, project);
		boolean needsUpdate = false;
		for(PathEntry p : buildConfig.parser.entries())
			if(getTimeStamp(p.getPath(), project) > lastModified)
				needsUpdate = true;
		if(!needsUpdate)
			return;
		
		try {
			concatFiles(project, buildConfig.parser.entries(), parserFileName);
			if (convertToBeaverSpec(project, parserFileName, beaverFileName)) {
				IFile file = project.getFile(beaverFileName);
				ArrayList<String> args = new ArrayList<String>();
				args.add("-c");
				args.add("-t");
				args.add("-w");
				args.add(file.getLocation().toOSString());
				beaver.comp.run.Make.main(args.toArray(new String[] { }));		
			}
		} catch (IOException e) {
			logError(e, "Problem generating parser");
		} catch (CoreException e) {
			logError(e, "Problem generating parser");
		} catch (beaver.Parser.Exception e) {
			logError(e, "Problem generating parser");
		}
		
	}
	
	private void concatFiles(IProject project, java.util.List<PathEntry> entries, String targetFileName) {
		try {
			IFile targetFile = project.getFile(targetFileName);
			boolean firstFile = true;
			for (PathEntry entry : entries) {
				IFile srcFile = project.getFile(entry.getPath());
				InputStream stream = srcFile.getContents(true);
				if (firstFile) {
					if (!targetFile.exists()) {
						targetFile.create(stream, true, null);
					} else {
						targetFile.setContents(stream, true, false, null);
					}
					firstFile = false;
				} else {
					targetFile.appendContents(stream, true, false, null);
				}
				stream.close();
			}
		} catch (IOException e) {
			logError(e, "Problem concatenating files");
		} catch (CoreException e) {
			logError(e, "Problem concatenating files");
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
  				logError(new Throwable("There were errors in " + parserFile + ", " + iter.next()), 
  				"Problem converting to beaver format");
  				return false;
  			}
  		}
  		
  		// Done the old way because root.pp takes a PrintStream
  		java.io.File bFile = new File(beaverFile.getLocation().toOSString());
  		PrintStream out = new PrintStream(new FileOutputStream(bFile));
  		root.pp(out);
  		out.close();

  		return true;
  	}
  	
	protected String[] filterNames = {"flex.xml", "parser.xml"};
	
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
  	
  	/*
	public Collection recoverCompletion(int documentOffset, StringBuffer buf, 
			IProject project, String fileName, IJastAddNode node, String filter, 
			String leftContent, boolean withDot) throws IOException, Exception {
		synchronized (node.treeLockObject()) {
			if (node == null) {
				// Try recovery
				SOF sof = getRecoveryLexer().parse(buf);
				LexicalNode recoveryNode = Recovery.findNodeForOffset(sof, documentOffset);
				Recovery.doRecovery(sof);
				buf = Recovery.prettyPrint(sof);
				documentOffset += recoveryNode.getInterval().getPushOffset();			
				node = findNodeInDocument(project, fileName, new Document(buf.toString()), documentOffset - 1);
				if (node == null) {
					System.out.println("Structural recovery failed");
					return new ArrayList();
				}
			}
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
							new org.jastadd.plugin.jastadd.generated.AST.List()));
				} else {
					newNode = new MethodAccess("X", 
							new org.jastadd.plugin.jastadd.generated.AST.List());
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
}
