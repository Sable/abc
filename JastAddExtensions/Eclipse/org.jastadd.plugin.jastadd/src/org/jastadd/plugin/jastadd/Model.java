package org.jastadd.plugin.jastadd;

import java.io.ByteArrayInputStream;
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
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.editor.highlight.JastAddColors;
import org.jastadd.plugin.jastadd.generated.AST.ASTNode;
import org.jastadd.plugin.jastadd.generated.AST.Access;
import org.jastadd.plugin.jastadd.generated.AST.AttributeDecl;
import org.jastadd.plugin.jastadd.generated.AST.BytecodeParser;
import org.jastadd.plugin.jastadd.generated.AST.CompilationUnit;
import org.jastadd.plugin.jastadd.generated.AST.Expr;
import org.jastadd.plugin.jastadd.generated.AST.JavaParser;
import org.jastadd.plugin.jastadd.generated.AST.List;
import org.jastadd.plugin.jastadd.generated.AST.MethodAccess;
import org.jastadd.plugin.jastadd.generated.AST.MethodDecl;
import org.jastadd.plugin.jastadd.generated.AST.ParExpr;
import org.jastadd.plugin.jastadd.generated.AST.Problem;
import org.jastadd.plugin.jastadd.generated.AST.Program;
import org.jastadd.plugin.jastadd.generated.AST.SimpleSet;
import org.jastadd.plugin.jastadd.generated.AST.TypeDecl;
import org.jastadd.plugin.jastadd.properties.JastAddBuildConfiguration;
import org.jastadd.plugin.jastadd.properties.FolderList.PathEntry;
import org.jastadd.plugin.jastaddj.AST.ICompilationUnit;
import org.jastadd.plugin.jastaddj.AST.IProgram;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.model.JastAddJModel;
import org.jastadd.plugin.model.repair.LexicalNode;
import org.jastadd.plugin.model.repair.Recovery;
import org.jastadd.plugin.model.repair.SOF;
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
		try {
			Map<String,IFile> map = sourceMap(project, buildConfiguration);
			for(String fileName : map.keySet())
				program.addSourceFile(fileName);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return program;	   
	}
	
	@Override
	protected void reinitProgram(IProject project, IProgram program, JastAddJBuildConfiguration buildConfiguration) {
		Program realProgram = (Program)program;
		// Init
		realProgram.options().initOptions();
		realProgram.options().addKeyValueOption("-classpath");
		realProgram.options().addKeyValueOption("-bootclasspath");
		realProgram.options().addKeyValueOption("-d");
		addBuildConfigurationOptions(project, realProgram, buildConfiguration);   
	}	
	
	public ArrayList<String> lookupJVMName(IProject project, String packageName) {
		ArrayList<String> nameList = new ArrayList<String>();
		
		IProgram p = getProgram(project);
		if (!(p instanceof Program))
			return nameList;
		Program program = (Program)p;
		
		int packageEndIndex = packageName.lastIndexOf('.');
		String tName = packageName.substring(packageEndIndex+1, packageName.length());
		packageName = packageName.substring(0, packageEndIndex);
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
					return nameList;
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
					return nameList;
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
		// Find attribute declaration
		AttributeDecl aDecl = null;
		//if(decl.name().equals("Block") && decl.memberMethods("b").isEmpty())
		//	System.out.println("Strange");
		for (Iterator itr = decl.methodsIterator(); itr.hasNext();) {
			MethodDecl mDecl = (MethodDecl)itr.next();
		//	System.out.println(mDecl.signature());
			if (mDecl instanceof AttributeDecl) {
				aDecl = (AttributeDecl)mDecl;
				nameList.add(aDecl.name());
			}
		}
		/*if(nameList.isEmpty()) {
			System.out.println("Strange");
		}*/
		return nameList;
	}
	
	protected void completeBuild(IProject project) {
		// Build a new project from saved files only.
		try {
			try {
				deleteErrorMarkers(ERROR_MARKER_TYPE, project);
				deleteErrorMarkers(PARSE_ERROR_MARKER_TYPE, project);

				JastAddJBuildConfiguration buildConfiguration = readBuildConfiguration(project);
				JastAddBuildConfiguration jastAddBuildConfig = new JastAddBuildConfiguration(project); 

				// Generate scanner and parser
				buildJFlexScanner(project, jastAddBuildConfig.flex.entries());
				buildBeaverParser(project, jastAddBuildConfig.parser.entries());

				Program program = (Program) initProgram(project, buildConfiguration);
				if (program == null)
					return;

				Map<String, IFile> map = sourceMap(project, buildConfiguration);
				boolean build = true;
				for (Iterator iter = program.compilationUnitIterator(); iter.hasNext();) {
					ICompilationUnit unit = (ICompilationUnit) iter.next();

					if (unit.fromSource()) {
						Collection errors = unit.parseErrors();
						Collection warnings = new LinkedList();
						if (errors.isEmpty()) { // only run semantic checks if
							// there are no parse errors
							unit.errorCheck(errors, warnings);
						}
						if (!errors.isEmpty())
							build = false;
						errors.addAll(warnings);
						if (!errors.isEmpty()) {
							for (Iterator i2 = errors.iterator(); i2.hasNext();) {
								Problem error = (Problem) i2.next();
								int line = error.line();
								int column = error.column();
								String message = error.message();
								IFile unitFile = map.get(error.fileName());
								int severity = IMarker.SEVERITY_INFO;
								if (error.severity() == Problem.Severity.ERROR)
									severity = IMarker.SEVERITY_ERROR;
								else if (error.severity() == Problem.Severity.WARNING)
									severity = IMarker.SEVERITY_WARNING;
								if (error.kind() == Problem.Kind.LEXICAL
										|| error.kind() == Problem.Kind.SYNTACTIC) {
									addParseErrorMarker(unitFile, message,
											line, column, severity);
								} else if (error.kind() == Problem.Kind.SEMANTIC) {
									addErrorMarker(unitFile, message, line,
											severity);
								}
							}
						}
						if (build) {
							// unit.java2Transformation();
							// unit.generateClassfile();
						}
					}
				}

				// Use for the bootstrapped version of JastAdd

				if (build) {
					program.generateIntertypeDecls();
					program.transformation();
					program.generateClassfile();
				}
			} catch (CoreException e) {
				addErrorMarker(project, "Build failed because: "
						+ e.getMessage(), -1, IMarker.SEVERITY_ERROR);
				logCoreException(e);
			}
		} catch (Throwable e) {
			logError(e, "Build failed!");
		}
	}
	
	public Collection recoverCompletion(int documentOffset, String[] linePart, StringBuffer buf, IProject project, String fileName, IJastAddNode node) throws IOException, Exception {
		if(node == null) {
			// Try a structural recovery
			/* Old recovery 
			 documentOffset += (new JastAddStructureModel(buf)).doRecovery(documentOffset); // Return recovery offset change
			 */
			/* New recovery */
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
				org.jastadd.plugin.jastadd.scanner.JavaScanner scanner = new org.jastadd.plugin.jastadd.scanner.JavaScanner(new scanner.Unicode(is));
				newNode = (Expr)((ParExpr)new org.jastadd.plugin.jastadd.parser.JavaParser().parse(
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
		return new ArrayList();
	}	
	
	protected void updateModel(IDocument document, String fileName, IProject project) {
		JastAddJBuildConfiguration buildConfiguration = getBuildConfiguration(project);
		if (buildConfiguration == null)
			return;
		JastAddBuildConfiguration jastAddBuildConfig = new JastAddBuildConfiguration(project);
		
		// Regenerate scanner or parser if there was a change in a flex or parser file
		if (fileName.endsWith(".flex")) {
			buildJFlexScanner(project, jastAddBuildConfig.flex.entries());
		} else if (fileName.endsWith(".parser")) {
			buildBeaverParser(project, jastAddBuildConfig.parser.entries());
		}
		
		IProgram program = getProgram(project);
		if(program instanceof Program) {
			((Program)program).flushIntertypeDecls();
		}
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
			StringBuffer buf = new StringBuffer(document.get());
			
			// recover the current document if needed
			//SOF sof = getRecoveryLexer().parse(buf);
			//Recovery.doRecovery(sof);
			//buf = Recovery.prettyPrint(sof);
			
			// build the current document
			program.addSourceFile(fileName, buf.toString());
		} catch (Throwable e) {
			logError(e, "Updating model failed!");
		}
	}

	
	private void buildJFlexScanner(IProject project, java.util.List<PathEntry> entries) {
		String flexFileName = "Scanner.flex";
		concatFiles(project, entries, flexFileName);
		IFile file = project.getFile(flexFileName); 
		flexFileName = file.getLocation().toOSString();
		File jFlexFile = new File(flexFileName);
		if (jFlexFile.exists()) {
			JFlex.Main.generate(jFlexFile);
		} else {
			logError(new Throwable("Cannot find jflex file: " + flexFileName), 
			"Problem generating scanner");
		}
	}

	private void buildBeaverParser(IProject project, java.util.List<PathEntry> entries) {
		String parserFileName = "Parser.parser";
		String beaverFileName = "Parser.beaver";
		try {
			concatFiles(project, entries, parserFileName);
			if (convertToBeaverSpec(project, parserFileName, beaverFileName)) {
				IFile file = project.getFile(beaverFileName);
				beaver.comp.run.Make.main(new String[] {file.getLocation().toOSString()});
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
			targetFile.delete(true, null);
			boolean firstFile = true;
			for (PathEntry entry : entries) {
				IFile srcFile = project.getFile(entry.getPath());
				InputStream stream = srcFile.getContents(true);
				if (firstFile) {
					targetFile.create(stream, true, null);
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
}
