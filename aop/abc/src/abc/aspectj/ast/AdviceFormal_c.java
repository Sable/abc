
package abc.aspectj.ast;

import polyglot.ast.Formal;
import polyglot.ext.jl.ast.Formal_c;

public class AdviceFormal_c extends Formal_c implements AdviceFormal {

   public AdviceFormal_c(Formal f) {
   	   super(f.position(), f.flags(), f.type(), f.name());
   }

}
