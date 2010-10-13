package jester;

public interface Report {
	public static final int INITIAL_SCORE = -1;

	void setNumberOfFilesThatWillBeTested(int numberOfFilesThatWillBeTested);
	void startFile(String sourceFileName, IgnoreListDocument originalContents) throws SourceChangeException;
	void finishFile(String sourceFileName) throws SourceChangeException;

	int fileScore();
	int totalScore();

	void changeThatCausedTestsToFail(int indexOfChange, String valueChangedFrom, String valueChangedTo) throws SourceChangeException;
	void changeThatDidNotCauseTestsToFail(int indexOfChange, String valueChangedFrom, String valueChangedTo) throws SourceChangeException;
}