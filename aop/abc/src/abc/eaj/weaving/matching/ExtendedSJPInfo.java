package abc.eaj.weaving.matching;

import soot.*;
import soot.tagkit.Host;
import abc.weaving.matching.SJPInfo;

public class ExtendedSJPInfo
{
    public static String makeCastSigData(SootMethod container, Type cast_to)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("0-cast-cast-");
        sb.append(SJPInfo.getTypeString(cast_to));
        sb.append('-');
        return sb.toString();
    }
}
