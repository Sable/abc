package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.Node_c;

/**
 * 
 * @author Oege de Moor
 *
 */
public class MethodConstructorPattern_c extends Node_c 
                                        implements MethodConstructorPattern
{

    public MethodConstructorPattern_c(Position pos) {
        super(pos);
    }

}
