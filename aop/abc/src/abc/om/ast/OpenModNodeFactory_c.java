/*
 * Created on May 13, 2005
 *
 */
package abc.om.ast;

import java.util.List;

import polyglot.util.*;
import abc.aspectj.ast.*;

/**
 * @author Neil Ongkingco
 *
 */
public class OpenModNodeFactory_c extends AJNodeFactory_c implements
        OpenModNodeFactory {

    public ModMember ModMemberAspect(Position pos, String name) {
        return new ModMemberAspect_c(pos,name);
    }
    public ModMember ModMemberClass(Position pos,
            ClassnamePatternExpr classPattern) {
        return new ModMemberClass_c(pos,classPattern);
    }
    public ModMember ModMemberModule(Position pos, String name, boolean isConstrained) {
        return new ModMemberModule_c(pos, name, isConstrained);
    }
    public ModuleBody ModuleBody(Position pos, List members, List sigMembers) {
        return new ModuleBody_c(pos, members, sigMembers);
    }
    public ModuleDecl ModuleDecl(Position pos, String name, ModuleBody body, Position namePos) {
        return new ModuleDecl_c(pos, name, body, namePos);
    }
    public SigMember SigMemberPCDecl(Position pos, Pointcut pc, boolean isPrivate) {
        return new SigMemberPCDecl_c(pos, pc, isPrivate);
    }
    
    public SigMember SigMemberMethodDecl(Position pos,
            MethodConstructorPattern methodPattern,
            boolean isPrivate) {
        return new SigMemberMethodDecl_c(pos, methodPattern, isPrivate);
    }
    
    public AspectDecl DummyAspectDecl(Position pos, String moduleName) {
        return new DummyAspectDecl_c(pos, moduleName);
    }
}
