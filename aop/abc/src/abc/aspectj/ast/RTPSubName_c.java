package arc.aspectj.ast;

import arc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

import polyglot.ext.jl.ast.Node_c;

import java.util.*;

public class RTPSubName_c extends Node_c 
                          implements RTPSubName
{
    protected NamePattern pat;
    protected Integer dims;

    public RTPSubName_c(Position pos, 
                        NamePattern pat,
                        Integer dims)  {
	super(pos);
        this.pat = pat;
        this.dims = dims;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	print(pat,w,tr);
        w.write("+");
	if (dims != null) {
	    for (int i = 0; i < dims.intValue(); i++) 
		w.write("[]");
	}
    }

    public boolean matchesClass(PCNode context, PCNode cl) {
	return dims == null && matches(context, cl);
    }

    public boolean matchesClassArray(PCNode context, PCNode cl, int dim) {
	return dims != null && dims.intValue() == dim && matches(context, cl);
    }

    private boolean matches(PCNode context, PCNode cl) {
	Set pat_matches = pat.match(context);
	if (pat_matches.contains(cl)) {
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
		    if (pat_matches.contains(parent)) {
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
