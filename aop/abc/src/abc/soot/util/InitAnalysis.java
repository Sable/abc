package abc.soot.util;

import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import soot.toolkits.scalar.*;
import soot.toolkits.graph.*;


public class InitAnalysis extends ForwardFlowAnalysis {
    FlowSet allLocals;

    public InitAnalysis(UnitGraph g) {
	super(g);
	Chain locs=g.getBody().getLocals();
	allLocals=new ArraySparseSet();
	Iterator it=locs.iterator();
	while(it.hasNext()) {
	    Local loc=(Local) it.next();
	    allLocals.add(loc);
	}

	doAnalysis();
    }

    protected Object entryInitialFlow() {
	return new ArraySparseSet();
    }
    protected Object newInitialFlow() {
	FlowSet ret=new ArraySparseSet();
	allLocals.copy(ret);
	return ret;
    }

    protected void flowThrough(Object in,Object unit,Object out) {
	FlowSet inSet=(FlowSet) in;
	FlowSet outSet=(FlowSet) out;
	Stmt s=(Stmt) unit;

	inSet.copy(outSet);

	if(s instanceof DefinitionStmt) {
	    DefinitionStmt ds=(DefinitionStmt) s;
	    if(ds.getLeftOp() instanceof Local) {
		Local l=(Local) ds.getLeftOp();
		outSet.add(l);
	    }
	} 
    }

    protected void merge(Object in1,Object in2,Object out) {
	FlowSet outSet=(FlowSet) out;
	FlowSet inSet1=(FlowSet) in1;
	FlowSet inSet2=(FlowSet) in2;
	inSet1.intersection(inSet2,outSet);
    }

    protected void copy(Object source,Object dest) {
	FlowSet sourceSet=(FlowSet) source;
	FlowSet destSet=(FlowSet) dest;
	sourceSet.copy(destSet);
    }
	

}
