package abc.weaving.residues;

import soot.Value;
import soot.Type;
import abc.weaving.weaver.WeavingContext;

/** A formal parameter to advice
 *  @author Ganesh Sittampalam
 *  @date 04-May-04
 */ 

public class AdviceFormal implements WeavingVar {
    public int pos;
    public Type type;

    public AdviceFormal(int pos,Type type) {
	this.pos=pos;
	this.type=type;
    }

    public String toString() {
	return "advicearg("+pos+":"+type+")";
    }

    public void set(WeavingContext wc,Value v) {
	wc.arglist.setElementAt(v,pos);
    }

    public Type getType() {
	return type;
    }

}

