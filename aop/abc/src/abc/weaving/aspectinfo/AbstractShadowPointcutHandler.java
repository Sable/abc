package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.MethodPosition;
import abc.weaving.residues.Residue;

import java.util.*;

/** Skeleton implementation of the {@link abc.weaving.aspectinfo.ShadowPointcutHandler} interface.
 *  Useful when implementing shadow pointcut handlers.
 */
public abstract class AbstractShadowPointcutHandler implements ShadowPointcutHandler {
    // Keep a record of what class is what shadow type?
    static private List/*<ShadowType>*/ allShadowTypes=new LinkedList();

    /** All classes that implement a new shadow type should call this in their static initializer */
    static public void registerShadowType(ShadowType st) {
	allShadowTypes.add(st);
    }

    static public Iterator shadowTypesIterator() {
	return allShadowTypes.iterator();
    }

    /* remove this once all deriving classes implement it */
    //    public Residue matchesAt(MethodPosition position) {
    //System.out.println("Returning null for unimplemented shadow pointcut type "+this.getClass());
    //return null;
    //}

}
