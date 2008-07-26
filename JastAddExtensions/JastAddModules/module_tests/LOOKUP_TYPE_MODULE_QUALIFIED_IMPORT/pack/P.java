package pack;
public class P {
	public P() {
		System.out.println(this.getClass());
	}
	public static class PInner {
		public static class PInner2{}
	}
}
