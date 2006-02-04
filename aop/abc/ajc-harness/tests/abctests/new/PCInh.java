
abstract aspect A {

	pointcut p() : call(* foo(..));

	// pointcut p(int a) : call(* foo(..)) && args(a);

	before() : p() { System.out.println("advice 1"); }
}

aspect B extends A {

	pointcut p() : A.p() || call(* baz(..));

	before() : p() { System.out.println("advice 2"); }

}

public class PCInh {

	void foo() {System.out.println("foo");}
	void baz() {System.out.println("baz");}

	public static void main(String[] args) {
	   PCInh t = new PCInh();
	   t.foo(); t.baz();
	}
}


