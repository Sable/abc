package abc.aspectj.visit;

import java.util.*;

import polyglot.ast.Node;

import abc.aspectj.ast.AspectJNodeFactory;
import abc.aspectj.types.AJTypeSystem;

public interface TransformsAspectReflection {

    public void enterAspectReflectionInspect(AspectReflectionInspect v,Node parent);
    public void leaveAspectReflectionInspect(AspectReflectionInspect v);

    public void enterAspectReflectionRewrite(AspectReflectionRewrite v,AJTypeSystem ts);
    public Node leaveAspectReflectionRewrite(AspectReflectionRewrite v,AspectJNodeFactory nf);

}
