/*
 * Created on Sep 1, 2005
 *
 */
package abc.om.weaving.aspectinfo;

import polyglot.util.Position;
import abc.weaving.aspectinfo.ShadowPointcut;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;

/**
 * @author Neil Ongkingco
 *
 */
public class BoolPointcut extends ShadowPointcut {

    boolean b = false;
    
    public BoolPointcut(boolean b, Position pos) {
        super(pos);
        this.b = b;
    }
    
    /* (non-Javadoc)
     * @see abc.weaving.aspectinfo.ShadowPointcut#matchesAt(abc.weaving.matching.ShadowMatch)
     */
    protected Residue matchesAt(ShadowMatch sm) {
        Residue ret = null;
        if (b) {
            ret = AlwaysMatch.v();
        }
        else {
            ret = NeverMatch.v();
        }
        return ret;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new Boolean(b).toString();
    }

}
