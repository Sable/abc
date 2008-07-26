module m3;
public class C {
	m1::A a;
	m1::m11::AA aa = new m1::m11::AA();
	m1::m12::AB ab = new m1::m12::AB();
	m1::m13alias::AC ac = new m1::m13alias::AC();
	public C() {
		System.out.println(this.getClass());
	}
}
