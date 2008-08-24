module m1;
public class A {
	m2a::B b = new m2a::B();
	m2b::B b2 = new m2b::B();
	m2merged::B b3 = new m2merged::B();

	public A() {
		System.out.println(this.getClass());
	}
}
