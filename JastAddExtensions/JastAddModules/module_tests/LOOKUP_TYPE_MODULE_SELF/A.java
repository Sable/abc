module m1;

public class A{
	alias1.B b1 = new alias1.B();
	alias2.B b2 = new alias2.B();
	m1::AA aa = new AA();
	m1::a.AAA aaa = new a.AAA();
	m1::alias1::B b = new alias1::B();
	m1::alias2::B b3;
	public A() {
		System.out.println(this.getClass());
	}
}
