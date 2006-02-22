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

import polyglot.util.*;
import abc.aspectj.ast.*;
import abc.eaj.ast.EAJNodeFactory;

import java.util.*;

/**
 * @author Neil Ongkingco
 *  
 */
public interface OpenModNodeFactory extends EAJNodeFactory {
    public ModuleDecl ModuleDecl(Position pos, 
            String name, 
            ModuleBody body, 
            Position namePos, 
            boolean isProtected);

    public ModuleBody ModuleBody(Position pos, List members);
    
    public ModMember ModMemberAspect(Position pos, CPEName cpe);

    public ModMember ModMemberClass(Position pos,
            ClassnamePatternExpr classPattern);

    public ModMember ModMemberModule(Position pos, String name);

    public SigMember SigMemberExposeDecl(Position pos, 
            Pointcut pc, 
            boolean isPrivate, 
            ClassnamePatternExpr toClause);
    
    public SigMember SigMemberAdvertiseDecl(Position pos, 
            Pointcut pc,
            boolean isPrivate, 
            ClassnamePatternExpr toClause);
    
    public AspectDecl DummyAspectDecl(Position pos, String moduleName);
    
    public OpenClassMember OpenClassMember(OpenClassFlags flags, 
    		ClassnamePatternExpr cpe, 
    		Position pos);
}
