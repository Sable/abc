package abc.eaj.ast;

import polyglot.util.*;
import abc.aspectj.ast.*;

import java.util.*;

/**
 * NodeFactory for Extended AspectJ extension.
 */
public class EAJNodeFactory_c extends AspectJNodeFactory_c
                              implements EAJNodeFactory
{
    // TODO:  Implement factory methods for new AST nodes.
    // TODO:  Override factory methods for overriden AST nodes.
    // TODO:  Override factory methods for AST nodes with new extension nodes.

    public PCLocalVars PCLocalVars(Position pos, List varlist, Pointcut pointcut)
    {
        return new PCLocalVars_c(pos, varlist, pointcut);
    }
}
