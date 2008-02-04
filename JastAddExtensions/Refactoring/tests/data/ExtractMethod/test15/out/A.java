class B {
  void m() {
    n();
  }
  B() {
    super();
  }
  private void n() {
    System.out.println(1);
  }
}

class C extends B {
  void n() {
    System.out.println(2);
  }
  C() {
    super();
  }
}

public class Tst2 {
  public static void main(String[] args) {
    B b = new C();
    b.m();
  }
  public Tst2() {
    super();
  }
}
