module m3;

public class C{
	public C() {
		System.out.println(this.getClass());
		System.out.print("From m3.C: ");
		m5.E e = new E();
	}
}
