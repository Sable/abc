/* abc - The AspectBench Compiler
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

package abc.aspectj.visit;

import abc.aspectj.ast.*;

import polyglot.frontend.*;
import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.types.*;

import abc.aspectj.ExtensionInfo;

import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.AbcFactory;
import abc.weaving.aspectinfo.AbcClass;

import java.util.*;

/** Type checks <code>declare parents</code> declarations and integrates the
 *  declared parents into the Polyglot class hierarchy.
 *  @author Aske Simon Christensen
 */
public class ParentDeclarer extends ErrorHandlingVisitor {
    private ExtensionInfo ext;

    public ParentDeclarer(Job job, TypeSystem ts, NodeFactory nf,
			  ExtensionInfo ext) {
	super(job, ts, nf);
	this.ext = ext;
    }

    public NodeVisitor enterCall(Node n) throws SemanticException {
	//TODO: More sanity checks

	if (n instanceof DeclareParents) {
	    DeclareParents dp = (DeclareParents)n;
	    ClassnamePatternExpr pat = dp.pat();
	    List/*<TypeNode>*/ parents = dp.parents();

	    if (parents.size() == 1 && !((ClassType)((TypeNode)parents.get(0)).type()).flags().isInterface()) {
		// Extending a singe class

		// Change into EXTENDS internally
		dp.setKind(DeclareParents.EXTENDS);

		ClassType parentct = (ClassType)((TypeNode)parents.get(0)).type();

		Iterator cti = new ArrayList(ext.hierarchy.getClassTypes()).iterator();
		while (cti.hasNext()) {
		    ClassType ct = (ClassType)cti.next();
		    PCNode hi_cl = ext.hierarchy.getClass(ct);
		    if (hi_cl.isWeavable() && pat.matches(PatternMatcher.v(), hi_cl)) {
			//System.out.println(ct);
			if (ct.flags().isInterface()) {
			    throw new SemanticException("Interface "+ct+" cannot be extended by a class",dp.position());
			}
			if (!ts.isSubtype(ct, parentct)) {
			    if (!ts.isSubtype(parentct, ct.superType())) {
				throw new SemanticException("Declared parent class "+parentct+
							    " is not a subclass of original superclass "+ct.superType()+
							    " of base class "+ct);
			    }
			    if (ts.isSubtype(parentct, ct)) {
				throw new SemanticException("Declared parent class "+parentct+
							    " is a subclass of child class "+ct);
			    }

                            AbcClass cl = AbcFactory.AbcClass(ct);
			    dp.addTarget(cl);
                            abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().registerWeave(cl);

			    PCNode hi_parent = ext.hierarchy.insertClassAndSuperclasses(parentct, false);
			    hi_cl.addParent(hi_parent);
			    if (ct instanceof ParsedClassType) {
				ParsedClassType pct = (ParsedClassType)ct;
				pct.superType(parentct);
			    }
			}
		    }
		}
		
	    } else {
		// Extending or implementing a list of interfaces

		// Change into IMPLEMENTS internally
		dp.setKind(DeclareParents.IMPLEMENTS);

		List/*<ClassType>*/ ints = new ArrayList();
		Iterator pi = parents.iterator();
		while (pi.hasNext()) {
		    TypeNode p = (TypeNode)pi.next();
		    ClassType pct;
		    try {
			pct = (ClassType)p.type();
		    } catch (ClassCastException e) {
			throw new SemanticException("Type "+p.type()+" is not a class",dp.position());
		    }
		    if (!pct.flags().isInterface()) {
			throw new SemanticException("Type "+pct+" is not an interface",dp.position());
		    }
		    ints.add(pct);
		}

		Iterator cti = new ArrayList(ext.hierarchy.getClassTypes()).iterator();
		while (cti.hasNext()) {
		    ClassType ct = (ClassType)cti.next();
		    PCNode hi_cl = ext.hierarchy.getClass(ct);
		    if (hi_cl.isWeavable() && pat.matches(PatternMatcher.v(), hi_cl)) {

                        AbcClass cl = AbcFactory.AbcClass(ct);
                        dp.addTarget(cl);
                        abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().registerWeave(cl);

			if (ct instanceof ParsedClassType) {
			    ParsedClassType pct = (ParsedClassType)ct;
			    Iterator incti = ints.iterator();
			    while (incti.hasNext()) {
				ClassType inct = (ClassType)incti.next();
                                if (!inct.equals(ct)) {
                                    if (ts.isSubtype(inct, ct)) {
                                        throw new SemanticException("Interface "+ct+" cannot extend subinterface "+inct,dp.position());
                                    }
                                    PCNode hi_in = ext.hierarchy.insertClassAndSuperclasses(inct, false);
				
                                    //System.err.println("Declared "+ct.fullName()+" to implement "+inct.fullName());
				
                                    pct.addInterface(inct);
                                    hi_cl.addParent(hi_in);
                                    //System.out.println(hi_cl+" implements "+hi_in);
                                }
			    }
			}
		    }
		}
	    }
	}

	return this;
    }

}
