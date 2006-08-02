public class CollWeakRefs {
    public static void foo(Object o) { }
    public static void bar(Object o) {
	System.out.println("Matched " + o);
    }

    public static int matched = 0;

    public static void main(String[] args) {
	Object o = "";
	Object orig = "orig";
	foo(orig);
	for(int i = 0; i < 100; i++) {
	    o = "" + i;
	    foo(o);
	    System.gc(); System.gc(); System.gc(); System.gc(); System.gc(); 
	}
        bar(orig); bar(o); foo(o); foo(o); bar(o);
	if(matched != 3) throw new RuntimeException("Matched " + matched + " times, rather than 3.");
    }
}

aspect A {
    tracematch(Object o) {
	sym foo before : call(* *.foo(Object)) && args(o);
	sym bar before : call(* *.bar(Object)) && args(o);

	foo bar {
	    CollWeakRefs.matched++;
	}
    }
}
