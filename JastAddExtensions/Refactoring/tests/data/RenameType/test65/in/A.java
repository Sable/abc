// RenameType/test65/in/A.java p A B
package p;

public class A { }

class C {
  class D<T> { }
}

class E {
    C.D<A> b = new C().new D<A>();
}