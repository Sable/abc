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
public abstract class NamePattern_c extends Node_c implements NamePattern
{

    public NamePattern_c(Position pos) {
        super(pos);
    }

    public abstract String toString();

}
