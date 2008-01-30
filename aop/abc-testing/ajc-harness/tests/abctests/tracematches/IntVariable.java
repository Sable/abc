public class IntVariable {
    public static boolean matched = false;
    public static void f(int o) { }
    public static void main(String[] args) {
	f(1);
	if(!matched) throw new RuntimeException("Matching TM failed");
    }
}

aspect FG {
    tracematch(int o) {
	sym f after : call(* *.f(..)) && args(o);

	f

	    {
		IntVariable.matched = true;
	    }
    }
}
