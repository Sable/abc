public class Test { 
    static void f(Object o1, Object o2) { }
    static void g(Object o) { }
    public static void main(String args[]) {
	Object o = new Object(); 
	Object o2 = new Object(); 
	f(o, o); 
	f(o, o2); 
	g(o);
    }
}
aspect TM { 
    tracematch (Object o1, Object o2) {
	sym f after : call(* *.f(..)) && args(o1, o2);
	sym g after : call(* *.g(..)) && args(o1);
	f g
	    { System.out.println("MATCHED"); }
    } 
}
