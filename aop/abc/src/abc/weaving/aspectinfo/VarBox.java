/*
 * Created on Sep 1, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.weaving.aspectinfo;


/** A wrapper class for an optional Var value (used in Pointcut.canRenameTo)
 */
public class VarBox {

	private Var var;
	private boolean hasVar;

	public VarBox() {
		this.hasVar = false;
	}

	public VarBox(Var var) {
		this.var = var;
		this.hasVar = true;
	}

	public boolean hasVar() {
		return hasVar;
	}
	public Var getVar() {
		return var;
	}

	public boolean equals(Object o) {
		if (o.getClass() == this.getClass()) {
			if (!this.hasVar) return (!((VarBox)o).hasVar());
			return var.equals(((VarBox)o).getVar());
		} else return false;
	}

	public boolean equalsvar(Var v) {
		if (!hasVar) return false;
		return (var.equals(v));
	}

	public void unset() {
		this.hasVar = false;
	}
	
	public void set(Var v) {
		this.var = v;
		this.hasVar = true;
	}
	
	public String toString() {
		if (hasVar) return var.toString();
		else return "X";
	}

}