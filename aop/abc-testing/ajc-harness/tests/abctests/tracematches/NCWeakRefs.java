public class NCWeakRefs {
    public static void foo(String s) { }
    public static void bar() { 
	//System.out.println("Matched " + matched + " times."); 
	if(matched != 10) throw new RuntimeException("Matched " + matched + " times, rather than 10.");
    }

    public static int matched = 0;

    public static void main(String[] args) {
	String s;
	for(int i = 0; i < 10; i++) {
	    s = "" + i;
	    foo(s);
	    System.gc(); System.gc(); System.gc(); System.gc(); System.gc(); 
	}
	bar();
    }
}

aspect A {
    tracematch(String s) {
	sym foo before : call(* *.foo(String)) && args(s);
	sym bar before : call(* *.bar());

	foo bar {
	    NCWeakRefs.matched++;
	}
    }
}
