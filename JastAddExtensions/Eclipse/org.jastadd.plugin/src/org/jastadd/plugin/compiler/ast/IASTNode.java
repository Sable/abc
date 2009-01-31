package org.jastadd.plugin.compiler.ast;

/**
 * This interface must be implemented by all nodes in the AST, hence should be
 * implemented by ASTNode. Required by the org.jastadd.plugin.compile.ICompiler interface
 * used by the org.jastadd.plugin.Builder and org.jastadd.plugin.compilers extension point.
 * Contains methods required by the AST registry.
 * 
 * @author emma
 *
 */
public interface IASTNode {

	/**
	 * Checks is this node is the root of the project AST
	 * @return true if this is the root of the project AST
	 */
	public boolean isProjectAST();
	
	/**
	 * Looks up a child AST in the AST using a key corresponding to, e.g., file name. 
	 * @param key The key to compare with
	 * @return A node implementing IASTNode, or null if no match was found
	 */
	public IASTNode lookupChildAST(String key);
	
	/**
	 * Flushes all calculated attribute values in the project AST.
	 */
	public void flushAttributes();	

	
	/**
	 * Nodes which can be looked up have keys. Typical examples are nodes which
	 * can appear as roots in an outline and have a natural file name key.
	 * @return true if a node as a lookup key, otherwPositionise false
	 */
	public boolean hasLookupKey();
	
	/**
	 * If hasLookupKey() returns true the key can be acquired with this method.
	 * @return This nodes key as a String, null the node doesn't have a key
	 */
	public String lookupKey();
	
	/**
	 * Replaces this node with the given node by adding this node as a child to
	 * this nodes parent at the right position. This method should not be
	 * used/implemented if the issue of flushing attributes hasn't been dealt with.
	 * @param node The node to replace this node with.
	 */
	public void replaceWith(IASTNode node);
	
	
	/**
	 * Lock object used for synchronization when an AST is updated in the registry.
	 * Should be the same for the whole project AST and used for synchronization at
	 * attribute access. The reason being that the AST containing attribute values
	 * could be updated at the same time that attribute values are requested. 
	 * @return The lock object for this project AST
	 */
	public Object treeLockObject();
	

}
