module m1;
import m2::m3::pack.*;
import m2::m3::pack.P.*;
import m2::m3::pack.P.PInner.*;

public class AAADemand {
	P p = new P();
	PInner pinner = new PInner();
	PInner2 pinner2 = new PInner2();
	public AAADemand() {
		System.out.println(this.getClass());
	}
}
