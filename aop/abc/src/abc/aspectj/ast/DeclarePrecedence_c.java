package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import abc.aspectj.visit.ContainsAspectInfo;

import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.GlobalAspectInfo;

public class DeclarePrecedence_c extends DeclareDecl_c 
    implements DeclarePrecedence, ContainsAspectInfo
{

    TypedList pats;

    public DeclarePrecedence_c(Position pos, 
                               List pats)
    {
	super(pos);
        this.pats = TypedList.copyAndCheck(pats,
                                           ClassnamePatternExpr.class,
                                           true);
    }

    protected DeclarePrecedence_c reconstruct(TypedList pats) {
	if (!CollectionUtil.equals(pats, this.pats)) {
	    DeclarePrecedence_c n = (DeclarePrecedence_c) copy();
	    n.pats = TypedList.copyAndCheck(pats, ClassnamePatternExpr.class, true);
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	TypedList pats = new TypedList(visitList(this.pats, v), ClassnamePatternExpr.class, true);
	return reconstruct(pats);
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("declare precedence : ");
        for (Iterator i = pats.iterator(); i.hasNext(); ) {
           ClassnamePatternExpr en = (ClassnamePatternExpr) i.next();
           print(en, w, tr);

           if (i.hasNext()) {
                w.write (", ");
           }
        }
        w.write(";");
    }

    public List pats() {
	return pats;
    }

    public void update(GlobalAspectInfo gai, Aspect current_aspect) {
	List cnpats = new ArrayList();
	Iterator pi = pats.iterator();
	while (pi.hasNext()) {
	    ClassnamePatternExpr p = (ClassnamePatternExpr)pi.next();
	    cnpats.add(p.makeAIClassnamePattern());
	}
	gai.addDeclarePrecedence(new abc.weaving.aspectinfo.DeclarePrecedence
				 (cnpats, current_aspect, position()));
    }	
}




