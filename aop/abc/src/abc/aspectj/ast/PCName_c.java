package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;
import polyglot.ext.jl.parse.Name;

public class PCName_c extends Pointcut_c implements PCName
{
    protected Name name;
    protected TypedList args;

    public PCName_c(Position pos, Name name, List args)  {
	super(pos);
        this.name = name;
        this.args = TypedList.copyAndCheck(args,FormalPattern.class,true);
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write(name+"(");
        for (Iterator i = args.iterator(); i.hasNext(); ) {
	        FormalPattern id = (FormalPattern) i.next();
                print(id,w,tr);
		
		if (i.hasNext()) {
		    w.write(",");
		    w.allowBreak(4, " ");
		}
	    }
        w.write(")");
    }

}
