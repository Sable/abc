module m1;
public class A{
	m3xx::C c = new m3xx::C();
	m3xx::CX cx = new m3xx::CX();
	m3xx::CXX cxx = new m3xx::CXX();

	m3xxx::C c2 = new m3xxx::C();
	m3xxx::CX cx2 = new m3xxx::CX();
	m3xxx::CXX cxx2 = new m3xxx::CXX();
	m3xxx::CXXX cxxx = new m3xxx::CXXX();

	m3xx::pack.C pc = new m3xx::pack.C();
	m3xx::pack.CX pcx = new m3xx::pack.CX();
	m3xx::pack.CXX pcxx = new m3xx::pack.CXX();

	m3xxx::pack.C pc2 = new m3xxx::pack.C();
	m3xxx::pack.CX pcx2 = new m3xxx::pack.CX();
	m3xxx::pack.CXX pcxx2 = new m3xxx::pack.CXX();
	m3xxx::pack.CXXX pcxxx = new m3xxx::pack.CXXX();
	public A() {
		System.out.println(this.getClass());
	}
}
