package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;
import polyglot.ext.jl.ast.Node_c;

public class PointcutDecl_c extends Node_c implements PointcutDecl
{
    Flags flags;
    String name;
    TypedList formals;
    Pointcut pc;

    public PointcutDecl_c(Position pos,
                          Flags flags,
                          String name,
                          List formals,
                          Pointcut pc)
    {
	super(pos);
        this.flags = flags;
        this.name = name;
        this.formals = TypedList.copyAndCheck(formals,Formal.class,true);
        this.pc = pc;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.begin(0);
	w.write(flags.translate());
	w.write("pointcut " + name + "(");

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

       

	if (pc != null) 
	  {
            w.write(" :");
            w.allowBreak(0, " "); 
            print(pc, w, tr);
          }

	w.write(";");
    }
    
}






