/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Neil Ongkingco
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

/*
 * Created on May 13, 2005
 *
 */
package abc.om.ast;

import java.util.*;

import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.ast.MakesAspectMethods;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.visit.AspectMethods;
import abc.om.weaving.aspectinfo.OMClassnamePattern;
import abc.weaving.aspectinfo.Pointcut;
import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ext.jl.ast.Node_c;
import polyglot.ext.jl.ast.Term_c;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * Represents an expose signature member.
 * @author Neil Ongkingco 
 *
 */
public class SigMemberExposeDecl_c extends SigMember_c implements SigMemberExposeDecl, MakesAspectMethods {

    public SigMemberExposeDecl_c(polyglot.util.Position pos, 
            abc.aspectj.ast.Pointcut pc, 
            boolean isPrivate, 
            ClassnamePatternExpr toClauseCPE) {
        super(pos, pc, isPrivate, toClauseCPE);
    }
}
