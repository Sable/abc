
package abc.aspectj.ast;

import polyglot.util.Position;
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.types.Context;

import polyglot.ext.jl.ast.Formal_c;

/** A class for representing special advice formals like the return value 
 * of <code> afterreturning</code> or <code>afterthrowing</code>.
 * @author Oege de Moor */
public class AdviceFormal_c extends Formal_c implements AdviceFormal {

   public AdviceFormal_c(Position pos, Flags flags, TypeNode tn, String name) {
   	   super(pos, flags, tn, name);
   }

	/** advice formals are not automatically added to the context,
     * unlike ordinary formals: they are not in scope in the pointcut,
     * but they are visible in the advice body.
     * @see{Context AdviceDecl_c.enterScope(Node child, Context c)}
	 */
   public void addDecls(Context c) {
	 // do nothing
   }

}
