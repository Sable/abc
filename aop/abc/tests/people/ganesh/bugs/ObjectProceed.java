public class ObjectProceed {
    static void foo(Object o) {
        System.out.println(o);
    }
    public static void main(String[] args) {
        Object o=new Object();
        System.out.println(o);
        foo(o);
    }
}

aspect OPAspect {
    void around(Object x) : call(void foo(..)) && args(x) {
        proceed(new Object());
    }
}