package abc.weaving.weaver;

/** A different version of {@link ShadowPoints} which uses
 *  nop statements that were inserted before the unweaver
 *  saves copies of methods. Therefore, to retrieve the
 *  correct nops during weaving, the stored nops need to
 *  be mapped to the new versions.
 *  @author Ganesh Sittampalam
 */

import soot.SootMethod;
import soot.jimple.Stmt;

public class RebindingShadowPoints extends ShadowPoints {
    public RebindingShadowPoints(SootMethod container,Stmt b, Stmt e) {
        super(container,b,e);
    }

    public Stmt getBegin(){
        return (Stmt)Weaver.rebind(super.getBegin());
    }

    public Stmt getEnd(){
        return (Stmt)Weaver.rebind(super.getEnd());
    }

    public String toString(){
        return ("RebindingShadowPoint< begin:" + super.getBegin() + " end:" + super.getEnd() + " >");
    }

}