public class IntertypePrecedence {

}

aspect A {
    void IntertypePrecedence.foo() { }
}

aspect B {
    void IntertypePrecedence.foo() { }
}

aspect C {
    declare precedence: C,A;
    declare precedence: C,B;
    void IntertypePrecedence.foo() { }
}