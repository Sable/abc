package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class Around_c extends AdviceSpec_c 
                              implements Around
{


    public Around_c(Position pos, 
		    TypeNode returnType,
		    List formals)
    {
		super(pos);
    	this.formals = formals;
		this.returnType = returnType;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
		w.begin(0);
		print(returnType,w,tr);
		w.allowBreak(0," ");

        w.write("around (");

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
        w.end();
    }

}
