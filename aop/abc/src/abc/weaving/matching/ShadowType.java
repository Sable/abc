package abc.weaving.matching;

import java.util.*;

import soot.*;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.*;

/** Each possible joinpoint shadow type extends this class and registers a
 *  singleton instance with it; the sole purpose of the hierarchy is to 
 *  provide something for the matcher to iterate over. For each
 *  ShadowType class there is a ShadowMatch class that is used to hold
 *  individual matching results.
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public abstract class ShadowType {
    /** Could a given MethodPosition match here? */
    public abstract ShadowMatch matchesAt(MethodPosition pos);

    private static List/*<ShadowType>*/ allShadowTypes=new LinkedList();

    /** Call this for each shadow type we want to be active */
    public static void register(ShadowType st) {
        allShadowTypes.add(st);
    }

    public static Iterator shadowTypesIterator() {
        return allShadowTypes.iterator();
    }

}
