import org.aspectj.testing.Tester;

public class ContainsTest {
	public static int callCtr = 0;

	public static void main(String args[]) {
		System.out.println("main");
		f();
		g();
		h(true);
		i();
		j();
		k();
		l();

		System.out.println("callctr = " + callCtr);
		Tester.checkEqual(callCtr, 6);
	}
	static void f() {
		System.out.println("f");
	}

	static void g() {
		System.out.println("g");
		f();
	}

	static void h(boolean b) {
		System.out.println("h");
		if (b) {
			f();
		}
	}

	static void i() {
		System.out.println("i");
		for (int i = 0; i < 5; i++) {
			f();
		}
	}

	static void j() {
		System.out.println("j");

		int i = 0;
		while (i < 5) {
			f();
			i++;
		}
	}

	static void k() {
		System.out.println("k");
	}

	static void l() {
		System.out.println("l");
		g();
	}
}

aspect A {
    pointcut f() : call(* f());
	pointcut pc() : contains(f()) && !within(A);

	before() : pc() {
		System.out.println(thisJoinPoint.getSignature());
		ContainsTest.callCtr++;
	}
}
