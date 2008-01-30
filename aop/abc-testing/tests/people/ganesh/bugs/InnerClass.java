public class InnerClass {
    class Foo { }
}

aspect ICAspect {
    before() : initialization(*.new()) { System.out.println("foo"); }
}