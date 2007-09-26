package org.jastadd.plugin.AST;

public interface IFindDeclarationNode {
	
	public IJastAddNode declaration();

	public int declarationLocationLine();

	public int declarationLocationColumn();

	public int declarationLocationLength();
	
}
