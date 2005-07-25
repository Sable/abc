import org.aspectj.testing.Tester;

public class Basic {
    public static void f() { }
    public static void g() { }
    public static void main(String[] args) {
	f(); g(); g(); f(); g(); f(); f(); g();
	Tester.expectEvent("fg");
	Tester.expectEvent("fg");
	Tester.expectEvent("fg");
	Tester.checkAllEvents();
    }
}

aspect FG {
    tracematch() {
	sym f after : call(* *.f(..));
	sym g after : call(* *.g(..));

	f g

	    {
		Tester.event("fg");
	    }
    }
}
