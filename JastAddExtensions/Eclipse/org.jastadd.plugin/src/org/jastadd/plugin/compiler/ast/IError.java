package org.jastadd.plugin.compiler.ast;

/**
 * Error object should implement this interface to communicate errors to
 * the IDE. Required by the core.compiler.ICompiler interface.
 * @author emma
 *
 */
public interface IError {
	
	public static final int LEXICAL = 0;
	public static final int SYNTACTIC = 1;
	public static final int SEMANTIC = 2;
	public static final int OTHER = 2;
	
	/**
	 * A message explaining the error
	 * @return The error message
	 */
	public String getMessage();
	
	/**
	 * Kind of error as provided by the attributes in this interface
	 * lexical, syntactic, semantic or other
	 * @return The kind of error
	 */
	public int getKind();
	
	/**
	 * Severity as provided by the IMarker interface
	 * @return The severity of the error
	 */
	public int getSeverity();
	
	/**
	 * The line where the error starts
	 * @return The line number
	 */
	public int getLine();
	
	/**
	 * The offset where the error starts
	 * @return The start offset of the error
	 */
	public int getStartOffset();

	/**
	 * The offset where the error ends
	 * @return The end offset of the error
	 */
	public int getEndOffset();
	
}
