import org.aspectj.testing.Tester;

public class FlowIns {

	static int matches = 0;
	
    public void a() {  }
    public void b() {  }
    
    public static void main(String[] args) {
    	//a() and b() both exist but on different objects
    	new FlowIns().a(); //<--- shadow can be removed
    	new FlowIns().b(); //<--- shadow can be removed

    	//here we can actually have a match
    	FlowIns x = new FlowIns();
    	x.a();
    	x.b();

    	Tester.check(matches==1,"expected 1 match but saw "+matches);
    }
}

aspect AB {
    tracematch(Object o) {
    	sym a before : call(* *.a(..)) && target(o);
		sym b before : call(* *.b(..)) && target(o);

    	a b {
	    	FlowIns.matches++;
    	} //does not match in the above code due to inconsistent bindings
    }
}
