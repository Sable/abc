package p;

class D {
  
  static class C {
    static int x = 42;
    C() {
      super();
    }
  }
  D() {
    super();
  }
}

public class A {
  
  static class C extends D {
    static int x = 23;
    static int m() {
      return A.C.x;
    }
    C() {
      super();
    }
  }
  public static void main(String[] args) {
    System.out.println(C.m());
  }
  public A() {
    super();
  }
}
