
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

import java.util.*;

/** A pointcut declaration. */
public class PointcutDecl extends Syntax {
    private String name;
    private List/*<Formal>*/ formals;
    private Pointcut pc;
    private Aspect aspect;

    /** Create a pointcut declaration.
     *  @param formals a list of {@link abc.weaving.aspectinfo.Formal} objects
     */
    public PointcutDecl(String name, List formals, Pointcut pc, Aspect aspect, Position pos) {
	super(pos);
	this.name = name;
	this.formals = formals;
	this.pc = pc;
	this.aspect = aspect;
    }

    public String getName() {
	return name;
    }

    /** Get the formals of the pointcut declaration.
     *  @return a list of {@link abc.weaving.aspectinfo.Formal} objects.
     */
    public List getFormals() {
	return formals;
    }

    public Pointcut getPointcut() {
	return pc;
    }

    public Aspect getAspect() {
	return aspect;
    }

    public String toString() {
	return "pointcut "+name+"(...): "+pc;
    }

}
