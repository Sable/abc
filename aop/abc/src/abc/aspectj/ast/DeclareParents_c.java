/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Oege de Moor
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

package abc.aspectj.ast;

import abc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.GlobalAspectInfo;

import abc.weaving.aspectinfo.AbcFactory;

/** declare parents : pat extends parents
 * 
 * @author Oege de Moor
 * @author Aske Simon Christensen
 */
public class DeclareParents_c extends DeclareDecl_c 
    implements DeclareParents, ContainsAspectInfo
{

    ClassnamePatternExpr pat;
    TypedList parents;
    Kind kind;

    Collection/*<AbcClass>*/ targets = new ArrayList();

    public DeclareParents_c(Position pos, 
			    ClassnamePatternExpr pat,
			    List parents,
			    Kind kind)
    {
	super(pos);
        this.pat  = pat;
        this.parents = TypedList.copyAndCheck(parents,
					      TypeNode.class,
					      true);
	this.kind = kind;
    }

    protected DeclareParents_c reconstruct(ClassnamePatternExpr pat,
					       TypedList interfaces) {
	if (pat != this.pat || !CollectionUtil.equals(parents, this.parents)) {
	    DeclareParents_c n = (DeclareParents_c) copy();
	    n.pat = pat;
	    n.parents = TypedList.copyAndCheck(parents, TypeNode.class, true);
	    n.kind = kind;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	ClassnamePatternExpr pat = (ClassnamePatternExpr) visitChild(this.pat, v);
	TypedList parents = new TypedList(visitList(this.parents, v), TypeNode.class, true);
	return reconstruct(pat, parents);
    }

    public Node disambiguate(DeclareParentsAmbiguityRemover ar) throws SemanticException {
	TypedList parents_disam = new TypedList(new ArrayList(), TypeNode.class, false);
	Iterator ti = parents.iterator();
	while (ti.hasNext()) {
	    TypeNode tn = (TypeNode)ti.next();
	    tn = (TypeNode)ar.disamb(tn);
	    parents_disam.add(tn);
	}
	parents = parents_disam;
	return this;
    }
    
    public Node typeCheck(TypeChecker tc) throws SemanticException {
    	if (targets.size() == 0)
    		abc.main.Main.v().error_queue.enqueue(ErrorInfo.WARNING,
					"Does not alter parents of any classes (is the pattern correct?)", 
					position());
    	return this;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("declare parents : ");
        print(pat, w, tr);
        w.write(" "+kind+" ");
        for (Iterator i = parents.iterator(); i.hasNext(); ) {
           TypeNode tn = (TypeNode) i.next();
           print(tn, w, tr);

           if (i.hasNext()) {
                w.write (", ");
           }
        }
        w.write(";");
    }

    public ClassnamePatternExpr pat() {
	return pat;
    }

    public List/*<TypeNode>*/ parents() {
	return parents;
    }

    public Kind kind() {
	return kind;
    }

    public void addTarget(AbcClass cl) {
	targets.add(cl);
    }

    public void setKind(Kind kind) {
	this.kind = kind;
    }

    public void update(GlobalAspectInfo gai, Aspect current_aspect) {
	if (kind == IMPLEMENTS) {
	    List/*<AbcClass>*/ ints = new ArrayList();
	    Iterator ii = parents.iterator();
	    while (ii.hasNext()) {
		TypeNode i = (TypeNode)ii.next();
		ints.add(AbcFactory.AbcClass((ClassType)i.type()));
	    }
	    gai.addDeclareParents(new abc.weaving.aspectinfo.DeclareParentsImpl
				  (pat.makeAIClassnamePattern(),
				   targets,
				   ints,
				   current_aspect,
				   position()));
	} else if (kind == EXTENDS) {
	    gai.addDeclareParents(new abc.weaving.aspectinfo.DeclareParentsExt
				  (pat.makeAIClassnamePattern(),
				   targets,
				   AbcFactory.AbcClass((ClassType)((TypeNode)parents.get(0)).type()),
				   current_aspect,
				   position()));
	} else {
	    throw new InternalCompilerError("Unknown declare parents kind: "+kind);
	}
    }

}
