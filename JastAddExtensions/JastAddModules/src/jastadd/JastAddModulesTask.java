package jastadd;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import java.util.*;
import java.io.*;

import javax.swing.Box.Filler;

public class JastAddModulesTask extends Task {
	public JastAddModulesTask() {
		super();
	}

	public void init() {
		super.init();
	}

	private LinkedHashSet files = new LinkedHashSet();

	public void addConfiguredFileSet(FileSet fileset) {
		DirectoryScanner s = fileset.getDirectoryScanner(getProject());
		String[] files = s.getIncludedFiles();
		String baseDir = s.getBasedir().getPath();
		for (int i = 0; i < files.length; i++)
			this.files.add(baseDir + File.separator + files[i]);
	}
	
	// place the generated files in this directory
	private String outdir = null;

	public void setOutdir(String dir) {
		outdir = dir;
	}

	private Path classpath = null;

	public void setClasspath(Path dir) {
		classpath = dir;
	}

	private boolean verbose = false;

	public void setVerbose(boolean b) {
		verbose = b;
	}

	// generate check for detection of circular evaluation of non circular
	// attributes
	private boolean novisitcheck = false;

	public void setNovisitcheck(boolean b) {
		novisitcheck = b;
	}

	private boolean componentCheck = true;

	public void setComponentCheck(boolean b) {
		componentCheck = b;
	}

	private boolean cacheCycle = true;

	public void setCacheCycle(boolean b) {
		cacheCycle = b;
	}

	// generate last cycle cache optimization for circular attributes
	private boolean noCacheCycle = false;

	public void setNoCacheCycle(boolean b) {
		noCacheCycle = b;
	}

	private boolean weaveInline = false;

	public void setWeaveInline(boolean b) {
		weaveInline = b;
	}

	private boolean inhInASTNode = false;

	public void setInhInASTNode(boolean b) {
		inhInASTNode = b;
	}

	private String packageName = null;

	public void setPackage(String packageName) {
		this.packageName = packageName;
	}

	private boolean debug = false;

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	private boolean jastaddframework = false;

	public void setJastaddFramework(boolean jastaddframework) {
		this.jastaddframework = jastaddframework;
	}
	
	private String mainclass = null;
	
	public void setMainClass(String mainclass) {
		this.mainclass = mainclass;
	}

	private String instanceModule = null;

	public void setInstanceModule(String instanceModule) {
		this.instanceModule = instanceModule;
	}

	public void execute() throws BuildException {
		if (files.size() == 0)
			throw new BuildException(
					"JastAdd requires grammar and aspect files");
		/*
		 * File generated = new File((packageName == null ? "" : (packageName +
		 * "/")) + "ASTNode.class"); if(generated.exists()) { boolean changed =
		 * false; for(Iterator iter = files.iterator(); iter.hasNext(); ) {
		 * String fileName = (String)iter.next(); File file = new
		 * File(fileName); if(!file.exists() || file.lastModified() >
		 * generated.lastModified()) changed = true; } if(!changed) { return; } }
		 */
		ArrayList args = new ArrayList();
		if (outdir != null) {
			args.add("-d");
			args.add(outdir);
		}
		if (classpath != null) {
			args.add("-classpath");
			String classpathStr = "";
			boolean first = true;
			for (String elem : classpath.list()) {
				if (!first) {
					classpathStr += File.pathSeparator;
				}
				classpathStr += elem;
				first = false;
			}
			args.add(classpathStr);
		}
		if (verbose)
			args.add("-verbose");
		if (novisitcheck)
			args.add("-no_visit_check");
		if (!componentCheck)
			args.add("-no_component_check");
		if (!cacheCycle)
			args.add("-no_cache_cycle");
		if (weaveInline)
			args.add("-weave_inline");
		if (inhInASTNode)
			args.add("-inh_in_astnode");
		if (packageName != null) {
			args.add("-package");
			args.add(packageName);
		}
		if (instanceModule != null) {
			args.add("-instance-module");
			args.add(instanceModule);
		}
		if (jastaddframework) {
			args.add("-jastaddframework");
		}
		if (mainclass != null) {
			args.add("-mainclass");
			args.add(mainclass);
		}
		if (debug) {
			args.add("-debug");
		}
		args.addAll(files);

		int i = 0;
		String[] argsArray = new String[args.size()];
		for (Iterator iter = args.iterator(); iter.hasNext(); i++)
			argsArray[i] = ((String) iter.next()).trim();
		System.err.println("generating node types and weaving aspects");
		JastAddModules.main(argsArray);
		System.err.println("done");
	}
}
