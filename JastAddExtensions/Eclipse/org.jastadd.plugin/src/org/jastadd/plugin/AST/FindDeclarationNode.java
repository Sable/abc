package org.jastadd.plugin.AST;

public interface FindDeclarationNode {
	
	public ASTNode declaration();

	public int declarationLocationLine();

	public int declarationLocationColumn();

	public int declarationLocationLength();
	
}
