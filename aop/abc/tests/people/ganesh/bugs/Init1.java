aspect Aspect {

    static String show(String x) {
	System.out.println(x);
	return x;
    }

    String J.z = show("z");

    String C.u = show("u");

    String K.y = show("y");
    String K.x = show("x");

    pointcut init() : initialization(K.new()) || initialization(J.new())
	|| initialization(C.new());

    before() : initialization(K.new()) {
	System.out.println("before K");
    }

    after() : initialization(K.new()) {
	System.out.println("after K");
    }
    before() : initialization(J.new()) {
	System.out.println("before J");
    }

    after() : initialization(J.new()) {
	System.out.println("after J");
    }
    before() : initialization(C.new()) {
	System.out.println("before C init");
    }

    after() : initialization(C.new()) {
	System.out.println("after C init");
    }
    before() : execution(C.new()) {
	System.out.println("before C exec");
    }

    after() : execution(C.new()) {
	System.out.println("after C exec");
    }

}

interface J {}
interface K {}

class D implements K,J {}
class C implements J,K {}


public class Init1 {

    public static void main(String[] args) {
	C c = new C();
	D d = new D();
    }
}
