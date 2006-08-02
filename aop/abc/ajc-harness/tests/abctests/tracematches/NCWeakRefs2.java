public class NCWeakRefs2 {
    public static void foo(Object o) { }
    public static void bar() {
	//System.out.println("Matched " + matched + " times."); 
	if(matched != 100) throw new RuntimeException("Matched " + matched + " times, rather than 100.");
    }

    public static int matched = 0;

    public static void main(String[] args) {
	Object o;
	for(int i = 0; i < 100; i++) {
	    o = new Object();
	    foo(o);
	    System.gc(); System.gc(); System.gc(); System.gc(); System.gc(); 
	}
	bar();
    }
}

aspect A {
    tracematch(Object o) {
	sym foo before : call(* *.foo(Object)) && args(o);
	sym bar before : call(* *.bar());

	foo bar {
	    NCWeakRefs2.matched++;
	}
    }
}
