package polyglot.ext.aspectj.ast;

import java.util.Iterator;
import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.util.CodeWriter;
import polyglot.util.UniqueID;
import polyglot.util.Position;
import polyglot.util.UniqueID;
import polyglot.util.TypedList;
import polyglot.visit.PrettyPrinter;
import polyglot.ast.Formal;

import polyglot.types.Context;
import polyglot.types.ClassType;
import polyglot.types.TypeSystem;
import polyglot.types.LocalInstance;
import polyglot.types.MethodInstance;
import polyglot.ext.jl.ast.MethodDecl_c;

import polyglot.ext.aspectj.types.AspectJTypeSystem;

public class AdviceDecl_c extends MethodDecl_c
                          implements AdviceDecl
{
    protected AdviceSpec spec;
    protected Pointcut pc;
 
    
    // if the returnVal of "after returning" or "after throwing" is
    // specified, make it an additional parameter to the advice body
    private static List locs(Formal rt, List formals) {
    	if (rt==null)
    	  return formals;
    	else {
    		List locs = ((TypedList)formals).copy();
    		locs.add(rt);
    		return locs;
    	}
    }

    public AdviceDecl_c(Position pos,
                        Flags flags,
                        AdviceSpec spec,
                        List throwTypes,
                        Pointcut pc,
	  	                Block body) {
	super(pos,
	      flags, 
	      spec.returnType(),
	      UniqueID.newID("$advice$"),
	      locs(spec.returnVal(),spec.formals()),
	      throwTypes,
	      body);
	this.spec = spec;
    this.pc = pc;
    }
    
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.begin(0);
	w.write(flags.translate());

        print(spec,w,tr);

	w.begin(0);

        if (! throwTypes.isEmpty()) {
	    w.allowBreak(6);
	    w.write("throws ");

	    for (Iterator i = throwTypes.iterator(); i.hasNext(); ) {
	        TypeNode tn = (TypeNode) i.next();
		print(tn, w, tr);

		if (i.hasNext()) {
		    w.write(",");
		    w.allowBreak(4, " ");
		}
	    }
	}

	w.end();

	w.write(":");

	w.allowBreak(0);

	print(pc, w, tr);
	
	if (body != null) {
	    printSubStmt(body, w, tr);
	}
	else {
	    w.write(";");
	}

	w.end();

    }
    

	static private MethodInstance proceedInstance = null;
	static private Context scope = null;
	static public MethodInstance  proceedInstance(Context c) {
		if (c==null)
		   return null;
		if (c==scope)
		   return proceedInstance;
		else return proceedInstance(c.pop());
	}

	public Context enterScope(Context c) {
		Context nc = super.enterScope(c);
		
		AspectJTypeSystem ts = (AspectJTypeSystem)nc.typeSystem();
	    LocalInstance jp = ts.localInstance(position(), Flags.NONE, ts.JoinPoint(), "thisJoinPoint");
		nc.addVariable(jp);
						
		if (spec instanceof Around)
			proceedInstance = methodInstance().name("proceed");
		else
		    proceedInstance = null;
        scope = nc;
        
		return nc;
	}
			
}
	

	

     


