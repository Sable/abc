package arc.aspectj.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import polyglot.ext.jl.types.TypeSystem_c;

import java.util.*;

public class Before_c extends AdviceSpec_c 
                              implements Before
{

    public Before_c(Position pos, 
                    List formals,
                    TypeNode voidn)
    {
	    super(pos);
        this.formals = formals;
        this.returnType = voidn;
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

}
