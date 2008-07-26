module m1;
import pack.*;
import pack.P.*;
import pack.P.PInner.*;

public class ADemand {
	P p5 = new P();
	PInner pinner = new PInner();
	PInner2 pinner2 = new PInner2();
	::pack.P.PInner pinner3 = new ::pack.P.PInner();
	public ADemand() {
		System.out.println(this.getClass());
	}
}
