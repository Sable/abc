package p;
class B<T> { }
public class A<T> {
    enum TEST {
        FIRST, SECOND
    }
    void foo() {
        B<T> b= getB();
    }
    B<T> getB() {
        final ///A.
	      TEST temp= TEST.FIRST;
        A.TEST test= temp;
        return null;
    }
    void bar() {
        A<String> s= new A<String>();
        A<T> a= new A<T>();
    }
}