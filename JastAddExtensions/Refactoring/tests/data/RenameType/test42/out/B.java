package p;

class B {
  B(B A) {
    super();
  }
  B A(B A) {
    A = new B(new B(A));
    return A;
  }
}
