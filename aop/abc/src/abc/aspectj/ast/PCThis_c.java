package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import abc.aspectj.visit.AspectInfoHarvester;
import abc.weaving.aspectinfo.AbcFactory;
import abc.aspectj.types.AJContext;

public class PCThis_c extends Pointcut_c implements PCThis
{
    protected Node pat; // ArgPattern, becomes TypeNode, Local or ArgStar

    public PCThis_c(Position pos, ArgPattern pat)  {
	super(pos);
        this.pat = pat;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }
    
    public Set pcRefs() {
    	return new HashSet();
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
	   AJContext c = (AJContext) tc.context();
	   
	   	
		if (pat instanceof TPEUniversal)
			return this;
		
		if (pat instanceof ArgStar)
			return this;
		
		if (! ((pat instanceof Typed) && ((Typed)pat).type() instanceof ReferenceType))
		   throw new SemanticException("Argument of \"this\" must be of reference type",pat.position());
		   
		if (c.inDeclare())
			throw new SemanticException("this(..) requires a dynamic test and cannot be used inside a \"declare\" statement",position());
		  
		return this;
	}
	
	public Collection mayBind() throws SemanticException {
		Collection result = new HashSet();
		if (pat instanceof Local) {
				String l = ((Local)pat).name();
				if (l == Pointcut_c.initialised)
							throw new SemanticException("cannot explicitly bind local \"" + l + "\"", pat.position());
				result.add(((Local)pat).name());
		}
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
    
    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	if (pat instanceof Local) {
	    return new abc.weaving.aspectinfo.ThisVar
		(new abc.weaving.aspectinfo.Var(((Local)pat).name(),
						((Local)pat).position()),
		 position());
	} else if (pat instanceof TypeNode) {
	    return new abc.weaving.aspectinfo.ThisType
		(AbcFactory.AbcType(((TypeNode)pat).type()),
		 position());
	} else if (pat instanceof ArgStar) {
	    return new abc.weaving.aspectinfo.ThisAny(position());
	} else {
	    throw new RuntimeException("Unexpected pattern in this pointcut: "
				       +pat);
	}
    }
    

}
