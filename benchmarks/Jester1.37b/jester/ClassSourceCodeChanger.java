package jester;

public interface ClassSourceCodeChanger {
	IgnoreListDocument getOriginalContents() throws ConfigurationException;

	void writeOriginalContentsBack() throws SourceChangeException;
	void writeOverSourceReplacing(int index, String oldContents, String newContents) throws SourceChangeException;

	void startJesting() throws SourceChangeException;
	void finishJesting() throws SourceChangeException;

	void lastChangeCausedTestsToFail() throws SourceChangeException;
	void lastChangeDidNotCauseTestsToFail() throws SourceChangeException;
}