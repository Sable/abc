package tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

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
	
	public static Program compileProjectInClassPathFile(File classpath) throws Exception {
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
		List<String> files = new ArrayList<String>();
		for (int i = 0; classpathentry_nodes.item(i) != null; i++) {
			Node n = classpathentry_nodes.item(i);
			if (n.getAttributes().getNamedItem("kind") == null)
				continue;
			String kind_value = n.getAttributes().getNamedItem("kind").getNodeValue();
			if (n.getAttributes().getNamedItem("path") == null)
				continue;
			String path_value = n.getAttributes().getNamedItem("path").getNodeValue();
			if (kind_value.equals("src")) {
				if(!path_value.startsWith(File.separator))
					path_value = classpath.getParentFile().getPath() + File.separator + path_value; 
				File f = new File(path_value);
				if(!f.exists())
					throw new Error("File "+f+" does not exist.");
				files.addAll(findAllJavaFiles(f));
			} else if (kind_value.equals("lib")) {
				if(!path_value.startsWith(File.separator))
					path_value = classpath.getParentFile().getPath() + File.separator + path_value; 
				File f = new File(path_value);
				if(!f.exists())
					throw new Error("File "+f+" does not exist.");
				files.add(path_value);
			}
		}
		
		return compile(files.toArray(new String[0]));
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
		return process(createArglist(sources, jars));
	}

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
		if(f.process(arglist, br, jp))
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