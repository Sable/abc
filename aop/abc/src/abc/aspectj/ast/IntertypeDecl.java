package abc.aspectj.ast;

import polyglot.ast.ClassMember;
import polyglot.ast.TypeNode;
import polyglot.ast.Expr;

import abc.aspectj.types.AJTypeSystem;

/**
 * 
 * @author Oege de Moor
 *
 */
public interface IntertypeDecl extends ClassMember, MakesAspectMethods
{
    
    /** the target class of the intertype decl */
    public TypeNode host();
    
  //  /** set the "this" parameter for dealing with hostSpecial */
  //  public IntertypeDecl thisParameter(AJNodeFactory nf, AJTypeSystem ts);
    
    /** create a reference to the "this" parameter for dealing with hostSpecial */
    public Expr thisReference(AJNodeFactory nf, AJTypeSystem ts);
    
}
