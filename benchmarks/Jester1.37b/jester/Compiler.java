package jester;

public interface Compiler {
	void compile(String sourceFileName) throws SourceChangeException;
}