/*
 * Created on Jul 21, 2004
 */
package abc.weaving.aspectinfo;

import polyglot.util.ErrorInfo;
import polyglot.util.Position;

import polyglot.types.ClassType;
import polyglot.types.MemberInstance;

import soot.Modifier;

/**
 * @author pavel
 *
 * Abstract class representing a generic accessor method - Set, Get or Dispatch. It stores
 * all important information about a method, i.e. name, type, argument types, containing class
 * etc., and provides facilities to get a Jimple representation of the method - overriding
 * classes have to provide the body() method for constructing the method's jimple body.
 */
public abstract class AccessorMethod {
    String name;
    Position position;
    ClassType target;
    
    public AccessorMethod(String name, ClassType target, Position pos) {
        this.name = name;
        this.target = target;
        this.position = pos;

        // As there is no way to generate a compiler error/warning from here, add it to a
        // list of errors. The closest instance of AspectDecl on the stack will generate 
        // the messages.
        if(!GlobalAspectInfo.v().getWeavableClasses().contains(AbcFactory.AbcClass(target))) {
            GlobalAspectInfo.v().addClassNotWeavableError(new ErrorInfo(ErrorInfo.WARNING, 
                    "Need to weave into class " + target + ", but it is not weavable. " +
                    "Execution of compiled code will probably fail.", position));
        }
    }
    
    /**
     * Get the SootMethod representing this accessor method with the default modifiers (i.e. PUBLIC).
     * This method also adds the generated method to the target class.
     */
    public void addSootMethod() {
        addSootMethod(Modifier.PUBLIC);
    }
    
    /**
     * Get the SootMethod representing this accessor method with the given modifiers. This method also
     * adds the generated method to the target class.
     * 
     * @param modifiers Modifiers to declare the method with - compare soot.Modifier
     * @return the SootMethod object.
     */
    abstract public void addSootMethod(int modifiers);
    
    /**
     * Used to register the method's category with the static MethodCategory class.
     * The default implementation (registering field get/set methods and dispatches) should be
     * sufficient for most purposes.
     */
    abstract public void registerMethod(soot.SootMethod sm);
}
