import org.aspectj.testing.Tester;

public class Test {
    void foo() {
    }
    public static void main(String[] args) {
        Test test = new Test();
        test.foo();
        Tester.expectEvent("before foo");
        Tester.checkAllEvents();
    }
}

