package jester;

import java.io.*;
import java.lang.reflect.Method;

import junit.framework.*;

/**
 * Taken from JUnit 3.2 - see JUnit license.
 * Modified by Ivan Moore for use by Jester.
 *
 * A command line based tool to run tests.
 * <pre>
 * java test.textui.TestRunner TestCaseClass
 * </pre>
 * TestRunner expects the name of a TestCase class as argument.
 * If this class defines a static <code>suite</code> method it 
 * will be invoked and the returned test is run. Otherwise all 
 * the methods starting with "test" having no arguments are run.
 * <p>
 * TestRunner prints a trace as the tests are executed followed by a
 * summary at the end. 
 */
public class TestRunnerImpl implements TestListener {
	static String FAILED = "FAILED";
	static String PASSED = "PASSED";
	static long ONE_HOUR = 1 * 60 * 60 * 1000;
	static long TEN_SECONDS = 10 * 1000;
	static long MINIMUM_TEST_TIMEOUT = TEN_SECONDS;
	static long TEST_TIMEOUT_IF_NONE_SET = ONE_HOUR;

	public TestRunnerImpl() {
	}

	public synchronized void addError(Test test, Throwable t) {
		print(FAILED);
		System.exit(0);
	}

	public synchronized void addFailure(Test test, AssertionFailedError t) {
		print(FAILED);
		System.exit(0);
	}
	/**
	 * Creates the TestResult to be used for the test run.
	 */
	protected TestResult createTestResult() {
		return new TestResult();
	}

	public void endTest(Test test) {
	}

	/**
	 * main entry point.
	 */
	public static void main(String args[]) {
		final long startTime = System.currentTimeMillis();
		boolean timeoutFileNeedsWriting = false;
		long timeout = 0;
		try {
			timeout = Math.max(readTestsTimeout() * 2, MINIMUM_TEST_TIMEOUT);
		} catch (IOException ex) {
			timeoutFileNeedsWriting = true;
			timeout = TEST_TIMEOUT_IF_NONE_SET;
		}
		final long testsTimeout = timeout;
		final long delayBeforeCheckingIfTestsTakingTooLong = 1000;
		Runnable timeoutChecker = new Runnable() {
			public void run() {
				while (true) {
					if (System.currentTimeMillis() > startTime + testsTimeout) {
						System.exit(0);
					}
					try {
						Thread.sleep(delayBeforeCheckingIfTestsTakingTooLong);
					} catch (InterruptedException ex) {
					}
				}
			}
		};
		new Thread(timeoutChecker).start();
		TestRunnerImpl aTestRunner = new TestRunnerImpl();
		aTestRunner.start(args);
		if (timeoutFileNeedsWriting) {
			writeTestsTimeout(System.currentTimeMillis() - startTime);
		}
		System.exit(0);
	}
	/**
	 * Prints failures to the standard output
	 */
	public synchronized void print(TestResult result) {
		//don't care about details for the moment - just the bottom line
		print(result.wasSuccessful() ? PASSED : FAILED);
	}

	/**
	 * Starts a test run. Analyzes the command line arguments
	 * and runs the given test suite.
	 */
	protected void start(String args[]) {
		try {
			Class testClass = getTestCaseClass(args);
			Test suite = getTestSuite(testClass);
			doRun(suite);
		} catch (Exception e) {
			println("Could not create and run test suite");
			System.exit(-1);
		}
	}
	
	private Test getTestSuite(Class testClass) {
		try {
			Method suiteMethod = testClass.getMethod("suite", new Class[0]);
			try {
				return (Test) suiteMethod.invoke(null, new Class[0]);
				// static method
			} catch (Exception e) {
				println("Could not invoke the suite() method");
				System.exit(-1);
				return null;//can never happen because of System.exit(-1); but compiler doesn't realise that
			}
		} catch (Exception e) {
			// try to extract a test suite automatically
			return new TestSuite(testClass);
		}
	}

	private Class getTestCaseClass(String[] args){
		String testCaseName = getTestCaseName(args);
		
		try {
			return Class.forName(testCaseName);
		} catch (Exception e) {
			println("Suite class \"" + testCaseName + "\" not found");
			System.exit(-1);
			return null;//can never happen because of System.exit(-1); but compiler doesn't realise that
		}
	}
	
	private String getTestCaseName(String[] args) {
		String testCaseName = "";

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-v"))
				println("JUnit 3.2 by Kent Beck and Erich Gamma");
			else
				testCaseName = args[i];
		}

		if (testCaseName.equals("")) {
			println("Usage: TestRunner [-wait] testCaseName, where name is the name of the TestCase class");
			System.exit(-1);
		}
		
		return testCaseName;
	}

	public synchronized void startTest(Test test) {
		print(".");
	}
	
	private void print(String string) {
		System.out.print(string);
	}
	
	private void println(String string) {
		System.out.println(string);
	}
	
	private void println() {
		System.out.println();
	}

	protected void doRun(Test suite) {
		TestResult result = createTestResult();
		result.addListener(this);
		long startTime = System.currentTimeMillis();
		suite.run(result);
		long endTime = System.currentTimeMillis();
		long runTime = endTime - startTime;
		println();
		println("Time: " + runTime);
		print(result);

		println();
	}

	private static long readTestsTimeout() throws IOException {
		FileReader timeoutFile = new FileReader(TestTester.TIMEOUT_FILENAME);
		BufferedReader in = new BufferedReader(timeoutFile);
		String firstLine = in.readLine();
		timeoutFile.close();
		return Long.parseLong(firstLine);
	}

	private static void writeTestsTimeout(long timeToRunTests) {
		try {
			FileWriter timeoutFile = new FileWriter(TestTester.TIMEOUT_FILENAME);
			timeoutFile.write(Long.toString(timeToRunTests));
			timeoutFile.close();
		} catch (IOException notMuchYouCanDo) {
		}
	}
}