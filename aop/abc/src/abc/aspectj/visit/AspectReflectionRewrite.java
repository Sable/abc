package abc.aspectj.visit;

import java.util.*;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.NodeVisitor;

import abc.aspectj.ast.AspectJNodeFactory;
import abc.aspectj.types.AspectJTypeSystem;


public class AspectReflectionRewrite extends NodeVisitor {

    private Stack/*<LocalInstance>*/ jpsps; /* The thisJoinPointStaticPart LocalInstances, 
					       or null if we are not transforming them */


    public AspectJNodeFactory nf;
    public AspectJTypeSystem ts;

    public AspectReflectionRewrite(NodeFactory nf,TypeSystem ts) {
	super();
	this.nf=(AspectJNodeFactory) nf;
	this.ts=(AspectJTypeSystem) ts;
	this.jpsps=new Stack();
    }
    
    public void enterAdvice(LocalInstance jpsp) {
	jpsps.push(jpsp);
    }

    public void leaveAdvice() {
	jpsps.pop();
    }

    public boolean inspectingLocals() {
	return !jpsps.empty();
    }

    public LocalInstance getJPSP() {
	return ((LocalInstance) jpsps.peek());
    }

    public NodeVisitor enter(Node n) {
	JL del=n.del();
	if(del instanceof TransformsAspectReflection) 
	    ((TransformsAspectReflection) del).enterAspectReflectionRewrite(this,ts);
	return this;
    }

    public Node leave(Node old,Node n,NodeVisitor v) {
	JL del=n.del();
	if(del instanceof TransformsAspectReflection)
	    return((TransformsAspectReflection) del).leaveAspectReflectionRewrite(this,nf);
	return n;
    }
}
