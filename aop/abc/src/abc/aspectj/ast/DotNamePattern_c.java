package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;


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

}
