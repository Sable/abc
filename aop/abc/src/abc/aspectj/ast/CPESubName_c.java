package abc.aspectj.ast;

import abc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class CPESubName_c extends ClassnamePatternExpr_c 
                          implements CPESubName
{
    protected NamePattern pat;

    public CPESubName_c(Position pos, NamePattern pat)  {
	super(pos);
        this.pat = pat;
    }

    protected CPESubName_c reconstruct(NamePattern pat) {
	if (pat != this.pat) {
	    CPESubName_c n = (CPESubName_c) copy();
	    n.pat = pat;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	NamePattern pat = (NamePattern) visitChild(this.pat, v);
	return reconstruct(pat);
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	print(pat,w,tr);
	w.write("+");
    }

    public String toString() {
	return pat+"+";
    }

    public boolean matches(PatternMatcher matcher, PCNode cl) {
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
}
