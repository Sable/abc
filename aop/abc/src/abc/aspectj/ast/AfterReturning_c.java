package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.NodeFactory_c;

import abc.weaving.aspectinfo.AbcFactory;

public class AfterReturning_c extends AdviceSpec_c 
                              implements AfterReturning
{
    
    public AfterReturning_c(Position pos, 
                            List formals,
                            AdviceFormal returnResult,
                            TypeNode voidn)
    {
	super(pos, formals, voidn, returnResult);
    }
    
    public String kind() {
    	return "afterReturning";
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
    
    public abc.weaving.aspectinfo.AdviceSpec makeAIAdviceSpec() {
	if (returnVal == null) {
	    return new abc.weaving.aspectinfo.AfterReturningAdvice(position());
	} else {
	    abc.weaving.aspectinfo.Formal f = new abc.weaving.aspectinfo.Formal
		(AbcFactory.AbcType(returnVal.type().type()),
		 returnVal.name(),
		 returnVal.position());
	    return new abc.weaving.aspectinfo.AfterReturningArgAdvice(f, position());
	}
    }

}






