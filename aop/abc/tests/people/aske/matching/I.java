
public interface I {
    public void xia();
    public void xiab();
}

interface M {}

interface J extends M {}
interface K extends M {}

abstract class O implements I,K {}

class A extends O implements I,J,K {
    public String fa;
    public String fab;
    public static String fsa;
    public static String fsab;
    public void xia()  { System.out.println("ia");  }
    public void xiab() { System.out.println("iab"); }
    public void xa()   { System.out.println("a");   }
    public void xab()  { System.out.println("ab");  }
    public static void xsa()  { System.out.println("sa");  }
    public static void xsab() { System.out.println("sab"); }
}

class X extends A {}

class B extends X {
    public String fb;
    public String fab;
    public static String fsb;
    public static String fsab;
    public void xiab() { System.out.println("iab"); }
    public void xab()  { System.out.println("ab");  }
    public void xb()   { System.out.println("b");   }
    public static void xsb()  { System.out.println("sb");  }
    public static void xsab() { System.out.println("sab"); }
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
	a.fab = "AA.ab";
	System.out.println("Receiver type A, runtime type B");
	a = new B();
	a.xia();
	a.xiab();
	a.xa();
	a.xab();
	a.fa = "AB.a";
	a.fab = "AB.ab";

	System.out.println("Receiver type B, runtime type B");
	B b = new B();
	b.xia();
	b.xiab();
	b.xa();
	b.xab();
	b.xb();
	b.fa = "BB.a";
	b.fb = "BB.b";
	b.fab = "BB.ab";

	System.out.println("Static calls on A");
	A.xsa();
	A.xsab();
	System.out.println("Static calls on B");
	B.xsa();
	B.xsb();
	B.xsab();

	System.out.println("Static fields");
	A.fsa = "A.sa";
	A.fsab = "A.sab";
	B.fsa = "B.sa";
	B.fsb = "B.sb";
	B.fsab = "B.sab";
    }
}
