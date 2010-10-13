package tests.jigsaw;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import junit.framework.TestCase;
import tests.CompileHelper;
import AST.ASTNode;
import AST.AccessibilityConstraint;
import AST.ClassDecl;
import AST.ClassDeclSubstituted;
import AST.CompilationUnit;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.SimpleSet;
import AST.TypeDecl;

public class RealProgramTests extends TestCase {
	private static String HOME = System.getProperty("user.home");
	
	// NB: adjust the following path for your machine
	private static Program compile(String benchmarkName) throws Exception {
		return CompileHelper.buildProjectFromClassPathFile(new File(HOME + File.separator + "JastAdd"
																		 + File.separator + "benchmarks"
																		 + File.separator + benchmarkName
																		 + File.separator + ".classpath"));
	}
	
	// 101 KSLOC
	private static Program compileJigsaw() throws Exception {
		return checkProgram(compile("Jigsaw"));
	}
	
	// 76 KSLOC
	private static Program compileJHotDraw() throws Exception {
		return checkProgram(compile("JHotDraw"));
	}
	
	// 110 KSLOC
	private static Program compileXalan() throws Exception {
		return checkProgram(compile("xalan"));
	}
	
	// 144 KSLOC
	private static Program compileHSQLDB() throws Exception {
		return checkProgram(compile("hsqldb"));	}

	// 49 KSLOC
	private static Program compileHadoop() throws Exception {
		return checkProgram(compile("hadoop-core"));
	}
	
	// 169 KSLOC
	private static Program compileTomcat() throws Exception {
		return checkProgram(compile("tomcat-6.0.x"));
	}
	
	//  62 KSLOC
	private static Program compileJGroups() throws Exception {
		return checkProgram(compile("jgroups"));
	}
	
	//  84 KSLOC
	private static Program compileLucene() throws Exception {
		return checkProgram(compile("lucene-3.0.1"));
	}
	
	//  41 KSLOC
	private static Program compileJMeter() throws Exception {
		return checkProgram(compile("jmeter"));
	}
	
	//  65 KSLOC
	private static Program compileServingXML() throws Exception {
		return checkProgram(compile("servingxml-1.1.2"));
	}
	
	//  12 KSLOC
	private static Program compileJaxen() throws Exception {
		return checkProgram(compile("org.jaxen 1.1.1"));
	}
	
	//  22 KSLOC
	private static Program compileHTMLParser() throws Exception {
		return checkProgram(compile("org.htmlparser 1.6"));
	}
	
	//   2 KSLOC
	private static Program compileJester() throws Exception {
		return checkProgram(compile("Jester1.37b"));
	}
	
	//   4 KSLOC
	private static Program compileJUnit381() throws Exception {
		return checkProgram(compile("JUnit 3.8.1"));
	}
	
	//   4 KSLOC
	private static Program compileJUnit382() throws Exception {
		return checkProgram(compile("JUnit 3.8.2"));
	}
	
	//   5 KSLOC
	private static Program compileJUnit45() throws Exception {
		return checkProgram(compile("junit4.5"));
	}
	
	//   2 KSLOC
	private static Program compileApacheCodec() throws Exception {
		return checkProgram(compile("org.apache.commons.codec 1.3"));
	}
	
	//   5 KSLOC
	private static Program compileApacheIO() throws Exception {
		return checkProgram(compile("org.apache.commons.io 1.4"));
	}
	
	//  23 KSLOC
	private static Program compileDraw2D() throws Exception {
		return checkProgram(compile("org.eclipse.draw2d 3.4.2"));
	}
	
	//  29 KSLOC
	private static Program compileClojure() throws Exception {
		return checkProgram(compile("clojure-1.1.0"));
	}
	
	// total: 1009 KSLOC
	
	private static Program checkProgram(Program prog) {
		assertNotNull(prog);
		for(AccessibilityConstraint ac : prog.accessibilityConstraints())
			if(!ac.isSolved())
				fail();
		return prog;
	}
	
	private String orig;
	private void exhaustivelyChangeParameterTypes(Program prog, boolean checkUndo) {
		Collection<MethodDecl> meths = findOverloadedSourceMethods(prog);
		System.out.println(meths.size() + " overloaded source methods");
		orig = checkUndo ? prog.toString() : null;
		for(MethodDecl md : findOverloadedSourceMethods(prog)) {
			for(int i=0;i<md.getNumParameter();++i) {
				TypeDecl tp = md.getParameter(i).type();
				for(TypeDecl stp : tp.supertypes()) {
					System.out.print("refactoring parameter #" + i + " of method " + md.hostType().typeName() + "." + md.signature() + " to " + stp.fullName() + "... ");
					long start = System.currentTimeMillis(), elapsed = -1;
					try {
						Program.startRecordingASTChangesAndFlush();
						md.getParameter(i).changeType(stp);
						elapsed = System.currentTimeMillis()-start;
						System.out.print("success; ");
						LinkedList errors = new LinkedList();
						prog.errorCheck(errors);
						if(!errors.isEmpty())
							System.out.println("\n Refactoring introduced errors: " + errors);
					} catch(RefactoringException rfe) {
						elapsed = System.currentTimeMillis()-start;
						System.out.print("failed (" + rfe.getMessage() + "); ");
					} finally {
						System.out.println(elapsed + "ms");
						Program.undoAll();
						prog.flushCaches();
						if(checkUndo)
							assertEquals(orig, prog.toString());
					}
				}
			}
		}
	}
	
	private void exhaustivelyChangeParameterTypes(Program prog) { exhaustivelyChangeParameterTypes(prog, false); }
	
	private Collection<MethodDecl> findOverloadedSourceMethods(Program prog) {
		Collection<MethodDecl> res = new LinkedList<MethodDecl>();
		findOverloadedMethods(prog, res);
		return res;
	}

	private void findOverloadedMethods(ASTNode nd, Collection<MethodDecl> res) {
		if(nd instanceof CompilationUnit && !((CompilationUnit)nd).fromSource())
			return;
		if(nd instanceof ClassDecl) {
			ClassDecl cd = (ClassDecl)nd;
			if(!cd.isParameterizedType() && !(cd instanceof ClassDeclSubstituted) && !cd.isArrayDecl()) {
				Map<String, Collection<MethodDecl>> mmap = cd.methodsNameMap();
				for(Map.Entry<String, Collection<MethodDecl>> e : mmap.entrySet()) {
					Collection<MethodDecl>[] sizemap = new Collection[50];
					for(MethodDecl md : e.getValue()) {
						if(md.getNumParameter() < 2)
							continue;
						if(md.getNumParameter() > 50)
							throw new Error("insanity");
						Collection<MethodDecl> mds = sizemap[md.getNumParameter()];
						if(mds == null)
							sizemap[md.getNumParameter()] = mds = new LinkedList<MethodDecl>();
						mds.add(md);
					}
					for(Collection<MethodDecl> mds : sizemap)
						if(mds != null && mds.size() > 1)
							for(MethodDecl md : mds)
								if(md.fromSource() && !md.isNative())
									res.add(md);
				}
			}
		}
		for(int i=0;i<nd.getNumChild();++i)
			findOverloadedMethods(nd.getChild(i), res);
	}

	private int countSourceTypes(ASTNode nd) {
		if(nd instanceof CompilationUnit && !((CompilationUnit)nd).fromSource())
			return 0;
		int cnt = 0;
		if(nd instanceof TypeDecl)
			++cnt;
		for(int i=0;i<nd.getNumChild();++i)
			cnt += countSourceTypes(nd.getChild(i));
		return cnt;
	}
	
	// ensure that all projects compile and accessibility constraints are initially satisfied
	public void testCompile() throws Exception {
		compileJigsaw();
		compileJHotDraw();
		compileHadoop();
		compileHSQLDB();
		compileXalan();
		compileTomcat();
		compileJGroups();
		compileLucene();
		compileJMeter();
		compileServingXML();
		compileJaxen();
		compileHTMLParser();
		compileJester();
		compileJUnit381();
		compileJUnit382();
		compileJUnit45();
		compileApacheCodec();
		compileApacheIO();
		compileDraw2D();
		compileClojure();
	}
	
	// exhaustively generalise parameter types
	public void testJigsaw() throws Exception {
		exhaustivelyChangeParameterTypes(compileJigsaw());
	}
	
	public void testJHotDraw() throws Exception {
		exhaustivelyChangeParameterTypes(compileJHotDraw());
	}
	
	public void testXalan() throws Exception {
		exhaustivelyChangeParameterTypes(compileXalan());
	}
	
	public void testHSQLDB() throws Exception {
		exhaustivelyChangeParameterTypes(compileHSQLDB());
	}
	
	public void testHadoop() throws Exception {
		exhaustivelyChangeParameterTypes(compileHadoop());
	}
	
	public void testTomcat() throws Exception {
		exhaustivelyChangeParameterTypes(compileTomcat());
	}
	
	public void testJGroups() throws Exception {
		exhaustivelyChangeParameterTypes(compileJGroups());
	}
	
	public void testLucene() throws Exception {
		exhaustivelyChangeParameterTypes(compileLucene());
	}
	
	public void testJMeter() throws Exception {
		exhaustivelyChangeParameterTypes(compileJMeter());
	}
	
	public void testServingXML() throws Exception {
		exhaustivelyChangeParameterTypes(compileServingXML());
	}
	
	public void testJaxen() throws Exception {
		exhaustivelyChangeParameterTypes(compileJaxen());
	}
	
	public void testHTMLParser() throws Exception {
		exhaustivelyChangeParameterTypes(compileHTMLParser());
	}
	
	public void testJester() throws Exception {
		exhaustivelyChangeParameterTypes(compileJester());
	}
	
	public void testJUnit381() throws Exception {
		exhaustivelyChangeParameterTypes(compileJUnit381());
	}
	
	public void testJUnit382() throws Exception {
		exhaustivelyChangeParameterTypes(compileJUnit382());
	}
	
	public void testJUnit45() throws Exception {
		exhaustivelyChangeParameterTypes(compileJUnit45());
	}
	
	public void testApacheCodec() throws Exception {
		exhaustivelyChangeParameterTypes(compileApacheCodec());
	}
	
	public void testApacheIO() throws Exception {
		exhaustivelyChangeParameterTypes(compileApacheIO());
	}
	
	public void testDraw2D() throws Exception {
		exhaustivelyChangeParameterTypes(compileDraw2D());
	}
	
	public void testClojure() throws Exception {
		exhaustivelyChangeParameterTypes(compileClojure());
	}
	
	public void generaliseParameterType(Program prog, String type, String sig, int idx, String newType) {
		TypeDecl td = prog.findType(type);
		assertNotNull(td);
		SimpleSet s = td.localMethodsSignature(sig);
		assertTrue(s instanceof MethodDecl);
		TypeDecl newtd = prog.findType(newType);
		((MethodDecl)s).getParameter(idx).changeType(newtd);
		LinkedList errors = new LinkedList();
		prog.errorCheck(errors);
		if(!errors.isEmpty())
			System.out.println("\n Refactoring introduced errors: " + errors);
	}
	
	public void testGeneraliseParameterType() throws Exception {
		generaliseParameterType(compileJUnit45(), "junit.framework.Assert", 
								"assertEquals(java.lang.String, java.lang.String, java.lang.String)", 
								0, "java.lang.Object");
	}
}
