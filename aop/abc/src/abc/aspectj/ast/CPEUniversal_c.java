package abc.aspectj.ast;

import abc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class CPEUniversal_c extends ClassnamePatternExpr_c implements CPEUniversal
{
    private List excludes = new ArrayList();

    public CPEUniversal_c(Position pos)  {
	super(pos);
    }

    public Precedence precedence() {
	return Precedence.UNARY;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("*");
    }

    public String toString() {
	return "*";
    }

    public void addExclude(ClassnamePatternExpr pat) {
	excludes.add(pat);
    }

    public void setExcludes(List excludes) {
	this.excludes = excludes;
    }

    public boolean matches(PatternMatcher matcher, PCNode cl) {
	Iterator ei = excludes.iterator();
	while (ei.hasNext()) {
	    ClassnamePatternExpr e = (ClassnamePatternExpr)ei.next();
	    if (e.matches(matcher, cl)) return false;
	}
	return true;
    }

    public boolean equivalent(ClassnamePatternExpr otherexp) {
	if (otherexp instanceof CPEUniversal) {
	    return true;
	} else return false;
    }
}
