//Listing 4.19 TestAssociationScope.java

public class TestAssociationScope {
    public static void main(String[] args) {
	A a = new A();
	a.m();
    }
}

class A {
    public void m() {
	B b = new B();
	b.m();
    }
}

class B {
    public void m() {
    }
}

aspect TestAspect perthis(execution(void A.*())) {
    before() : !within(TestAspect) {
	System.out.println(thisJoinPoint);
    }
}
