
package abc.aspectj.types;

import java.util.List;
import java.util.LinkedList;

import polyglot.util.Position;
import polyglot.util.UniqueID;

import polyglot.ext.jl.types.ConstructorInstance_c;

import polyglot.ast.ConstructorCall;
import polyglot.ast.Expr;
import polyglot.ast.New;
import polyglot.ast.Formal;
import polyglot.ast.TypeNode;

import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.Flags;
import polyglot.types.TypeSystem;
import polyglot.types.LocalInstance;


import abc.aspectj.ast.AspectJNodeFactory;

/**
 * @author Oege de Moor
 *
 * A constructor that was introduced via an intertype declaration.
 */

public class InterTypeConstructorInstance_c
	extends ConstructorInstance_c
	implements InterTypeMemberInstance {
		
	protected ConstructorInstance mangled;
	protected ClassType mangleType;
    
	protected ClassType origin;
	
	public ClassType origin() {
		return origin;
	}

	/** create a constructor that can be traced back to the aspect
	 * that introduced it.
	 * 
	 */
	public InterTypeConstructorInstance_c(
		TypeSystem ts,
		Position pos,
		ClassType origin,
		ClassType container,
		Flags flags,
		List formalTypes,
		List excTypes) {
		super(ts, pos, container, flags, formalTypes, excTypes);
		this.origin = origin;
		
		if (flags().isPrivate() || flags().isPackage()) {
			mangleType = origin; // not quite right, same as ajc.
								// ought to generate a fresh type for each aspect
			List fts = new LinkedList(formalTypes);
			fts.add(mangleType);
			mangled = new ConstructorInstance_c(ts,pos,container,flags,fts,excTypes);
		}
	}
	
	
	public void setMangle(AspectJTypeSystem ts) {
		// to be filled in!
	}
	
	public ConstructorInstance mangled() {
		if (flags().isPrivate() || flags().isPackage())
			return mangled;
		else
			return this;
	}
	
	public ConstructorCall mangledCall(ConstructorCall cc, AspectJNodeFactory nf, AspectJTypeSystem ts) {
		if (flags().isPrivate() || flags.isPackage()) {
			Expr nl = nf.NullLit(cc.position());
			nl = nl.type(mangleType);
			List args = new LinkedList(cc.arguments());
			args.add(nl);
			ConstructorCall nc = cc.arguments(args);
			return nc.constructorInstance(mangled()); 
		} else return cc;
	}

	public New mangledNew(New cc, AspectJNodeFactory nf, AspectJTypeSystem ts) {
		New nc;
		if (flags().isPrivate() || flags().isPackage()) {
			Expr nl = nf.NullLit(cc.position());
			nl = nl.type(mangleType);
			List args = new LinkedList(cc.arguments());
			args.add(nl);
		 	nc = cc.arguments(args);
			return nc.constructorInstance(mangled()); 
		} else return cc;
	}
	
	public Formal mangledFormal(AspectJNodeFactory nf, AspectJTypeSystem ts) {
		TypeNode tn = nf.CanonicalTypeNode(position,mangleType);
		String name = UniqueID.newID("formal");
		Formal mangledFormal = nf.Formal(position,Flags.NONE,tn,name);
		LocalInstance li = ts.localInstance(position,Flags.NONE,mangleType,name);
		return mangledFormal.localInstance(li);
	}
}
