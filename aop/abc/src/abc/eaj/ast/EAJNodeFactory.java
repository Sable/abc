package abc.eaj.ast;

import polyglot.ast.*;
import polyglot.util.*;

import abc.aspectj.ast.*;

import java.util.*;

/**
 * NodeFactory for Extended AspectJ extension.
 */
public interface EAJNodeFactory extends AJNodeFactory {
    // TODO: Declare any factory methods for new AST nodes.

    public PCCast PCCast(Position pos, TypePatternExpr type_pattern);

    public PCLocalVars PCLocalVars(Position pos, List varlist, Pointcut pointcut);

    public GlobalPointcutDecl GlobalPointcutDecl(
                                    Position pos,
                                    ClassnamePatternExpr aspect_pattern,
                                    Pointcut pointcut,
                                    String name,
                                    TypeNode voidn);
}
