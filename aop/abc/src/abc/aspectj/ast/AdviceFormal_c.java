
package abc.aspectj.ast;

import polyglot.util.Position;
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.types.Context;

import polyglot.ext.jl.ast.Formal_c;

public class AdviceFormal_c extends Formal_c implements AdviceFormal {

   public AdviceFormal_c(Position pos, Flags flags, TypeNode tn, String name) {
   	   super(pos, flags, tn, name);
   }

   public void addDecls(Context c) {
	 // do nothing
   }

}
