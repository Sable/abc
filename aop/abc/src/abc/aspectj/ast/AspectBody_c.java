package abc.aspectj.ast;

import java.util.List;

import abc.aspectj.ast.AspectBody;
import polyglot.ast.ClassBody;
import polyglot.ext.jl.ast.ClassBody_c;
import polyglot.util.Position;

/**
 * An <code>AspectBody</code> represents the body of an aspect
 * declaration 
 */
public class AspectBody_c extends ClassBody_c implements AspectBody
{

    public AspectBody_c(Position pos, List members) {
        super(pos,members);
    }

    /* add stuff for pointcuts, advice and intertype declarations */
     
}
