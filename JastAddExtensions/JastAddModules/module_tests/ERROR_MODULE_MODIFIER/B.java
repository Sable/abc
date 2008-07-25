module m2;
public class B {
	b.BB bb = new b.BB();
	public B() {
		System.out.println(this.getClass());
		bb.modulef();
		bb.publicf();
		bb.packagef();	
	}
	public static class BInner {};
}
