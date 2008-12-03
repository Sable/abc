class A {
    // made public for compilability
    public void f() {}
}

class B {
    private A b;
    public void g() {
        b./*[*/f()/*]*/;
    }
}