/*
 * Created on 09-Feb-2005
 */
package abcexer2.weaving.aspectinfo;

import polyglot.util.Position;
import abc.weaving.aspectinfo.ShadowPointcut;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;
import abcexer2.weaving.matching.ArrayGetShadowMatch;

/**
 * @author Sascha Kuzins
 *
 */
public class ArrayGet extends ShadowPointcut {

	public ArrayGet(Position pos) {
		super(pos);
	}
	protected Residue matchesAt(ShadowMatch sm) {
		
        if (!(sm instanceof ArrayGetShadowMatch)) return NeverMatch.v();
        
        return AlwaysMatch.v();    
	}

	public String toString() {
		return "arrayget()";
	}

}
