package abc.aspectj.ast;

import polyglot.ast.Node;
import abc.aspectj.ast.AspectJNodeFactory;
import abc.aspectj.types.AspectJTypeSystem;
import abc.aspectj.visit.AspectMethods;

// All AST nodes visited by AspectMethods should implement this interface

public interface MakesAspectMethods
{
        void aspectMethodsEnter(AspectMethods visitor);
        Node aspectMethodsLeave(AspectMethods visitor, AspectJNodeFactory nf, AspectJTypeSystem ts);
}
