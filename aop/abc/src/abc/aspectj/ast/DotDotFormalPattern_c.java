package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.Node_c;

public class DotDotFormalPattern_c extends Node_c 
                             implements DotDotFormalPattern
{

    public DotDotFormalPattern_c(Position pos) {
        super(pos);
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
	w.write("..");
    }

}
