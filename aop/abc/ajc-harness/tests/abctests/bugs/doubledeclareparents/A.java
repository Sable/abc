public class A {
}
interface B {
}
aspect C {
    declare parents: A implements B;
    declare parents: A* implements B;
}
