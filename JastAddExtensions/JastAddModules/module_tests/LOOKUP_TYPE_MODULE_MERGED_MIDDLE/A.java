module m1;
public class A{
	m2a::B b1 = new m2a::B();
	m2b::B b2 = new m2b::B();
	m3a::C c1 = new m3a::C();
	m3b::C c2 = new m3b::C();

	m4aliasA::D d1 = new m4aliasA::D();
	m4aliasB::D d2 = new m4aliasB::D();

	m2alias::B b3 = new m2alias::B();
	m3alias::C c3 = new m3alias::C();

	m4alias::D d3 = new m4alias::D();
	m2alias::m4export::D d4 = new m2alias::m4export::D();
	m3alias::m4export::D d5 = new m3alias::m4export::D();
	m4aliasA::D d6 = new m4aliasA::D(); //should still point to the old m4alias
	m4aliasB::D d7 = new m4aliasB::D(); //should still point to the old m4alias

	public A() {
		System.out.println(this.getClass());
	}
}
