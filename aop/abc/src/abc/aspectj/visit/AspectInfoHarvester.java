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

package abc.aspectj.visit;

import abc.aspectj.ast.*;
import abc.weaving.aspectinfo.*;

import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.util.*;
import polyglot.types.*;
import polyglot.frontend.*;

import java.util.*;

/** Picks up all AST nodes that implement {@link ContainsAspectInfo} and
 *  tells them to put their information into the {@link abc.weaving.aspectinfo.GlobalAspectInfo}.
 *  @author Aske Simon Christensen
 *  @author Oege de Moor
 */
public class AspectInfoHarvester extends ContextVisitor {
    private static Map pc_decl_map = new HashMap();

    public static void reset() {
	pc_decl_map=new HashMap();
    }

    private GlobalAspectInfo gai;
    private ParsedClassType current_aspect_scope;
    private Aspect current_aspect;

    public AspectInfoHarvester(Job job, TypeSystem ts, NodeFactory nf) {
	super(job, ts, nf);
	gai = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo();
    }

    public NodeVisitor enter(Node parent, Node n) {
	ParsedClassType scope = context().currentClassScope();
	if (scope != null && !scope.equals(current_aspect_scope)) {
	    current_aspect_scope = scope;
	    current_aspect = gai.getAspect(AbcFactory.AbcClass(scope));
	}

	if (n instanceof ContainsAspectInfo) {
	    ((ContainsAspectInfo)n).update(gai, current_aspect);
	}
	//System.out.println(n.getClass());
	return super.enter(parent, n);
    }

    public Node leave(Node parent, Node old, Node n, NodeVisitor v) {

	return super.leave(parent, old, n, v);
    }

    /** Convert a list of polyglot nodes representing argument patterns.
     *  @param nodes a list containing {@link polyglot.ast.Local}, {@link polyglot.types.TypeNode},
     *               {@link abc.aspectj.ast.ArgStar} and {@link abc.aspectj.ast.ArgDotDot} objects.
     *  @return a list of {@link abc.weaving.aspectinfo.ArgPattern} objects.
     */
    public static List/*<ArgPattern>*/ convertArgPatterns(List/*<Node>*/ nodes) {
	List aps = new ArrayList();
	Iterator ni = nodes.iterator();
	while (ni.hasNext()) {
	    Node n = (Node) ni.next();
	    abc.weaving.aspectinfo.ArgPattern ap;
	    if (n instanceof Local) {
		ap = new abc.weaving.aspectinfo.ArgVar(new Var(((Local)n).name(), n.position()), n.position());
	    } else if (n instanceof TypeNode) {
		ap = new abc.weaving.aspectinfo.ArgType(AbcFactory.AbcType(((TypeNode)n).type()), n.position());
	    } else if (n instanceof ArgStar) {
		ap = new abc.weaving.aspectinfo.ArgAny(n.position());
	    } else if (n instanceof ArgDotDot) {
		ap = new abc.weaving.aspectinfo.ArgFill(n.position());
	    } else {
		throw new RuntimeException("Unknown argument pattern type: "+n.getClass());
	    }
	    aps.add(ap);
	}
	return aps;
    }

    /** Convert a list of polyglot formals into aspect info formals.
     *  @param pformals a list of {@link polyglot.ast.Formal} objects.
     *  @return a list of {@link abc.weaving.aspectinfo.Formal} objects.
     */
    public static List/*<abc.weaving.aspectinfo.Formal>*/ convertFormals(List/*<polyglot.ast.Formal>*/ pformals) {
	List formals = new ArrayList();
	Iterator mdfi = pformals.iterator();
	while (mdfi.hasNext()) {
	    polyglot.ast.Formal mdf = (polyglot.ast.Formal)mdfi.next();
	    formals.add(new abc.weaving.aspectinfo.Formal(AbcFactory.AbcType((polyglot.types.Type)mdf.type().type()),
							  mdf.name(), mdf.position()));
	}
	return formals;
    }

    public static Map pointcutDeclarationMap() {
	return pc_decl_map;
    }
    
  
}
