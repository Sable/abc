/*
 * Created on May 13, 2005
 *
 */
package abc.om.ast;

import abc.aspectj.ast.ClassnamePatternExpr;

/**
 * @author Neil Ongkingco
 *
 */
public interface ModMemberClass extends ModMember {
    ClassnamePatternExpr getCPE();
}
