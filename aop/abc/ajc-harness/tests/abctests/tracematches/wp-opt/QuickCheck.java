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
    }
}

aspect FG {
    tracematch() {
	  sym f after : call(* *.f(..));
	  sym g after : call(* *.g(..));

	  f g { } //cannot match
    }

    tracematch() {
  	  sym f after : call(* *.f(..));
  	  sym g after : call(* *.g(..));

  	  f g* { } //can match
    }

    tracematch() {
  	  sym f after : call(* *.f(..));
  	  sym g after : call(* *.g(..));

  	  f g | f { } //can match
    }
}

aspect AB {
    tracematch(Object o) {
    	sym a after : call(* *.a(..)) && target(o);
		sym b after : call(* *.b(..)) && target(o);

    	a b {} //does not match but quick check does not suffice to recognize that
    }
}
