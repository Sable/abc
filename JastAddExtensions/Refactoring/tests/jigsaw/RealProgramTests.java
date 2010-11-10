package tests.jigsaw;

import static tests.jigsaw.RealProgramTests.BenchmarkProgram.apachecodec;
import static tests.jigsaw.RealProgramTests.BenchmarkProgram.clojure;
import static tests.jigsaw.RealProgramTests.BenchmarkProgram.hadoop;
import static tests.jigsaw.RealProgramTests.BenchmarkProgram.jgroups;
import static tests.jigsaw.RealProgramTests.BenchmarkProgram.junit45;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import junit.framework.TestCase;
import tests.CompileHelper;
import tests.jigsaw.DiffMatchPatch.Diff;
import AST.ASTNode;
import AST.AccessibilityConstraint;
import AST.ClassDecl;
import AST.CompilationUnit;
import AST.MethodDecl;
import AST.Predicate;
import AST.Program;
import AST.RefactoringException;
import AST.SimpleSet;
import AST.TypeConstraint;
import AST.TypeDecl;

public class RealProgramTests extends TestCase {
	private static String HOME = System.getProperty("user.home");
	
	public static void main(String[] args) throws Exception {
		if(args.length >= 1 && args[0].equals("cpt")) {
			for(int i=1;i<args.length;++i) {
				BenchmarkProgram bp = BenchmarkProgram.valueOf(args[i]);
				exhaustivelyChangeParameterTypes(bp.compile());
			}
		} else if(args.length >= 1 && args[0].equals("pum")) {
			for(int i=1;i<args.length;++i) {
				BenchmarkProgram bp = BenchmarkProgram.valueOf(args[i]);
				exhaustivelyPullUpMethods(bp.compile());
			}
		} else if(args.length >= 6 && args[0].equals("generalise-parameter")) {
			BenchmarkProgram bp = BenchmarkProgram.valueOf(args[1]);
			String type = args[2];
			String sig = args[3];
			int idx = Integer.parseInt(args[4]);
			String newType = args[5];
			generaliseParameterType(bp.compile(), type, sig, idx, newType, true);
			pullUpMethod(bp.compile(), type, sig, true);
		} else if(args.length >= 4 && args[0].equals("pull-up")) {
			BenchmarkProgram bp = BenchmarkProgram.valueOf(args[1]);
			String type = args[2];
			String sig = args[3];
			pullUpMethod(bp.compile(), type, sig, true);
		} else {
			System.out.println(
					"Usage: generalise-parameter <benchmark name> <type name> <method signature> <parameter index> <new type name>\n" +
					"       pull-up <benchmark name> <type name> <method signature>");
			System.out.print("Available benchmarks:");
			for(BenchmarkProgram bp : BenchmarkProgram.values())
				System.out.print(" " + bp);
			System.out.println();
		}
	}
	
	// NB: adjust the following path for your machine
	private static Program compileBenchmark(String benchmarkName) throws Exception {
		return CompileHelper.buildProjectFromClassPathFile(new File(HOME + File.separator + "JastAdd"
																		 + File.separator + "benchmarks"
																		 + File.separator + benchmarkName
																		 + File.separator + ".classpath"));
	}
	
	static enum BenchmarkProgram {
		// 169 KSLOC
		tomcat {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("tomcat-6.0.x"));
			}
		},
		
		// 144 KSLOC
		hsqldb {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("hsqldb"));	
			}
		},

		// 110 KSLOC
		xalan {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("xalan"));
			}
		},
	
		// 101 KSLOC
		jigsaw {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("Jigsaw"));
			}
		},
	
		//  84 KSLOC
		lucene {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("lucene-3.0.1"));
			}
		},
	
		// 76 KSLOC
		jhotdraw {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("JHotDraw"));
			}
		},
		
		//  65 KSLOC
		servingxml {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("servingxml-1.1.2"));
			}
		},
		
		//  62 KSLOC
		jgroups {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("jgroups"));
			}
		},
			
		//  49 KSLOC
		hadoop {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("hadoop-core"));
			}
		},
		
		//  41 KSLOC
		jmeter {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("jmeter"));
			}
		},
		
		//  29 KSLOC
		clojure {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("clojure-1.1.0"));
			}
		},
			
		//  23 KSLOC
		draw2d {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("org.eclipse.draw2d 3.4.2"));
			}
		},
		
		//  22 KSLOC
		htmlparser {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("org.htmlparser 1.6"));
			}
		},
		
		//  12 KSLOC
		jaxen {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("org.jaxen 1.1.1"));
			}
		},
		
		//   5 KSLOC
		apacheio {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("org.apache.commons.io 1.4"));
			}
		},
		
		//   5 KSLOC
		junit45 {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("junit4.5"));
			}
		},
		
		//   4 KSLOC
		junit381 {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("JUnit 3.8.1"));
			}
		},
		
		//   4 KSLOC
		junit382 {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("JUnit 3.8.2"));
			}
		},
		
		//   2 KSLOC
		jester {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("Jester1.37b"));
			}
		},

		//   2 KSLOC
		apachecodec {
			public Program compile() throws Exception {
				return checkProgram(compileBenchmark("org.apache.commons.codec 1.3"));
			}
		};

		// total: 1009 KSLOC
		abstract Program compile() throws Exception;

		private static Program checkProgram(Program prog) {
			assertNotNull(prog);
			for(AccessibilityConstraint ac : prog.accessibilityConstraints())
				if(!ac.isSolved())
					fail();
			for(TypeConstraint constr : prog.typeConstraints(Predicate.TRUE))
				if(!constr.solved())
					fail();
			return prog;
		}

	}
	
	private static String orig;
	private static void exhaustivelyChangeParameterTypes(Program prog, boolean checkUndo) {
		Collection<MethodDecl> meths = prog.sourceMethods();
		System.out.println(meths.size() + " source methods");
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
	
	private static void exhaustivelyChangeParameterTypes(Program prog) { exhaustivelyChangeParameterTypes(prog, false); }
	
	
	private static void exhaustivelyPullUpMethods(Program prog, int cnt) {
		Collection<MethodDecl> meths = prog.sourceMethods();
		System.out.println(meths.size() + " source methods");
		for(MethodDecl md : meths) {
			if(cnt == 0)
				break;
			System.out.print("pulling up method " + md.fullName() + "... ");
			long start = System.currentTimeMillis(), elapsed = -1;
			try {
				Program.startRecordingASTChangesAndFlush();
				md.doPullUpWithRequired();
				elapsed = System.currentTimeMillis()-start;
				System.out.print("success; ");
				LinkedList errors = new LinkedList();
				prog.errorCheck(errors);
				if(!errors.isEmpty())
					System.out.println("\n Refactoring introduced errors: " + errors);
				prog.clearErrors();
			} catch(RefactoringException rfe) {
				elapsed = System.currentTimeMillis()-start;
				System.out.print("failed (" + rfe.getMessage() + "); ");
				// don't count trivial cases
				if(rfe.getMessage().equals("no fitting super class") || rfe.getMessage().startsWith("cannot insert"))
					++cnt;
			} catch(NullPointerException npe) {
				System.out.print("NPE! ");
			} catch(ArrayIndexOutOfBoundsException aioobe) {
				System.out.print("AIOOBE! ");
			} finally {
				System.out.println(elapsed + "ms");
				Program.undoAll();
				prog.flushCaches();
				--cnt;
			}
		}
	}
	
	private static void exhaustivelyPullUpMethods(Program prog) {
		exhaustivelyPullUpMethods(prog, -1);
	}
	
	// ensure that all projects compile and constraints are initially satisfied
	public void testCompile() throws Exception {
		for(BenchmarkProgram prog : BenchmarkProgram.values())
			prog.compile();
	}
	
	public void testExhaustivelyPullUp() throws Exception {
		exhaustivelyPullUpMethods(junit45.compile());
	}
	
	public void testExhaustivelyGeneraliseParameter() throws Exception {
		exhaustivelyChangeParameterTypes(jgroups.compile());
	}

	public static void generaliseParameterType(Program prog, String type, String sig, int idx, String newtype, boolean printReport) {
		TypeDecl newtd = prog.findType(newtype);
		assertNotNull(newtd);
		generaliseParameterType(prog, type, sig, idx, newtd, printReport);
	}

	public static void generaliseParameterType(Program prog, String type, String sig, int idx, TypeDecl newtd, boolean printReport) {
		TypeDecl td = prog.findType(type);
		assertNotNull(td);
		SimpleSet s = td.localMethodsSignature(sig);
		assertTrue(s instanceof MethodDecl);
		try {
			Program.startRecordingASTChangesAndFlush();
			((MethodDecl)s).getParameter(idx).changeType(newtd);
			LinkedList errors = new LinkedList();
			prog.errorCheck(errors);
			if(!errors.isEmpty())
				System.out.println("\n Refactoring introduced errors: " + errors);
		} catch(RefactoringException rfe) {
			rfe.printStackTrace();
		} finally {
			Map<String, String> changedCUs = printReport ? ASTNode.computeChanges(Program.getUndoStack()) : null;
			Program.undoAll();
			prog.flushCaches();
			if(printReport)
				printReport(prog, changedCUs);
		}
	}
	
	public static void pullUpMethod(Program prog, String type, String sig, boolean printReport) {
		TypeDecl td = prog.findType(type);
		assertNotNull(td);
		SimpleSet s = td.localMethodsSignature(sig);
		assertTrue(s instanceof MethodDecl);
		try {
			Program.startRecordingASTChangesAndFlush();
			((MethodDecl)s).doPullUpWithRequired();
			LinkedList errors = new LinkedList();
			prog.flushCaches();
			prog.errorCheck(errors);
			if(!errors.isEmpty())
				System.out.println("\n Refactoring introduced errors: " + errors);
		} catch(RefactoringException rfe) {
			rfe.printStackTrace();
		} finally {
			Map<String, String> changedCUs = printReport ? ASTNode.computeChanges(Program.getUndoStack()) : null;
			Program.undoAll();
			prog.flushCaches();
			if(printReport)
				printReport(prog, changedCUs);
		}
	}

	public static void extractInterface(Program prog, String type, String pkg, String name, boolean printReport) {
		TypeDecl td = prog.findType(type);
		assertTrue(td instanceof ClassDecl);
		Collection<MethodDecl> methods = new LinkedList<MethodDecl>();
		for(Iterator<MethodDecl> iter=td.localMethodsIterator();iter.hasNext();) {
			MethodDecl meth = iter.next();
			if(!meth.isStatic() && meth.isPublic())
				methods.add(meth);
		}		
		try {
			Program.startRecordingASTChangesAndFlush();
			((ClassDecl)td).doExtractInterface(pkg, name, methods);
			LinkedList errors = new LinkedList();
			prog.flushCaches();
			prog.errorCheck(errors);
			if(!errors.isEmpty())
				System.out.println("\n Refactoring introduced errors: " + errors);
		} catch(RefactoringException rfe) {
			rfe.printStackTrace();
		} finally {
			Map<String, String> changedCUs = printReport ? ASTNode.computeChanges(Program.getUndoStack()) : null;
			Program.undoAll();
			prog.flushCaches();
			if(printReport)
				printReport(prog, changedCUs);
		}
	}

	private static void printReport(Program prog, Map<String, String> changedCUs) {
		for(Map.Entry<String, String> changedCU : changedCUs.entrySet()) {
			String pathName = changedCU.getKey(),
				   newCU = changedCU.getValue();
			CompilationUnit cu = null;
			for(Iterator<CompilationUnit> iter=prog.compilationUnitIterator();iter.hasNext();) {
				CompilationUnit ccu = iter.next();
				if(ccu.pathName().equals(pathName)) {
					cu = ccu;
					break;
				}
			}
			assertNotNull(cu);
			DiffMatchPatch differ = new DiffMatchPatch();
			LinkedList<Diff> diffs = differ.diff_compute(cu.toString(), newCU, true);
			boolean printed_notification = false;
			for(Diff diff : diffs) {
				if(diff.text.length() == 0)
					continue;
				switch(diff.operation) {
				case EQUAL:
					break;
				case DELETE:
					printed_notification = printNotification(pathName, printed_notification);
					System.out.println("deleted '" + diff.text + "'");
					break;
				case INSERT:
					printed_notification = printNotification(pathName, printed_notification);
					System.out.println("inserted '" + diff.text + "'");
				}
				if(diff.operation != DiffMatchPatch.Operation.EQUAL)
					System.out.println();
			}
		}
	}

	private static boolean printNotification(String pathName, boolean printed_notification) {
		if(!printed_notification) {
			System.out.println("\nChanges in " + pathName + ":");
			printed_notification = true;
		}
		return printed_notification;
	}
	
	public void testGeneraliseParameterType() throws Exception {
		generaliseParameterType(hadoop.compile(), "org.apache.hadoop.fs.FileSystem", 
								"setOwner(org.apache.hadoop.fs.Path, java.lang.String, java.lang.String)",
								2, "java.lang.Object", false);
	}
	
	public void testPullUp() throws Exception {
		Program prog = clojure.compile();
		pullUpMethod(prog, "clojure.lang.PersistentTreeSet", "rseq()", false);
		/*generaliseParameterType(prog, "org.jgroups.blocks.AbstractConnectionMap", 
				  "hasOpenConnection(org.jgroups.Address)", 0, javaLangComparable.lookupParTypeDecl(args), false);*/
	}
	
	public void testExtractInterface() throws Exception {
		Program prog = apachecodec.compile();
		extractInterface(prog, "org.apache.commons.codec.net.URLCodec", "p", "I", false);
	}
}
