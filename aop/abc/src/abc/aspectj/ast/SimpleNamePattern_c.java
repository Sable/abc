package abc.aspectj.ast;

import abc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

import java.util.*;
import java.util.regex.*;

public class SimpleNamePattern_c extends NamePattern_c 
                                 implements SimpleNamePattern
{
    String pat;

    public SimpleNamePattern_c(Position pos,String pat) {
        super(pos);
        this.pat = pat;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write(pat);
    }

    public String toString() {
	return pat;
    }

    public Pattern getPattern() {
	return PatternMatcher.v().compileNamePattern(pat);
    }

    public String getPatternString() {
	return pat;
    }

    public Set/*<PCNode>*/ match(PCNode context, Set/*<PCNode>*/ classes, Set/*<PCNode>*/ packages) {
	return context.matchScope(getPattern(), classes, packages);
    }

    public boolean universal() {
	return pat.equals("*");
    }

    public boolean equivalent(NamePattern p) {
	if (p instanceof SimpleNamePattern) {
	    return (pat.equals(((SimpleNamePattern)p).getPatternString()));
	} else return false;
    }
}
