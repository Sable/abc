/* abc - The AspectBench Compiler
 * Copyright (C) 2005
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
import java.util.*;

/**
 * @author Neil Ongkingco
 *  
 */
public interface OpenModNodeFactory extends AJNodeFactory {
    public ModuleDecl ModuleDecl(Position pos, String name, ModuleBody body, Position namePos);

    public ModuleBody ModuleBody(Position pos, List members, List sigMembers);
    
    public ModMember ModMemberAspect(Position pos, String name);

    public ModMember ModMemberClass(Position pos,
            ClassnamePatternExpr classPattern);

    public ModMember ModMemberModule(Position pos, String name, boolean isConstrained);

    public SigMember SigMemberPCDecl(Position pos, Pointcut pc, boolean isPrivate);
    
    public SigMember SigMemberMethodDecl(Position pos, 
            MethodConstructorPattern methodPattern,
            boolean isPrivate);
    
    public AspectDecl DummyAspectDecl(Position pos, String moduleName);
}