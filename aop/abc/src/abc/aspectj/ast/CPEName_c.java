package arc.aspectj.ast;

import arc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class CPEName_c extends ClassnamePatternExpr_c 
                       implements CPEName
{
    protected NamePattern pat;

    public CPEName_c(Position pos, NamePattern pat)  {
	super(pos);
        this.pat = pat;
    }

    protected CPEName_c reconstruct(NamePattern pat) {
	if (pat != this.pat) {
	    CPEName_c n = (CPEName_c) copy();
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
    }

    public boolean matches(PatternMatcher matcher, PCNode cl) {
	return matcher.getMatches(pat).contains(cl);
    }

}
