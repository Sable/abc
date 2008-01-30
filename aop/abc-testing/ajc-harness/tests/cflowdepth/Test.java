public class Test {
    public static void main(String[] args) {
        foo();
    }
    static void foo() {
        System.out.println("foo");
    }
}
aspect A {
    pointcut pc1() : call(* *.foo());
    pointcut pc() : pc1() || pc1();
    before(int i) : pc() && cflowdepth(i, pc1()) {
        System.out.println("before foo at depth: " + i);
    }
}
