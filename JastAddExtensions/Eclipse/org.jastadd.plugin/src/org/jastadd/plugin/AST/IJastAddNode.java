package org.jastadd.plugin.AST;

public interface IJastAddNode {

	public IJastAddNode getChild(int i);
	public int getNumChild();
	public IJastAddNode getParent();

	public int getBeginLine();
	public int getBeginColumn();
	public int getEndLine();
	public int getEndColumn();
	
	public String getFileName();
	public int getBeginOffset();
	public int getEndOffset();
	
	public Object treeLockObject();
	
}
