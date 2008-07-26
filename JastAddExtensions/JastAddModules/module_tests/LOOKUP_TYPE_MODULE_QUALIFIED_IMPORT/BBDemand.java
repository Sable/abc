module m2;
import m3::pack.*;
import m3::pack.P.*;
import m3::pack.P.PInner.*;

public class BBDemand {
	P p2 = new P();
	PInner pinner = new PInner();
	PInner2 pinner2 = new PInner2();
	public BBDemand() {
		System.out.println(this.getClass());
	}
}
