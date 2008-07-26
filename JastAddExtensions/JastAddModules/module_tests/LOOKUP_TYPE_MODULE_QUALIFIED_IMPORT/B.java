module m2;
import pack.P;
import pack.P.PInner;
import pack.P.PInner.PInner2;
public class B {
	P p5 = new P();
	PInner pinner = new PInner();
	PInner2 pinner2 = new PInner2();
	public B() {
		System.out.println(this.getClass());
	}
}
