package abc.weaving.residues;

import soot.*;

/** A formal parameter to advice
 *  @author Ganesh Sittampalam
 *  @date 04-May-04
 */ 

public class AdviceFormal implements WeavingVar {
    public int pos;

    public AdviceFormal(int pos) {
	this.pos=pos;
    }

    public String toString() {
	return "advicearg("+pos+")";
    }

}
