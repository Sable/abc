
package abc.aspectj.ast;

import polyglot.ast.Ambiguous;
import polyglot.util.Position;

/** Fillers for args(x,..) etc. 
 * 
 * @author Oege de Moor
 */
public class ArgDotDot_c extends ArgPattern_c implements ArgDotDot {

	public ArgDotDot_c(Position pos) {
		super(pos);
	}
}
