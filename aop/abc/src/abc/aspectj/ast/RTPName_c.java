package arc.aspectj.ast;

import arc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

import polyglot.ext.jl.ast.Node_c;

import java.util.*;

public class RTPName_c extends Node_c 
                       implements RTPName
{
    protected NamePattern pat;

    public RTPName_c(Position pos, 
                     NamePattern pat)  {
	super(pos);
        this.pat = pat;
    }
    
	/** Reconstruct the pointcut call. */
	protected RTPName_c reconstruct(NamePattern pat) {
		if (this.pat != pat) {
			 RTPName_c n = (RTPName_c) copy();
			 n.pat = pat;
			 return n;
		}
		return this;
	}

	/** Visit the children of the pointcut call. */
	public Node visitChildren(NodeVisitor v) {
		NamePattern pat = (NamePattern)visitChild(this.pat, v);
		return reconstruct(pat);
	}

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	print(pat,w,tr);
    }

    public boolean matchesClass(PatternMatcher matcher, PCNode cl) {
	return matcher.getMatches(pat).contains(cl);
    }

    public boolean matchesArray(PatternMatcher matcher) {
	return false;
    }

    public ClassnamePatternExpr transformToClassnamePattern(AspectJNodeFactory nf) throws SemanticException {
	return nf.CPEName(position, pat);
    }

}
