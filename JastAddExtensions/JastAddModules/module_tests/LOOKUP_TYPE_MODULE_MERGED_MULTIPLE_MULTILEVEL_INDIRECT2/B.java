module m2;
public class B{
	m3a::C c1 = new m3a::C();
	m3b::C c2 = new m3b::C();

	m3a::m4::D d1= new m3a::m4::D();
	m3b::m4::D d2= new m3b::m4::D();
	m4alias2::D d3 = new m4alias2::D();
	D d4 = new D();

	
	public B() {
		System.out.println(this.getClass());
	}
}
