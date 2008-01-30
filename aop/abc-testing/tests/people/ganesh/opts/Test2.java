public class Test2 {

}

aspect Test2_Aspect {

    void Test2.foo() {
    }

    static String str="foo";

    void Test2.bar() {
	System.out.println(str);
    }

}
