
package abc.aspectj.ast;



import polyglot.util.Position;
import polyglot.util.CodeWriter;
import polyglot.ast.Precedence;
import polyglot.visit.PrettyPrinter;
import abc.weaving.aspectinfo.EmptyPointcut;

public class PCEmpty_c extends Pointcut_c implements PCEmpty {

	public PCEmpty_c(Position pos) {
		super(pos);
	}

	public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	  return new EmptyPointcut(position);
	 }
	 
	public Precedence precedence() {
		  return Precedence.LITERAL;
	  }

	public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
		  w.write("/* empty pointcut */");
	  }
    
}
