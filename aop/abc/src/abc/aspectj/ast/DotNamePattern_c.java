package abc.aspectj.ast;

import abc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

import java.util.*;
import java.util.regex.*;

public class DotNamePattern_c extends NamePattern_c 
                              implements DotNamePattern
{
    NamePattern init;
    SimpleNamePattern last;

    public DotNamePattern_c(Position pos,NamePattern init,SimpleNamePattern last) {
        super(pos);
        this.init = init;
	this.last = last;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	print(init,w,tr);
	w.write(".");
	print(last,w,tr);
    }

    public String toString() {
	return init+"."+last;
    }

    public Set/*<PCNode>*/ match(PCNode context, Set/*<PCNode>*/ classes, Set/*<PCNode>*/ packages) {
	Set/*<PCNode>*/ init_matches = init.match(context, classes, packages);
	Set/*<PCNode>*/ result = new HashSet();
	Pattern lp = PatternMatcher.v().compileNamePattern(((SimpleNamePattern_c)last).pat);
	Iterator imi = init_matches.iterator();
	while (imi.hasNext()) {
	    PCNode im = (PCNode)imi.next();
	    result.addAll(im.matchClass(lp));
	}
	return result;
    }

    public boolean universal() {
	return false;
    }

}
