package polyglot.ext.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class TPEType_c extends TypePatternExpr_c implements TPEType
{
    protected TypeNode type;

    public TPEType_c(Position pos, TypeNode type)  {
	super(pos);
        this.type = type;
    }

    public Precedence precedence() {
	return Precedence.UNARY;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        print(type, w, tr);
    }

}
