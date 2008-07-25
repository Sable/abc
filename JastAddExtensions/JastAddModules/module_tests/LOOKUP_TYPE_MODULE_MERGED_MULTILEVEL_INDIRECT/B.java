module m2;

public class B{
	public B() {
		System.out.println(this.getClass());
		System.out.print("From m2.B: ");
		m4::D d = new m4::D();
	}
}
