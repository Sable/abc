package abc.weaving.residues;


import soot.*;
import soot.jimple.*;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** A polymorphic local variable whose type is determined by
 *  the first thing it is set to
 *  @author Ganesh Sittampalam
 */ 

public class PolyLocalVar extends WeavingVar {
    public String name;
    private Local loc;
    private Type type;

    /** The name parameter is just for debugging purposes;
     *  identity of the variable comes from the reference
     */
    public PolyLocalVar(String name) {
	this.name=name;
    }

    public String toString() {
	return "polylocalvar("+name+":"+type+")";
    }

    public Stmt set(LocalGeneratorEx localgen,Chain units,Stmt begin,WeavingContext wc,Value val) {
	type=val.getType();
	if(loc==null) loc = localgen.generateLocal(type,"pointcutlocal");	
	Stmt assignStmt=Jimple.v().newAssignStmt(loc,val);
	units.insertAfter(assignStmt,begin);
	return assignStmt;
    }

    public Local get() {
	if(loc==null) 
	    throw new RuntimeException
		("Internal error: someone tried to read from a variable bound "
		 +"to a polymorphic local before it was written");

	return loc;
    }

    public boolean hasType() {
	return type!=null;
    }

    public Type getType() {
	if(type==null)
	    throw new RuntimeException
		("Internal error: someone tried to inspect the type of "
		 +"a polymorphic local before it was written");
	return type;
    }


}
