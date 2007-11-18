package abc.ra.ast;

import java.util.List;

import polyglot.ast.Formal;
import polyglot.ast.NodeFactory;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import abc.aspectj.ast.AspectDecl;

public interface RelAspectDecl extends AspectDecl {

	/**
	 * Generates associate and release methods.
	 */
	public RelAspectDecl declareMethods(NodeFactory nf, TypeSystem ts);

	/**
	 * @return the relational aspect formals
	 */
	public List<Formal> formals();

	/** 
	 * Registers the name of a generated tracematch body method.
	 * Those bodies must be post-processed in the backend, replacing <code>this</code>
	 * by the state variable. 
	 */
	public void addTmBodyMethodName(String tmBodyMethodName);

}
