package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import abc.aspectj.visit.ContainsAspectInfo;
import abc.aspectj.visit.AspectInfoHarvester;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.Aspect;

public class DeclareSoft_c extends DeclareDecl_c 
    implements DeclareSoft, ContainsAspectInfo
{

    TypeNode type;
    Pointcut pc;

    public DeclareSoft_c(Position pos, 
                         TypeNode type,
                         Pointcut pc)
    {
	super(pos);
        this.type = type;
        this.pc   = pc;
    }

    protected DeclareSoft_c reconstruct(TypeNode type, Pointcut pc) {
	if (type != this.type || pc != this.pc) {
	    DeclareSoft_c n = (DeclareSoft_c) copy();
	    n.type = type;
	    n.pc = pc;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	TypeNode type = (TypeNode) visitChild(this.type, v);
	Pointcut pc = (Pointcut) visitChild(this.pc, v);
	return reconstruct(type, pc);
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("declare soft : ");
        print(type,w,tr);
        w.write(" : ");
        print(pc, w, tr);
        w.write(";");
    }

    public void update(GlobalAspectInfo gai, Aspect currrent_aspect) {
	abc.weaving.aspectinfo.DeclareSoft ds = new abc.weaving.aspectinfo.DeclareSoft
	    (AspectInfoHarvester.toAbcType(type.type()),
	     pc.makeAIPointcut(),
	     currrent_aspect,
	     position());
	gai.addDeclareSoft(ds);
    }

}
