public aspect InterfaceInitJP {
    before() : initialization(I.new()) {
      System.out.println("before");
    }

    after() : initialization(I.new()) {
      System.out.println("after");
    }

    public static int foo() {
      System.out.println("foo called");
      return 3;
    }

    int I.v = foo();
    public static void main(String[] args) {
	C c=new C();
    }
}

interface I {

}

class C implements I {

}