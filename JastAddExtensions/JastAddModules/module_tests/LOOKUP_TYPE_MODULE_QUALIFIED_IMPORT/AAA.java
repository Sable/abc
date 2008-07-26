module m1;
import m2::m3::pack.P;
import m2::m3::pack.P.PInner;
import m2::m3::pack.P.PInner.PInner2;

public class AAA {
	P p = new P();
	PInner pinner = new PInner();
	PInner2 pinner2 = new PInner2();
	public AAA() {
		System.out.println(this.getClass());
	}
}
