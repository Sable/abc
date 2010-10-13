package jester;

public interface TestRunner {
	boolean testsRunWithoutFailures() throws SourceChangeException;
}