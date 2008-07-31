module m2x;
import b.BB;
import b.BBModule;
public class BX {
	BB bb = new BB();
	BB bb2 = new BB(1L);
	BBModule bbm = new BBModule();
	public BX() {
		System.out.println(this.getClass());
		bb.publicf();
		bb.modulef();

	}
	public static class BXInner {};
}
