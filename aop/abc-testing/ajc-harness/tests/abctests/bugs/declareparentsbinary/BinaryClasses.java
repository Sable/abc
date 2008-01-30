import org.aspectj.testing.Tester;

// Expecting an aspect to make A the parent of B
// Distributed in compiled form as BinaryClasses.jar
// To regenerate .jar file,
// abc BinaryClasses.java
// jar -cvf BinaryClasses.jar A.class B.class BinaryClasses.class

class A {
    A() {
        Tester.event("Called standard A constructor");
    }
}

class B {
    B() {
        Tester.event("Called standard B constructor");
    }
}

public class BinaryClasses {
    public static void main(String[] args) {
        new B();
        Tester.expectEvent("Called standard A constructor");
        Tester.expectEvent("Called standard B constructor");
        Tester.checkAllEvents();
    }
}