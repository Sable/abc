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

public class DeclareError_c extends DeclareDecl_c 
    implements DeclareError, ContainsAspectInfo
{

    Pointcut pc;
    String text;

    public DeclareError_c(Position pos, 
                          Pointcut pc,
                          String text)
    {
	super(pos);
        this.pc   = pc;
        this.text = text;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("declare error : ");
        print(pc, w, tr);
        w.write(" : ");
        w.write("\""+text+"\"");
        w.write(";");
    }

    public void update(GlobalAspectInfo gai, Aspect current_aspect) {
	gai.addDeclareMessage(new DeclareMessage
			      (DeclareMessage.ERROR,
			       pc.makeAIPointcut(),
			       text,
			       current_aspect,
			       position()));
    }

}
