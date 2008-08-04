module m1;
import pack1.*;
import pack2.*;
import pack3.*;

public class A {
	P1 p1 = new P1();
	P2 p2 = new P2();
	P3 p3 = new P3();
	public A() {
		System.out.println(this.getClass());
	}
}
