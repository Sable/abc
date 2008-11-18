class B {
  void m() {
    n();
  }
  private void n() {
    System.out.println(1);
  }
  B() {
    super();
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
