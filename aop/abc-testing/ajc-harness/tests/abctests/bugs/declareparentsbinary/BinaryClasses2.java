import org.aspectj.testing.Tester;

// Expecting an aspect to make A the parent of B
// Distributed in compiled form as BinaryClasses2.jar
// To regenerate .jar file,
// abc (or javac or ajc) BinaryClasses2.java
// jar -cvf BinaryClasses2.jar A.class B.class C.class BinaryClasses2.class

class A extends C {
    A() {
        super("");
        Tester.event("Called standard A constructor");
    }
}

class C {
    C(String x) {
        Tester.event("Called string C constructor");
    }
}

class B extends C {
    B() {
        super("");
        Tester.event("Called standard B constructor");
    }
}

public class BinaryClasses2 {
    public static void main(String[] args) {
        // should compile-time error, not expected to run
    }
}