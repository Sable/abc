package p;
class X {
  static int a;
  void B.m() {
    int i = X.a;
  }
  X() {
    super();
  }
}

class A {
  int a;
  A() {
    super();
  }
}

class B extends A {
  B() {
    super();
  }
}
