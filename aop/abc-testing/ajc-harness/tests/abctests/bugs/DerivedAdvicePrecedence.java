import org.aspectj.testing.Tester;

public class DerivedAdvicePrecedence {
	public static String s = "";
	public static String correct = "A5;A4;A3;A2;A1;";
	public static void main(String args[]) {
		f();
		System.out.println(s);
		Tester.checkEqual(s.compareTo(correct), 0);
	}
	static void f() {}
}

abstract aspect Q {
	abstract protected String getName();

	before() : call(* f()) {
		DerivedAdvicePrecedence.s += getName() + ";";
	}

	declare precedence: A5, A4, A3, A2, A1;
}

aspect A1 extends Q{
	protected String getName() {
		return "A1";
	}
}
aspect A2 extends Q{
	protected String getName() {
		return "A2";
	}
}
aspect A3 extends Q{
	protected String getName() {
		return "A3";
	}
}
aspect A4 extends Q{
	protected String getName() {
		return "A4";
	}
}
aspect A5 extends Q{
	protected String getName() {
		return "A5";
	}
}
