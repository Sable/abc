import org.aspectj.testing.Tester;

public class PrimitiveVariables {
    public static void f(int o) { }
    public static void g(int o) { }
    public static void main(String[] args) {
	f(1); f(2); g(2);/*match 2*/ g(1);/*match 1*/ f(1);
	g(2);/*match 2*/ f(1); f(2); g(1);/*match 1*/
	g(1);/*match 1*/ g(2);/*match 2*/
	Tester.expectEvent(2+ "");
	Tester.expectEvent(1+ "");
 	Tester.expectEvent(2+ "");
	Tester.expectEvent(1+ "");
	Tester.expectEvent(1+ "");
 	Tester.expectEvent(2+ "");
	Tester.checkAllEvents();
    }
}

aspect FG {
    tracematch(int o) {
	sym f after : call(* *.f(..)) && args(o);
	sym g after : call(* *.g(..)) && args(o);

	f g+

	    {
		Tester.event(o + "");
	    }
    }
}
