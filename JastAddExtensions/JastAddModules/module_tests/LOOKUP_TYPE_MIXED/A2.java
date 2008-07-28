module m1;

public class A2{
	B b1 = new B(); //should lookup to m2.B
	pack.B b2 = new pack.B(); 
	m2.B b3 = new m2.B();

	C c1 = new C();
	m3.C c2 = new m3.C(); //should lookup to m1$m3.C

	public A2() {
		System.out.println(this.getClass());
	}
}
