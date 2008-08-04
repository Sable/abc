public class Main {
	public static void main(String args[]) {
		m1.A a = new m1.A();
		m2.B b = new m2.B();
		m2.m3.C c = new m2.m3.C();
		System.out.println(a.getClass());
		System.out.println(b.getClass());
		System.out.println(c.getClass());
	}
}
