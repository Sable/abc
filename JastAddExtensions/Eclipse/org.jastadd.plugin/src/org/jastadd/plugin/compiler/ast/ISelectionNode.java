package org.jastadd.plugin.compiler.ast;

public interface ISelectionNode extends IJastAddNode {
	public int selectionLine();
	public int selectionColumn();
	public int selectionLength();
	public int selectionEndLine();
	public int selectionEndColumn();
}
