public class Test3 {
    void bar() {
    }

    public static void main(String[] args) {
	new Test3().bar();
    }
}

class Baz3 extends Test3 {
}

class Blat3 extends Baz3 {
}

aspect Test3Aspect {
    pointcut a(Blat3 o) : target(o) ;

    pointcut b(Baz3 o) : a(o);

    before() : call(void bar()) && b(*) {
	System.out.println("ran advice");
    }

}
