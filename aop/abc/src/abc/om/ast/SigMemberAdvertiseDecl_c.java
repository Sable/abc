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
 * Created on May 30, 2005
 *
 */
package abc.om.ast;

import java.util.Collections;
import java.util.LinkedList;

import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.ast.MakesAspectMethods;
import abc.aspectj.ast.MethodConstructorPattern;
import abc.aspectj.ast.PCCall_c;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.visit.AspectMethods;
import abc.weaving.aspectinfo.Pointcut;
import polyglot.ast.Node;
import polyglot.ext.jl.ast.Node_c;
import polyglot.util.CodeWriter;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * Represents an advertise signature member.
 * @author Neil Ongkingco
 *
 */
public class SigMemberAdvertiseDecl_c extends SigMember_c implements
        SigMemberAdvertiseDecl, MakesAspectMethods {

    public SigMemberAdvertiseDecl_c(polyglot.util.Position pos, 
            abc.aspectj.ast.Pointcut pc, 
            boolean isPrivate, 
            ClassnamePatternExpr toClauseCPE) {
        super(pos, pc, isPrivate, toClauseCPE);
    }    
}
