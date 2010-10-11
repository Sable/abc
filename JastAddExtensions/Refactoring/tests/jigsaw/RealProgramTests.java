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
	
	private static Program checkProgram(Program prog) {
		assertNotNull(prog);
		for(AccessibilityConstraint ac : prog.accessibilityConstraints())
			if(!ac.isSolved())
				fail();
		return prog;
	}
	
	private String orig;
	private void exhaustivelyChangeParameterTypes(Program prog, boolean checkUndo) {
		Collection<MethodDecl> meths = prog.sourceMethods();
		System.out.println(meths.size()+" source methods");
		orig = checkUndo ? prog.toString() : null;
		for(MethodDecl md : meths) {
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
	
	private Collection<Collection<MethodDecl>> findOverloadedMethods(Program prog) {
		Collection<Collection<MethodDecl>> res = new LinkedList<Collection<MethodDecl>>();
		findOverloadedMethods(prog, res);
		return res;
	}

	private void findOverloadedMethods(ASTNode nd, Collection<Collection<MethodDecl>> res) {
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
							res.add(mds);
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
}
