aspect Aspect {

    static String show(String x) {
	System.out.println(x);
	return x;
    }

    String I.x = show("x");
    String I.y = show("y");
    String J.z = show("z");

    String C.u = show("u");

}

interface I {}
interface J {}

class C implements I,J {}

class D implements J,I {}

public class Init1 {

    public static void main(String[] args) {
	C c = new C();
	D d = new D();
    }
}
