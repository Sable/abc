package tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

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
import AST.ReferenceType;
import AST.Stmt;
import AST.StringLiteral;
import AST.TypeAccess;
import AST.TypeDecl;
import AST.VarAccess;
import AST.VariableDeclaration;
import AST.VoidType;

public class CacheManipulation {

	public static void addStoreAndCompareFunctions() throws Exception {
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
						
						Stmt changed = new ExprStmt(new Dot(new TypeAccess("AggregatePrinter"), new MethodAccess("print", new AST.List<AST.Expr>().add(new StringLiteral(d.type().name() + " " +  d.name() + " changed\n")))));
//						Stmt same = new ExprStmt(new Dot(new TypeAccess("System"), new Dot(new VarAccess("out"), new MethodAccess("print", new AST.List<AST.Expr>().add(new StringLiteral(d.type().name() + " " + d.name() + " same\n"))))));
						Stmt same = new ExprStmt(new Dot(new TypeAccess("AggregatePrinter"), new MethodAccess("print", new AST.List<AST.Expr>().add(new StringLiteral("")))));
						
						
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
	}
	
	
	// each element as groupname and regexp to match elements, make sure it has Other on last place
	private static String[][] attributeGroups = {
		{ "Type", 
			"lookupType_String_String_values",
			"lookupType_String_values",
			"lookupParTypeDecl_ArrayList_values",
			"lookupParTypeDecl_ParTypeAccess_values",
			"collect_contributors_TypeDecl_childTypes",
			"collecting_contributors_TypeDecl_childTypes",
			"TypeDecl_childTypes_computed",
			"TypeDecl_childTypes_contributors",
			"TypeDecl_childTypes_initialized",
			"TypeDecl_childTypes_value",
			"TypeDecl_childTypes_visited"
		},
		{ "Other",
			".*"
		}
	};
	public void splitFlushCacheMethod(File f) throws IOException {
		StringBuffer out = new StringBuffer();
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		final String EOL = System.getProperty("line.separator");
		
		int state = 0; // 1 = in flush cache method
		boolean add_super_call = false;
		Map<String/*group name*/, StringBuffer/*lines of code*/> groupedAttributes = new HashMap<String, StringBuffer>();
		// init map
		for (int group = 0; group < attributeGroups.length; group++) {
			String group_name = attributeGroups[group][0];
			groupedAttributes.put(group_name, new StringBuffer());
		}
		
		while(null != (line = br.readLine())) {
			
			if (state == 0 && line.matches(".*public void flushCache\\(\\)\\ \\{")) {
				state = 1;
				
			} else if (state == 0) {
				out.append(line + EOL);
				
			} else if (state == 1 && line.matches("^\\s*\\}\\s*$")) {
				state = 0;
				
				out.append("public void flushCache() {" + EOL);
				if (add_super_call)
					out.append("\tsuper.flushCache();" + EOL);
				for (int group = 0; group < attributeGroups.length; group++) {
					String group_name = attributeGroups[group][0];
					out.append("\tflush" + group_name + "Cache();" + EOL);
				}
				out.append("}" + EOL);
				for (int group = 0; group < attributeGroups.length; group++) {
					String group_name = attributeGroups[group][0];
					out.append("public void flush" + group_name + "Cache() {" + EOL);
					if (add_super_call)
						out.append("\tsuper.flush" + group_name + "Cache();" + EOL);
					out.append(groupedAttributes.get(group_name));
					groupedAttributes.put(group_name, new StringBuffer());
					out.append("}" + EOL);
				}
				add_super_call = false;
				
			} else if (state == 1) {
				if (line.matches(".*super\\.flushCache\\(\\);")) {
					add_super_call = true;
					continue;
				}
				
				int group = 0;
				group_loop: for (group = 0; group < attributeGroups.length; group++) {
					String group_name = attributeGroups[group][0];
					for (int pattern = 1; pattern < attributeGroups[group].length; pattern++)
						if (line.matches("\\s*" + attributeGroups[group][pattern] + "\\s*=.*") || group_name.equals("Other")) {
							groupedAttributes.get(group_name).append(line + EOL);
							break group_loop; 
						}
				}
			}
		}
		br.close();
		fr.close();
		FileWriter fw = new FileWriter(f);
		fw.write(out.toString());
		fw.close();
	}
	
	public void splitAllFlushCacheMethods(File ASTDir) throws IOException {
		File[] files = ASTDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches(".*\\.java");
			}
		});
		
		for(File f : files) {
			splitFlushCacheMethod(f);
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		addStoreAndCompareFunctions();
	}
	
}
