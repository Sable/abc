package tests.jigsaw;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import tests.CompileHelper;
import AST.AccessibilityConstraint;
import AST.Program;
import junit.framework.TestCase;

public abstract class AbstractRealProgramTest extends TestCase {

	private static final boolean checkUndo = false;

	private static final String[] benchmarkprograms = {
	"Jigsaw",  // 101 KSLOC
	"JHotDraw",  // 76 KSLOC
	"xalan-j_2_4_1",  // 110 KSLOC
	"hsqldb",  // 144 KSLOC
	"hadoop-core",  // 49 KSLOC
	"tomcat-6.0.x",  // 169 KSLOC
	"jgroups",  //62 KSLOC
	"lucene-3.0.1",  // 84 KSLOC
	"jmeter",  // 41 KSLOC
	"servingxml-1.1.2",  // 65 KSLOC
	"org.jaxen 1.1.1",  // 12 KSLOC
	"org.htmlparser 1.6",  // 22 KSLOC
	"Jester1.37b",  // 2 KSLOC
	"JUnit 3.8.1",  // 4 KSLOC
	"JUnit 3.8.2",  // 4 KSLOC
	"junit4.5",  // 5 KSLOC
	"org.apache.commons.codec 1.3",  // 2 KSLOC
	"org.apache.commons.io 1.4",  // 5 KSLOC
	"org.eclipse.draw2d 3.4.2",  // 23 KSLOC 
	"clojure-1.1.0",  // 29 KSLOC
	}; // total: 1009 KSLOC
	
	private int runs = 0;
	private int successes = 0;
	private int errors = 0;
	
	private static Program compile(String benchmarkName) throws Exception {
		
		// NB: adjust the following path for your machine
		String path = System.getProperty("user.home") 
		     + File.separator + "JastAdd"
		     + File.separator + "benchmarks"
		     + File.separator + benchmarkName
		     + File.separator + ".classpath";
//		String path = "D:" 
//            + File.separator + "Programme" 
//            + File.separator + "Eclipse" 
//            + File.separator + "workspace" 
//            + File.separator + benchmarkName 
//            + File.separator + ".classpath";
		return CompileHelper.buildProjectFromClassPathFile(new File(path));
	}

	private Collection<Program> getPrograms() throws Exception {
		Collection<Program> programs = new LinkedList<Program>();
		for (String benchmarkprogram : benchmarkprograms) {
			programs.add(compile(benchmarkprogram));
		}
		return programs;
	}

	public void testCompile() throws Exception {
		for (Program prog : getPrograms())
			for (AccessibilityConstraint ac : prog.accessibilityConstraints())
				if (!ac.isSolved())
					fail();
	}

	String orig;
	Program actual; 
	public void testBenchmark() throws Exception{
		reset();
		for (Program prog: getPrograms()){
			actual = prog;
			orig = checkUndo ? prog.toString() : null;
			performChanges(prog);
		}
		printresults();
	}

	protected abstract void performChanges(Program prog);

	private void reset(){
		runs = successes = errors = 0;
	}
	
	private void printresults(){
		System.out.println();
		System.out.println("runs:             " + runs);
		System.out.println("successes:        " + successes);
		System.out.println("errors:           " + errors);
		long totalduration = 0;
		for(long duration : durations)
			totalduration+=duration;
		System.out.println("average duration: " + ((double)totalduration)/durations.size());
	}
	
	long start;
	boolean running = false;
	Collection<Long> durations = new LinkedList<Long>();
	protected void newRun(){
		running = true;
		runs ++;
		if(runs%10 == 0)
			printresults();
		start = System.currentTimeMillis();
	}
	
	protected void runFinished(){
		if(running)
			durations.add(System.currentTimeMillis()-start);
		running = false;
	}
	
	protected void success() {
		successes++;
		System.out.println("success; ");
	}
	protected void error() {
		errors++;
	}
	protected void checkUndo() {
		if(checkUndo)
			assertEquals(orig, actual.toString());
	}
}
