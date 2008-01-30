import org.aspectj.testing.Tester;

public class PlusStar {
    public static void f() { }
    public static void g() { }
    public static void main(String[] args) {
	f(); f();/*match*/ g(); g(); f();/*match*/
	g(); f();/*match*/ f();/*match*/ g();
	Tester.expectEvent("fg*f+");
	Tester.expectEvent("fg*f+");
	Tester.expectEvent("fg*f+");
	Tester.expectEvent("fg*f+");
	Tester.checkAllEvents();
    }
}

aspect FG {
    tracematch() {
	sym f after : call(* *.f(..));
	sym g after : call(* *.g(..));

	f g* f+

	    {
		Tester.event("fg*f+");
	    }
    }
}
