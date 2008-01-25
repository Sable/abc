// Encapsulate/test9/in/A.java p B i
package p;

// this refactoring currently fails, which is expected behavior
// although in this particular case there is, in fact, a possible way to
// make it work, in many related cases there isn't

class A {
    public int getI() { return 23; }
}

class B extends A {
    public int i;
    public void m() {
	getI();
    }
}
