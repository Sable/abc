package naiveaspects;
import org.aspectj.testing.Tester;

import org.apache.commons.collections.map.*;
import java.util.*;

public aspect FailSafeEnum {

    int creations = 0;
    int nexts = 0;
    int updates = 0;
    int violations = 0;

    private static ReferenceIdentityMap makeMap() {
	return new ReferenceIdentityMap(AbstractReferenceMap.HARD,
                                        AbstractReferenceMap.HARD,
                                        false);
    }

    private Map ds_state      = makeMap();
    private Map enum_ds       = makeMap();
    private Map enum_ds_state = makeMap();

    synchronized after(Vector ds) returning(Enumeration e) :
	call(Enumeration+.new(..)) &&
        args(ds) {
	// System.out.println("create");
	enum_ds.put(e,ds);
        Object s = ds_state.get(ds);
        if (s != null)
          enum_ds_state.put(e,ds_state.get(ds));
        creations++;
    }

    synchronized before(Enumeration e):
        call(Object Enumeration.nextElement())
        && target(e) {
        // System.out.println("nextElement");
        // System.out.println("ds_state:"+ds_state.size() + "enum_ds:"+enum_ds.size()+"enum_ds_state:"+enum_ds_state.size());
        if (ds_state.get(enum_ds.get(e)) != enum_ds_state.get(e))
	    violations++ // skip: throw new ConcurrentModificationException()
            ;
        nexts++;
    }

    pointcut vector_update() :
	call(* Vector.add*(..)) ||
	call(* Vector.clear()) ||
        call(* Vector.insertElementAt(..)) ||
        call(* Vector.remove*(..)) ||
        call(* Vector.retainAll(..)) ||
        call(* Vector.set*(..));

    private static class StateId {}    
    
    synchronized after(Vector ds) :
	vector_update() && target(ds) {
        // System.out.println("update");
	ds_state.put(ds,new StateId());
        updates++;
    }

    after() : execution(* main(..)) {
        Tester.check(violations==100,"100 violations"); 
   }

}