
package arc.aspectj.types;

import java.util.List;
import java.util.Iterator;

import polyglot.util.Position;

import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.ReferenceType;
import polyglot.types.Flags;


import polyglot.ext.jl.types.MethodInstance_c;

import arc.aspectj.ast.AdviceSpec;


public class AdviceInstance_c extends MethodInstance_c {
	
	AdviceSpec spec;
	
	/** Used for deserializing types. */
	protected AdviceInstance_c() { }

	public AdviceInstance_c(TypeSystem ts, Position pos,
				 			ReferenceType container,
						 Flags flags, Type returnType, String name,
				 List formalTypes, List excTypes, AdviceSpec spec) {
 	    super(ts,pos,container,flags,returnType,name,formalTypes,excTypes);
 		this.spec = spec;
 	}	
 	
	public String toString() {
	   String s = designator() + " " + flags.translate() +
					  signature();

	   if (! excTypes.isEmpty()) {
		   s += " throws ";

		   for (Iterator i = excTypes.iterator(); i.hasNext(); ) {
		   Type t = (Type) i.next();
		   s += t.toString();

		   if (i.hasNext()) {
			   s += ", ";
		   }
		   }
	   }

	   return s;
	}
	   
	public String signature() {
		return spec.toString();
	}

	public String designator() {
		   return "advice";
	}

}
