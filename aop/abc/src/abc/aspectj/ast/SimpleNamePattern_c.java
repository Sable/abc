package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;


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

}
