public class Test2 {
    static boolean foo() {
	System.out.println("In Test2.foo");
	return true;
    }

    void bar() {
    }

    public static void main(String[] args) {
	new Test2().bar();
    }
}

class Baz2 extends Test2 {
    static boolean foo() {
	System.out.println("In Baz2.foo");
	return true;
    }
}

class Blat2 extends Baz2 {
    static boolean foo() {
	System.out.println("In Blat2.foo");
	return true;
    }
}

aspect Test2Aspect {
    pointcut a(Blat2 o) : target(o) && if(o.foo()) && if(true);

    pointcut b(Baz2 o) : a(o) && if(o.foo());

    // call(void bar() && local(Baz2 o) . (local(Blat2 o2) . target(o2) && if(o2.foo()) && cast(o2,o)) && if(

    before() : call(void bar()) && b(*) {
	System.out.println("ran advice");
    }

}
