package abc.aspectj.visit;

import java.util.*;

import polyglot.ast.Node;
import polyglot.ast.JL;
import polyglot.visit.NodeVisitor;

public class AspectReflectionInspect extends NodeVisitor {

    private Stack/*<Boolean>*/ canTransform; /* Can we transform thisJoinPoint to 
                                                thisJoinPointStaticPart in the current advice body? */

    public AspectReflectionInspect() {
	super();
	this.canTransform=new Stack();
    }
    
    public void enterAdvice() {
	canTransform.push(new Boolean(true));
    }

    public void disableTransform() {
	if(((Boolean) canTransform.peek()).booleanValue()) {
	    canTransform.pop();
	    canTransform.push(new Boolean(false));
	}
    }

    public boolean inspectingLocals() {
	return !canTransform.empty();
    }

    public boolean leaveAdvice() {
	return ((Boolean) canTransform.pop()).booleanValue();
    }

    public NodeVisitor enter(Node parent, Node n) {
	JL del=n.del();
	if(del instanceof TransformsAspectReflection) 
	    ((TransformsAspectReflection) del).enterAspectReflectionInspect(this,parent);
	return this;
    }

    public Node leave(Node old,Node n,NodeVisitor v) {
	JL del=n.del();
	if(del instanceof TransformsAspectReflection)
	    ((TransformsAspectReflection) del).leaveAspectReflectionInspect(this);
	return n;
    }
}
