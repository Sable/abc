package arc.aspectj.ast;

import arc.aspectj.visit.*;

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

    public Set/*<PCNode>*/ match(PCNode context) {
	Set/*<PCNode>*/ init_matches = init.match(context);
	Set/*<PCNode>*/ result = new HashSet();
	Pattern lp = Pattern.compile(((SimpleNamePattern_c)last).pat);
	Iterator imi = init_matches.iterator();
	while (imi.hasNext()) {
	    PCNode im = (PCNode)imi.next();
	    result.add(im.matchClass(lp));
	}
	return result;
    }
}
