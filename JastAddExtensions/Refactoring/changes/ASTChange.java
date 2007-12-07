package changes;

public abstract class ASTChange {
	
	public abstract String prettyprint();
    public abstract void undo();

}
