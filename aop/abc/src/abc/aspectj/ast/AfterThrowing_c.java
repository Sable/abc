package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class AfterThrowing_c extends AdviceSpec_c 
                             implements AfterThrowing
{
   
    public AfterThrowing_c(Position pos, 
                           List formals,
                           Formal exc,
                           TypeNode voidn)
    {
	    super(pos);
        this.formals = formals;
        this.returnType = voidn;
        this.returnVal = exc;
    }

       
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.begin(0);
        
        w.write("after(");

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
        w.allowBreak(0, " ");
        w.write("throwing");

		if (returnVal != null) {
	 	   w.write("(");
	 	   print(returnVal,w,tr);
	    	w.write(")");
		}

        w.end();
    }

}






