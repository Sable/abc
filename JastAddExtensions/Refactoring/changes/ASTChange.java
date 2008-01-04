package changes;

/*
 * An ASTChange represents an undoable change to the syntax tree.
 */

public abstract class ASTChange {
	
	public abstract String prettyprint();
    public abstract void undo();

}
