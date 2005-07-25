import org.aspectj.testing.Tester;

public class Variables {
    public static void f(Object o) { }
    public static void g(Object o) { }
    public static void main(String[] args) {
	Object o1 = new Object(), o2 = new Object();
	f(o1); f(o2); g(o2);/*match o2*/ g(o1);/*match o1*/ f(o1);
	g(o2);/*match o2*/ f(o1); f(o2); g(o1);/*match o1*/
	g(o1);/*match o1*/ g(o2);/*match o2*/
	Tester.expectEvent(o2.toString());
	Tester.expectEvent(o1.toString());
 	Tester.expectEvent(o2.toString());
	Tester.expectEvent(o1.toString());
	Tester.expectEvent(o1.toString());
 	Tester.expectEvent(o2.toString());
	Tester.checkAllEvents();
    }
}

aspect FG {
    tracematch(Object o) {
	sym f after : call(* *.f(..)) && args(o);
	sym g after : call(* *.g(..)) && args(o);

	f g+

	    {
		Tester.event(o.toString());
	    }
    }
}
