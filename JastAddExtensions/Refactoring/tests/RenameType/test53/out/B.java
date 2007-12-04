package p;
public class B {
  B(){
    super();
  }
  B(p.B A){
    super();
  }
  p.B m() {
    return (p.B)new p.B();
  }
}
