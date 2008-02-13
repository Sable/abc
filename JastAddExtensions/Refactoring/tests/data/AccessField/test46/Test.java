package Access.test46;

class A {
    Boolean x;
}

public class Test extends A {
    String x;
    class C extends A {
        Object o = Test.this.x;
    }
}
