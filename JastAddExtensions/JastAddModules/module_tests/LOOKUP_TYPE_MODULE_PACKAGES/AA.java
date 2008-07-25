module m1;
package a;
public class AA{
	public m2.B b;
	//public m2::B b;
	//public m2::b.BB bb;
	public AA() {
		System.out.println(this.getClass());
	}
}
