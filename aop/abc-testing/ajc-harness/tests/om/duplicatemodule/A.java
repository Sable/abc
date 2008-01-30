
/*
 * Created on May 15, 2005
 *
 */
/**
 * @author Neil Ongkingco
 *
 */
public class A {
	public static void main(String args[]) {
		System.out.println("Hello world!");

		Another a = new Another();
		Another b = new AnotherChild();

		a.print();
		b.print();
	}

}
