package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PCArgs_c extends Pointcut_c implements PCArgs
{
    protected List pats;

    public PCArgs_c(Position pos, List pats)  {
		super(pos);
        this.pats = copyList(pats);
    }
    
	private List copyList(List xs) {
		return new LinkedList(xs);
	}

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    protected PCArgs_c reconstruct(List pats) {
		if (! CollectionUtil.equals(pats,this.pats)) {
			PCArgs_c n = (PCArgs_c) copy();
			n.pats =  copyList(pats); // may become a list of TypeNode, Local and TPEUniversal
			return n;
		}
 		return this;
	}

	public Node visitChildren(NodeVisitor v) {
		List pats = visitList(this.pats, v);
		return reconstruct(pats);
	}
	
	
	public Collection mayBind() throws SemanticException {
		Collection result = new HashSet();
		for (Iterator i = pats.iterator(); i.hasNext(); ) {
			Node pat = (Node) i.next();
			if (pat instanceof Local) {
				String l = ((Local) pat).name();
				 if (result.contains(l))
				     throw new SemanticException("repeated binding of \"" + l +"\"",
				                                                               pat.position());
				 else result.add(l);
			}
		}
		return result;
	}
   
		public Collection mustBind() {
			Collection result = new HashSet();
			for (Iterator i = pats.iterator(); i.hasNext(); ) {
				Node pat = (Node) i.next();
				if (pat instanceof Local)
					 result.add(((Local)pat).name());
			}
			return result;
		}
   
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("args(");
        for (Iterator i = pats.iterator(); i.hasNext(); ) {
	        FormalPattern fp = (FormalPattern) i.next();
		print(fp, w, tr);

		if (i.hasNext()) {
		    w.write(",");
		    w.allowBreak(4, " ");
		}
	    }
        w.write(")");
    }

}
