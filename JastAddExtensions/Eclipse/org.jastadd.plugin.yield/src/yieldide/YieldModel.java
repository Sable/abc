package yieldide;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.jastadd.plugin.AST.IJastAddNode;
import yieldide.generated.AST.*;
import org.jastadd.plugin.jastaddj.AST.ICompilationUnit;
import org.jastadd.plugin.jastaddj.AST.IProgram;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.editor.JastAddJEditor;
import org.jastadd.plugin.jastaddj.model.JastAddJEditorConfiguration;
import org.jastadd.plugin.jastaddj.model.JastAddJModel;
import org.jastadd.plugin.model.repair.JastAddStructureModel;
import org.jastadd.plugin.resources.JastAddNature;

import beaver.Parser.Exception;

import yieldide.generated.AST.BytecodeParser;
import yieldide.generated.AST.CompilationUnit;
import yieldide.generated.AST.JavaParser;
import yieldide.generated.AST.Problem;
import yieldide.generated.AST.Program;

public class YieldModel extends JastAddJModel {
	

	public boolean isModelFor(IProject project) {
		try {
			if (project != null && project.isOpen() && project.isNatureEnabled(yieldide.YieldNature.NATURE_ID)) {
				return true;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public String getNatureID() {
		return YieldNature.NATURE_ID;
	}

	//*************** Protected methods
	
	@Override
	protected void initModel() {
		editorConfig = new JastAddJEditorConfiguration(this);
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
						return new yieldide.parser.JavaParser().parse(is, fileName);
					}
				}
		);
		program.initOptions();
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
		program.initOptions();
		program.addKeyValueOption("-classpath");
		program.addKeyValueOption("-bootclasspath");
		program.addKeyValueOption("-d");
		addBuildConfigurationOptions(project, realProgram, buildConfiguration);   
	}
	
	protected void completeBuild(IProject project) {
		// Build a new project from saved files only.
		try {
			try {
				deleteErrorMarkers(ERROR_MARKER_TYPE, project);
				deleteErrorMarkers(PARSE_ERROR_MARKER_TYPE, project);

				JastAddJBuildConfiguration buildConfiguration = readBuildConfiguration(project);

				Program program = (Program) initProgram(project,
						buildConfiguration);
				if (program == null)
					return;

				Map<String, IFile> map = sourceMap(project, buildConfiguration);
				boolean build = true;
				for (Iterator iter = program.compilationUnitIterator(); iter
						.hasNext();) {
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
				yieldide.scanner.JavaScanner scanner = new yieldide.scanner.JavaScanner(new scanner.Unicode(is));
				newNode = (Expr)((ParExpr)new yieldide.parser.JavaParser().parse(
						scanner, yieldide.parser.JavaParser.AltGoals.expression)
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
			// recover the current document
			StringBuffer buf = new StringBuffer(document.get());
			new JastAddStructureModel(buf).doRecovery(0);
			// build the current document
			program.addSourceFile(fileName, buf.toString());
		} catch (Throwable e) {
			logError(e, "Updating model failed!");
		}
	
		
		
	}
}
