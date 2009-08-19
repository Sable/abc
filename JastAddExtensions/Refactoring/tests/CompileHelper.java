package tests;

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