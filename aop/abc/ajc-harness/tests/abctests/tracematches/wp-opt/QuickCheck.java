public class QuickCheck {
    public static void f() { System.out.println("f"); }
    public static void g() { System.out.println("g"); }
    public static void main(String[] args) {
    	f(); 
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

  	  f g* { }
    }

    tracematch() {
  	  sym f after : call(* *.f(..));
  	  sym g after : call(* *.g(..));

  	  f g | f { }
    }
}
