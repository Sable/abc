module m1;
package pack;
public class P {
	public P() {
		System.out.println(this.getClass());
	}
	public static class PInner {
		public PInner() {
			System.out.println(this.getClass());
		}
		public static class PInner2{
			public PInner2() {
				System.out.println(this.getClass());
			}
		}
	}
}
