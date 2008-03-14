import org.aspectj.testing.Tester;

public class FlowIns2 {

	static int matches = 0;
	
    static void a(Object x) {  }
    static void b(Object x) {  }
    static void c(Object x, Object y) {  }
    
    public static void main(String[] args) {
    	Object o = new Object();
    	Object p = new Object();
    	a(o);
    	b(p);
    	c(o,p);

    	Object o2 = new Object();
    	Object p2 = new Object();
    	a(o2);
    	b(p2);
    }
}

aspect AB {
    tracematch(Object o, Object p) {
    	sym a before : call(* *.a(..)) && args(o);
		sym b before : call(* *.b(..)) && args(p);
		sym c before : call(* *.c(..)) && args(o,p);

    	a b c {
	    	FlowIns2.matches++;
    	} //does not match in the above code due to inconsistent bindings
    }
}
