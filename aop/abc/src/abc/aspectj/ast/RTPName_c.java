package polyglot.ext.aspectj.ast;

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
    protected Integer dims;

    public RTPName_c(Position pos, 
                     NamePattern pat,
                     Integer dims)  {
	super(pos);
        this.pat = pat;
        this.dims = dims;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	print(pat,w,tr);
	if (dims != null) {
	    for (int i = 0; i < dims.intValue(); i++) 
		w.write("[]");
	}
    }

}
