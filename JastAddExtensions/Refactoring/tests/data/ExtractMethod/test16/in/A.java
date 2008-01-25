// ExtractMethod/test16/in/A.java B m 0 0 n default

class B {
    void m() {
	System.out.println(1);
    }
}

class C extends B {
    void n() {
	System.out.println(2);
    }
}

public class Tst2 {
    public static void main(String[] args) {
	B b = new C();
	b.m();
    }
}