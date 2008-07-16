package p;

public class A {
  public static void main(String[] args) {
    new A().m();
  }
  private int i;
  A[] a;
  void m() {
    a = new A[1];
    a[0] = new A();
    a[n()].assignPlusI(2);
  }
  int n() {
    System.out.println("here");
    return 0;
  }
  public A() {
    super();
  }
  int getI() {
    return i;
  }
  int setI(int i) {
    return this.i = i;
  }
  int assignPlusI(int i) {
    return this.i += i;
  }
}
