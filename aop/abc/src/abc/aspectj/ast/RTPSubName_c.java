package abc.aspectj.ast;

import abc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

import polyglot.ext.jl.ast.Node_c;

import java.util.*;

/**
 * A reference type pattern that matches all subclasses (..)+
 * @author Oege de Moor
 * @author Aske Simon Christensen
 *
 */
public class RTPSubName_c extends Node_c 
    implements RTPSubName, ContainsNamePattern
{
    protected NamePattern pat;

    public RTPSubName_c(Position pos, 
                        NamePattern pat)  {
	super(pos);
        this.pat = pat;
    }
    
	/** Reconstruct the pointcut call. */
	protected RTPSubName_c reconstruct(NamePattern pat) {
		if (this.pat != pat) {
			 RTPSubName_c n = (RTPSubName_c) copy();
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
        w.write("+");
    }

    public String toString() {
	return pat.toString()+"+";
    }

    public NamePattern getNamePattern() {
	return pat;
    }

    public boolean matchesClass(PatternMatcher matcher, PCNode cl) {
	return matches(matcher, cl);
    }

    public boolean matchesArray(PatternMatcher matcher) {
	return matcher.matchesObject(pat);
    }

    private boolean matches(PatternMatcher matcher, PCNode cl) {
	if (matcher.matchesName(pat, cl)) {
	    return true;
	}
	Set tried = new HashSet();
	tried.add(cl);
	LinkedList worklist = new LinkedList(tried);
	while (!worklist.isEmpty()) {
	    PCNode n = (PCNode)worklist.removeFirst();
	    Iterator pi = n.getParents().iterator();
	    while (pi.hasNext()) {
		PCNode parent = (PCNode)pi.next();
		if (!tried.contains(parent)) {
		    if (matcher.matchesName(pat, parent)) {
			return true;
		    }
		    tried.add(parent);
		    worklist.addLast(parent);
		}
	    }
	}
	return false;
    }

    public ClassnamePatternExpr transformToClassnamePattern(AJNodeFactory nf) throws SemanticException {
	return nf.CPESubName(position, pat);
    }


    public boolean equivalent(RefTypePattern p) {
	if (p instanceof RTPSubName) {
	    return (pat.equivalent(((RTPSubName)p).getNamePattern()));
	} else return false;
    }

}
