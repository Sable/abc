package jester;

public interface Configuration {
	//e.g. "javac"
	public String compilationCommand();

	//e.g. ".java"
	public String sourceFileExtension();

	//e.g. "java jester.TestRunnerImpl"
	public String testRunningCommand();

	//e.g. "PASSED"
	public String testsPassString();

	//e.g. "jesterReport.xml"
	public String xmlReportFileName();

	public boolean shouldReportEagerly();
	public Logger getLogger();

	public boolean closeUIOnFinish();
}