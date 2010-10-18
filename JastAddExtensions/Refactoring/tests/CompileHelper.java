package tests;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import AST.BytecodeParser;
import AST.BytecodeReader;
import AST.CompilationUnit;
import AST.Frontend;
import AST.JavaParser;
import AST.Program;

public class CompileHelper {
	public static Collection<String> findAllJavaFiles(File f) {
		Collection<String> res = new LinkedList<String>();
		findAllJavaFiles(f, res);
		return res;
	}
	
	public static void findAllJavaFiles(File f, Collection<String> res) {
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
	
	public static Program buildProjectFromClassPathFile(File classpath) throws Exception {
		List<String> sources = new LinkedList<String>(), jars = new LinkedList<String>(), srcdirs = new LinkedList<String>();
		if (!classpath.exists())
			throw new Exception("classpath file does not exist");
		Document doc;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(classpath);
		} catch (Exception e) {
			throw new Exception("classpath file parsing(or so) failed");
		}
		doc.getDocumentElement().normalize();
		
		NodeList classpathentry_nodes = doc.getElementsByTagName("classpathentry");
		for (int i = 0; classpathentry_nodes.item(i) != null; i++) {
			Node n = classpathentry_nodes.item(i);
			if (n.getAttributes().getNamedItem("kind") == null)
				continue;
			String kind_value = n.getAttributes().getNamedItem("kind").getNodeValue();
			if (n.getAttributes().getNamedItem("path") == null)
				continue;
			String path_value = n.getAttributes().getNamedItem("path").getNodeValue();
			if (kind_value.equals("src")) {
				if(!path_value.startsWith(File.separator) && !path_value.contains(":"))
					path_value = classpath.getParentFile().getPath() + File.separator + path_value; 
				File f = new File(path_value);
				if(!f.exists())
					throw new Error("File "+f+" does not exist.");
				sources.addAll(findAllJavaFiles(f));
				srcdirs.add(path_value);
			} else if (kind_value.equals("lib")) {
				if(!path_value.startsWith(File.separator) && !path_value.contains(":"))
					path_value = classpath.getParentFile().getPath() + File.separator + path_value; 
				File f = new File(path_value);
				if(!f.exists())
					throw new Error("File "+f+" does not exist.");
				jars.add(path_value);
			}
		}
		
		return process(createArglist(sources, jars, srcdirs));
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
		return process(createArglist(sources, jars, null));
	}

	public static boolean ignoreCompilationErrors = false;
	public static Program process(String[] arglist) {
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
		if(f.process(arglist, br, jp) || ignoreCompilationErrors)
			return f.getProgram();
		return null;
	}

	static String[] createArglist(List<String> sources, List<String> jars, List<String> srcdirs) {
		LinkedList<String> args = new LinkedList<String>(sources);
		if(jars.size() == 0)
			return args.toArray(new String[]{});
		StringBuffer classpath = new StringBuffer();
		for(String j : jars) {
			classpath.append(j);
			classpath.append(File.pathSeparatorChar);
		}
		classpath.append(".");
		args.add(0, "-classpath");
		args.add(1, classpath.toString());
		if(srcdirs != null) {
			StringBuffer srcpath = new StringBuffer();
			for(String s : srcdirs) {
				srcpath.append(s);
				srcpath.append(':');
			}
			srcpath.append(".");
			args.add(2, "-sourcepath");
			args.add(3, srcpath.toString());
		}
		return args.toArray(new String[]{});
	}
}