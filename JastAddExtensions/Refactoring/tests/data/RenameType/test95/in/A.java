// RenameType/test95/in/A.java p A B
package p;

public class A {
    public static void main(String[] args) {
        C<A> a = new D<String>();
    }
}

class C<T> { }

class D<B> extends C<A> { }