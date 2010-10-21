package tests.jigsaw;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import junit.framework.TestCase;
import tests.CompileHelper;
import AST.ClassDecl;
import AST.MethodDecl;
import AST.Problem;
import AST.Program;

public class OpenBugs extends TestCase {

	public void testChangeParameterType1() throws Exception {
		testChangeParameterType(
				"Jigsaw",
				"org.w3c.jigsaw.ccpp", "CCPPFrame", "acknowledgeExtension",
				1,
				"org.w3c.www.http", "HttpEntityMessage");
	}

	public void testChangeParameterType2() throws Exception {
		testChangeParameterType(
				"Jigsaw",
				"org.w3c.jigsaw.ssi.commands","ExecCommand","addEnv",
				1,
				"java.lang","CharSequence");
	}
	
	public void testExtractInterface1() throws Exception {
		testExtractInterface(
				"Jigsaw", 
				"org.w3c.jigsaw.http","ClientException",
				"FRESH_PACKAGE_NAME","FRESH_INTERFACE_NAME",
				false);
	}
	
	public void testExtractInterface2() throws Exception {
		testExtractInterface(
				"Jigsaw", 
				"org.w3c.jigedit.filters","PutedEntry",
				"FRESH_PACKAGE_NAME","FRESH_INTERFACE_NAME",
				false);
	}
	 
	private void testChangeParameterType(String project, String pkg, String type, String method, int parameter, String newParameterPkg, String newParameterType) throws Exception{
		Program prog = compile(project);		
		prog.findType(pkg, type).findMethod(method).getParameter(1).changeType(prog.findType(newParameterPkg, newParameterType));
		check(prog);
	}

	private void testPullUpMethod(String project, String pkg, String type, String method) throws Exception {
		Program prog = compile("Jigsaw");
		prog.findType("org.w3c.jigsaw.filters", "DebugFilter").findMethod("getOnOffFlag").doPullUp();
		check(prog);
	}
	
	private void testExtractInterface(String project, String pkg, String className, String interfacePkg, String interfaceName, boolean containsMethods) throws Exception {
		Program prog = compile(project);
		ClassDecl cls = (ClassDecl)prog.findType(pkg, className);
		Collection<MethodDecl> methods = new LinkedList<MethodDecl>();
		if (containsMethods) {
			Iterator<MethodDecl> methodIterator = cls.methodsIterator();
			while (methodIterator.hasNext()) {
				MethodDecl method = methodIterator.next();
				if (method.isPublic() && method.getParent(2) == cls)
					methods.add(method);
			}
		}
		cls.doExtractInterface(interfacePkg, interfaceName, methods);
		check(prog);
	}
	
	private Program compile(String benchmarkName) throws Exception {
		String path = "D:" 
	        + File.separator + "Programme" 
	        + File.separator + "Eclipse" 
	        + File.separator + "workspace" 
	        + File.separator + benchmarkName 
	        + File.separator + ".classpath";
		return CompileHelper.buildProjectFromClassPathFile(new File(path));
	}

	private void check(Program prog) {
		Collection<Problem> errors = new LinkedList<Problem>();
		prog.errorCheck(errors);		
		assertEquals("[]", errors.toString());
	}
}
