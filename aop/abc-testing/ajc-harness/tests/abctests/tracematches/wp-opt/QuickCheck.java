import org.aspectj.testing.Tester;

public class QuickCheck {
    public static void f() {  }
    public static void g() {  }
    
    public void a() {  }
    public void b() {  }
    
    public static void main(String[] args) {
    	//only f(); g() is missing
    	f();	//<---- a shadow for the first tracematch should
    	        //      be removed here by the quick-check! 
    	
    	//a() and b() both exist but on different objects
    	new QuickCheck().a();
    	new QuickCheck().b();
    	
    	Tester.expectEvent("f g*");
    	Tester.expectEvent("f g | f");    	
    	Tester.checkAllEvents();
    }
}

aspect FG {
    tracematch() {
	  sym f before : call(* *.f(..));
	  sym g before : call(* *.g(..));

	  f g {
	    	Tester.event("f g");    	
	  } //cannot match
    }

    tracematch() {
  	  sym f before : call(* *.f(..));
  	  sym g before : call(* *.g(..));

  	  f g* {
	    	Tester.event("f g*");    	
  	  } //matches
    }

    tracematch() {
  	  sym f before : call(* *.f(..));
  	  sym g before : call(* *.g(..));

  	  f g | f {
	    	Tester.event("f g | f");    	
  	  } //matches
    }
}

aspect AB {
    tracematch(Object o) {
    	sym a before : call(* *.a(..)) && target(o);
		sym b before : call(* *.b(..)) && target(o);

    	a b {
	    	Tester.event("a b");    	
    	} //does not match but quick check does not suffice to recognize that
    }
}
