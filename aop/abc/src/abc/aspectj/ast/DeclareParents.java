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

package abc.aspectj.ast;

import polyglot.util.Enum;
import polyglot.ast.Node;
import polyglot.types.SemanticException;

import abc.aspectj.visit.DeclareParentsAmbiguityRemover;

import abc.weaving.aspectinfo.AbcClass;

import java.util.*;

/**
 * 
 * @author Oege de Moor
 * @author Aske Simon Christensen
 *
 */
public interface DeclareParents extends DeclareDecl
{
    /* new stuff to be added */

    public static class Kind extends Enum {
	public Kind(String name) {
	    super(name);
	}
    }

    public static final Kind EXTENDS = new Kind("extends");
    public static final Kind IMPLEMENTS = new Kind("implements");


    public ClassnamePatternExpr pat();
    public List/*<TypeNode>*/ parents();
    public Kind kind();

    public Node disambiguate(DeclareParentsAmbiguityRemover ar) throws SemanticException;

    public void setKind(Kind kind);
    public void addTarget(AbcClass cl);
}
