/*
 * Created on Jul 29, 2005
 *
 */
package abc.om.visit;

import abc.aspectj.ast.ClassnamePatternExpr;

/**
 * @author Neil Ongkingco
 *
 */
public class ModuleNodeClass extends ModuleNode {
    private ClassnamePatternExpr cpe;//for TYPE_CLASS nodes

    public ModuleNodeClass(String parentName, ClassnamePatternExpr cpe) {
        this.cpe = cpe;
        //name is the class expression itself.
        this.name = cpe.toString();
    }
    
    public ClassnamePatternExpr getCPE() {
        return cpe;
    }
    
    public boolean isAspect() {
        return false;
    }
    public boolean isClass() {
        return true;
    }
    public boolean isModule() {
        return false;
    }
    public int type() {
        return ModuleNode.TYPE_CLASS;
    }
}
