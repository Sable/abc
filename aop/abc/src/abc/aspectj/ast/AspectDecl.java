package abc.aspectj.ast;

import polyglot.ast.ClassDecl;
import polyglot.ast.MethodDecl;
import abc.aspectj.types.AJTypeSystem;

/**
 * A <code>AspectDecl</code> represents a top-level, member, or local class
 * declaration.
 * 
 * @author Oege de Moor
 */
public interface AspectDecl extends ClassDecl, MakesAspectMethods
{
   
   
}


