package abc.weaving.residues;

import polyglot.util.InternalCompilerError;
import soot.Value;
import soot.Local;
import soot.Type;
import soot.jimple.Stmt;
import soot.jimple.Jimple;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;
import abc.weaving.weaver.AdviceWeavingContext;

/** A formal parameter to advice
 *  @author Ganesh Sittampalam
 *  @date 04-May-04
 */ 

public class AdviceFormal extends WeavingVar {
    public int pos;
    public Type type;
    private Local loc=null;

    public AdviceFormal(int pos,Type type) {
	this.pos=pos;
	this.type=type;
    }

    public String toString() {
	return "advicearg("+pos+":"+type+")";
    }

    public Stmt set(LocalGeneratorEx localgen,Chain units,Stmt begin,WeavingContext wc,Value val) {
	if(loc==null) loc = localgen.generateLocal(type,"adviceformal");
	Stmt assignStmt=Jimple.v().newAssignStmt(loc,val);
	units.insertAfter(assignStmt,begin);
	((AdviceWeavingContext) wc).arglist.setElementAt(loc,pos);
	return assignStmt;
    }

    public Local get() {
	if(loc==null) 
	    throw new InternalCompilerError
		("Someone tried to read from a variable bound "
		 +"to an advice formal before it was written: "+this);

	return loc;
    }

    public boolean hasType() {
	return true;
    }

    public Type getType() {
	return type;
    }

}

