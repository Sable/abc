/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Ondrej Lhotak
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

package abc.weaving.matching;

import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.tagkit.Host;
import soot.util.Chain;

import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.residues.*;


/** A base class for shadow types that are associated with the whole body 
 *  or a section of it (i.e. execution and [pre]initialisation shadows), 
 *  rather than with a specific jimple statement.
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 */

public abstract class BodyShadowMatch extends ShadowMatch {
    protected BodyShadowMatch(SootMethod container) {
	super(container);
    }

    public ShadowMatch getEnclosing() {
	return this;
    }

    public ContextValue getTargetContextValue() {
	return getThisContextValue();
    }


    public Host getHost() {
	// We deviate from ajc's behaviour, and return the position of the signature 
	// instead if it has one.

	if(container.hasTag("SourceLnPosTag") || container.hasTag("LineNumberTag")) return container;
	if(container.getActiveBody().hasTag("SourceLnPosTag") 
	   || container.getActiveBody().hasTag("LineNumberTag")) return container.getActiveBody();

	if(container.getName().equals(SootMethod.staticInitializerName) 
	   && (container.getDeclaringClass().hasTag("SourceLnPosTag")
	       || container.getDeclaringClass().hasTag("LineNumberTag"))) 
	    return container.getDeclaringClass();

	// otherwise, try to go for an internal statement

	Chain units=container.getActiveBody().getUnits();
	Host h=(Host) units.getFirst();
	while(h!=null) {
	    if(h.hasTag("SourceLnPosTag") || h.hasTag("LineNumberTag")) return h;
	    h=(Host) units.getSuccOf(h);
	}

	// Give up and return the container
	return container;

	/*
	// FIXME:  this is close to what we want,  but in the case of
	//            a constructor execution we really want the position
	//            of the first statement after the super()
	// FIXME:  this works for static initialization when there are
	//         real static initializers in there.  Otherwise we
	//         should report the line number of the beginning of
	//         the class being initialized.
        // TODO:  rethink the structure of this code .... where should we
	//          find the position??  should have some utililty 
	//          methods?
	// Want to return the first "real" statement of the body that
	//   is not an identity statement or a nop
	
	Stmt firstRealStmt = Restructure.findFirstRealStmt
	    (container,container.getActiveBody().getUnits());

	return firstRealStmt;
	*/
    }

    public List/*<ContextValue>*/ getArgsContextValues() {
	int count=container.getParameterCount();
	Vector ret=new Vector(count);
	ret.setSize(count);
	Iterator stmtsIt=container.getActiveBody().getUnits().iterator();
	// how much is the parameter list offset from the index into the args vector?
	int offset=container.isStatic() ? 0 : 1; 
	while(stmtsIt.hasNext()) {
	    Stmt stmt=(Stmt) stmtsIt.next();
	    if(!(stmt instanceof IdentityStmt)) break;
	    IdentityStmt istmt=(IdentityStmt) stmt;
	    Value right=istmt.getRightOp();
	    if(!(right instanceof ParameterRef)) continue;
	    ParameterRef param=(ParameterRef) right;
	    ret.set(param.getIndex(),new JimpleValue((Immediate)istmt.getLeftOp()));
	}
	
	// change by Oege: some parameters are implicit
 	List cvs = ret.subList(MethodCategory.getSkipFirst(container),count-MethodCategory.getSkipLast(container));
	return cvs;
	
    }
}
