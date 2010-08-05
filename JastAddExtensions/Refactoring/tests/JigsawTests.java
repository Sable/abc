package tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;
import AST.AndLogicalExpr;
import AST.AssignSimpleExpr;
import AST.Block;
import AST.CastExpr;
import AST.ClassDecl;
import AST.ClassInstanceExpr;
import AST.CompilationUnit;
import AST.Dot;
import AST.EQExpr;
import AST.Expr;
import AST.ExprStmt;
import AST.FieldDeclaration;
import AST.ForStmt;
import AST.GenericTypeDecl;
import AST.IfStmt;
import AST.IntegerLiteral;
import AST.LTExpr;
import AST.List;
import AST.MethodAccess;
import AST.MethodDecl;
import AST.Modifiers;
import AST.NEExpr;
import AST.Opt;
import AST.OrLogicalExpr;
import AST.PostIncExpr;
import AST.Program;
import AST.RefactoringException;
import AST.ReferenceType;
import AST.SimpleSet;
import AST.Stmt;
import AST.StringLiteral;
import AST.TypeAccess;
import AST.TypeDecl;
import AST.VarAccess;
import AST.Variable;
import AST.VariableDeclaration;
import AST.VoidType;


public class JigsawTests extends TestCase {
	
	private final static boolean RECOMPILE_FOR_EACH_TEST = true;
	

	private Map<String, Long> test_class_total = new LinkedHashMap<String, Long>();
	public void printTestTime(String test_class, String info, long start_time, long end_time) {
		if (!test_class_total.containsKey(test_class))
			test_class_total.put(test_class, end_time - start_time);
		else
			test_class_total.put(test_class, test_class_total.get(test_class) + end_time - start_time);
		System.out.println("Test: " + test_class + " " + info + " (" + (end_time - start_time) + "ms)");
	}
	
	public void printStats() {
		Iterator<String> keys = test_class_total.keySet().iterator();
		Iterator<Long> times = test_class_total.values().iterator();
		System.out.println();
		for(int i = 0; i < test_class_total.size(); i++) {
			String key = keys.next();
			Long time = times.next();
			System.out.println(key + ": " + time + "ms");
		}
	}
	
	public static void main(String[] args) throws Exception {
		JigsawTests t = new JigsawTests();
		
		Program p = CompileHelper.compileAllJavaFilesUnder("/home/etome/comlab/internship2010/eclipse-workspace2/Refactoring/AST");
		
		int cnt = 0;
		for (Iterator cu_it = p.compilationUnitIterator(); cu_it.hasNext();) {
			if (cnt%10==0) System.out.print(cnt + ".."); if(cnt%200==0) System.out.println();
			cnt++;
//			if (cnt > 10)
//				break;
			CompilationUnit cu = (CompilationUnit) cu_it.next();
			for(Iterator types = cu.getTypeDecls().iterator(); types.hasNext();) {
				TypeDecl td = (TypeDecl) types.next();
				if (td instanceof ClassDecl) {
					ClassDecl cd = (ClassDecl) td;
					if (cd.localMethodsSignature("flushCache()").isEmpty())
							continue;
					
					assert(cd.localMethodsSignature("flushCache()").isSingleton());
					MethodDecl flushCache = (MethodDecl) cd.localMethodsSignature("flushCache()")
															.iterator().next();
					// store function
					String store_nm = "storeValues";
					assert(cd.localMethodsSignature(store_nm + "()").size() <= 1);
					if (cd.localMethodsSignature(store_nm + "()").size() == 1)
						cd.removeBodyDecl(((MethodDecl) cd.localMethodsSignature(store_nm + "()").iterator().next()));
					MethodDecl store = new MethodDecl(new Modifiers(), ((VoidType) p.typeVoid()).createLockedAccess(), 
							store_nm, new AST.List<AST.ParameterDeclaration>(), new AST.List<AST.Access>().add(new TypeAccess("CloneNotSupportedException")), new Opt<Block>(new Block()));
					p.flushCaches();
					cd.getBodyDeclList().add(store);
					store.getBlock().addStmt(new ForStmt(new List<Stmt>().add(new VariableDeclaration(new TypeAccess("int"), "i", new IntegerLiteral(0))), 
							new Opt<Expr>(new LTExpr(new VarAccess("i"), new MethodAccess("getNumChild", new List<Expr>()))), 
							new List<Stmt>().add(new ExprStmt(new PostIncExpr(new VarAccess("i")))), new Block(
									new ExprStmt(new Dot(new MethodAccess("getChild", new List<Expr>().add(new VarAccess("i"))),
											new MethodAccess(store_nm, new List<Expr>()))))));
					
					// compare function
					String compare_nm = "compareValues";
					assert(cd.localMethodsSignature(compare_nm + "()").size() <= 1);
					if (cd.localMethodsSignature(compare_nm + "()").size() == 1)
						cd.removeBodyDecl(((MethodDecl) cd.localMethodsSignature(compare_nm + "()").iterator().next()));
					MethodDecl compare = new MethodDecl(new Modifiers(), ((VoidType) p.typeVoid()).createLockedAccess(), 
							compare_nm, new AST.List<AST.ParameterDeclaration>(), new AST.List<AST.Access>(), new Opt<Block>(new Block()));
					p.flushCaches();
					cd.getBodyDeclList().add(compare);
					compare.getBlock().addStmt(new ForStmt(new List<Stmt>().add(new VariableDeclaration(new TypeAccess("int"), "i", new IntegerLiteral(0))), 
							new Opt<Expr>(new LTExpr(new VarAccess("i"), new MethodAccess("getNumChild", new List<Expr>()))), 
							new List<Stmt>().add(new ExprStmt(new PostIncExpr(new VarAccess("i")))), new Block(
									new ExprStmt(new Dot(new MethodAccess("getChild", new List<Expr>().add(new VarAccess("i"))),
											new MethodAccess(compare_nm, new List<Expr>()))))));
					
					for (int i = 0; i < flushCache.getBlock().getNumStmt(); i++) {
						if (((ExprStmt) flushCache.getBlock().getStmt(i)).getExpr() instanceof AST.Dot)
							continue;
						AssignSimpleExpr s = (AssignSimpleExpr) ((ExprStmt) (Stmt) flushCache.getBlock().getStmt(i)).getExpr();
						
						// add tmp field
						FieldDeclaration d = (FieldDeclaration) ((AST.VarAccess) s.getDest()).decl();
						String nm = d.name() + "_tmp";
						
						FieldDeclaration d_tmp = null;
						if (cd.localFields(nm).isEmpty()) {
							d_tmp = new FieldDeclaration(new Modifiers(), ((AST.VarAccess) s.getDest()).decl().type().createLockedAccess(), nm); 
							cd.insertField(d_tmp);
						} else
							d_tmp = (FieldDeclaration) cd.localFields(nm).iterator().next();
						
						TypeDecl type = d.type();
						String type_name = type.name();
						
						// store
						if (type instanceof ReferenceType && !type_name.equals("String")) {
							Block b = new Block();
							store.getBlock().addStmt(new ExprStmt(new AssignSimpleExpr(d_tmp.createLockedAccess(), (Expr)s.getSource().fullCopy())));
							store.getBlock().getStmtList().add(new IfStmt(new NEExpr(d.createLockedAccess(), new TypeAccess("null")), b));
							if (type_name.equals("Collection") || (type_name.equals("List") && type.packageName().equals("java.util")))
								b.getStmtList().add(new ExprStmt(new AssignSimpleExpr(d_tmp.createLockedAccess(), 
									new ClassInstanceExpr(((GenericTypeDecl)p.findType("java.util", "LinkedList")).rawType().createLockedAccess(), new List<Expr>().add((Expr) d.createLockedAccess())))));
							else if (type_name.equals("ArrayList"))
								b.getStmtList().add(new ExprStmt(new AssignSimpleExpr(d_tmp.createLockedAccess(), 
									new ClassInstanceExpr(((GenericTypeDecl)p.findType("java.util", "ArrayList")).rawType().createLockedAccess(), new List<Expr>().add((Expr) d.createLockedAccess())))));
							else if (type_name.equals("HashSet") || type_name.equals("Set"))
								b.getStmtList().add(new ExprStmt(new AssignSimpleExpr(d_tmp.createLockedAccess(), 
									new ClassInstanceExpr(((GenericTypeDecl)p.findType("java.util", "HashSet")).rawType().createLockedAccess(), new List<Expr>().add((Expr) d.createLockedAccess())))));
							else if (type instanceof AST.RawInterfaceDecl && type_name.equals("Map") ||
									type_name.equals("HashMap"))
								b.getStmtList().add(new ExprStmt(new AssignSimpleExpr(d_tmp.createLockedAccess(), 
										new ClassInstanceExpr(((GenericTypeDecl)p.findType("java.util", "LinkedHashMap")).rawType().createLockedAccess(), new List<Expr>().add((Expr) d.createLockedAccess())))));
							else if (type.packageName().equals("AST") && type.name().equals("List"))
								b.getStmtList().add(new ExprStmt(new AssignSimpleExpr(d_tmp.createLockedAccess(),
										new Dot(d.createLockedAccess(), new MethodAccess("clone", new List<Expr>())))));
							else if (type.packageName().equals("AST"))
								b.getStmtList().add(new ExprStmt(new AssignSimpleExpr(d_tmp.createLockedAccess(),
										d.createLockedAccess())));
							else
								throw new Exception();
						} else
							store.getBlock().getStmtList().add(new ExprStmt(new AssignSimpleExpr(d_tmp.createLockedAccess(),
									d.createLockedAccess())));
						
						// compare
						
						Stmt changed = new ExprStmt(new Dot(new TypeAccess("AggregatePrinter"), new MethodAccess("print", new AST.List<AST.Expr>().add(new StringLiteral(d.name() + " : " + d.type().name() + " changed\n")))));
						Stmt same = new ExprStmt(new Dot(new TypeAccess("AggregatePrinter"), new MethodAccess("print", new AST.List<AST.Expr>().add(new StringLiteral(d.name() + " : " + d.type().name() + " same\n")))));
						
						
						if (type instanceof ReferenceType && !type_name.equals("String")) {
							Block b = new Block();
							compare.getBlock().getStmtList().add(new IfStmt(new NEExpr(d_tmp.createLockedAccess(), new TypeAccess("null")), b));
							
							// recompute
							if (d.name().endsWith("_value"))
								b.addStmt(new ExprStmt(new MethodAccess(d.name().replaceAll("_value$", "")
										.replaceAll("^[^_]+_", ""), new List<Expr>())));
							else if (d.name().endsWith("_values")) {
								int n = d.name().replaceAll("[^_]", "").length() - 1; // num of arguments
								String[] a = d.name().split("_");
								for (int ii = 1; ii < a.length; ii++)
									if (a[ii].equals("boolean"))
										a[ii] = "Boolean";
									else if (a[ii].equals("int"))
										a[ii] = "Integer";
								if (n == 1) {
									Block bl = new Block();
									b.addStmt(new ForStmt(new List<Stmt>().add(
											new VariableDeclaration(
													new TypeAccess("Iterator"), 
													"it", 
													new Dot(new Dot(new VarAccess(d.name() + "_tmp"),
															new MethodAccess("keySet", new List<Expr>())), new MethodAccess("iterator", new List<Expr>())))), 
											new Opt<Expr>(new Dot(new VarAccess("it"), new MethodAccess("hasNext", new List<Expr>()))), 
											new List<Stmt>(),
											bl));
									bl.addStmt(new ExprStmt(
											new MethodAccess(
													d.name().replaceAll("_.*", ""), 
													new List<Expr>().add(
															new CastExpr(
																	new TypeAccess(a[1]),
																	new Dot(new VarAccess("it"), new MethodAccess("next", new List<Expr>())))))));
								} else if (n == 2) { // TODO: rewrite as loop...
									Block bl = new Block();
									b.addStmt(new ForStmt(new List<Stmt>().add(
											new VariableDeclaration(
													new TypeAccess("Iterator"), 
													"it", 
													new Dot(new Dot(new VarAccess(d.name() + "_tmp"),
															new MethodAccess("keySet", new List<Expr>())), new MethodAccess("iterator", new List<Expr>())))), 
											new Opt<Expr>(new Dot(new VarAccess("it"), new MethodAccess("hasNext", new List<Expr>()))), 
											new List<Stmt>(),
											bl));
									bl.addStmt(new VariableDeclaration(new TypeAccess("java.util.List"), 
												"tmp_list", 
												new CastExpr(new TypeAccess("java.util.List"), new Dot(new VarAccess("it"), new MethodAccess("next", new List<Expr>())))));
									bl.addStmt(new ExprStmt(
											new MethodAccess(
													a[0], 
													new List<Expr>()
														.add(new CastExpr(new TypeAccess(a[1]), 
																new Dot(new VarAccess("tmp_list"), 
																		new MethodAccess("get", new List<Expr>().add(new IntegerLiteral(0))))))
														.add(new CastExpr(new TypeAccess(a[2]), 
																new Dot(new VarAccess("tmp_list"), 
																		new MethodAccess("get", new List<Expr>().add(new IntegerLiteral(1)))))))));
								} else
									throw new Exception("add a new case here");
							}
							// check
							if (type_name.equals("Collection") || type_name.equals("ArrayList") || type_name.equals("List")
									 || type_name.equals("HashSet") || type_name.equals("Set"))
								b.addStmt(new IfStmt(new Dot(d.createLockedAccess(), new MethodAccess("containsAll", new List<Expr>().add(d_tmp.createLockedAccess()))), 
										(Stmt)same.fullCopy(), (Stmt)changed.fullCopy()));
							else if (type instanceof AST.RawInterfaceDecl && type_name.equals("Map") ||
									type_name.equals("HashMap")) {
								Block bl = new Block();
								b.addStmt(new ForStmt(new List<Stmt>().add(
										new VariableDeclaration(
												new TypeAccess("Iterator"), 
												"it", 
												new Dot(new Dot(new VarAccess(d.name() + "_tmp"),
														new MethodAccess("keySet", new List<Expr>())), new MethodAccess("iterator", new List<Expr>())))), 
										new Opt<Expr>(new Dot(new VarAccess("it"), new MethodAccess("hasNext", new List<Expr>()))), 
										new List<Stmt>(),
										bl));
								bl.addStmt(new VariableDeclaration(p.typeObject().createLockedAccess(), "key", new Dot(new VarAccess("it"), new MethodAccess("next", new List<Expr>()))));
								bl.addStmt(
										new IfStmt(
											new OrLogicalExpr(
												new AndLogicalExpr(
														new EQExpr(new Dot(
																	d_tmp.createLockedAccess(), 
																	new MethodAccess("get", new List<Expr>().add(new VarAccess("key")))),
																new TypeAccess("null")),
														new EQExpr(new Dot(
																	d.createLockedAccess(), 
																	new MethodAccess("get", new List<Expr>().add(new VarAccess("key")))),
																new TypeAccess("null"))),
												new Dot(
														new Dot(
																d_tmp.createLockedAccess(), 
																new MethodAccess("get", new List<Expr>().add(new VarAccess("key")))), 
														new MethodAccess("equals", new List<Expr>().add(
																new Dot(
																		d.createLockedAccess(), 
																		new MethodAccess("get", new List<Expr>().add(new VarAccess("key")))))))),
												(Stmt)same.fullCopy(), (Stmt)changed.fullCopy()));
//								b.getStmtList().add(new ExprStmt(new AssignSimpleExpr(d_tmp.createLockedAccess(), 
//										new ClassInstanceExpr(((GenericTypeDecl)p.findType("java.util", "LinkedHashMap")).rawType().createLockedAccess(), new List<Expr>().add((Expr) d.createLockedAccess())))));
							} else if (type.packageName().equals("AST")) // ie check same reference
//								b.getStmtList().add(new ExprStmt(new AssignSimpleExpr(d_tmp.createLockedAccess(),
//										d.createLockedAccess())));
								compare.getBlock().getStmtList().add(new IfStmt(new EQExpr(d_tmp.createLockedAccess(), d.createLockedAccess()),
									(Stmt)same.fullCopy(), (Stmt)changed.fullCopy()));
							else
								throw new Exception();
						} else {
							compare.getBlock().getStmtList().add(new IfStmt(new EQExpr(d_tmp.createLockedAccess(), d.createLockedAccess()),
									(Stmt)same.fullCopy(), (Stmt)changed.fullCopy()));
						}
					}
//					System.out.println(store.toString());
//					System.out.println(compare.toString());
				}
			}
		}
		
		p.eliminate(Program.LOCKED_NAMES);
		
		p.writeBack();
		System.out.println("done");
//		
//		t.splitAllFlushCacheMethods(new File("/home/etome/comlab/internship2010/eclipse-workspace2/Refactoring/AST"));
		
//		return ;
		
//		t.testInlineTempSucc1();
		
//		t.testExtractClassSucc1();
		
//		t.testRenameFieldSucc1();
//		t.testRenameFieldSucc2();
//		t.testRenameFieldSucc3();
//		t.testRenameFieldSucc4();
//		t.testRenameFieldSucc5();

//		t.testRenameMethodSucc1();
//		t.testRenameMethodSucc2();
//		t.testRenameMethodSucc3();
//		t.testRenameMethodSucc4();
//		t.testRenameMethodSucc5();
		
//		t.testRenameMethodSucc6();
//		t.testRenameMethodSucc7()
//		t.testRenameMethodSucc8();
		
//		t.printStats();
	}
	
	public void renameFieldTestSucc(String pkg, String tp_name, String old_name, String new_name, Program in) throws Exception {
		if (in == null) {
			in = getJigsaw();
		}
		if (new_name == null)
			new_name = "NEW_UNIQUE_NAME_" + old_name;
		assertNotNull(in);
		//assertNotNull(out);
		TypeDecl tp = in.findType(pkg, tp_name);
		assertNotNull(tp);
		SimpleSet s = tp.localFields(old_name);
		assertTrue(s.isSingleton());
		FieldDeclaration fd = (FieldDeclaration)s.iterator().next();
		try {
			System.out.println("Renaming... ");
			long start_time = System.currentTimeMillis();
			fd.rename(new_name);
			long end_time = System.currentTimeMillis();
			printTestTime("RenameField", "("+pkg+".."+tp_name+".."+old_name+")", start_time, end_time);
		} catch(RefactoringException rfe) {
			fail("Refactoring was supposed to succeed; failed with "+rfe);
		}
	}
	
	public void renameMethodTestSucc(String pkg, String tp_name, String sig, String new_name, Program in) throws Exception {
		if (in == null) {
			in = getJigsaw();
		}
		if (new_name == null)
			new_name = "NEW_UNIQUE_NAME_" + sig.replaceFirst("\\(.*", "");
		assertNotNull(in);
		TypeDecl tp = in.findType(pkg, tp_name);
		assertNotNull(tp);
		SimpleSet s = tp.localMethodsSignature(sig);
		assertTrue(s.isSingleton());
		MethodDecl md = (MethodDecl)s.iterator().next();
		try {
			System.out.println("Renaming... ");
			long start_time = System.currentTimeMillis();
			md.rename(new_name);
			long end_time = System.currentTimeMillis();
			printTestTime("RenameMethod", "("+pkg+".."+tp_name+".."+sig+")", start_time, end_time);
		} catch(RefactoringException rfe) {
			fail("Refactoring was supposed to succeed; failed with "+rfe);
		}
	}
	
	public void extractClassTestSucc(String pkg, String tp_name, String[] fns, String new_class_name, String new_field_name, Program in) throws Exception {
		if (in == null) {
			in = getJigsaw();
		}
		assertNotNull(in);
		TypeDecl td = in.findType(pkg, tp_name);
		assertTrue(td instanceof ClassDecl);
		ArrayList<FieldDeclaration> fds = new ArrayList<FieldDeclaration>();
		for(String fn : fns) {
			FieldDeclaration fd = td.findField(fn);
			assertNotNull(fd);
			fds.add(fd);
		}
		try {
			System.out.println("Extracting class... ");
			long start_time = System.currentTimeMillis();
			((ClassDecl)td).doExtractClass(fds, new_class_name, new_field_name, true, false);
			long end_time = System.currentTimeMillis();
			printTestTime("ExtractClass", "("+pkg+".."+tp_name+".."+fns+")", start_time, end_time);
		} catch(RefactoringException rfe) {
			fail("Refactoring was supposed to succeed; failed with "+rfe);
		}
	}
	
	public void inlineTempTestSucc(String pkg, String tp_name, String sig, String var_name, Program in) throws Exception {		
		if (in == null) {
			in = getJigsaw();
		}
		assertNotNull(in);
		TypeDecl td = in.findType(pkg, tp_name);
		assertTrue(td instanceof ClassDecl);
		SimpleSet s = td.localMethodsSignature(sig);
		assertTrue(s.isSingleton());
		Variable v = ((MethodDecl)s.iterator().next()).findVariable(var_name);
		assertTrue(v instanceof VariableDeclaration);
		try {
			System.out.println("Inlining temp... ");
			long start_time = System.currentTimeMillis();
			((VariableDeclaration)v).doInline();
			long end_time = System.currentTimeMillis();
			printTestTime("InlineTemp", "("+pkg+".."+tp_name+".."+sig+":var " + var_name + ")", start_time, end_time);
		} catch(RefactoringException rfe) {
			fail("Refactoring was supposed to succeed; failed with "+rfe);
		}
	}
	
	private static Program jigsaw;
	public Program getJigsaw() throws Exception {
		if (jigsaw == null || RECOMPILE_FOR_EACH_TEST) {
		
			System.out.print("Compiling... ");
			long start_time = System.currentTimeMillis();
			
			jigsaw = CompileHelper.buildProjectFromClassPathFile(new File("/home/etome/comlab/internship2010/eclipse-workspace2/Jigsaw/.classpath"));
			
			long end_time = System.currentTimeMillis();
			System.out.println("ok." + " (" + (end_time - start_time) + "ms)");
			
		}
		if (RECOMPILE_FOR_EACH_TEST)
			return jigsaw;
		
		System.out.print("Copying program... ");
		long start_time = System.currentTimeMillis();
		Program prog = jigsaw.fullCopy();
		long end_time = System.currentTimeMillis();
		System.out.println("ok." + " (" + (end_time - start_time) + "ms)");
		
		return prog;
	}
	
	public void testRenameFieldSucc1() throws Exception {
		renameFieldTestSucc("org.w3c.tools.resources", "Attribute", "EDITABLE", null, null);
	}
	
	public void testRenameFieldSucc2() throws Exception {
		renameFieldTestSucc("org.w3c.www.http", "HTTP", "OK", null, null);
	}
	
	public void testRenameFieldSucc3() throws Exception {
		renameFieldTestSucc("org.w3c.www.http", "HTTP", "INTERNAL_SERVER_ERROR", null, null);
	}
	
	public void testRenameFieldSucc4() throws Exception {
		renameFieldTestSucc("org.w3c.jigsaw.https.socket", "SSLProperties", "DEFAULT_SSL_ENABLED", null, null);
	}
	
	public void testRenameFieldSucc5() throws Exception {
		renameFieldTestSucc("org.w3c.jigadm.editors", "PasswordEditor", "img", null, null);
	}
	
	// heavily overriden
	public void testRenameMethodSucc1() throws Exception {
		renameMethodTestSucc("org.w3c.tools.resources", "AttributeHolder", "initialize(java.lang.Object[])", null, null);
	}
	
	public void testRenameMethodSucc2() throws Exception {
		renameMethodTestSucc("org.w3c.jigadm.editors", "AttributeEditorInterface", "getValue()", null, null);
	}
	
	public void testRenameMethodSucc3() throws Exception {
		renameMethodTestSucc("org.w3c.jigadm.editors", "AttributeEditor", "getComponent()", null, null);
	}
	
	//heavily used
	public void testRenameMethodSucc4() throws Exception {
		renameMethodTestSucc("org.w3c.jigsaw.webdav", "DAVRequest", "makeReply(int)", null, null);
	}
	
	public void testRenameMethodSucc5() throws Exception {
		renameMethodTestSucc("org.w3c.tools.resources", "ResourceReference", "unlock()", null, null);
	}
	
//	public void testRenameMethodSucc6() throws Exception {
//		renameMethodTestSucc("org.w3c.cvs", "FileEnumeration", "nextElement()", null, null);
//	}
	
	// hardly used
//	public void testRenameMethodSucc7() throws Exception {
//		renameMethodTestSucc("org.w3c.jigadm.editors", "DispatcherRulesEditor", "msg(java.lang.String)", null, null);
//	}
	
	public void testRenameMethodSucc8() throws Exception {
		renameMethodTestSucc("org.w3c.cvs", "DirectoryFilter", "accept(File, String)", null, null);
	}
	
	public void testExtractClassSucc1() throws Exception {
		extractClassTestSucc("org.w3c.jigsaw.webdav", "DAVFrame", 
				new String[] {"deadindex", "deadpropmodified", "manager"},
				"Data", "data", null);
	}
	
	public void testInlineTempSucc1() throws Exception {
		inlineTempTestSucc("org.w3c.jigsaw.webdav", "DAVFrame", "proppatch(org.w3c.jigsaw.webdav.DAVRequest)", "lenn", null);
	}
}