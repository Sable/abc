package p;

class A {
    public static final int CONST= 17;
    A(int num) {}
    void run() {
        int k= CONST;
        new A(k);
    }
}