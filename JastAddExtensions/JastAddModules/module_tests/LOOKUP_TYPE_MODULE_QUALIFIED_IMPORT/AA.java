module m1;
import m2::pack.P;
import m2::pack.P.PInner;
import m2::pack.P.PInner.PInner2;

public class AA {
	m2::pack.P p2 = new m2::pack.P();
	PInner pinner = new PInner();
	PInner2 pinner2 = new PInner2();
	public AA() {
		System.out.println(this.getClass());
	}
}
