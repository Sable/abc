package jester;

public interface XMLReportWriter {
	void writeXMLReport(Object[] reportItems, String sourceFileName, int numberOfChangesThatDidNotCauseTestsToFail, int numberOfChanges, int score) throws SourceChangeException;
}
