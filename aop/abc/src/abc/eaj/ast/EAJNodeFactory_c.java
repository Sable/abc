package abc.eaj.ast;

import polyglot.ast.*;
import polyglot.types.*;
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

    public PCCast PCCast(Position pos, TypePatternExpr type_pattern)
    {
        return new PCCast_c(pos, type_pattern);
    }

    public PCLocalVars PCLocalVars(Position pos, List varlist, Pointcut pointcut)
    {
        return new PCLocalVars_c(pos, varlist, pointcut);
    }

    public GlobalPointcutDecl GlobalPointcutDecl(Position pos,
                                                 NamePattern aspect_pattern,
                                                 Pointcut pointcut,
                                                 String name,
                                                 TypeNode voidn)
    {
        return new GlobalPointcutDecl_c(pos, aspect_pattern, pointcut, name, voidn);
    }

    public AdviceDecl AdviceDecl(Position pos, Flags flags,
                                 AdviceSpec spec, List throwTypes,
                                 Pointcut pc, Block body)
    {
        return new EAJAdviceDecl_c(pos, flags, spec, throwTypes, pc, body);
    }
}
