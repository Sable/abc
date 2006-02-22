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

import java.util.List;

import polyglot.util.*;
import abc.aspectj.ast.*;
import abc.eaj.ast.EAJNodeFactory_c;

/**
 * 
 * Factory for om AST nodes.
 * @author Neil Ongkingco
 *
 */
public class OpenModNodeFactory_c extends EAJNodeFactory_c implements
        OpenModNodeFactory {

    public ModMember ModMemberAspect(Position pos, CPEName cpe) {
        return new ModMemberAspect_c(pos, cpe);
    }
    public ModMember ModMemberClass(Position pos,
            ClassnamePatternExpr classPattern) {
        return new ModMemberClass_c(pos,classPattern);
    }
    public ModMember ModMemberModule(Position pos, String name) {
        return new ModMemberModule_c(pos, name);
    }
    public ModuleBody ModuleBody(Position pos, List members) {
        return new ModuleBody_c(pos, members);
    }
    public ModuleDecl ModuleDecl(Position pos, 
            String name, 
            ModuleBody body, 
            Position namePos, 
            boolean isProtected) {
        return new ModuleDecl_c(pos, name, body, namePos, isProtected);
    }
    public SigMember SigMemberExposeDecl(Position pos, 
            Pointcut pc, 
            boolean isPrivate, 
            ClassnamePatternExpr toClause) {
        return new SigMemberExposeDecl_c(pos, pc, isPrivate, toClause);
    }
    
    public SigMember SigMemberAdvertiseDecl(Position pos,
            Pointcut pc,
            boolean isPrivate,
            ClassnamePatternExpr toClause) {
        return new SigMemberAdvertiseDecl_c(pos, pc, isPrivate, toClause);
    }
    
    public AspectDecl DummyAspectDecl(Position pos, String moduleName) {
        return new DummyAspectDecl_c(pos, moduleName);
    }
	public OpenClassMember OpenClassMember(OpenClassFlags flags, 
			ClassnamePatternExpr cpe, 
			Position pos) {
		return new OpenClassMember_c(flags, cpe, pos);	}
}
