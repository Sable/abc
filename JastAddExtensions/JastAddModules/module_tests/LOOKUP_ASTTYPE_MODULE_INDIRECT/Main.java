import jastadd$framework.*;

public class Main {
	public static void main(String args[]) {
		m1.A a = new m1.A();
		m1.A2 a2 = new m1.A2(a);
		m1.A3 a3 = new m1.A3(new List().add(new m1.m2.B()));
		m1.A4 a4 = new m1.A4(new Opt(new m1.m2.B()));
		m1.A5 a5 = new m1.A5(new m1.m2.m3.C());

		System.out.println(a.getClass());
		System.out.println(a2.getClass());
		System.out.println(a3.getClass());
		System.out.println(a3.getChild(0).getChild(0).getClass());
		System.out.println(a4.getClass());
		System.out.println(a4.getChild(0).getChild(0).getClass());
		System.out.println(a5.getClass());
		System.out.println(a5.getChild(0).getClass());
	}
}
