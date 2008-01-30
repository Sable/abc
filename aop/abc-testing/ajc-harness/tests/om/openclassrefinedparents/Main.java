import java.util.*;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List l = new LinkedList();
		l.add(new A());
		l.add(new B());
		l.add(new C());
		for (Iterator i = l.iterator(); i.hasNext(); ) {
			Type curr = (Type) i.next();
			if (curr instanceof Type1) {
				System.out.println("Type1 : " + curr.toString());
			} else if (curr instanceof Type2) {
				System.out.println("Type2 : " + curr.toString());
			}
		}
	}
}

interface Type{};
interface Type1 extends Type {};
interface Type2 extends Type {};

class A implements Type1 {}

class B implements Type2 {}

class C {}
