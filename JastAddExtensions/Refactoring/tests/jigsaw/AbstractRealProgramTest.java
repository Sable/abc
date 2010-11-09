package tests.jigsaw;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;
import tests.CompileHelper;
import AST.Program;

public abstract class AbstractRealProgramTest extends TestCase {

	protected static final boolean CHECK_UNDO = false;
	protected static final long TIMEOUT = 500000;
	
	// NB: adjust the following path for your machine
	private static final String logdir = "d:\\logs\\";
	
	private static Program compile(String benchmarkName) throws Exception {
		// NB: adjust the following path for your machine
//		String path = System.getProperty("user.home") 
//		     + File.separator + "JastAdd"
//		     + File.separator + "benchmarks"
//		     + File.separator + benchmarkName
//		     + File.separator + ".classpath";
		String path = "D:" 
            + File.separator + "Programme" 
            + File.separator + "Eclipse" 
            + File.separator + "workspace" 
            + File.separator + benchmarkName 
            + File.separator + ".classpath";
		return CompileHelper.buildProjectFromClassPathFile(new File(path));
	}
	
	private static final String[] benchmarkprograms = {
		"org.apache.commons.codec 1.3",  // 2 KSLOC
		"Jester1.37b",  // 2 KSLOC
		"JUnit 3.8.1",  // 4 KSLOC
		"JUnit 3.8.2",  // 4 KSLOC
		"org.apache.commons.io 1.4",  // 5 KSLOC
		"org.jaxen 1.1.1",  // 12 KSLOC
		"org.htmlparser 1.6",  // 22 KSLOC
		"org.eclipse.draw2d 3.4.2",  // 23 KSLOC 
 		"jmeter",  // 41 KSLOC
		"Jigsaw",  // 101 KSLOC
		"clojure",  // 29 KSLOC
		"hadoop-core",  // 49 KSLOC		
		"servingxml-1.1.2",  // 65 KSLOC
		"xalan-j_2_4_1",  // 110 KSLOC
		"hsqldb",  // 144 KSLOC	
		"tomcat-6.0.x",  // 169 KSLOC
		"junit4.5",  // 5 KSLOC
		"lucene-3.0.1",  // 84 KSLOC
		"JHotDraw",  // 76 KSLOC
		"jgroups",  //62 KSLOC
	}; // total: 1009 KSLOC

	public void testBenchmark() throws Exception{
		for (String benchmarkprogram : benchmarkprograms) {
			String filename = new SimpleDateFormat("yyyy_MM_dd kk_mm ").format(new Date()) + name() + " "+ benchmarkprogram.replaceAll("\\.", "_") +  ".csv";
			Log log = new Log(logdir+filename);		
			System.out.println();
			System.out.println("Next Project:" + benchmarkprogram);
			performChanges(log, compile(benchmarkprogram));
			log.done();
		}
	}
		
	abstract protected String name();
	abstract protected void performChanges(Log log, Program program) throws Exception;
}
