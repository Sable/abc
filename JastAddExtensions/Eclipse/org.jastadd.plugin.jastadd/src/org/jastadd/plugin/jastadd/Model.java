package org.jastadd.plugin.jastadd;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
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
import org.jastadd.plugin.jastadd.generated.AST.BytecodeParser;
import org.jastadd.plugin.jastadd.generated.AST.CompilationUnit;
import org.jastadd.plugin.jastadd.generated.AST.Expr;
import org.jastadd.plugin.jastadd.generated.AST.JavaParser;
import org.jastadd.plugin.jastadd.generated.AST.List;
import org.jastadd.plugin.jastadd.generated.AST.MethodAccess;
import org.jastadd.plugin.jastadd.generated.AST.ParExpr;
import org.jastadd.plugin.jastadd.generated.AST.Problem;
import org.jastadd.plugin.jastadd.generated.AST.Program;
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
		Program.initOptions();
		program.addKeyValueOption("-classpath");
		program.addKeyValueOption("-bootclasspath");
		program.addKeyValueOption("-d");
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
		Program.initOptions();
		program.addKeyValueOption("-classpath");
		program.addKeyValueOption("-bootclasspath");
		program.addKeyValueOption("-d");
		addBuildConfigurationOptions(project, realProgram, buildConfiguration);   
	}
	
	private boolean convertToBeaverSpec(String parserSpec, String beaverSpec) {
		try {
			String source = parserSpec;
			String dest = beaverSpec;
			File sourceFile = new File(source);
			File destFile = new File(dest);
			if(sourceFile.exists() && destFile.exists()
			   && sourceFile.lastModified() < destFile.lastModified()) {
				System.out.println("Parser specification " + dest + " need not be regenerated");
			} else {
				AST.ASTNode.sourceName = source;
				parser.GrammarScanner scanner = new parser.GrammarScanner(new FileReader(source));
				parser.GrammarParser parser = new parser.GrammarParser();
				Object o = parser.parse(scanner);
				AST.Grammar root = (AST.Grammar)o;
				Collection c = root.errorCheck();
				if(!c.isEmpty()) {
					for(Iterator iter = c.iterator(); iter.hasNext(); ) {
						logError(new Throwable(), "There were errors in " + source + ", " + iter.next());
						return false;
					}
				}
				FileOutputStream os = new FileOutputStream(dest);
				PrintStream out = new PrintStream(os);
				root.pp(out);
				out.close();
				//System.out.println("Parser specification " + dest + " generated from " + source);
			}
		} catch (FileNotFoundException e) {
			logError(e, "Convertion to beaver specification failed");
			return false;
		} catch (IOException e) {
			logError(e, "Convertion to beaver specification failed");
			return false;
		} catch (Exception e) {
			logError(e, "Convertion to beaver specification failed");
			return false;
		}
		return true;
	}
	
	protected void completeBuild(IProject project) {
		// Build a new project from saved files only.
		try {
			try {
				deleteErrorMarkers(ERROR_MARKER_TYPE, project);
				deleteErrorMarkers(PARSE_ERROR_MARKER_TYPE, project);

				JastAddJBuildConfiguration buildConfiguration = readBuildConfiguration(project);

				// Generate scanner
				// TODO Change ...
				/*
				String jFlexFileName = "/home/emma/runtime-New_configuration/JastAddExample/src/AST/DiagramScanner.flex";
				File jFlexFile = new File(jFlexFileName);
				if (jFlexFile.exists()) {
					JFlex.Main.generate(jFlexFile);
				} else {
					System.out.println("Cannot find jflex file: " + jFlexFileName);
				}
				
				// Convert parser specification to beaver specification
				// TODO Change ...
				String parserSpec = "/home/emma/runtime-New_configuration/JastAddExample/src/AST/DiagramParser.parser";
				String beaverSpec = "/home/emma/runtime-New_configuration/JastAddExample/src/AST/DiagramParser.beaver";
				convertToBeaverSpec(parserSpec, beaverSpec);
				
				// Generate parser
				beaver.comp.run.Make.main(new String[] {beaverSpec});
				*/
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
		//if (fileName.endsWith(".flex"))
		//	return;
		
		JastAddJBuildConfiguration buildConfiguration = getBuildConfiguration(project);
		if (buildConfiguration == null)
			return;
		
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
	
	/*
	@Override protected IJastAddNode getTreeRootNode(IProject project, String filePath) {
		if(filePath == null)
			return null;
		if (filePath.endsWith("ast")) {
			//System.out.println("Looking for tree root for: " + filePath);
			JastAddJBuildConfiguration buildConfiguration = getBuildConfiguration(project);
			if (buildConfiguration == null)
				return null;
			IProgram program = getProgram(project);
			if (program == null)
				return null;
			
			int i = 0;
			ArrayList<ICompilationUnit> cuList = new ArrayList<ICompilationUnit>();
			for (Iterator iter = program.compilationUnitIterator(); iter.hasNext();) {
				ICompilationUnit cu = (ICompilationUnit) iter.next();
				if (cu.fromSource()) {
					String name = cu.relativeName();
					if (name == null)
						System.out.println(cu);
					//System.out.println("CU: " + cu.relativeName());
					//System.out.println("\tpathName=" + cu.pathName());
					if (name.equals(filePath)) {
						//System.out.println("Looking for tree root in compilation unit: " + cu + ", i="+ i++);
						cuList.add(cu);
					}
				}
			}
			return cuList.get(cuList.size()-1);
		} else {
			return super.getTreeRootNode(project, filePath);
		}
	}
	*/
	
}
