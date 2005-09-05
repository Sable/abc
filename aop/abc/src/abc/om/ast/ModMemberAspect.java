/*
 * Created on May 13, 2005
 *
 */
package abc.om.ast;

import abc.aspectj.ast.NamePattern;

/**
 * @author Neil Ongkingco
 *
 */
public interface ModMemberAspect extends ModMember {
    public String name();
    public NamePattern getNamePattern();
}
