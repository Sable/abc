
package abc.aspectj.ast;

import polyglot.ext.jl.ast.Node_c;
import polyglot.util.Position;

/** argument patterns for args, this, target and named pointcuts 
 * @author Oege de Moor
 */
public class ArgPattern_c extends Node_c implements ArgPattern {

	public ArgPattern_c(Position pos) {
		super(pos);
	}
}
