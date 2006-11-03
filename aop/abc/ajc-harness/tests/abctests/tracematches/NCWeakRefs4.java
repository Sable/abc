public class NCWeakRefs4 {
    public static void foo(Object o) { }
    public static void bar() { 
	//System.out.println("Matched " + matched + " times."); 
    }

    public static long matched = 0;

    public static void main(String[] args) {
	Object o;
	for(int j = 0; j < 1000; j++) {
	    for(int i = 0; i < 1000; i++) {
		o = new Object();
		foo(o);
	    }
	    System.gc(); System.gc(); System.gc(); System.gc(); System.gc(); 
	    bar();
	    bar();
	    System.out.print(".");
	}
	System.out.println();
	if(matched != 1000000) throw new RuntimeException("Matched " + matched + " times, rather than 1000000.");
    }
}

aspect A {
    tracematch(Object o) {
	sym foo before : call(* *.foo(Object)) && args(o);
	sym bar before : call(* *.bar());

	foo bar bar {
	    NCWeakRefs4.matched++;
	}
    }
}
