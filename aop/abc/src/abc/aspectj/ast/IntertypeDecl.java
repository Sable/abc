package abc.aspectj.ast;

import polyglot.ast.ClassMember;
import polyglot.ast.TypeNode;
import polyglot.ast.Expr;

import abc.aspectj.visit.Supers;

import abc.aspectj.types.AJTypeSystem;

public interface IntertypeDecl extends ClassMember, MakesAspectMethods
{
    
    /** the target class of the intertype decl */
    public TypeNode host();
    
  //  /** set the "this" parameter for dealing with hostSpecial */
  //  public IntertypeDecl thisParameter(AspectJNodeFactory nf, AJTypeSystem ts);
    
    /** create a reference to the "this" parameter for dealing with hostSpecial */
    public Expr thisReference(AspectJNodeFactory nf, AJTypeSystem ts);
    
    /** data structure for recording "super" accesses that need to be dispatched */
    public Supers getSupers();
    
}
