package abc.aspectj.ast;

import polyglot.ast.ClassMember;
import polyglot.ast.TypeNode;

public interface IntertypeDecl extends ClassMember
{
    /* new stuff to be added */
    
    public TypeNode host();
    
}
