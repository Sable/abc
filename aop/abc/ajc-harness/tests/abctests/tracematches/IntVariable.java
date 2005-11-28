public class IntVariable {
    public static void f(int o) { }
    public static void main(String[] args) {
	f(1);
    }
}

aspect FG {
    tracematch(int o) {
	sym f after : call(* *.f(..)) && args(o);

	f

	    {
		System.out.println(o);
	    }
    }
}
