package abc.aspectj.ast;

import abc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

import java.util.*;

public class DotDotNamePattern_c extends NamePattern_c 
                                 implements DotDotNamePattern
{
    NamePattern init;

    public DotDotNamePattern_c(Position pos,NamePattern init) {
        super(pos);
        this.init = init;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	print(init,w,tr);
	w.write(".");
	// This node will always appear to the left of a dot.
	// Print one extra dot here - that makes two of them.
    }

    public String toString() {
	return init+".";
    }

     public Set/*<PCNode>*/ match(PCNode context, Set/*<PCNode>*/ classes, Set/*<PCNode>*/ packages) {
	Set/*<PCNode>*/ matches = init.match(context, classes, packages);
	LinkedList worklist = new LinkedList(matches);
	while (!worklist.isEmpty()) {
	    PCNode n = (PCNode)worklist.removeFirst();
	    Iterator ii = n.getInners().iterator();
	    while (ii.hasNext()) {
		PCNode inner = (PCNode)ii.next();
		if (!matches.contains(inner)) {
		    matches.add(inner);
		    worklist.addLast(inner);
		}
	    }
	}
	return matches;
    }

    public boolean universal() {
	return false;
    }

}
