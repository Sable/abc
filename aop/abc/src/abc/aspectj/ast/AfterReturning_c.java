package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.NodeFactory_c;

public class AfterReturning_c extends AdviceSpec_c 
                              implements AfterReturning
{
    
    public AfterReturning_c(Position pos, 
                            List formals,
                            Formal returnResult,
                            TypeNode voidn)
    {
	super(pos, formals, voidn, returnResult);
    }
    
	//	string representation for error messages
	public String toString() {
		String s = "after(";

		for (Iterator i = formals.iterator(); i.hasNext(); ) {
			Formal t = (Formal) i.next();
			s += t.toString();

			if (i.hasNext()) {
					 s += ", ";
			}
		}
		s = s + ") returning";
		
		if (returnVal != null)
		   s = s + " " + returnVal;
		
		return s;
	}
    
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.begin(0);
        w.write("after(");

		w.begin(0);
    
    	for (Iterator i = formals.iterator(); i.hasNext(); ){
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
        w.write("returning");

		if (returnVal != null) {
		    w.write("(");
		    print(returnVal,w,tr);
		    w.write(")");
		}

        w.end();
    }
    

}






