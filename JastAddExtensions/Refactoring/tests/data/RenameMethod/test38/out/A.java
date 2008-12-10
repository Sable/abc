package p;

class A {
  void g(long x) {
    System.out.println(x);
  }
  void g(int x) {
    System.out.println(x + 19);
  }
  {
    g((long)23);
  }
  A() {
    super();
  }
}
