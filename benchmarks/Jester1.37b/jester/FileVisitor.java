package jester;

public interface FileVisitor {
	void visit(String fileName) throws SourceChangeException;
}
