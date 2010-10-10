package tests.jigsaw;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import junit.framework.TestCase;
import tests.CompileHelper;
import AST.ASTNode;
import AST.ClassDecl;
import AST.ClassDeclSubstituted;
import AST.CompilationUnit;
import AST.MethodDecl;
import AST.ParameterDeclaration;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class RealProgramTests extends TestCase {
	private static String HOME = System.getProperty("user.home");
	
	// NB: adjust the following paths for your machine
	
	private static Program compileJigsaw() throws Exception {
		return CompileHelper.buildProjectFromClassPathFile(new File(HOME + File.separator + "Jigsaw" 
																		 + File.separator + ".classpath"));
	}
	
	private static Program compileJHotDraw() throws Exception {
		return CompileHelper.buildProjectFromClassPathFile(new File(HOME + File.separator + "JHotDraw 7.5.1"
																		 + File.separator + ".classpath"));
	}
	
	private static Program compileXalan() throws Exception {
		return CompileHelper.buildProjectFromClassPathFile(new File(HOME + File.separator + "RefactoringForConcurrency" 
																		 + File.separator + "trunk" 
																		 + File.separator + "concurrency-benchmarks" 
																		 + File.separator + "xalan-j_2_4_1" 
																		 + File.separator + ".classpath"));
	}
	
	public static void main(String[] args) {
		new RealProgramTests().test3();
	}
	
	public void test1() throws Exception {
		Program prog = compileJigsaw();
		exhaustivelyChangeParameterTypes(prog);
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
	
	public void test2() {
		try {
			Program prog = compileJigsaw();
			//String orig = prog.toString();
			assertNotNull(prog);
			TypeDecl tp = prog.findType("org.w3c.www.http", "ChunkedInputStream");
			assertNotNull(tp);
			MethodDecl md = (MethodDecl)tp.methodsSignature("read(byte[], int, int)");
			assertNotNull(md);
			try {
				int idx = 0;
				ParameterDeclaration pd = md.getParameter(idx);
				Program.startRecordingASTChangesAndFlush();
				long start = System.currentTimeMillis();
				md.getParameter(idx).changeType(prog.findType("java.lang", "Cloneable"));
				long elapsed = System.currentTimeMillis()-start;
				System.out.println("refactoring took " + elapsed + "ms");
				LinkedList errors = new LinkedList();
				prog.errorCheck(errors);
				if(!errors.isEmpty())
					System.out.println(errors);
				else
					System.out.println("OK");
			} catch(RefactoringException rfe) {
				System.out.println(rfe.getMessage());
			} finally {
				Program.undoAll();
				prog.flushCaches();
				//assertEquals(orig, prog.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void test3() {
		try {
			Program prog = compileJHotDraw();
			Collection<MethodDecl> meths = prog.sourceMethods();
			System.out.println(meths.size()+" source methods");
			int cnt=0;
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
							System.out.println("refactoring took " + elapsed + "ms");
							Program.undoAll();
							prog.flushCaches();
						}
					}
				}
				System.out.println(++cnt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void test4() {
		try {
			Program prog = compileJHotDraw();
			assertNotNull(prog);
			TypeDecl tp = prog.findType("org.jhotdraw.samples.pert.figures", "TaskFigure");
			assertNotNull(tp);
			MethodDecl md = tp.findMethod("setName");
			assertNotNull(md);
			TypeDecl newType = prog.findType("java.lang", "Object");
			assertNotNull(newType);
			try {
				Program.startRecordingASTChangesAndFlush();
				System.out.print("starting refactoring... ");
				long start = System.currentTimeMillis();
				md.getParameter(0).changeType(newType);
				long elapsed = System.currentTimeMillis()-start;
				System.out.println("done (" + elapsed + "ms)");
				LinkedList errors = new LinkedList();
				prog.errorCheck(errors);
				if(!errors.isEmpty())
					System.out.println("\n Refactoring introduced errors: " + errors);
			} catch(RefactoringException rfe) {
				System.out.println(rfe.getMessage());
			} finally {
				Program.undoAll();
				prog.flushCaches();
				//assertEquals(orig, prog.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}
	
	public void test5() throws Exception {
		Program prog = compileXalan();
		assertNotNull(prog);
		//exhaustivelyChangeParameterTypes(prog, true);
		TypeDecl td = prog.findType("org.apache.xalan.xsltc.compiler.util.IntType");
		assertNotNull(td);
		MethodDecl md = (MethodDecl)td.methodsSignature("translateTo(org.apache.xalan.xsltc.compiler.util.ClassGenerator, org.apache.xalan.xsltc.compiler.util.MethodGenerator, org.apache.xalan.xsltc.compiler.util.StringType)");
		assertNotNull(md);
		TypeDecl newType = prog.findType("org.apache.xalan.xsltc.compiler.util.Type");
		assertNotNull(newType);
		long start = System.currentTimeMillis();
		md.getParameter(2).changeType(newType);
		long elapsed = System.currentTimeMillis()-start;
		System.out.println("took " + elapsed + "ms");
		LinkedList errors = new LinkedList();
		prog.errorCheck(errors);
		if(!errors.isEmpty())
			System.out.println("\n Refactoring introduced errors: " + errors);
		/*Collection<Collection<MethodDecl>> overloaded_methods = findOverloadedMethods(prog);
		for(Collection<MethodDecl> mds : overloaded_methods) {
			if(mds.size() != 2)
				continue;
			Iterator<MethodDecl> iter = mds.iterator();
			MethodDecl md1 = iter.next(), md2 = iter.next();
			TypeDecl host = md1.hostType();
			for(int i=0;i<md1.getNumParameter()-1;++i) {
				for(int j=i+1;j<md1.getNumParameter();++j) {
					TypeDecl td1i = md1.getParameter(i).type(), td1j = md1.getParameter(j).type(),
							 td2i = md2.getParameter(i).type(), td2j = md2.getParameter(j).type();
					if(td1i==td2i && td1j!=td2j && td1j.subtype(td2j)) {
						System.out.println(host.fullName() + ":\n\t" + md1.signature() + ",\n\t" + md2.signature());
					} else if(td1i!=td2i && td1i.subtype(td2i) && td1j==td2j) {
						System.out.println(host.fullName() + ":\n\t" + md1.signature() + ",\n\t" + md2.signature());
					}
				}
			}
		}*/
	}

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
}
