
package arc.aspectj.types;
import java.util.List;
import java.util.Iterator;

import polyglot.util.Position;

import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.ReferenceType;
import polyglot.types.Flags;


import polyglot.ext.jl.types.MethodInstance_c;

public class PointcutInstance_c extends MethodInstance_c {

String localName;

	public PointcutInstance_c(TypeSystem ts, Position pos,
												ReferenceType container,
												Flags flags, Type returnType, String name,
												List formalTypes, List excTypes){
		super(ts,pos,container,flags,returnType,"$pointcut$"+name,formalTypes,excTypes);
		localName = name;
	}	
 	
	public String toString() {
	   String s = designator() + " " + flags.translate() +
					  signature();

	   return s;
	}
	
	public String signature() {
			String s = localName + "(";

			for (Iterator i = formalTypes.iterator(); i.hasNext(); ) {
				Type t = (Type) i.next();
				s += t.toString();

				if (i.hasNext()) {
					s += ",";
				}
			}

			s += ")";

			return s;
		}

	public String designator() {
		   return "pointcut";
	}

}
