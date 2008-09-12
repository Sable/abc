package test;

public aspect Test60 {
	ast B ::= A;
	ast A;
	
	inh int A.c2_a() circular [1];
	eq B.getA().c2_a() {
		c2_acount++;
		return getA().c2_b();
	}
	
	inh int A.c2_b() circular [1];
	eq B.getA().c2_b() {
		c2_bcount++;
		int c = getA().c2_a();
		return c == 2 ? 2 : c + 1;
	}
	
	inh int A.c3();
	eq B.getA().c3() {
		c3count++;
		return getA().c2_a();
	}
	
	inh int A.c1_a() circular [1];
	eq B.getA().c1_a() {
		c1_acount++;
		int c = getA().c1_b() + getA().c3();
		return c >= 5 ? 5 : c + 1;
	}
	
	inh int A.c1_b() circular [1];
	eq B.getA().c1_b() {
		c1_bcount++;
		return getA().c1_a();
	}
	
	public int B.c1_acount;
	public int B.c1_bcount;
	public int B.c3count;
	public int B.c2_acount;
	public int B.c2_bcount;
	
	public static void main(String[] args) {
		System.out.println("60, Circular: inherited attrs, no stack of strongly connected components");
		A a = new A();
		B root = new B(a);
    System.out.println("Order: c1 before c2");
		System.out.println("c1_a() = " + a.c1_a());
		System.out.println("c2_a() = " + a.c2_a());
		System.out.println("c1_acount " + root.c1_acount);
		System.out.println("c1_bcount " + root.c1_bcount);
		System.out.println("c3count " + root.c3count);
		System.out.println("c2_acount " + root.c2_acount);
		System.out.println("c2_bcount " + root.c2_bcount);
    
		a = new A();
		root = new B(a);
    System.out.println("Order: c2 before c1");
		System.out.println("c2_a() = " + a.c2_a());
		System.out.println("c1_a() = " + a.c1_a());
		System.out.println("c1_acount " + root.c1_acount);
		System.out.println("c1_bcount " + root.c1_bcount);
		System.out.println("c3count " + root.c3count);
		System.out.println("c2_acount " + root.c2_acount);
		System.out.println("c2_bcount " + root.c2_bcount);
	}
}
