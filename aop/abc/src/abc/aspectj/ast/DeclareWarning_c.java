package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import abc.aspectj.visit.ContainsAspectInfo;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.DeclareMessage;

public class DeclareWarning_c extends DeclareDecl_c 
    implements DeclareWarning, ContainsAspectInfo
{

    Pointcut pc;
    String text;

    public DeclareWarning_c(Position pos, 
                            Pointcut pc,
                            String text)
    {
	super(pos);
        this.pc   = pc;
        this.text = text;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("declare warning : ");
        print(pc, w, tr);
        w.write(" : ");
        w.write("\""+text+"\"");
        w.write(";");
    }

    protected DeclareWarning_c reconstruct(Pointcut pc) {
	if (pc != this.pc) {
	    DeclareWarning_c n = (DeclareWarning_c) copy();
	    n.pc = pc;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	Pointcut pc = (Pointcut) visitChild(this.pc, v);
	return reconstruct(pc);
    }

    public void update(GlobalAspectInfo gai, Aspect current_aspect) {
	gai.addDeclareMessage(new DeclareMessage
			      (DeclareMessage.WARNING,
			       pc.makeAIPointcut(),
			       text,
			       current_aspect,
			       position()));
    }
}
