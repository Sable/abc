package p;

public class B {
  B() {
    super();
  }
  B(B A) {
    super();
  }
  B m() {
    return (B)new B();
  }
}
