package abc.aspectj.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import polyglot.ext.jl.types.TypeSystem_c;

import java.util.*;

/** specification part of before advice
 * 
 * @author Oege de Moor
 */
public class Before_c extends AdviceSpec_c 
                              implements Before
{

    public Before_c(Position pos, 
                    List formals,
                    TypeNode voidn)
    {
	super(pos, formals, voidn, null);
    }

	public String kind() {
		return "before";
	}
	// string representation for error messages
	public String toString() {
		String s = "before(";

		for (Iterator i = formals.iterator(); i.hasNext(); ) {
			Formal t = (Formal) i.next();
			s += t.toString();

			if (i.hasNext()) {
				  s += ", ";
			}
		}
		s = s + ")";
		
		return s;
	}
	  
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("before(");

	w.begin(0);

	for (Iterator i = formals.iterator(); i.hasNext(); ) {
	    Formal f = (Formal) i.next();
	    print(f, w, tr);

	    if (i.hasNext()) {
		w.write(",");
		w.allowBreak(0, " ");
	    }
	}

	w.end();
	w.write(")");

    }

    public abc.weaving.aspectinfo.AdviceSpec makeAIAdviceSpec() {
	return new abc.weaving.aspectinfo.BeforeAdvice(position());
    }
}
