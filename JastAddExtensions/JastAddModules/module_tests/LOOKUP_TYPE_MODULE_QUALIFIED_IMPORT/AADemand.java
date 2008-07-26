module m1;
import m2::pack.*;
import m2::pack.P.*;
import m2::pack.P.PInner.*;

public class AADemand {
	P p2 = new P();
	PInner pinner = new PInner();
	PInner2 pinner2 = new PInner2();
	public AADemand() {
		System.out.println(this.getClass());
	}
}
