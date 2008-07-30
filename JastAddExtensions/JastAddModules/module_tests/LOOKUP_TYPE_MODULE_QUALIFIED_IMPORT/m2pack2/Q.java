module m2;
package pack2;
public class Q {
	public Q() {
		System.out.println(this.getClass());
	}
	public static class QInner {
		public QInner() {
			System.out.println(this.getClass());
		}
		public static class QInner2{
			public QInner2() {
				System.out.println(this.getClass());
			}
		}
	}
}
