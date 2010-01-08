
import AST.*;
import java.util.*;
import java.io.*;

/**
 * Compiles and counts number of dead statements while
 * measuring time and memory usage. Adjust the number of
 * pre-heat runs with N, the number of runs after the JIT
 * is turned of with M and finally the number of measurment
 * runs with K. For K < 30 the Students distribution should
 * be used and for K >= 30 the normal distribution. 
 * 
 * @author Emma (emma@cs.lth.se)
 */
class JavaCheckerTimeMem extends Frontend {

  public static final int N = 0;//5;
  public static final int M = 0;
  public static final int K = 1;//10;
  // Students distribution or t-distribution, alpha=0.05,f=K-1 => f=9 and alpha/2=0.025
  private static double t = 2.26;


  public static void main(String args[]) {
	try {

	// Setup files
	String resultFilePathBase = args[0];
	setupFiles(resultFilePathBase);

	// Move arguments
	String[] argList = new String[args.length-1];
	for (int i = 0; i < argList.length; i++) {
		argList[i] = args[i+1];
	}

	// Pre-heat
	System.out.println("- Pre-heating ..");
	for (int i = 0; i < N; i++) {
		System.out.println("- " + i + " - Compiling .. ");
		compile(argList);
	}

	// Turn of the JIT
	System.out.println("- Turning of JIT and rerunning ..");
	java.lang.Compiler.disable();
	for (int i = 0; i < M; i++) {
		System.out.println("- " + i + " - Compiling ..");
		System.gc();
		compile(argList);
	}

	// Measure
	System.out.println("- Measuring ..");
	Runtime runtime = Runtime.getRuntime();
	long[] time = new long[K];
	long[] mem = new long[K];
	for (int i = 0; i < K; i++) {

		// Run gc
		System.out.println("- Calling the GC ..");
		System.gc();

		// Reset counters
		deadStmtNum = 0;
		deadExprNum = 0;
		deadOtherNum = 0;
		deadCodeNum = 0;

		// Run
      	long used = runtime.totalMemory()-runtime.freeMemory();
		long start = System.currentTimeMillis();
    	compile(argList);
		time[i] = System.currentTimeMillis() - start;
		mem[i] = ((runtime.totalMemory()-runtime.freeMemory()) - used)/1000;

		// Print results
		outTime.println(time[i]);
		outMem.println(mem[i]);
		String resAnalysis = "# stmts: " + deadStmtNum + ", # exprs: " + deadExprNum + 
			", # others: " + deadOtherNum + ", # total: " + deadCodeNum;
		outAnalysis.println(resAnalysis);
		System.out.println("-- time[" + i + "]: " + time[i] + ", mem[" + i + "]: " + mem[i]);
		System.out.println("-- " + resAnalysis);

	}

	// Turning on the JIT
	System.out.println("- Turning on the JIT ..");
	java.lang.Compiler.enable();

/* Test values:
	time[0]= 3691; time[1]= 3816; time[2]= 2526; time[3]= 3698;
	time[4]= 3538; time[5]= 2617; time[6]= 3397; time[7]= 3032;
	time[8]= 3037; time[9]= 3814;
*/

	// Average
	long sumTime = 0;
	long sumMem = 0;
	for (int i = 0; i < time.length; i++) {
		sumTime += time[i];
		sumMem += mem[i];
	}
	double avgTime = (sumTime*1.0)/time.length;
	outTime.println("_" + avgTime + "_");
	double avgMem = (sumMem*1.0)/mem.length;
	outMem.println("_" + avgMem + "_");
	System.out.println("-- avg: time = " + avgTime + " ms, mem = " + avgMem + " kb");

	// Standard deviation
	double sTime = 0.0;
	double sMem = 0.0;
	for (int i = 0; i < time.length; i++) {
		sTime += Math.pow(time[i] - avgTime,2);
		sMem += Math.pow(mem[i] - avgMem,2);
	}
	sTime = sTime / (K - 1);
	sTime = Math.sqrt(sTime);
//	outTime.println(sTime);
	sMem = sMem / (K - 1);
	sMem = Math.sqrt(sMem);
//	outMem.println(sMem);
//	System.out.println("-- stand. dev.: time = " + sTime + ", mem = " + sMem);

	// Confidence interval
	double c1Time = avgTime - t*(sTime/Math.sqrt(K));
	double c2Time = avgTime + t*(sTime/Math.sqrt(K));
	outTime.println("[" + c1Time + "," + c2Time + "]");
	outTime.println("1/2: " + (c2Time-c1Time)/2);

	double c1Mem = avgMem - t*(sMem/Math.sqrt(K));
	double c2Mem = avgMem + t*(sMem/Math.sqrt(K));
	outMem.println("[" + c1Mem + "," + c2Mem + "]");
	outMem.println("1/2: " + (c2Mem-c1Mem)/2);

	System.out.println("-- confidence level = 95%");
	System.out.println("-- confidence interval: time = [" + c1Time + 
		"," + c2Time + "], mem = [" + c1Mem + "," + c2Mem + "]");

	closeFiles();

	} catch (Exception e) {
		System.out.println("error: " + e.getMessage());
		System.exit(1);
	} 
  }

  public static boolean compile(String args[]) {
	return new JavaCheckerTimeMem().process(
        args,
        new BytecodeParser(),
        new JavaParser() {
          parser.JavaParser parser = new parser.JavaParser();
          public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
            return parser.parse(is, fileName);
          }
		
        }
    );
  }

  static int deadCodeNum = 0;
  static int deadStmtNum = 0;
  static int deadExprNum = 0;
  static int deadOtherNum = 0;

  protected void processNoErrors(CompilationUnit unit) {
    //DeadCode 
	outAnalysis.println(" -- Unit: " + unit.pathName() + " -- ");
    for(Iterator it = unit.deadCode().iterator();it.hasNext();) {
    	CFNode node = (CFNode)it.next();
    	if(node instanceof Expr) {
    		outAnalysis.println(" [Expr:" + deadExprNum++ + "]: " + node+" in "+((Expr)node).enclosingStmt());
    	} if (node instanceof Stmt) {
			outAnalysis.println(" [Stmt:" + deadStmtNum++ + "]: " + node);
		} else {
    		outAnalysis.println(" [" + node.getClass().getName() + ":" + deadOtherNum++ + "]: " + node);
		}
    }
	deadCodeNum += unit.deadCode().size();  
  }


  private static PrintStream outTime;
  private static PrintStream outMem;
  private static PrintStream outAnalysis;

  private static void setupFiles(String resultFilePathBase) throws IOException {
	File fileTime = new File(resultFilePathBase+".time");
	if (fileTime.exists()) {
		fileTime.delete();
	} 
	fileTime.createNewFile();
	outTime = new PrintStream(fileTime);

	File fileMem = new File(resultFilePathBase+".mem");
	if (fileMem.exists()) {
		fileMem.delete();
	} 
	fileMem.createNewFile();
	outMem = new PrintStream(fileMem);

	File fileAnalysis = new File(resultFilePathBase+".analysis");
	if (fileAnalysis.exists()) {
		fileAnalysis.delete();
	} 
	fileAnalysis.createNewFile();
	outAnalysis = new PrintStream(fileAnalysis);
  }

  private static void closeFiles() throws IOException {
	outTime.close();
	outMem.close();
	outAnalysis.close();
  }

}
