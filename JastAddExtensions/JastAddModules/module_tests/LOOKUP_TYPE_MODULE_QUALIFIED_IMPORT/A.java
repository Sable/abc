module m1;
import ::pack.PP;
import pack.P;
import pack.P.PInner;
import pack.P.PInner.PInner2;

public class A {
	PP p4 = new PP();
	P p5 = new P();
	PInner pinner = new PInner();
	PInner2 pinner2 = new PInner2();
	::pack.P.PInner pinner3 = new ::pack.P.PInner();
	public A() {
		System.out.println(this.getClass());
	}
}
