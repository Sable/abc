
public interface I {
    public void xia();
    public void xiab();
}

interface J {}
interface K {}

abstract class O implements I,K {}

class A extends O implements I,J,K {
    public String fa;
    public void xia()  { System.out.println("ia");  }
    public void xiab() { System.out.println("iab"); }
    public void xa()   { System.out.println("a");   }
    public void xab()  { System.out.println("ab");  }
}

class B extends A {
    public String fb;
    public void xiab() { System.out.println("iab"); }
    public void xab()  { System.out.println("ab");  }
    public void xb()   { System.out.println("b");   }
}

class C {
    public static void run() {
	System.out.println("Receiver type I, runtime type A");
	I i = new A();
	i.xia();
	i.xiab();
	System.out.println("Receiver type I, runtime type B");
	i = new B();
	i.xia();
	i.xiab();

	System.out.println("Receiver type A, runtime type A");
	A a = new A();
	a.xia();
	a.xiab();
	a.xa();
	a.xab();
	a.fa = "AA.a";
	System.out.println("Receiver type A, runtime type B");
	a = new B();
	a.xia();
	a.xiab();
	a.xa();
	a.xab();
	a.fa = "AB.a";

	System.out.println("Receiver type B, runtime type B");
	B b = new B();
	b.xia();
	b.xiab();
	b.xa();
	b.xab();
	b.xb();
	b.fa = "BB.a";
	b.fb = "BB.b";
    }
}
