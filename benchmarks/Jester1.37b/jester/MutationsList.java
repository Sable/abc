package jester;

public interface MutationsList {
	void visit(MutationMaker aMutationMaker) throws SourceChangeException;
}