/*
 * Created on May 13, 2005
 *
 */
package abc.om.ast;

import abc.weaving.aspectinfo.Pointcut;
import polyglot.ast.ClassMember;
import polyglot.ast.Node;

/**
 * @author Neil Ongkingco
 *
 */
public interface SigMember extends Node {
    public Pointcut getAIPointcut(); 
    public boolean isPrivate();
}
