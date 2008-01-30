aspect Aspect {

    static String show(String x) {
	System.out.println(x);
	return x;
    }

    String J.z = show("z");

    String C.u = show("u");

    String K.y = show("y");
    String K.x = show("x");


    before(): (initialization(new(int)) || initialization(new()) || execution(new())) && !within(Aspect) {
        System.out.println("before "+thisJoinPointStaticPart);
    }

	after(): (initialization(new(int)) || initialization(new()) || execution(new())) && !within(Aspect) {
	System.out.println("after "+thisJoinPointStaticPart);
    }


}

interface J {}
interface K {}

class D implements K,J { D(int x) throws Exception { } }
class C implements J,K { 
    C(int x) { 
	System.out.println("foo");
    } 
}


public class Init1 {

    public static void main(String[] args) throws Exception {
	C c = new C(1);
	D d = new D(2);
    }
}
