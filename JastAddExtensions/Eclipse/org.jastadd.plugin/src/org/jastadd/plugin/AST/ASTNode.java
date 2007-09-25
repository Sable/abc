package org.jastadd.plugin.AST;

public interface ASTNode {

	public abstract ASTNode getChild(int i);
	public abstract int getNumChild();
	public abstract ASTNode getParent();

	public int getBeginLine();
	public int getBeginColumn();
	public int getEndLine();
	public int getEndColumn();
	
}
