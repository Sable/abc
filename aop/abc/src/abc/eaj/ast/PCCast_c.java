package abc.eaj.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

import abc.aspectj.ast.*;
import abc.aspectj.types.*;
import abc.aspectj.visit.AspectMethods;

import java.util.*;


public class PCCast_c extends Pointcut_c
                      implements PCCast
{
    protected TypePatternExpr type_pattern;

    public PCCast_c(Position pos, TypePatternExpr type_pattern)
    {
        super(pos);
        this.type_pattern = type_pattern;
    }

	public Set pcRefs() {
		return new HashSet();
	}
	
    public Precedence precedence()
    {
        return Precedence.LITERAL;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter pp)
    {
        w.write("cast(");
        print(type_pattern, w, pp);
        w.write(")");
    }

    protected PCCast_c reconstruct(TypePatternExpr type_pattern)
    {
        if (type_pattern != this.type_pattern) {
            PCCast_c n = (PCCast_c) copy();
            n.type_pattern = type_pattern;
            return n;
        }
        return this;
    }

    public Node visitChildren(NodeVisitor v)
    {
        TypePatternExpr type_pattern =
            (TypePatternExpr) visitChild(this.type_pattern, v);
        return reconstruct(type_pattern);
    }


    public boolean isDynamic() {
	return true;
    }

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut()
    {
        return new abc.eaj.weaving.aspectinfo.Cast
              (type_pattern.makeAITypePattern(), position());
    }
}
