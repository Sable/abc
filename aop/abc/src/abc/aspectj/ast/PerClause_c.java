package abc.aspectj.ast;

import polyglot.ast.*;


import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.Node_c;

import abc.aspectj.types.AspectType;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.visit.AspectMethods;

public abstract class PerClause_c extends Node_c implements PerClause,
							    MakesAspectMethods
{

    public PerClause_c(Position pos) {
        super(pos);
    }
    
	public Context enterScope(Context c) {
		return enterScope(c, true);
	}

	public Context enterScope(Context c, boolean inherit) {
		ClassType type = c.currentClass();
		addMembers(c, type, new HashSet(), inherit);
		return c;
	}
	
	public int kind() {
		return AspectType.PER_NONE;
	}
	
	protected void addMembers(Context c, ReferenceType type,
							   Set visited, boolean inherit) {
		
		 if (visited.contains(type)) {
			 return;
		 }

		 visited.add(type);

		 if (inherit) {
			 // Add supertype members first to ensure overrides work correctly.
			 if (type.superType() != null) {
				 if (! type.superType().isReference()) {
					 throw new InternalCompilerError(
						 "Super class \"" + type.superType() +
						 "\" of \"" + type + "\" is ambiguous.  " +
						 "An error must have occurred earlier.",
						 type.position());
				 }

				 addMembers(c, type.superType().toReference(), visited, true);
			 }

			 for (Iterator i = type.interfaces().iterator(); i.hasNext(); ) {
				 Type t = (Type) i.next();

				 if (! t.isReference()) {
					 throw new InternalCompilerError(
						 "Interface \"" + t + "\" of \"" + type +
						 "\" is ambiguous.  " +
						 "An error must have occurred earlier.",
						 type.position());
				 }

				 addMembers(c, t.toReference(), visited, true);
			 }
		 }

		 for (Iterator i = type.methods().iterator(); i.hasNext(); ) {
			 MethodInstance mi = (MethodInstance) i.next();
			 c.addMethod(mi);
		 }

		 for (Iterator i = type.fields().iterator(); i.hasNext(); ) {
			 FieldInstance fi = (FieldInstance) i.next();
			 c.addVariable(fi);
		 }

		 if (type.isClass()) {
			 for (Iterator i = type.toClass().memberClasses().iterator();
				  i.hasNext(); ) {
				 ClassType mct = (ClassType) i.next();
				 c.addNamed(mct);
			 }
		 }
	 }

    public void aspectMethodsEnter(AspectMethods visitor)
    {
        visitor.pushFormals(new LinkedList());
    }

    public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                   AJTypeSystem ts)
    {
        visitor.popFormals();
        return this;
    }

}
