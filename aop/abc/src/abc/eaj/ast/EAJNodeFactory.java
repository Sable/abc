package abc.eaj.ast;

import polyglot.util.*;
import abc.aspectj.ast.*;

import java.util.*;

/**
 * NodeFactory for Extended AspectJ extension.
 */
public interface EAJNodeFactory extends AspectJNodeFactory {
    // TODO: Declare any factory methods for new AST nodes.

    public PCLocalVars PCLocalVars(Position pos, List varlist, Pointcut pointcut);
}
