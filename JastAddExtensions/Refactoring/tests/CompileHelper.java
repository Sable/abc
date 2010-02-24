package tests;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import AST.BytecodeParser;
import AST.BytecodeReader;
import AST.CompilationUnit;
import AST.Frontend;
import AST.JavaParser;
import AST.Program;

public class CompileHelper {
	private static Collection<String> findAllJavaFiles(File f) {
		Collection<String> res = new LinkedList<String>();
		findAllJavaFiles(f, res);
		return res;
	}
	
	private static void findAllJavaFiles(File f, Collection<String> res) {
		if(f.isDirectory()) {
			for(File ff : f.listFiles())
				findAllJavaFiles(ff, res);
		} else {
			if(f.getName().endsWith(".java"))
				res.add(f.getPath());
		}
	}
 	
	public static Program compileAllJavaFilesUnder(String root) {
		return compileAllJavaFilesUnder(new File(root));
	}
	
	public static Program compileAllJavaFilesUnder(File root) {
		return compile(findAllJavaFiles(root).toArray(new String[]{}));
	}
	
	public static Program compile(String... files) {
		List<String> sources = new LinkedList<String>();
		List<String> jars = new LinkedList<String>();
		for(String n : files)
			if(n.endsWith(".jar"))
				jars.add(n);
			else
				sources.add(n);
		Frontend f = new Frontend() { 
			protected void processWarnings(Collection errors, CompilationUnit unit) { }
		};
		BytecodeReader br = new BytecodeParser();
		JavaParser jp = new JavaParser() {
			public CompilationUnit parse(java.io.InputStream is, String fileName) 
			throws java.io.IOException, beaver.Parser.Exception {
				return new parser.JavaParser().parse(is, fileName);
			}
		};
		if(f.process(createArglist(sources, jars), br, jp))
			return f.getProgram();
		return null;
	}

	static String[] createArglist(List<String> sources, List<String> jars) {
		if(jars.size() == 0)
			return sources.toArray(new String[]{});
		StringBuffer classpath = new StringBuffer();
		for(String j : jars) {
			classpath.append(j);
			classpath.append(':');
		}
		classpath.append(".");
		sources.add(0, "-classpath");
		sources.add(1, classpath.toString());
		return sources.toArray(new String[]{});
	}
}