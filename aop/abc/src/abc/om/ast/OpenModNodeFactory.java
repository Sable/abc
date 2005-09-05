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