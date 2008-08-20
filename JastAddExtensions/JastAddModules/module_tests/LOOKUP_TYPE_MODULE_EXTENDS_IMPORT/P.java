module m2;
package p;

import q.*;

public class P {
	Q q = new Q();
	public P() {
		System.out.println(this.getClass());
	}
}
