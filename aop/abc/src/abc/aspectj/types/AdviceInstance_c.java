
package polyglot.ext.aspectj.types;

import polyglot.util.Position;
import polyglot.types.*;

import java.util.List;
import polyglot.ext.jl.types.MethodInstance_c;

public class AdviceInstance_c
	extends MethodInstance_c
	implements AdviceInstance {

	protected int kind;
	protected PointcutInstance pci;
	
	protected AdviceInstance_c() { }

	public AdviceInstance_c(TypeSystem ts, 
	               Position pos,
				   ReferenceType container,
				   Flags flags,
				   Type returnType, 
				   String name,
				   List formalTypes, 
				   List excTypes,
				   int kind, 
		           PointcutInstance pci) {
		   super(ts, pos, container, flags, returnType, name, formalTypes, excTypes);
	   this.kind = kind;
	   this.pci = pci;
	   }
	   
	public int kind() {
		return kind;
	}
	
	public AdviceInstance kind(int k) {
		if (this.kind != k) { 
			AdviceInstance_c ai = (AdviceInstance_c) copy();
		  	ai.kind = k;
		  	return ai;
		  }
		else return this;
	}
	
	public PointcutInstance pointcut() {
		return pci;
	}
	
	public AdviceInstance pointcut(PointcutInstance pci) {
		if (this.pci != pci) {
			AdviceInstance_c ai = (AdviceInstance_c) copy();
			ai.pci = pci;
			return ai;
		} else return this;
	}

}
