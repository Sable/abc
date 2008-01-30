public class Main {
  
  public static void main(String [] args) {
    Main t = new Main();
    t.doit();
  }

  public void doit() {
    /* Doing things to an A object */
    System.out.println("Allocating an A");
    A a = new A();
    System.out.println("Calling a.m()");
    a.m();
    System.out.println("Calling a.f()");
    a.f();
    System.out.println("Setting a.field1\n\n");
    a.field1 = 3;

    /* Doing things to a B object */
    System.out.println("Allocating B");
    B b = new B();
    System.out.println("Calling b.m()");
    b.m();
    System.out.println("Calling b.f()");
    b.f();
    System.out.println("Setting b.field1");
    b.field1 = 3;
  }
}

class A {
  public int field1;

  public void m() {
    System.out.println("Executing A's m()");
    System.out.println("Setting A's f inside of m");
    field1 = 10;
  }

  public void f() {
    System.out.println("Executing A's f()");
    System.out.println("Calling A's m() from inside f()");
    this.m();

  }
}

class B {
  public int field1;

  public void m() {
    System.out.println("Executing B's m()");
    System.out.println("Setting B's f inside of m");
    field1 = 10;
  }

  public void f() {
    System.out.println("Executing B's f()");
    System.out.println("Calling B's m() from inside f()");
    this.m();
  }
}

