import org.aspectj.testing.Tester;

public class NegBindings {
    static void f(Object o) { }
    static void g(Object o, String s) { }
    static void h(String s) { }
    public static void main(String[] args) {
	Object o1 = new Object();
	Object o2 = new Object();
	f(o1);
	f(o2);
	String s = "1";
	g(o1, "2");
	g(o2, s);
	h(s);

	Tester.expectEvent(s);
	Tester.checkAllEvents();
    }
}
aspect TM {
    tracematch(Object o, String s) {
	sym f after : call(* *.f(..)) && args(o);
	sym g before : call(* *.g(..)) && args(o, s);
	sym h after : call(* *.h(..)) && args(s);

	f g h
	    {
		Tester.event(s);
	    }
    }
}
