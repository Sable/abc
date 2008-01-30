package tracematches;
import org.aspectj.testing.Tester;

import java.util.*;

public aspect FailSafeEnum {

    // Object dollar$name;
    int violations = 0;

    pointcut vector_update() :
	call(* Vector.add*(..)) ||
	call(* Vector.clear()) ||
        call(* Vector.insertElementAt(..)) ||
        call(* Vector.remove*(..)) ||
        call(* Vector.retainAll(..)) ||
        call(* Vector.set*(..));

    tracematch(Enumeration e, Vector ds) {
	sym create_enum after returning(e) :
	    call(Enumeration+.new(..))
            && args(ds);
	sym call_next before : call(Object Enumeration.nextElement())
	    && target(e);
        sym update_source after : vector_update() 
            && target(ds);

	create_enum call_next* update_source+ call_next
	{
	    violations++;
            // System.out.println(dollar$name);
	    // throw new ConcurrentModificationException();
	}
    }

    
    after() : execution(* main(..)) {
        Tester.check(violations==100,"100 violations");
    }

}
   
