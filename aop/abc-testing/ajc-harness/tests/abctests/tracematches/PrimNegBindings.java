import org.aspectj.testing.Tester;

public class PrimNegBindings {
    static void f(long o) { }
    static void g(long o, float s) { }
    static void h(float s) { }
    public static void main(String[] args) {
	long o1 = 1L;
	long o2 = 2L;
	f(o1);
	f(o2);
	float s = 3.14f;
	g(o1, 2.76f);
	g(o2, s);
	h(s);

	Tester.expectEvent(s + "");
	Tester.checkAllEvents();
    }
}
aspect TM {
    tracematch(long o, float s) {
	sym f after : call(* *.f(..)) && args(o);
	sym g before : call(* *.g(..)) && args(o, s);
	sym h after : call(* *.h(..)) && args(s);

	f g h
	    {
		Tester.event(s + "");
	    }
    }
}
