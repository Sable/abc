package abc.aspectj.ast;

import java.util.*;

public interface DeclareParentsImpl extends DeclareDecl
{
    /* new stuff to be added */

    public ClassnamePatternExpr pat();
    public List/*<TypeNode>*/ interfaces();

}
