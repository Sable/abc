/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 * Copyright (C) 2004 Aske Simon Christensen
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.weaving.aspectinfo;

import polyglot.util.Position;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import soot.*;

/** An intertype constructor declaration. 
  * 
  * given an intertype constructor declaration of the form
  *     mods A.new(formal1, ...,formaln) {
  * 		ccall(E1,E2,...,Ek);  // optional call to super or this
  *         init;
  *     }
  * 
  * the frontend transforms it into the following shape
  * 
  * 	mods A.new(formal1, ..., formaln) {
  *         qualifier.ccall(e1(this,formal1,...,formaln), ..., ek(this,formal1,...,formaln)); // no longer optional
  * 		body(this,formal1,...,formaln);
  *     }
  * where e1,...,ek and body are newly generated methods in the 
  * originating aspect.
  * 
  * The class below encodes the latter scheme, for code generation in the target class.
  * 
  * @author Aske Simon Christensen
  * @author Oege de Moor
  */

public class IntertypeConstructorDecl extends InAspect {
	
	static public int SUPER = 0;
	static public int THIS = 1;
	
	private AbcClass target;								// target of intertype decl
	private int mod;												// modifier
	private List /*<AbcType>*/ formalTypes;	// types of formal parameters
	private List /*<String>*/ throwTypes;			// names of exceptions
	private AbcClass qualifier;							// qualifier of inner ccall
	private int kind;												// kind of inner ccall
	private List /*<Integer | MethodSig>*/ arguments;	// dispatch methods (in aspect) to create arguments
																				// of inner ccall
	private MethodSig body;								// dispatch method (in aspect) to do the body of the
																				// constructor
	private boolean hasMangleParam;
	private int origmod;
	

    public IntertypeConstructorDecl( AbcClass target, 
    																Aspect aspct, 
    																int mod,
    																int origmod,
    																boolean hasMangleParam,
    																List formalTypes,
    																List throwTypes,
    																AbcClass qualifier,
    																int kind,
    																List arguments,
    																MethodSig body,
    																Position pos) {
		super(aspct, pos);
		this.target = target;
		this.mod = mod;
		this.origmod = origmod;
		this.formalTypes = formalTypes;
		this.throwTypes = throwTypes;
		this.qualifier = qualifier;
		this.kind = kind;
		this.arguments = arguments;
		this.body = body;
		this.hasMangleParam = hasMangleParam;
    }

    /** Get the target where of the intertype decl */
    public AbcClass getTarget() {
		return target;
    }

	/** Get the modifiers of intertype constructor */
	public int getModifiers() {
	  return mod;
	}
	
	/** Get the modifiers of intertype constructor */
	public int getOriginalModifiers() {
		return origmod;
	}
	
	/** Does this constructor have an additional last parameter for mangling purposes? */
	public boolean hasMangleParam() {
		return hasMangleParam;
	}

	/** Get the formal types of the intertype constructor.
	   *  @return a list of {@link abc.weaving.aspectinfo.AbcType} objects.
	   */
	public List getFormalTypes() {
		return formalTypes;
	}

	List sexc;
	 /** Get the exceptions thrown by the method.
	   *  @return a list of {@link soot.SootClass} objects.
	   */
	public List getExceptions() {
		if (sexc == null) {
			sexc = new ArrayList();
			Iterator ei = throwTypes.iterator();
			while (ei.hasNext()) {
				String e = (String)ei.next();
				sexc.add(Scene.v().getSootClass(e));
		  	}
	  	}
	  	return sexc;
	}

	/* return the qualifier of the inner ccall in this intertype constructor (always an enclosing
	 * instance of the target class)
	 */
	public AbcClass getQualifier() {
		return qualifier;
	}
	
	/* return the kind of the inner ccall in this intertype constructor: SUPER or THIS */
	public int getKind() {
		return kind;
	}
	
	/* return a list that describes how the arguments of the inner ccall in this intertype constructor are to
	 * be created: either an integer (the position of the formal parameter that is being referenced)
	 * or a method signature (to be called as static aspect.m(this,f1,...,fk)), where the fi are the formals of this
	 * constructor declaration.
	 */
	public List /*<Integer | MethodSig>*/ getArguments() {
		return arguments;
	}
	
	/* dispatch for the body of the constructor, to be called as static aspect.m(this,f1,...,fk) */	
	public MethodSig getBody() {
		return body;
	}
	
	public String toString() {
		return (target + ".new(" + formalTypes +") {...} from "+getAspect());
	}
	
}
