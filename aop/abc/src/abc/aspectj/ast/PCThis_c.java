package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PCThis_c extends Pointcut_c implements PCThis
{
    protected Node pat; // AmbTypeOrLocal, becomes TypeNode, Local, or TPEUniversal

    public PCThis_c(Position pos, AmbTypeOrLocal pat)  {
	super(pos);
        this.pat = pat;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }
    
    
	/** Reconstruct the pointcut. */
	protected PCThis_c reconstruct(Node pat) {
	 if (pat != this.pat) {
		   PCThis_c n = (PCThis_c) copy();
		   n.pat = pat;
		   return n;
		}
		return this;
	}

		/** Visit the children of the pointcut. */
	public Node visitChildren(NodeVisitor v) {
		Node pat = (Node) visitChild(this.pat, v);
		return reconstruct(pat);
	}

	/** type check the use of  this */
	public Node typeCheck(TypeChecker tc) throws SemanticException {
	   TypeSystem ts = tc.typeSystem();
	   Context c = tc.context();
	   Node voidType = tc.nodeFactory().CanonicalTypeNode(position(),ts.Void());

		if (pat instanceof TPEUniversal)
			return voidType;
		
		if (! (((Typed)pat).type() instanceof ReferenceType))
		   throw new SemanticException("Argument of \"this\" must be of reference type",pat.position());
		   
		return voidType;
	}
	
	public Collection mayBind() throws SemanticException {
		Collection result = new HashSet();
		if (pat instanceof Local)
			result.add(((Local)pat).name());
		 return result;
	}
   
	public Collection mustBind() {
		Collection result = new HashSet();
			if (pat instanceof Local)
				result.add(((Local)pat).name());
			 return result;
	}
 

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("this(");
        print(pat, w, tr);
        w.write(")");
    }
    
    

}
