import jastadd$framework.*;

public class Main {
	public static void main(String args[]) {
		m1.A a = new m1.A();
		m1.A1 a1 = new m1.A1(a);
		m1.A2 a2 = new m1.A2(a);
		m1.alias1.B b1 = a1;
		m1.alias2.B b2 = a2;

		m1.A3 a3 = new m1.A3(new List().add(new m1.alias1.B()), new Opt(new m1.alias2.B()));

		ASTNode node = a;
		node = a1;
		node = a2;
		node = b1;
		node = b2;


		//m1.alias1.B bx1 = a2;
		//m1.alias2.B bx2 = a1;

		System.out.println(a.getClass());
		System.out.println(a1.getClass());
		System.out.println(a2.getClass());
		System.out.println(a1.getChild(0).getClass());
		System.out.println(a2.getChild(0).getClass());
		System.out.println(a3.getClass());
		System.out.println(a3.getChild(0).getChild(0).getClass());
		System.out.println(a3.getChild(1).getChild(0).getClass());

	}
}
