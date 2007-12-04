package p;
public class B {
  B(p.B A){
    super();
  }
  p.B A(p.B A) {
    A = new p.B(new p.B(A));
    return A;
  }
}
