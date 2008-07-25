module m1;
public class A {
	a.AA aa1 = new a.AA();
	a.AA aa2 = new a.AA(1L);
	a.AA.AAPublic aa3 = new a.AA.AAPublic();
	a.AA.AAModule aa4 = new a.AA.AAModule();

	m2::b.BB bb = new m2::b.BB();
	B b = new B();

	public A() {
		System.out.println(this.getClass());
		aa1.publicf();
		aa1.modulef();
	}
}
