package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class After_c extends AdviceSpec_c 
                             implements After
{

    public After_c(Position pos, 
                    List formals,
                    TypeNode voidn)
    {
	    super(pos);
        this.formals = formals;
        this.returnType = voidn;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
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

    }

}






