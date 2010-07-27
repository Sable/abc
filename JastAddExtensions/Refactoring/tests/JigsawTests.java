package tests;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.text.DefaultEditorKit.CutAction;

import AST.FieldDeclaration;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.SimpleSet;
import AST.TypeDecl;

import junit.awtui.TestRunner;
import junit.framework.TestCase;
import junit.framework.TestResult;


public class JigsawTests extends TestCase {
	
	private final static boolean RECOMPILE_FOR_EACH_TEST = true;
//	private final static boolean STORE_ON_DISK = true;
//	private final static String STORE = "/home/etome/comlab/internship2010/eclipse-workspace2/Jigsaw/jigsaw.Program";
	
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
		t.testRenameMethodSucc8();
		
		t.printStats();
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
	
	private static Program jigsaw;
	public Program getJigsaw() throws Exception {
		if (jigsaw == null || RECOMPILE_FOR_EACH_TEST) {
		
			System.out.print("Compiling... ");
			long start_time = System.currentTimeMillis();
			
			jigsaw = CompileHelper.compileProjectInClassPathFile(new File("/home/etome/comlab/internship2010/eclipse-workspace2/Jigsaw/.classpath"));
			
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
	
}