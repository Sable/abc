/*
 * Created on Jul 20, 2004
 */
package abc.aspectj.visit;

import polyglot.ast.Node;
import polyglot.types.SemanticException;
/**
 * @author Oege de Moor
 */
public interface DependsCheck {

	Node checkDepends(DependsChecker dc) throws SemanticException;
	
}
