
package abc.aspectj.ast;

import polyglot.ast.Ambiguous;
import polyglot.util.Position;

/** Wildcard argument for args,this,target and named pointcuts.
 * 
 * @author Oege de Moor
 */
public class ArgStar_c extends ArgPattern_c implements ArgStar {

	public ArgStar_c(Position pos) {
		super(pos);
	}
}
