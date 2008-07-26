module m2;
import m3::pack.P;
import m3::pack.P.PInner;
import m3::pack.P.PInner.PInner2;

public class BB {
	m2::pack.P p2 = new m2::pack.P();
	PInner pinner = new PInner();
	PInner2 pinner2 = new PInner2();
	public BB() {
		System.out.println(this.getClass());
	}
}
