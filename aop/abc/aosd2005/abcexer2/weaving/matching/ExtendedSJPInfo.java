/*
 * Created on 09-Feb-2005
 */
package abcexer2.weaving.matching;

import soot.SootMethod;
import soot.tagkit.Host;
import abc.weaving.matching.AbcSJPInfo;
import abc.weaving.matching.SJPInfo;

/**
 * @author Sascha Kuzins
 * @author Julian Tibble
 *
 */
public class ExtendedSJPInfo  extends AbcSJPInfo implements SJPInfo {
    public ExtendedSJPInfo(String kind,String signatureTypeClass,
		   String signatureType,String signature,Host host) {
        super(kind, signatureTypeClass, signatureType, signature, host);
    }

    public static String makeArrayGetSigData(SootMethod container)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("-");     // an arrayget has no associated modifiers
        sb.append("-");     // an arrayget has no associated name-part
        sb.append(container.getDeclaringClass().getName());
        sb.append('-');
        //sb.append(AbcSJPInfo.getTypeString(cast_to));
        sb.append('-');
        return sb.toString();
    } 
}
