module m2;
package b;
import d.*;
public class B {
	D d = new D();
	public B() {
		System.out.println("In m2: " + this.getClass());
	}
}
