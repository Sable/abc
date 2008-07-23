module m1;
import pack.*;

public class A{
	B b1 = new B(); //should lookup to pack.B due to the ondemand import
	pack.B b2 = new pack.B();
	m2.B b3 = new m2.B();

	public A() {
		System.out.println(this.getClass());
	}
}
