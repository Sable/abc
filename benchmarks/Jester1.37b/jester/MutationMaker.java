package jester;

public interface MutationMaker {
	void mutate(String changeFrom1, String changeTo1) throws SourceChangeException;
}